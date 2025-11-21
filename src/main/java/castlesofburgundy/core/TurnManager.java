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


    private record Option(String description, Runnable run) {
    }

    private void handleDie(Player player, int die, int dieIndex) {
        System.out.println("\n[" + dieIndex + "번째 주사위: " + die + "]");

        List<Option> options = new ArrayList<>();
        BoardState boardState = ctx.boardState();


        takeTileFromBoardAction(player, die, boardState, options); // 1) 시장에서 타일 가져오기 (섹션 = 주사위 눈, 슬롯 0~3)
        placeTileFromStorageAction(player, die, options); // 2) 저장소 → 개인 보드 배치
        takeWorkersAction(player, options); // 3) 일꾼 2개 받기 (항상 가능)

        // 메뉴 출력
        for (int i = 0; i < options.size(); i++) {
            System.out.println(i + ") " + options.get(i).description());
        }

        int choice = readChoice(options.size());
        Option selected = options.get(choice);
        selected.run().run();
        System.out.println("=> 실행: " + selected.description());
    }

    private static void takeWorkersAction(Player player, List<Option> options) {
        options.add(new Option("일꾼 2개 받기", () ->
                PlayerActions.takeWorkers(player)
        ));
    }

    private static void placeTileFromStorageAction(Player player, int die, List<Option> options) {
        if (player.getStorage().isEmpty()) {
            return;
        }

        List<Tile> stored = player.getStorage().view();
        PersonalBoard board = player.getBoard();

        for (int storageIndex = 0; storageIndex < stored.size(); storageIndex++) {
            Tile t = stored.get(storageIndex);
            int sIdx = storageIndex;

            for (int cellId : board.legalPlacements(t, die)) {
                int cId = cellId;
                String desc = "저장소[" + sIdx + "]의 " + t.type() + " 를 cell " + cId + " 에 배치";
                options.add(new Option(desc, () ->
                        PlayerActions.placeTileFromStorage(player, sIdx, cId, die)
                ));
            }
        }
    }

    private void takeTileFromBoardAction(Player player, int die, BoardState boardState, List<Option> options) {
        for (int slotIndex = 0; slotIndex < 4; slotIndex++) {
            if (die < 1 || die > 6) {
                throw new IllegalArgumentException("주사위의 눈은 1~6 사이입니다");
            }
            if (player.getStorage().isFull()) {
                return;
            }
            BoardSlot slot = new BoardSlot(die, slotIndex);
            if (boardState.hasTile(slot)) {
                String desc = "시장 섹션 " + die + " 슬롯 " + slotIndex + "에서 타일 가져오기";
                int sIdx = slotIndex;
                options.add(new Option(desc, () ->
                        PlayerActions.takeTileFromBoard(ctx, player, die, sIdx)
                ));
            }
        }
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
