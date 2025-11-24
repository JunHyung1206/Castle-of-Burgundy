package castlesofburgundy.core;

import castlesofburgundy.player.*;
import castlesofburgundy.tile.TileType;
import castlesofburgundy.view.PlayerConsoleView;

import java.util.Scanner;

public final class PlayerSetup {

    private PlayerSetup() {}

    public static Player[] setupPlayers(Scanner in, int playerCount) {
        Player[] players = new Player[playerCount];
        PersonalLayout layout = new PersonalLayout();  // 공통 레이아웃 재사용

        for (int i = 0; i < playerCount; i++) {
            System.out.print("\n플레이어 " + (i + 1) + " 이름을 입력하세요: ");
            String name = in.nextLine().trim();
            if (name.isEmpty()) {
                name = "플레이어" + (i + 1);
            }

            PersonalBoard board = new PersonalBoard(layout);

            System.out.println("\n--- 개인 보드 초기 상태 (" + name + ") ---");
            System.out.println(PlayerConsoleView.renderBoardOnly(board));

            int castleCellId = chooseInitialCastleCell(layout, in, name);

            board.setupInitialCastle(castleCellId);

            System.out.println("\n--- 성 배치 완료 (" + name + ") ---");
            System.out.println(PlayerConsoleView.renderBoardOnly(board));


            players[i] = new Player(board, 3, name); // 기본 일꾼 2개 생성자
        }
        return players;
    }

    private static int chooseInitialCastleCell(PersonalLayout layout, Scanner in, String playerName) {
        System.out.println("\n=== " + playerName + " 시작 성 위치 선택 ===");

        System.out.println("선택 가능한 성 칸(id):");
        layout.all().stream()
                .filter(cell -> cell.type() == TileType.CASTLE)
                .forEach(cell ->
                        System.out.println("id=" + cell.id() + " (die=" + cell.die() + ")")
                );

        while (true) {
            System.out.print("성 시작 위치로 사용할 칸 id를 입력하세요: ");
            String line = in.nextLine();
            try {
                int id = Integer.parseInt(line);
                PersonalCell cell = layout.get(id);
                if (cell == null) {
                    System.out.println("존재하지 않는 칸입니다. 다시 입력하세요.");
                    continue;
                }
                if (cell.type() != TileType.CASTLE) {
                    System.out.println("해당 칸은 성이 아닙니다. 다시 입력하세요.");
                    continue;
                }
                return id;
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력해 주세요.");
            }
        }
    }
}