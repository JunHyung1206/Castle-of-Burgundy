package castlesofburgundy.core;

import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.PersonalLayout;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.BuildingKind;
import castlesofburgundy.tile.BuildingTile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BuildingTileTest {

    private Player player;

    @BeforeEach
    void setUp() {
        PersonalBoard board = new PersonalBoard(new PersonalLayout());
        board.setupInitialCastle(19);
        player = new Player(board, "P1");
    }

    /** cell 26(BUILDING/die3)은 성(19)과 인접하고, 도시 {26,27,32,33,37}이라 단독 배치로는 구역이 완성되지 않는다. */
    private void placeBuildingAt26(BuildingKind kind) {
        player.getBoard().getStorage().add(new BuildingTile(0, kind));
        TestGames.actions(2).placeTileFromStorage(player, 0, 0, 26, 3, Phase.A);
    }

    @Test
    @DisplayName("망루는 즉시 4점을 준다")
    void watchtowerScores4() {
        int before = player.getScore();
        placeBuildingAt26(BuildingKind.WATCHTOWER);
        assertThat(player.getScore() - before).isEqualTo(4);
    }

    @Test
    @DisplayName("은행은 즉시 은화 2개를 준다")
    void bankGives2Silver() {
        int before = player.getSilver();
        placeBuildingAt26(BuildingKind.BANK);
        assertThat(player.getSilver() - before).isEqualTo(2);
    }

    @Test
    @DisplayName("하숙집은 즉시 일꾼 4개를 준다")
    void boardingHouseGives4Workers() {
        int before = player.getWorkers();
        placeBuildingAt26(BuildingKind.BOARDING_HOUSE);
        assertThat(player.getWorkers() - before).isEqualTo(4);
    }
}
