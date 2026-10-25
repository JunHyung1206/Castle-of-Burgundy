package castlesofburgundy.view;

import castlesofburgundy.core.Decision;
import castlesofburgundy.core.Game;
import castlesofburgundy.player.Player;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * 두 번째 터미널에서 볼 상태 화면(공용 보드, 플레이어별 개인 보드, 이번 주사위 요약)을 파일로 쓴다.
 * 보는 쪽이 쓰다 만 파일을 읽지 않도록 임시 파일에 쓴 뒤 한 번에 바꾼다.
 */
final class StatusFile {
    private final Path path;

    StatusFile(Path path) {
        this.path = path.toAbsolutePath();
    }

    Path path() {
        return path;
    }

    void write(Game game) throws IOException {
        Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
        Files.writeString(tmp, render(game), StandardCharsets.UTF_8);
        Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    static String render(Game game) {
        StringBuilder sb = new StringBuilder();
        List<Player> order = game.board().turnOrder().currentOrder();
        if (game.isOver()) {
            sb.append("=== 게임 종료 ===\n");
        } else {
            sb.append("=== Phase ").append(game.phase()).append(" / Round ").append(game.round())
                    .append(" · 차례: ").append(game.currentPlayer().getName()).append(" ===\n");
        }
        sb.append("턴 순서: ").append(String.join(" → ", order.stream().map(Player::getName).toList())).append("\n\n");

        sb.append(GameBoardConsoleView.render(game.board()));

        for (Player player : order) {
            sb.append("\n\n--- ").append(player.getName()).append(" ---");
            sb.append(PlayerConsoleView.render(player));
        }

        if (game.pending() instanceof Decision.ChooseDieAction d) {
            sb.append("\n--- ").append(game.currentPlayer().getName())
                    .append("의 주사위 ").append(d.die()).append(" ---\n");
            sb.append(TurnContextView.render(game, d.die()));
        }
        return sb.toString();
    }
}
