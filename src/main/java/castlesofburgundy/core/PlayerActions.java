package castlesofburgundy.core;

import castlesofburgundy.board.Depot;
import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.Player;
import castlesofburgundy.player.Storage;
import castlesofburgundy.tile.BuildingKind;
import castlesofburgundy.tile.BuildingTile;
import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * 플레이어·개인 보드·공용 보드를 함께 바꾸는 게임 액션.
 * 각 액션은 검증 → 비용 차감(일꾼/은화) → 타일·상품 이동 → 효과 순서로 진행해,
 * 검증에 실패하면 아무 상태도 바꾸지 않는다.
 * 배·건물처럼 배치 후 선택이 필요한 효과도 여기서 GameUi로 선택을 받아 끝까지 처리한다.
 */
public final class PlayerActions {
    private static final int CENTRAL_MARKET_COST = 2;
    private static final int WORKERS_PER_TAKE = 2;

    private final GameContext ctx;
    private final GameUi ui;
    private final int playerCount;

    public PlayerActions(GameContext ctx, GameUi ui, int playerCount) {
        if (playerCount < 1) {
            throw new IllegalArgumentException("playerCount >= 1");
        }
        this.ctx = ctx;
        this.ui = ui;
        this.playerCount = playerCount;
    }

    public void takeTileFromBoard(Player player, int usedWorkers, int depotNumber, int slotIndex) {
        Depot depot = ctx.board().depot(depotNumber);
        requireDepotTile(depot, slotIndex);
        requireStorageSpace(player);

        player.spendWorkers(usedWorkers);
        storage(player).add(depot.takeTile(slotIndex).orElseThrow());
    }

    public Tile replaceTileFromBoard(Player player, int usedWorkers, int depotNumber, int slotIndex, int storageIndexToDiscard) {
        Depot depot = ctx.board().depot(depotNumber);
        requireDepotTile(depot, slotIndex);
        requireStorageIndex(player, storageIndexToDiscard);

        player.spendWorkers(usedWorkers);
        Tile discarded = storage(player).removeAt(storageIndexToDiscard);
        storage(player).add(depot.takeTile(slotIndex).orElseThrow());
        return discarded;
    }

    public void buyTileFromCentralMarket(Player player, int marketIndex) {
        requireCentralMarketTile(marketIndex);
        requireStorageSpace(player);

        player.spendSilver(CENTRAL_MARKET_COST);
        storage(player).add(ctx.board().removeCentralMarketAt(marketIndex).orElseThrow());
    }

    public Tile replaceTileFromCentralMarket(Player player, int marketIndex, int storageIndexToDiscard) {
        requireCentralMarketTile(marketIndex);
        requireStorageIndex(player, storageIndexToDiscard);

        player.spendSilver(CENTRAL_MARKET_COST);
        Tile discarded = storage(player).removeAt(storageIndexToDiscard);
        storage(player).add(ctx.board().removeCentralMarketAt(marketIndex).orElseThrow());
        return discarded;
    }

    /**
     * 저장소의 타일을 영지에 배치하고, 그에 따른 효과(구역 완성 점수, 타일 고유 효과,
     * 배·건물의 선택형 효과)를 모두 적용한다.
     *
     * @return 일어난 일(추가 행동 여부, 완성된 구역 크기/점수). 추가 행동 진행은 호출부가 한다.
     */
    public PlacementOutcome placeTileFromStorage(
            Player player,
            int usedWorkers,
            int storageIndex,
            int cellId,
            int dieUsed,
            Phase phase
    ) {
        PersonalBoard board = player.getBoard();
        Tile tile = board.getStorage().get(storageIndex);
        if (!board.canPlace(cellId, tile, dieUsed)) {
            throw new IllegalArgumentException("배치 불가: cell=" + cellId + ", die=" + dieUsed + ", tile=" + tile);
        }

        player.spendWorkers(usedWorkers);
        board.placeFromStorage(storageIndex, cellId, dieUsed);

        // 1) 구역 완성 점수
        int regionSize = board.completedRegionSizeIfAny(cellId);
        int regionScore = 0;
        if (regionSize > 0) {
            regionScore = phaseBonus(phase) + regionSizeBonus(regionSize);
            player.addScore(regionScore);
        }

        // 2) 타일 고유 효과(즉시 수치)
        TilePlacementResult tileResult = tile.onPlaced(new TileEffectContext(player, phase, cellId, dieUsed));
        player.addScore(tileResult.bonusScore());
        player.gainWorkers(tileResult.bonusWorkers());
        player.addSilver(tileResult.bonusSilver());

        // 3) 선택이 필요한 효과
        if (tile.type() == TileType.SHIP) {
            shipEffect(player);
        }
        if (tile instanceof BuildingTile building) {
            buildingEffect(player, building.kind());
        }

        if (regionSize > 0) {
            ui.message("[구역 완성] " + player.getName()
                    + "이(가) 크기 " + regionSize
                    + " 구역을 완성했습니다! (+" + regionScore + "점, 페이즈 " + phase + ")");
        }
        return new PlacementOutcome(tileResult.grantExtraAction(), regionSize, regionScore);
    }

