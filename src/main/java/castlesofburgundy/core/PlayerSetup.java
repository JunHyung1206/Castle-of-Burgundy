package castlesofburgundy.core;

import castlesofburgundy.goods.GoodsSupply;
import castlesofburgundy.player.*;

public final class PlayerSetup {

    private PlayerSetup() {}

    private static final int STARTING_GOODS = 3;

    public static Player[] setupPlayers(GameUi ui, GoodsSupply goodsSupply) {
        int playerCount = readPlayerCount(ui);
        Player[] players = new Player[playerCount];
        PersonalLayout layout = new PersonalLayout();  // 공통 레이아웃 재사용

        for (int i = 0; i < playerCount; i++) {
            String name = ui.askLine("\n플레이어 " + (i + 1) + " 이름을 입력하세요: ").trim();
            if (name.isEmpty()) {
                name = "플레이어" + (i + 1);
            }

            PersonalBoard board = new PersonalBoard(layout);
            board.setupStartingCastle();

            ui.message("\n--- 개인 보드 초기 상태 (" + name + ", 시작 성: " + layout.startingCastleCell() + "번 칸) ---");
            ui.showPersonalBoard(board);

            // 시작 플레이어부터 일꾼 1, 2, 3, 4개 + 은화 1개 + 무작위 상품 3개
            Player player = new Player(board, name, i + 1);
            player.addSilver(1);
            for (int g = 0; g < STARTING_GOODS; g++) {
                player.getBoard().getGoods().add(goodsSupply.draw());
            }
            players[i] = player;
        }
        return players;
    }

    private static int readPlayerCount(GameUi ui) {
        while (true) {
            String line = ui.askLine("플레이어 수를 입력하세요 (1~4): ");
            try {
                int n = Integer.parseInt(line.trim());
                if (n >= 1 && n <= 4) {
                    return n;
                }
            } catch (NumberFormatException ignored) {
            }
            ui.message("1~4 사이 숫자를 입력하세요.");
        }
    }
}
