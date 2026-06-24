package castlesofburgundy.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiceTest {

    @Test
    @DisplayName("주사위 눈은 항상 1~6 범위 안이다")
    void rollIsInRange() {
        Dice dice = new Dice(42L);
        for (int i = 0; i < 10_000; i++) {
            int roll = dice.roll();
            assertThat(roll).isBetween(1, 6);
        }
    }

    @Test
    @DisplayName("같은 시드의 주사위는 동일한 수열을 만든다 (재현 가능)")
    void sameSeedSameSequence() {
        Dice a = new Dice(42L);
        Dice b = new Dice(42L);

        List<Integer> seqA = new ArrayList<>();
        List<Integer> seqB = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            seqA.add(a.roll());
            seqB.add(b.roll());
        }

        assertThat(seqA).isEqualTo(seqB);
    }
}
