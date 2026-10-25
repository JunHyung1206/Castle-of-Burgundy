package castlesofburgundy.core;

import castlesofburgundy.board.Depot;
import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.AnimalTile;
import castlesofburgundy.tile.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public final class TurnManager {
    private final GameContext ctx;
    private final GameUi ui;
    private final PlayerActions playerActions;

    public TurnManager(GameContext ctx, GameUi ui) {
        this(ctx, ui, 1);
    }

    public TurnManager(GameContext ctx, GameUi ui, int playerCount) {
        this.ctx = ctx;
        this.ui = ui;
        this.playerActions = new PlayerActions(ctx, ui, playerCount);
    }

    public void playerTurn(Player player, Phase phase) {
        ui.message("\n===" + player.getName() + "의 턴 ===");
        int d1 = ctx.dice().roll();
        int d2 = ctx.dice().roll();

        ui.message("굴린 주사위: " + d1 + ", " + d2);

        Turn turn = new Turn();
        handleDie(player, d1, 1, phase, turn);
        handleDie(player, d2, 2, phase, turn);
        offerCentralMarketAfterDice(player, turn);
    }

    /**
     * 하나의 선택 가능한 액션. 메뉴 설명과 실행만 담고, 실제 상태 변경은 PlayerActions가 한다.
     * apply 실행 결과 boolean = 성 효과처럼 '추가 행동'을 부여하면 true.
     * usesDie = false면 주사위를 쓰지 않는 액션(중앙 시장 구매)이라 같은 주사위로 다시 고른다.
     */
    public record Action(String description, boolean usesDie, BooleanSupplier apply) {
        public Action(String description, BooleanSupplier apply) {
            this(description, true, apply);
        }
    }

    private record DieSetting(int adjustedDie, String label) {
    }

    private void handleDie(Player player, int die, int dieIndex, Phase phase, Turn turn) {
        ui.message("\n[" + dieIndex + "번째 주사위: " + die + "]");
        while (true) {
            ui.message("현재 일꾼 수 : " + player.getWorkers());

            List<Action> actions = buildOptionsForDie(player, die, phase, turn);
            ui.message(renderActionContext(player, die));

            Action selected = chooseAction(actions);
            boolean grantsExtraAction = selected.apply().getAsBoolean();
            ui.message("=> 실행: " + selected.description());

            if (!selected.usesDie()) {
                continue; // 중앙 시장 구매는 주사위를 쓰지 않으므로 같은 주사위로 다시 고른다
            }
            if (grantsExtraAction) {
                runCastleExtraAction(player, phase);
            }
            return;
        }
    }

    /** 주사위를 다 쓴 뒤에도 이번 턴에 구매하지 않았다면 중앙 시장 구매 기회를 한 번 준다. */
    private void offerCentralMarketAfterDice(Player player, Turn turn) {
        List<Action> purchases = new ArrayList<>();
        buyTileFromCentralMarketAction(player, turn, purchases);
        if (purchases.isEmpty()) {
            return;
        }

        List<Action> options = new ArrayList<>();
        options.add(new Action("구매하지 않고 턴 종료", false, () -> false));
        options.addAll(purchases);

        ui.message("\n[턴 종료 전] 중앙 시장에서 한 번 구매할 수 있습니다.");
        Action selected = chooseAction(options);
        selected.apply().getAsBoolean();
        ui.message("=> 실행: " + selected.description());
    }

    private static int wrapAdd(int face, int delta) {
        return ((face - 1 + delta) % 6 + 6) % 6 + 1;
    }

    List<Action> buildOptionsForDie(Player player, int baseDie, Phase phase, Turn turn) {
        List<Action> actions = new ArrayList<>();
        int availableWorkers = player.getWorkers();
        int maxWorkersToUse = Math.min(3, availableWorkers);

        // 일꾼 0개 사용 (조정 없음)
        addActionsForSetting(player, 0, baseDie, "", phase, actions);

        // 일꾼 1~max 사용, 각 ± 방향
        for (int used = 1; used <= maxWorkersToUse; used++) {
            int minusDie = wrapAdd(baseDie, -used);
            addActionsForSetting(player, used, minusDie, "[일꾼 " + used + "개 소모] ", phase, actions);

            if (used == 3) {
                break;  // ±3은 같은 눈이라 한 번만 표시
            }
            int plusDie = wrapAdd(baseDie, +used);
            addActionsForSetting(player, used, plusDie, "[일꾼 " + used + "개 소모] ", phase, actions);
        }

        buyTileFromCentralMarketAction(player, turn, actions);
        takeWorkersAction(player, actions);
        return actions;
    }

    private void addActionsForSetting(Player player, int usedWorkers, int adjustedDie, String prefix, Phase phase, List<Action> actions) {
        takeTileFromBoardAction(player, usedWorkers, adjustedDie, prefix, actions);
        placeTileFromStorageAction(player, usedWorkers, adjustedDie, prefix, phase, actions);
        sellGoodsAction(player, usedWorkers, adjustedDie, prefix, actions);
    }

    private void placeTileFromStorageAction(Player player,
                                            int usedWorkers,
                                            int adjustedDie,
                                            String prefix,
                                            Phase phase,
                                            List<Action> actions) {
        if (player.getBoard().getStorage().isEmpty()) {
            return;
        }

        List<Tile> stored = player.getBoard().getStorage().view();

        for (int storageIndex = 0; storageIndex < stored.size(); storageIndex++) {
            Tile t = stored.get(storageIndex);
            int sIdx = storageIndex;

            for (int cellId : player.getBoard().legalPlacements(t, adjustedDie)) {
                String desc = prefix + "저장소[" + sIdx + "]의 " + t.type() + " 를 cell " + cellId + " 에 배치";

                actions.add(new Action(desc, () -> playerActions
                        .placeTileFromStorage(player, usedWorkers, sIdx, cellId, adjustedDie, phase)
                        .grantsExtraAction()));
            }
        }
    }

    private void buyTileFromCentralMarketAction(Player player, Turn turn, List<Action> actions) {
        if (!turn.canBuyFromCentralMarket() || player.getSilver() < 2) {
            return;
        }

        List<Tile> centralMarket = ctx.board().centralMarket();
        if (centralMarket.isEmpty()) {
            return;
        }

        for (int marketIndex = 0; marketIndex < centralMarket.size(); marketIndex++) {
            int mIdx = marketIndex;
            Tile tile = centralMarket.get(marketIndex);
            if (!player.getBoard().getStorage().isFull()) {
                String desc = "[주사위 미사용] 은화 2개로 중앙 시장[" + marketIndex + "]의 "
                        + tile.type() + " 구매";
                actions.add(new Action(desc, false, () -> {
                    playerActions.buyTileFromCentralMarket(player, mIdx);
                    turn.markCentralMarketBought();
                    return false;
                }));
                continue;
            }

            List<Tile> stored = player.getBoard().getStorage().view();
            for (int storageIndex = 0; storageIndex < stored.size(); storageIndex++) {
                int discardIndex = storageIndex;
                Tile discarded = stored.get(storageIndex);
                String desc = "[주사위 미사용] 저장소[" + discardIndex + "] 버리고 은화 2개로 중앙 시장["
                        + marketIndex + "]의 " + tile.type() + " 구매"
                        + " (버림: " + discarded.type() + ")";
                actions.add(new Action(desc, false, () -> {
                    playerActions.replaceTileFromCentralMarket(player, mIdx, discardIndex);
                    turn.markCentralMarketBought();
                    return false;
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
        int count = player.getBoard().getGoods().count(color);
        if (count == 0) {
            return;
        }

        String desc = prefix + color + " 상품 판매 (" + count + "개)";
        actions.add(new Action(desc, () -> {
            int sold = playerActions.sellGoods(player, usedWorkers, color);
            ui.message("[상품 판매] " + color + " " + sold + "개 → 은화 +1, 점수 +" + playerActions.scoreForSelling(sold));
            return false;
        }));
    }

    private void takeTileFromBoardAction(Player player,
                                         int usedWorkers,
                                         int adjustedDie,
                                         String prefix,
                                         List<Action> actions) {
        Depot depot = ctx.board().depot(adjustedDie);
        for (int slotIndex = 0; slotIndex < depot.slotCount(); slotIndex++) {
            Tile tile = depot.tileAt(slotIndex).orElse(null);
            if (tile == null) {
                continue;
            }

            int sIdx = slotIndex;
            if (!player.getBoard().getStorage().isFull()) {
                String desc = prefix + "섹션 " + adjustedDie + " 슬롯 " + slotIndex
                        + "에서 타일 " + tile.type() + " 가져오기";
                actions.add(new Action(desc, () -> {
                    playerActions.takeTileFromBoard(player, usedWorkers, adjustedDie, sIdx);
                    return false;
                }));
                continue;
            }

            List<Tile> stored = player.getBoard().getStorage().view();
            for (int storageIndex = 0; storageIndex < stored.size(); storageIndex++) {
                int discardIndex = storageIndex;
                Tile discarded = stored.get(storageIndex);
                String desc = prefix + "저장소[" + discardIndex + "] 버리고 섹션 "
                        + adjustedDie + " 슬롯 " + slotIndex
                        + "에서 타일 " + tile.type() + " 가져오기"
                        + " (버림: " + discarded.type() + ")";
                actions.add(new Action(desc, () -> {
                    playerActions.replaceTileFromBoard(player, usedWorkers, adjustedDie, sIdx, discardIndex);
                    return false;
                }));
            }
        }
    }

    private void takeWorkersAction(Player player, List<Action> actions) {
        actions.add(new Action("일꾼 2개 받기", () -> {
            playerActions.takeWorkers(player);
            return false;
        }));
    }

    private String renderActionContext(Player player, int baseDie) {
        return """
                [현재 상태]
                타일 저장소: %s
                보유 상품: %s
                판매 가능: %s
                동물 점수 후보: %s
                창고 상품: %s
                은화/점수: %d/%d
                """.formatted(
                storageSummary(player),
                goodsSummary(player),
                sellableGoodsSummary(player, baseDie),
                animalScoreCandidatesSummary(player, baseDie),
                depotGoodsSummary(),
                player.getSilver(),
                player.getScore()
        );
    }

    private String storageSummary(Player player) {
        List<Tile> stored = player.getBoard().getStorage().view();
        if (stored.isEmpty()) {
            return "(empty)";
        }

        List<String> parts = new ArrayList<>();
        for (int i = 0; i < stored.size(); i++) {
            Tile tile = stored.get(i);
            parts.add("#" + i + "=" + tileSummary(tile));
        }
        return String.join(", ", parts);
    }

    private String tileSummary(Tile tile) {
        if (tile instanceof AnimalTile animal) {
            return tile.type() + ":" + animal.species() + " x" + animal.count() + "(" + tile.id() + ")";
        }
        return tile.type() + "(" + tile.id() + ")";
    }

    private String goodsSummary(Player player) {
        List<String> parts = new ArrayList<>();
        for (GoodsColor color : GoodsColor.values()) {
            int count = player.getBoard().getGoods().count(color);
            if (count > 0) {
                parts.add(color + "=" + count);
            }
        }
        return parts.isEmpty() ? "(empty)" : String.join(", ", parts);
    }

    private String sellableGoodsSummary(Player player, int baseDie) {
        List<String> parts = new ArrayList<>();
        for (DieSetting setting : dieSettings(baseDie, player.getWorkers())) {
            GoodsColor color = GoodsColor.forDie(setting.adjustedDie());
            int count = player.getBoard().getGoods().count(color);
            if (count > 0) {
                parts.add(setting.label() + " 주사위 " + setting.adjustedDie()
                        + " -> " + color + " " + count + "개 판매 가능"
                        + " (+은화 1, +" + playerActions.scoreForSelling(count) + "점)");
            }
        }
        return parts.isEmpty() ? "(없음)" : "\n- " + String.join("\n- ", parts);
    }

    private String animalScoreCandidatesSummary(Player player, int baseDie) {
        if (player.getBoard().getStorage().isEmpty()) {
            return "(없음)";
        }

        List<String> parts = new ArrayList<>();
        List<Tile> stored = player.getBoard().getStorage().view();
        for (int storageIndex = 0; storageIndex < stored.size(); storageIndex++) {
            Tile tile = stored.get(storageIndex);
            if (!(tile instanceof AnimalTile animal)) {
                continue;
            }

            for (DieSetting setting : dieSettings(baseDie, player.getWorkers())) {
                for (int cellId : player.getBoard().legalPlacements(animal, setting.adjustedDie())) {
                    int score = animalScoreIfPlaced(player, animal, cellId);
                    parts.add(setting.label() + " 주사위 " + setting.adjustedDie()
                            + " -> 저장소[" + storageIndex + "] "
                            + animal.species() + " x" + animal.count()
                            + " cell " + cellId + " 배치 시 +" + score + "점");
                }
            }
        }
        return parts.isEmpty() ? "(없음)" : "\n- " + String.join("\n- ", parts);
    }

    private int animalScoreIfPlaced(Player player, AnimalTile animal, int cellId) {
        int score = animal.count();
        for (Tile placed : player.getBoard().placedTilesInRegion(cellId)) {
            if (placed instanceof AnimalTile other && other.species() == animal.species()) {
                score += other.count();
            }
        }
        return score;
    }

    private List<DieSetting> dieSettings(int baseDie, int availableWorkers) {
        List<DieSetting> settings = new ArrayList<>();
        settings.add(new DieSetting(baseDie, "현재"));

        int maxWorkersToUse = Math.min(3, availableWorkers);
        for (int used = 1; used <= maxWorkersToUse; used++) {
            settings.add(new DieSetting(wrapAdd(baseDie, -used), "일꾼 " + used + "개 사용"));
            if (used == 3) {
                break;
            }
            settings.add(new DieSetting(wrapAdd(baseDie, used), "일꾼 " + used + "개 사용"));
        }
        return settings;
    }

    private String depotGoodsSummary() {
        List<String> parts = new ArrayList<>();
        for (Depot depot : ctx.board().depots()) {
            List<GoodsColor> goods = depot.goods();
            if (!goods.isEmpty()) {
                parts.add(depot.number() + "=" + goods);
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
        addActionsForSetting(player, 0, baseDie, "", phase, actions);
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
