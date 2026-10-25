package castlesofburgundy.core;

import castlesofburgundy.goods.GoodsSupply;
import castlesofburgundy.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerSetupTest {

    @Test
    @DisplayName("플레이어 수만큼 생성하고, 시작 순서대로 일꾼 1·2개 + 은화 1개를 준다")
    void setsUpPlayersWithOrderedWorkersAndSilver() {
        RecordingGameUi ui = new RecordingGameUi();
        // 플레이어 수 → (이름, 성 칸) × 2
        ui.scriptLines("2", "Alice", "19", "Bob", "7");

        Player[] players = PlayerSetup.setupPlayers(ui, new GoodsSupply(1L));

        assertThat(players).hasSize(2);

        assertThat(players[0].getName()).isEqualTo("Alice");
        assertThat(players[0].getWorkers()).isEqualTo(1);
        assertThat(players[0].getSilver()).isEqualTo(1);
        assertThat(players[0].getBoard().getGoods().totalStored()).isEqualTo(3);
        assertThat(players[0].getBoard().isOccupied(19)).isTrue();

        assertThat(players[1].getName()).isEqualTo("Bob");
        assertThat(players[1].getWorkers()).isEqualTo(2);
        assertThat(players[1].getSilver()).isEqualTo(1);
        assertThat(players[1].getBoard().getGoods().totalStored()).isEqualTo(3);
        assertThat(players[1].getBoard().isOccupied(7)).isTrue();
    }

    @Test
    @DisplayName("플레이어 수 입력이 범위를 벗어나면 다시 묻는다")
    void repromptsOnInvalidPlayerCount() {
        RecordingGameUi ui = new RecordingGameUi();
        // 0, 비숫자 → 거부, 그 다음 1로 진행 (이름, 성 칸)
        ui.scriptLines("0", "x", "1", "Solo", "19");

        Player[] players = PlayerSetup.setupPlayers(ui, new GoodsSupply(1L));

        assertThat(players).hasSize(1);
        assertThat(players[0].getWorkers()).isEqualTo(1);
        assertThat(ui.messaged("1~4 사이 숫자")).isTrue();
    }
}
