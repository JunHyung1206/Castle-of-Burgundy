package castlesofburgundy.core;

import castlesofburgundy.board.BoardSlot;
import castlesofburgundy.board.BoardState;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public final class TurnManager {
    private final GameContext ctx;
    private final GameUi ui;

    public TurnManager(GameContext ctx, GameUi ui) {
        this.ctx = ctx;
        this.ui = ui;
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

    private void takeTileFromBoardAction(Player player,
                                         BoardState boardState,
                                         int usedWorkers,
                                         int adjustedDie,
                                         String prefix,
                                         List<Action> actions) {
        if (player.isStorageFull()) {
            return;
        }

        for (int slotIndex = 0; slotIndex < 4; slotIndex++) {
            BoardSlot slot = new BoardSlot(adjustedDie, slotIndex);

            Tile tile = boardState.get(slot).orElse(null);
            if (tile != null) {
                int sIdx = slotIndex;
                String desc = prefix + "섹션 " + adjustedDie + " 슬롯 " + slotIndex
                        + "에서 타일 " + tile.type() + " 가져오기";
                actions.add(new Action(desc, () -> {
                    player.spendWorkers(usedWorkers);
                    PlayerActions.takeTileFromBoard(ctx, player, adjustedDie, sIdx);
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
