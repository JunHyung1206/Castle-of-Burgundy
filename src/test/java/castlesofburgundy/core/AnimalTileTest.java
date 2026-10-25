package castlesofburgundy.core;

import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.PersonalLayout;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.AnimalSpecies;
import castlesofburgundy.tile.AnimalTile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnimalTileTest {

    private PersonalBoard board;
    private Player player;
    private int nextTileId = 0;

    @BeforeEach
    void setUp() {
        board = new PersonalBoard(new PersonalLayout());
        // 성을 2번에 두면 가축 구역 {1,5,6,10,11}이 성과 인접 연결되어 실제 배치로 테스트 가능
        board.setupInitialCastle(2);
        player = new Player(board, "P1");
    }

    /** 가축 타일을 저장소에 넣고 배치한 뒤, 그 배치로 얻은 점수(직전 대비 증가분)를 돌려준다. */
    private int placeAnimal(AnimalSpecies species, int count, int cellId, int die) {
        int before = player.getScore();
        player.getBoard().getStorage().add(new AnimalTile(nextTileId++, species, count));
        PlayerActions.placeTileFromStorage(player, 0, cellId, die, Phase.A);
        return player.getScore() - before;
    }

    @Test
    @DisplayName("가축 타일은 그려진 수만큼 점수를 준다 (구역에 같은 종이 없으면)")
    void firstAnimalScoresItsCount() {
        assertThat(placeAnimal(AnimalSpecies.SHEEP, 3, 1, 6)).isEqualTo(3);
    }

    @Test
    @DisplayName("같은 농장에 같은 종을 더하면 기존 같은 종들의 수만큼 추가로 얻는다 (누적)")
    void sameSpeciesInSamePastureStacks() {
        assertThat(placeAnimal(AnimalSpecies.SHEEP, 3, 1, 6)).isEqualTo(3);
        // 양4 추가: 4 + (기존 양3) = 7
        assertThat(placeAnimal(AnimalSpecies.SHEEP, 4, 5, 2)).isEqualTo(4 + 3);
        // 양2 추가: 2 + (기존 양3, 양4) = 9
        assertThat(placeAnimal(AnimalSpecies.SHEEP, 2, 6, 1)).isEqualTo(2 + 3 + 4);
    }

    @Test
    @DisplayName("같은 농장이라도 종이 다르면 추가 점수를 주지 않는다")
    void differentSpeciesNoBonus() {
        assertThat(placeAnimal(AnimalSpecies.SHEEP, 3, 1, 6)).isEqualTo(3);
        // 소4 추가: 양과 종이 다르므로 보너스 없음 → 4
        assertThat(placeAnimal(AnimalSpecies.COW, 4, 5, 2)).isEqualTo(4);
    }
}
