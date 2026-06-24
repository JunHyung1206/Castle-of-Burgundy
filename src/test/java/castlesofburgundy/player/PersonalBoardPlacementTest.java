package castlesofburgundy.player;

import castlesofburgundy.tile.BaseTile;
import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersonalBoardPlacementTest {

    private PersonalBoard board;

    // cell 13: KNOWLEDGE / die 1, 시작 성(19)과 인접
    private final Tile knowledge = BaseTile.of(TileType.KNOWLEDGE, 100);
    private final Tile ship = BaseTile.of(TileType.SHIP, 101);

    @BeforeEach
    void setUp() {
        board = new PersonalBoard(new PersonalLayout());
        board.setupInitialCastle(19);
    }

    @Test
    @DisplayName("타입·주사위가 맞고 성과 인접하면 배치 가능하다")
    void canPlace_valid() {
        assertThat(board.canPlace(13, knowledge, 1)).isTrue();
    }

    @Test
    @DisplayName("칸의 타입과 타일 타입이 다르면 배치할 수 없다")
    void canPlace_typeMismatch() {
        assertThat(board.canPlace(13, ship, 1)).isFalse();
    }

    @Test
    @DisplayName("주사위 눈이 칸의 눈과 다르면 배치할 수 없다")
    void canPlace_dieMismatch() {
        assertThat(board.canPlace(13, knowledge, 2)).isFalse();
    }

    @Test
    @DisplayName("이미 점유된 칸에는 배치할 수 없다")
    void canPlace_occupied() {
        // 19는 시작 성으로 이미 점유됨
        assertThat(board.canPlace(19, BaseTile.of(TileType.CASTLE, 0), 6)).isFalse();
    }

    @Test
    @DisplayName("타입·주사위가 맞아도 이미 놓인 타일과 인접하지 않으면 배치할 수 없다")
    void canPlace_notAdjacent() {
        // cell 4: KNOWLEDGE / die 3, 시작 성(19)과 떨어져 있음
        assertThat(board.canPlace(4, knowledge, 3)).isFalse();
    }

    @Test
    @DisplayName("legalPlacements는 인접 조건까지 만족하는 칸만 돌려준다")
    void legalPlacements_onlyAdjacent() {
        // KNOWLEDGE die 1 칸은 13, 36 두 곳이지만 36은 성과 떨어져 있다 → 13만 합법
        assertThat(board.legalPlacements(knowledge, 1)).containsExactly(13);
    }
}