    public void takeWorkers(Player player) {
        player.gainWorkers(WORKERS_PER_TAKE);
    }

    /**
     * 해당 색 상품을 전부 판매한다.
     * 판매가 일어나면 은화 1개(개수 무관) + 타일당 playerCount(2/3/4인) 점수를 얻는다.
     *
     * @return 판매한 타일 수(0이면 그 색 상품이 없어 일꾼도 쓰지 않고 아무 일도 안 일어남)
     */
    public int sellGoods(Player player, int usedWorkers, GoodsColor color) {
        if (player.getBoard().getGoods().count(color) == 0) {
            return 0;
        }

        player.spendWorkers(usedWorkers);
        int sold = player.getBoard().getGoods().sell(color);
        player.addSilver(1);
        player.addScore(sold * playerCount);
        return sold;
    }

    public int scoreForSelling(int count) {
        return count * playerCount;
    }

    private void shipEffect(Player player) {
        List<Depot> depots = new ArrayList<>();
        List<String> options = new ArrayList<>();
        for (Depot depot : ctx.board().depots()) {
            List<GoodsColor> goods = depot.goods();
            if (!goods.isEmpty()) {
                depots.add(depot);
                options.add("창고 " + depot.number() + " 상품 가져오기 " + goods);
            }
        }

        if (depots.isEmpty()) {
            ui.message("[배] 가져올 상품이 있는 창고가 없습니다.");
        } else {
            Depot depot = depots.get(ui.chooseIndex(options));
            int stored = 0;
            int dropped = 0;
            for (GoodsColor color : depot.takeAllGoods()) {
                if (player.getBoard().getGoods().add(color)) {
                    stored++;
                } else {
                    dropped++;
                }
            }

            ui.message("[배] 창고 " + depot.number() + "에서 상품 " + stored + "개를 가져왔습니다.");
            if (dropped > 0) {
                ui.message("[배] 상품 저장소 색 제한으로 " + dropped + "개는 보관하지 못했습니다.");
            }
        }

        ctx.board().turnOrder().advance(player);
        ui.message("[다리] " + player.getName() + "의 턴 순서 토큰이 앞으로 이동했습니다.");
    }

    private void buildingEffect(Player player, BuildingKind kind) {
        switch (kind) {
            case MARKET -> takeBoardTileByBuildingEffect(player, EnumSet.of(TileType.SHIP, TileType.ANIMAL), "[시장]");
            case CARPENTER -> takeBoardTileByBuildingEffect(player, EnumSet.of(TileType.BUILDING), "[목수]");
            case CHURCH -> takeBoardTileByBuildingEffect(
                    player,
                    EnumSet.of(TileType.MINE, TileType.KNOWLEDGE, TileType.CASTLE),
                    "[교회]"
            );
            case WAREHOUSE -> sellGoodsByWarehouse(player);
            default -> {
            }
        }
    }

