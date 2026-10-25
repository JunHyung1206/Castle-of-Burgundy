package castlesofburgundy.core;

import castlesofburgundy.board.Depot;
import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.Player;
import castlesofburgundy.player.Storage;
import castlesofburgundy.tile.AnimalTile;
import castlesofburgundy.tile.BuildingKind;
import castlesofburgundy.tile.BuildingTile;
import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;

import java.util.EnumSet;
import java.util.List;

/**
 * 플레이어·개인 보드·공용 보드를 함께 바꾸는 게임 액션.
 * 각 액션은 검증 → 비용 차감(일꾼/은화) → 타일·상품 이동 → 효과 순서로 진행해,
 * 검증에 실패하면 아무 상태도 바꾸지 않는다.
 * 배·건물처럼 배치 후 선택이 필요한 효과는 PlacementOutcome.FollowUp으로 알리고,
 * 선택이 들어오면 해당 실행 메서드(takeShipGoods 등)로 마무리한다. 진행 순서는 Game이 정한다.
 */
public final class PlayerActions {
    private static final int CENTRAL_MARKET_COST = 2;
    private static final int WORKERS_PER_TAKE = 2;

    private final GameContext ctx;
    private final int playerCount;

    public PlayerActions(GameContext ctx, int playerCount) {
        if (playerCount < 1) {
            throw new IllegalArgumentException("playerCount >= 1");
        }
        this.ctx = ctx;
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
     * 저장소의 타일을 영지에 배치하고, 그에 따른 효과(구역 완성 점수, 타일 효과)를 모두 적용한다.
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

        // 2) 타일 효과
        PlacementOutcome.FollowUp followUp = applyPlacementEffect(player, tile, cellId);
        return new PlacementOutcome(tile.type() == TileType.CASTLE, regionSize, regionScore, followUp);
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

    /**
     * 타일을 영지에 놓았을 때의 효과. 모든 타일 효과는 여기서만 정한다.
     * 즉시 효과(가축 점수, 망루·은행·하숙집)는 바로 적용하고, 선택이 필요한 효과는 FollowUp으로 돌려준다.
     * 성의 추가 행동은 PlacementOutcome.grantsExtraAction, 광산은 페이즈 종료 때(Game), 지식 타일은 미구현이다.
     */
    private PlacementOutcome.FollowUp applyPlacementEffect(Player player, Tile tile, int cellId) {
        return switch (tile) {
            case AnimalTile animal -> {
                player.addScore(animal.scoreIn(player.getBoard().placedTilesInRegion(cellId)));
                yield new PlacementOutcome.FollowUp.None();
            }
            case BuildingTile building -> buildingEffect(player, building.kind());
            default -> tile.type() == TileType.SHIP
                    ? new PlacementOutcome.FollowUp.ShipGoods()
                    : new PlacementOutcome.FollowUp.None();
        };
    }

    private PlacementOutcome.FollowUp buildingEffect(Player player, BuildingKind kind) {
        PlacementOutcome.FollowUp none = new PlacementOutcome.FollowUp.None();
        return switch (kind) {
            case WATCHTOWER -> {
                player.addScore(4);
                yield none;
            }
            case BANK -> {
                player.addSilver(2);
                yield none;
            }
            case BOARDING_HOUSE -> {
                player.gainWorkers(4);
                yield none;
            }
            case MARKET -> new PlacementOutcome.FollowUp.BoardTile(kind, EnumSet.of(TileType.SHIP, TileType.ANIMAL));
            case CARPENTER -> new PlacementOutcome.FollowUp.BoardTile(kind, EnumSet.of(TileType.BUILDING));
            case CHURCH -> new PlacementOutcome.FollowUp.BoardTile(
                    kind, EnumSet.of(TileType.MINE, TileType.KNOWLEDGE, TileType.CASTLE));
            case WAREHOUSE -> new PlacementOutcome.FollowUp.WarehouseSale();
            case TOWN_HALL -> none; // 시청: 미구현
        };
    }

    /** 배 효과로 고를 수 있는 창고: 상품이 쌓여 있는 번호 창고. */
    public List<Depot> depotsWithGoods() {
        return ctx.board().depots().stream()
                .filter(depot -> !depot.goods().isEmpty())
                .toList();
    }

    /**
     * 배 효과: 창고의 상품을 모두 가져온다. 상품 저장소 3색 제한으로 둘 수 없는 상품은 버려진다.
     * 턴 순서 이동은 상품이 없어도 일어나므로 advanceTurnOrder로 따로 한다.
     */
    public ShipGoods takeShipGoods(Player player, int depotNumber) {
        Depot depot = ctx.board().depot(depotNumber);
        if (depot.goods().isEmpty()) {
            throw new IllegalStateException("상품이 없는 창고입니다: " + depot);
        }

        int stored = 0;
        int dropped = 0;
        for (GoodsColor color : depot.takeAllGoods()) {
            if (player.getBoard().getGoods().add(color)) {
                stored++;
            } else {
                dropped++;
            }
        }
        return new ShipGoods(stored, dropped);
    }

    public record ShipGoods(int stored, int dropped) {
    }

    public void advanceTurnOrder(Player player) {
        ctx.board().turnOrder().advance(player);
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
