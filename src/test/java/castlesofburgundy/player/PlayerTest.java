package castlesofburgundy.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    Player player;

    @BeforeEach
    void setUp() {
        PersonalBoard personalBoard = new PersonalBoard(new PersonalLayout());
        personalBoard.setupInitialCastle(19);
        player = new Player(personalBoard, "player1", 2);
    }

    @Test
    @DisplayName("점수와 은화를 더하면 누적된다")
    void scoreAndSilverAccumulate() {
        assertThat(player.getScore()).isZero();
        assertThat(player.getSilver()).isZero();

        player.addScore(16);
        player.addScore(4);
        assertThat(player.getScore()).isEqualTo(20);

        player.addSilver(2);
        assertThat(player.getSilver()).isEqualTo(2);
    }

    @Test
    @DisplayName("일꾼을 사용하면 그만큼 감소한다.")
    void spendWorkersSuccessTest() {
        // given
        assertThat(player.getWorkers()).isEqualTo(2);

        // when
        int remain = player.spendWorkers(1);

        // then
        assertThat(remain).isEqualTo(1);
        assertThat(player.getWorkers()).isEqualTo(1);

        // 한 번 더 사용
        remain = player.spendWorkers(1);
        assertThat(remain).isEqualTo(0);
        assertThat(player.getWorkers()).isEqualTo(0);
    }

    @Test
    @DisplayName("가지고 있는 일꾼보다 많이 쓰려고 하면 예외가 발생한다.")
    void spendWorkersTooManyTest() {
        // 현재 2개 있음
        assertThat(player.getWorkers()).isEqualTo(2);

        assertThrows(IllegalStateException.class, () -> player.spendWorkers(3));
        // 실패 후에도 일꾼 수는 그대로여야 함
        assertThat(player.getWorkers()).isEqualTo(2);
    }

    @Test
    @DisplayName("일꾼 사용 개수가 음수이면 예외가 발생한다.")
    void spendWorkersNegativeTest() {
        assertThrows(IllegalArgumentException.class, () -> player.spendWorkers(-1));
    }

    @Test
    @DisplayName("일꾼을 얻으면 증가한다.")
    void gainWorkersTest() {
        // 기본 2개
        assertThat(player.getWorkers()).isEqualTo(2);

        player.gainWorkers(2);
        assertThat(player.getWorkers()).isEqualTo(4);

        player.spendWorkers(1);
        assertThat(player.getWorkers()).isEqualTo(3);
    }

}