    private void takeBoardTileByBuildingEffect(Player player, Set<TileType> allowedTypes, String label) {
        List<String> options = new ArrayList<>();
        List<Runnable> effects = new ArrayList<>();

        for (Depot depot : ctx.board().depots()) {
            for (int slotIndex = 0; slotIndex < depot.slotCount(); slotIndex++) {
                Tile tile = depot.tileAt(slotIndex).orElse(null);
                if (tile == null || !allowedTypes.contains(tile.type())) {
                    continue;
                }

                int depotNumber = depot.number();
                int sIdx = slotIndex;
                if (!storage(player).isFull()) {
                    options.add(label + " 섹션 " + depotNumber + " 슬롯 " + sIdx
                            + "에서 타일 " + tile.type() + " 가져오기");
                    effects.add(() -> takeTileFromBoard(player, 0, depotNumber, sIdx));
                    continue;
                }

                List<Tile> stored = storage(player).view();
                for (int storageIndex = 0; storageIndex < stored.size(); storageIndex++) {
                    int discardIndex = storageIndex;
                    Tile discarded = stored.get(storageIndex);
                    options.add(label + " 저장소[" + discardIndex + "] 버리고 섹션 "
                            + depotNumber + " 슬롯 " + sIdx
                            + "에서 타일 " + tile.type() + " 가져오기"
                            + " (버림: " + discarded.type() + ")");
                    effects.add(() -> replaceTileFromBoard(player, 0, depotNumber, sIdx, discardIndex));
                }
            }
        }

        if (options.isEmpty()) {
            ui.message(label + " 가져올 수 있는 타일이 없습니다.");
            return;
        }

        effects.get(ui.chooseIndex(options)).run();
        ui.message(label + " 효과를 실행했습니다.");
    }

    private void sellGoodsByWarehouse(Player player) {
        List<String> options = new ArrayList<>();
        List<GoodsColor> colors = new ArrayList<>();

        for (GoodsColor color : GoodsColor.values()) {
            int count = player.getBoard().getGoods().count(color);
            if (count > 0) {
                colors.add(color);
                options.add(color + " 상품 판매 (" + count + "개)");
            }
        }

        if (options.isEmpty()) {
            ui.message("[창고] 판매할 상품이 없습니다.");
            return;
        }

        GoodsColor color = colors.get(ui.chooseIndex(options));
        int sold = sellGoods(player, 0, color);
        ui.message("[창고] " + color + " " + sold + "개 → 은화 +1, 점수 +" + scoreForSelling(sold));
    }

    private static Storage storage(Player player) {
        return player.getBoard().getStorage();
    }

    private static void requireDepotTile(Depot depot, int slotIndex) {
        if (depot.tileAt(slotIndex).isEmpty()) {
            throw new IllegalStateException("해당 슬롯에 타일이 없습니다: " + depot + "#" + slotIndex);
        }
    }

    private static void requireStorageSpace(Player player) {
        if (storage(player).isFull()) {
            throw new IllegalStateException("저장소 가득참: 먼저 버려야 함");
        }
    }

    private static void requireStorageIndex(Player player, int storageIndex) {
        if (storageIndex < 0 || storageIndex >= storage(player).size()) {
            throw new IndexOutOfBoundsException("index=" + storageIndex);
        }
    }

    private void requireCentralMarketTile(int marketIndex) {
        if (ctx.board().getCentralMarket(marketIndex).isEmpty()) {
            throw new IllegalStateException("중앙 시장에 타일이 없습니다: " + marketIndex);
        }
    }

    private static int phaseBonus(Phase phase) {
        return switch (phase) {
            case A -> 10;
            case B -> 8;
            case C -> 6;
            case D -> 4;
            case E -> 2;
        };
    }

    static int regionSizeBonus(int size) { // 같은 패키지 테스트에서 표 전체를 검증하기 위해 package-private
        return switch (size) {
            case 1 -> 1;
            case 2 -> 3;
            case 3 -> 6;
            case 4 -> 10;
            case 5 -> 15;
            case 6 -> 21;
            case 7 -> 28;
            case 8 -> 36;
            default -> throw new IllegalArgumentException("지원하지 않는 구역 크기: " + size);
        };
    }
}
