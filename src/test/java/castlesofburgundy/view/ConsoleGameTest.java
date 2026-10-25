package castlesofburgundy.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleGameTest {

    @Test
    @DisplayName("뒤로·잘못된 입력이 섞인 무작위 입력으로도 게임이 끝까지 진행되고 상태 파일이 남는다")
    void playsToEndWithRandomInput(@TempDir Path dir) throws Exception {
        StringBuilder input = new StringBuilder("3\nA\nB\nC\n");
        Random random = new Random(7);
        for (int i = 0; i < 200_000; i++) {
            int r = random.nextInt(12);
            input.append(r < 9 ? String.valueOf(r) : r == 9 ? "b" : r == 10 ? "x" : "99").append('\n');
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Path status = dir.resolve("status.txt");

        new ConsoleGame(new Scanner(input.toString()), new PrintStream(bytes, true, StandardCharsets.UTF_8), status)
                .run(42L);

        String output = bytes.toString(StandardCharsets.UTF_8);
        assertThat(output).contains("=== 모든 페이즈 종료 ===", "=== 최종 플레이어 보드 (C) ===", "b) 뒤로");
        // 보드 그림은 상태 파일로 옮겨, 진행 화면에는 마지막 결과에서 한 번만 나온다
        assertThat(output.split("=== Market Board", -1)).hasSize(2);
        assertThat(output).doesNotContain("현재 보드 상태", "[현재 상태]");
        assertThat(Files.readString(status)).contains("=== 게임 종료 ===");
    }
}
