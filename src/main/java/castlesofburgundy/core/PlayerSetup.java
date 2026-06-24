package castlesofburgundy.core;

import castlesofburgundy.player.*;
import castlesofburgundy.tile.TileType;

public final class PlayerSetup {

    private PlayerSetup() {}

    public static Player[] setupPlayers(GameUi ui, int playerCount) {
        Player[] players = new Player[playerCount];
        PersonalLayout layout = new PersonalLayout();  // 공통 레이아웃 재사용

        for (int i = 0; i < playerCount; i++) {
            String name = ui.askLine("\n플레이어 " + (i + 1) + " 이름을 입력하세요: ").trim();
            if (name.isEmpty()) {
                name = "플레이어" + (i + 1);
            }

            PersonalBoard board = new PersonalBoard(layout);

            ui.message("\n--- 개인 보드 초기 상태 (" + name + ") ---");
            ui.showPersonalBoard(board);

            int castleCellId = chooseInitialCastleCell(layout, ui, name);

            board.setupInitialCastle(castleCellId);

            ui.message("\n--- 성 배치 완료 (" + name + ") ---");
            ui.showPersonalBoard(board);

            players[i] = new Player(board, 3, name); // 기본 일꾼 2개 생성자
        }
        return players;
    }

    private static int chooseInitialCastleCell(PersonalLayout layout, GameUi ui, String playerName) {
        ui.message("\n=== " + playerName + " 시작 성 위치 선택 ===");

        ui.message("선택 가능한 성 칸(id):");
        layout.all().stream()
                .filter(cell -> cell.type() == TileType.CASTLE)
                .forEach(cell ->
                        ui.message("id=" + cell.id() + " (die=" + cell.die() + ")")
                );

        while (true) {
            String line = ui.askLine("성 시작 위치로 사용할 칸 id를 입력하세요: ");
            try {
                int id = Integer.parseInt(line);
                PersonalCell cell = layout.get(id);
                if (cell == null) {
                    ui.message("존재하지 않는 칸입니다. 다시 입력하세요.");
                    continue;
                }
                if (cell.type() != TileType.CASTLE) {
                    ui.message("해당 칸은 성이 아닙니다. 다시 입력하세요.");
                    continue;
                }
                return id;
            } catch (NumberFormatException e) {
                ui.message("숫자를 입력해 주세요.");
            }
        }
    }
}
