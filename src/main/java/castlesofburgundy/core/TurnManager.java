package castlesofburgundy.core;

import castlesofburgundy.board.BoardSlot;
import castlesofburgundy.board.BoardState;
import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public final class TurnManager {
    private final GameContext ctx;
    private final GameUi ui;
    private final int playerCount;
    private final TurnOrder turnOrder;

    public TurnManager(GameContext ctx, GameUi ui) {
        this(ctx, ui, 1, null);
    }

    public TurnManager(GameContext ctx, GameUi ui, int playerCount) {
        this(ctx, ui, playerCount, null);
    }

    public TurnManager(GameContext ctx, GameUi ui, int playerCount, TurnOrder turnOrder) {
        if (playerCount < 1) {
            throw new IllegalArgumentException("playerCount >= 1");
        }
        this.ctx = ctx;
        this.ui = ui;
        this.playerCount = playerCount;
        this.turnOrder = turnOrder;
    }

    public void playerTurn(Player player, Phase phase) {
        ui.message("\n===" + player.getName() + "의 턴 ===");
        int d1 = ctx.dice().roll();
        int d2 = ctx.dice().roll();

        ui.message("굴린 주사위: " + d1 + ", " + d2);

        handleDie(player, d1, 1, phase);
        handleDie(player, d2, 2, phase);
    }

    /**
     * 하나의 선택 가능한 액션.
     * apply 실행 결과 boolean = 성 효과처럼 '추가 행동'을 부여하면 true.
     * apply 안에서는 상태만 바꾸고 결과를 서술(ui.message)할 뿐, 메뉴 출력·입력·추가 행동 진행은 호출부가 담당한다.
     */
    public record Action(String description, BooleanSupplier apply) {
    }

    private void handleDie(Player player, int die, int dieIndex, Phase phase) {
        ui.message("\n[" + dieIndex + "번째 주사위: " + die + "]");
        ui.message("현재 일꾼 수 : " + player.getWorkers());

        List<Action> actions = buildOptionsForDie(player, die, phase);
        ui.message(renderActionContext(player));

        Action selected = chooseAction(actions);
        boolean grantsExtraAction = selected.apply().getAsBoolean();
        ui.message("=> 실행: " + selected.description());

        if (grantsExtraAction) {
            runCastleExtraAction(player, phase);
        }
    }

    private static int wrapAdd(int face, int delta) {
        return ((face - 1 + delta) % 6 + 6) % 6 + 1;
    }

    public List<Action> buildOptionsForDie(Player player, int baseDie, Phase phase) {
        List<Action> actions = new ArrayList<>();
        BoardState boardState = ctx.boardState();

        int availableWorkers = player.getWorkers();
        int maxWorkersToUse = Math.min(3, availableWorkers);

        // 일꾼 0개 사용 (조정 없음)
        addActionsForSetting(player, boardState, 0, baseDie, "", phase, actions);

        // 일꾼 1~max 사용, 각 ± 방향
        for (int used = 1; used <= maxWorkersToUse; used++) {
            int minusDie = wrapAdd(baseDie, -used);
            addActionsForSetting(player, boardState, used, minusDie, "[일꾼 " + used + "개 소모] ", phase, actions);

            if (used == 3) {
                break;  // ±3은 같은 눈이라 한 번만 표시
            }
            int plusDie = wrapAdd(baseDie, +used);
            addActionsForSetting(player, boardState, used, plusDie, "[일꾼 " + used + "개 소모] ", phase, actions);
        }

        takeWorkersAction(player, actions);
        return actions;
    }

    private void addActionsForSetting(Player player, BoardState boardState, int usedWorkers, int adjustedDie, String prefix, Phase phase, List<Action> actions) {
        takeTileFromBoardAction(player, boardState, usedWorkers, adjustedDie, prefix, actions);
        placeTileFromStorageAction(player, usedWorkers, adjustedDie, prefix, phase, actions);
        sellGoodsAction(player, usedWorkers, adjustedDie, prefix, actions);
    }

    private void placeTileFromStorageAction(Player player,
                                            int usedWorkers,
                                            int adjustedDie,
                                            String prefix,
                                            Phase phase,
                                            List<Action> actions) {
        if (player.isStorageEmpty()) {
            return;
        }

        List<Tile> stored = player.storedTiles();

        for (int storageIndex = 0; storageIndex < stored.size(); storageIndex++) {
            Tile t = stored.get(storageIndex);
            int sIdx = storageIndex;

            for (int cellId : player.legalPlacements(t, adjustedDie)) {
                String desc = prefix + "저장소[" + sIdx + "]의 " + t.type() + " 를 cell " + cellId + " 에 배치";

                actions.add(new Action(desc, () -> {
                    player.spendWorkers(usedWorkers);
                    PlacementOutcome outcome = PlayerActions.placeTileFromStorage(player, sIdx, cellId, adjustedDie, phase);
                    if (t.type() == TileType.SHIP) {
                        handleShipPlacement(player);
                    }
                    if (outcome.completedRegionSize() > 0) {
                        ui.message("[구역 완성] " + player.getName()
                                + "이(가) 크기 " + outcome.completedRegionSize()
                                + " 구역을 완성했습니다! (+" + outcome.regionScore() + "점, 페이즈 " + phase + ")");
                    }
                    return outcome.grantsExtraAction();
                }));
            }
        }
    }

    private void sellGoodsAction(Player player,
                                 int usedWorkers,
                                 int adjustedDie,
                                 String prefix,
                                 List<Action> actions) {
        GoodsColor color = GoodsColor.forDie(adjustedDie);
        int count = player.goods().count(color);
        if (count == 0) {
            return;
        }

        String desc = prefix + color + " 상품 판매 (" + count + "개)";
        actions.add(new Action(desc, () -> {
            player.spendWorkers(usedWorkers);
            int sold = PlayerActions.sellGoods(player, color, playerCount);
            ui.message("[상품 판매] " + color + " " + sold + "개 → 은화 +1, 점수 +" + (sold * playerCount));
            return false;
        }));
    }

    private void handleShipPlacement(Player player) {
        List<Integer> warehouses = new ArrayList<>();
        List<String> options = new ArrayList<>();
        for (int warehouse = 1; warehouse <= 6; warehouse++) {
            List<GoodsColor> goods = ctx.boardState().goodsAt(warehouse);
            if (!goods.isEmpty()) {
                warehouses.add(warehouse);
                options.add("창고 " + warehouse + " 상품 가져오기 " + goods);
            }
        }

        if (warehouses.isEmpty()) {
            ui.message("[배] 가져올 상품이 있는 창고가 없습니다.");
            advanceTurnOrder(player);
            return;
        }

        int choice = ui.chooseIndex(options);
        int warehouse = warehouses.get(choice);
        List<GoodsColor> taken = ctx.boardState().takeAllGoods(warehouse);
        int stored = 0;
        int dropped = 0;
        for (GoodsColor color : taken) {
            if (player.addGoods(color)) {
                stored++;
            } else {
                dropped++;
            }
        }

        ui.message("[배] 창고 " + warehouse + "에서 상품 " + stored + "개를 가져왔습니다.");
        if (dropped > 0) {
            ui.message("[배] 상품 저장소 색 제한으로 " + dropped + "개는 보관하지 못했습니다.");
        }
        advanceTurnOrder(player);
    }

    private void advanceTurnOrder(Player player) {
        if (turnOrder == null) {
            return;
        }
        turnOrder.advance(player);
        ui.message("[다리] " + player.getName() + "의 턴 순서 토큰이 앞으로 이동했습니다.");
    }

    private void takeTileFromBoardAction(Player player,
                                         BoardState boardState,
                                         int usedWorkers,
                                         int adjustedDie,
                                         String prefix,
                                         List<Action> actions) {
        for (int slotIndex = 0; slotIndex < 4; slotIndex++) {
            BoardSlot slot = new BoardSlot(adjustedDie, slotIndex);

            Tile tile = boardState.get(slot).orElse(null);
            if (tile == null) {
                continue;
            }

            int sIdx = slotIndex;
            if (!player.isStorageFull()) {
                String desc = prefix + "섹션 " + adjustedDie + " 슬롯 " + slotIndex
                        + "에서 타일 " + tile.type() + " 가져오기";
                actions.add(new Action(desc, () -> {
                    player.spendWorkers(usedWorkers);
                    PlayerActions.takeTileFromBoard(ctx, player, adjustedDie, sIdx);
                    return false;
                }));
                continue;
            }

            List<Tile> stored = player.storedTiles();
            for (int storageIndex = 0; storageIndex < stored.size(); storageIndex++) {
                int discardIndex = storageIndex;
                Tile discarded = stored.get(storageIndex);
                String desc = prefix + "저장소[" + discardIndex + "] 버리고 섹션 "
                        + adjustedDie + " 슬롯 " + slotIndex
                        + "에서 타일 " + tile.type() + " 가져오기"
                        + " (버림: " + discarded.type() + ")";
                actions.add(new Action(desc, () -> {
                    player.spendWorkers(usedWorkers);
                    PlayerActions.replaceTileFromBoard(ctx, player, adjustedDie, sIdx, discardIndex);
                    return false;
                }));
            }
        }
    }

    private void takeWorkersAction(Player player, List<Action> actions) {
        actions.add(new Action("일꾼 2개 받기", () -> {
            PlayerActions.takeWorkers(player);
            return false;
        }));
    }

    private String renderActionContext(Player player) {
        return """
                [현재 상태]
                타일 저장소: %s
                보유 상품: %s
                창고 상품: %s
                은화/점수: %d/%d
                """.formatted(
                storageSummary(player),
                goodsSummary(player),
                warehouseGoodsSummary(),
                player.getSilver(),
                player.getScore()
        );
    }

    private String storageSummary(Player player) {
        List<Tile> stored = player.storedTiles();
        if (stored.isEmpty()) {
            return "(empty)";
        }

        List<String> parts = new ArrayList<>();
        for (int i = 0; i < stored.size(); i++) {
            Tile tile = stored.get(i);
            parts.add("#" + i + "=" + tile.type() + "(" + tile.id() + ")");
        }
        return String.join(", ", parts);
    }

    private String goodsSummary(Player player) {
        List<String> parts = new ArrayList<>();
        for (GoodsColor color : GoodsColor.values()) {
            int count = player.goods().count(color);
            if (count > 0) {
                parts.add(color + "=" + count);
            }
        }
        return parts.isEmpty() ? "(empty)" : String.join(", ", parts);
    }

    private String warehouseGoodsSummary() {
        List<String> parts = new ArrayList<>();
        for (int warehouse = 1; warehouse <= 6; warehouse++) {
            List<GoodsColor> goods = ctx.boardState().goodsAt(warehouse);
            if (!goods.isEmpty()) {
                parts.add(warehouse + "=" + goods);
            }
        }
        return parts.isEmpty() ? "(empty)" : String.join(", ", parts);
    }

    // 성 추가 행동: 주사위 입력 없이, 가능한 모든 액션을 보여준다.
    public void runCastleExtraAction(Player player, Phase phase) {
        List<Action> extraActions = new ArrayList<>();
        for (int die = 1; die <= 6; die++) {
            for (Action action : buildCastleActionOptionsForDie(player, die, phase)) {
                String desc = "[성 추가 행동][주사위 " + die + "] " + action.description();
                extraActions.add(new Action(desc, action.apply()));
            }
        }

        if (extraActions.isEmpty()) {
            ui.message("\n[성 효과] 추가 행동이 가능하지만 현재 가능한 선택지가 없습니다.");
            return;
        }

        ui.message("\n[성 효과] 추가로 한 번 더 행동할 수 있습니다.");
        Action selected = chooseAction(extraActions);
        boolean grantsExtraAction = selected.apply().getAsBoolean();
        ui.message("=> [성 추가 행동] 실행: " + selected.description());

        if (grantsExtraAction) {
            runCastleExtraAction(player, phase);  // 성으로 또 성을 놓으면 연쇄
        }
    }

    public List<Action> buildCastleActionOptionsForDie(Player player, int baseDie, Phase phase) {
        List<Action> actions = new ArrayList<>();
        addActionsForSetting(player, ctx.boardState(), 0, baseDie, "", phase, actions);
        return actions;
    }

    /** 메뉴를 보여주고 선택을 받아 해당 Action을 반환한다. (실제 출력/입력은 GameUi가 담당) */
    private Action chooseAction(List<Action> actions) {
        List<String> descriptions = new ArrayList<>(actions.size());
        for (Action a : actions) {
            descriptions.add(a.description());
        }
        int choice = ui.chooseIndex(descriptions);
        return actions.get(choice);
    }
}
