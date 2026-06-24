package castlesofburgundy.player;

import castlesofburgundy.tile.BuildingKind;
import castlesofburgundy.tile.BuildingTile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BuildingPlacementTest {

    private PersonalBoard board;

    @BeforeEach
    void setUp() {
        board = new PersonalBoard(new PersonalLayout());
        board.setupInitialCastle(19);
        // 도시 {26,27,32,33,37}의 26번에 망루를 둔다
        board.place(26, new BuildingTile(0, BuildingKind.WATCHTOWER), 3);
    }

    @Test
    @DisplayName("같은 도시에 같은 종류 건물은 둘 수 없다")
    void sameKindSameCityRejected() {
        // cell 27도 같은 도시 → 망루 거부
        assertThat(board.canPlace(27, new BuildingTile(1, BuildingKind.WATCHTOWER), 1)).isFalse();
    }

    @Test
    @DisplayName("같은 도시라도 다른 종류 건물은 둘 수 있다")
    void differentKindSameCityAllowed() {
        assertThat(board.canPlace(27, new BuildingTile(1, BuildingKind.BANK), 1)).isTrue();
    }

    @Test
    @DisplayName("다른 도시라면 같은 종류 건물도 둘 수 있다")
    void sameKindDifferentCityAllowed() {
        // cell 12는 단독 도시 {12} → 망루 허용
        assertThat(board.canPlace(12, new BuildingTile(1, BuildingKind.WATCHTOWER), 3)).isTrue();
    }
}
