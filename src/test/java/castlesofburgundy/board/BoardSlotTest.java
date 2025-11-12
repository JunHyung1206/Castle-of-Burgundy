package castlesofburgundy.board;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardSlotTest {

    @Test
    @DisplayName("정상적인 slot에 대해서는 정상적으로 생성된다.")
    void successTest() {
        BoardSlot boardSlot = new BoardSlot(1, 1);
        Assertions.assertThat(boardSlot.getIndex()).isEqualTo(1);
        Assertions.assertThat(boardSlot).isEqualTo(new BoardSlot(1, 1));
    }

    @Test
    @DisplayName("유효하지 않은 slot이 나왔을 때는 오류가 발생한다")
    void invalidSlot() {
        assertThrows(IllegalArgumentException.class, () -> {
            new BoardSlot(0, 1);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new BoardSlot(7, 1);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new BoardSlot(7, 0);
        });

    }
}
