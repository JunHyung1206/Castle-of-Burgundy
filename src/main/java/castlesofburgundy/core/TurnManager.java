package castlesofburgundy.core;

import castlesofburgundy.board.BoardSlot;
import castlesofburgundy.board.BoardState;
import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class TurnManager {
    private final GameContext ctx;
    private final Scanner in;

    public TurnManager(GameContext ctx, Scanner in) {
        this.ctx = ctx;
        this.in = in;
    }

    public void playerTurn(Player player) {
        System.out.println("\n===" + player.getName() + "의 턴 ===");
        int d1 = Dice.roll();
        int d2 = Dice.roll();

        System.out.println("굴린 주사위: " + d1 + ", " + d2);

        handleDie(player, d1, 1);
        handleDie(player, d2, 2);
    }


    public record Option(String description, Runnable run) {
    }


    private void handleDie(Player player, int die, int dieIndex) {
        System.out.println("\n[" + dieIndex + "번째 주사위: " + die + "]");
        System.out.println("현재 일꾼 수 : " + player.getWorkers());

        List<Option> options = buildOptionsForDie(player, die);

        // 메뉴 출력
        for (int i = 0; i < options.size(); i++) {
            System.out.println(i + ") " + options.get(i).description());
        }

        int choice = readChoice(options.size());
        Option selected = options.get(choice);
        selected.run().run();
        System.out.println("=> 실행: " + selected.description());
    }


    private static int wrapAdd(int face, int delta) {
        return ((face - 1 + delta) % 6 + 6) % 6 + 1;
    }


    public List<Option> buildOptionsForDie(Player player, int baseDie) {
        List<Option> options = new ArrayList<>();
        BoardState boardState = ctx.boardState();

        int availableWorkers = player.getWorkers();
        int maxWorkersToUse = Math.min(3, availableWorkers);

        // 일꾼 0개 사용 (조정 없음)
        addActionsForSetting(player, boardState, 0, baseDie, "", options);

        // 일꾼 1~max 사용, 각 ± 방향
        for (int used = 1; used <= maxWorkersToUse; used++) {
            int minusDie = wrapAdd(baseDie, -used);
            addActionsForSetting(player, boardState, used, minusDie, "[일꾼 " + used + "개 소모] ", options);

            int plusDie = wrapAdd(baseDie, +used);
            addActionsForSetting(player, boardState, used, plusDie, "[일꾼 " + used + "개 소모] ", options);
        }

        takeWorkersAction(player, options);
        return options;
    }

    private void addActionsForSetting(Player player, BoardState boardState, int usedWorkers, int adjustedDie, String prefix, List<Option> options) {
        takeTileFromBoardAction(player, boardState, usedWorkers, adjustedDie, prefix, options);
        placeTileFromStorageAction(player, usedWorkers, adjustedDie, prefix, options);
    }

    private static void placeTileFromStorageAction(Player player, int usedWorkers, int adjustedDie, String prefix, List<Option> options) {
        if (player.getStorage().isEmpty()) {
            return;
        }

        List<Tile> stored = player.getStorage().view();
        PersonalBoard board = player.getBoard();

        for (int storageIndex = 0; storageIndex < stored.size(); storageIndex++) {
            Tile t = stored.get(storageIndex);
            int sIdx = storageIndex;

            for (int cellId : board.legalPlacements(t, adjustedDie)) {
                String desc = prefix + "저장소[" + sIdx + "]의 " + t.type() + " 를 cell " + cellId + " 에 배치";

                options.add(new Option(desc, () -> {
                    player.spendWorkers(usedWorkers);
                    PlayerActions.placeTileFromStorage(player, sIdx, cellId, adjustedDie);
                }));
            }
        }
    }


    private void takeTileFromBoardAction(Player player, BoardState boardState, int usedWorkers, int adjustedDie, String prefix, List<Option> options) {
        if (player.getStorage().isFull()) {
            return;
        }

        for (int slotIndex = 0; slotIndex < 4; slotIndex++) {
            BoardSlot slot = new BoardSlot(adjustedDie, slotIndex);
            if (boardState.hasTile(slot)) {
                int sIdx = slotIndex;
                String desc = prefix + "섹션 " + adjustedDie + " 슬롯 " + slotIndex + "에서 타일 가져오기";
                options.add(new Option(desc, () -> {
                    player.spendWorkers(usedWorkers);
                    PlayerActions.takeTileFromBoard(ctx, player, adjustedDie, sIdx);
                }));
            }
        }
    }

    private void takeWorkersAction(Player player, List<Option> options) {
        options.add(new Option("일꾼 2개 받기", () -> PlayerActions.takeWorkers(player)));
    }

    private int readChoice(int size) {
        while (true) {
            System.out.print("번호 선택(0~" + (size - 1) + "): ");
            String line = in.nextLine();
            try {
                int v = Integer.parseInt(line);
                if (v >= 0 && v < size) return v;
            } catch (NumberFormatException ignored) {
            }
            System.out.println("잘못된 입력입니다.");
        }
    }
}
