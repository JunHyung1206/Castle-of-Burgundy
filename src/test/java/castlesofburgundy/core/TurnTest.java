package castlesofburgundy.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TurnTest {

    @Test
    @DisplayName("굴린 주사위를 하나씩 쓰면 남은 주사위에서 빠진다")
    void useDieRemovesOne() {
        Turn turn = new Turn(3, 3);

        turn.useDie(3);

        assertThat(turn.remainingDice()).containsExactly(3);
    }

    @Test
    @DisplayName("남아 있지 않은 주사위는 쓸 수 없다")
    void cannotUseMissingDie() {
        Turn turn = new Turn(2, 5);

        assertThrows(IllegalArgumentException.class, () -> turn.useDie(4));
        assertThat(turn.remainingDice()).containsExactly(2, 5);
    }

    @Test
    @DisplayName("중앙 시장 구매는 턴당 한 번이다")
    void centralMarketOncePerTurn() {
        Turn turn = new Turn(1, 2);

        turn.markCentralMarketBought();

        assertThat(turn.canBuyFromCentralMarket()).isFalse();
        assertThrows(IllegalStateException.class, turn::markCentralMarketBought);
    }
}
