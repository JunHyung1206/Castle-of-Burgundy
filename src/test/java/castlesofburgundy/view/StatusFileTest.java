package castlesofburgundy.view;

import castlesofburgundy.core.Game;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StatusFileTest {

    @Test
    @DisplayName("상태 파일에 진행 위치, 턴 순서, 공용 보드, 플레이어별 개인 보드를 쓰고 임시 파일은 남기지 않는다")
    void writesStatus(@TempDir Path dir) throws Exception {
        Game game = Game.start(42L, List.of("Alice", "Bob"));
        StatusFile statusFile = new StatusFile(dir.resolve("status.txt"));

        statusFile.write(game.state(), game.pending(), game.options());

        String written = Files.readString(dir.resolve("status.txt"));
        assertThat(written).contains(
                "=== Phase A / Round 1 · 차례: Alice ===",
                "턴 순서: Alice → Bob",
                "=== Market Board",
                "--- Alice ---",
                "--- Bob ---",
                "=== Personal Board ===");
        assertThat(dir.resolve("status.txt.tmp")).doesNotExist();
    }

    @Test
    @DisplayName("다시 쓰면 이전 내용을 덮어쓴다")
    void overwrites(@TempDir Path dir) throws Exception {
        Path path = dir.resolve("status.txt");
        Files.writeString(path, "old");
        Game game = Game.start(1L, List.of("P1"));

        new StatusFile(path).write(game.state(), game.pending(), game.options());

        assertThat(Files.readString(path)).doesNotContain("old").contains("차례: P1");
    }
}
