package castlesofburgundy.core;

import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.PersonalCell;
import castlesofburgundy.player.PersonalLayout;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.BaseTile;
import castlesofburgundy.tile.TileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegionScoreTest {

    private PersonalLayout layout;
    private PersonalBoard board;
    private Player player;

    @BeforeEach
    void setUp() {
        layout = new PersonalLayout();
        board = new PersonalBoard(layout);
        board.setupInitialCastle(19); // 시작 성
        player = new Player(board, 3, "P1");
    }

    @Test
    @DisplayName("KNOWLEDGE 구역(13-8-4)을 모두 채우면 구역 크기(3)만큼 점수가 증가한다")
    void completedKnowledgeRegionGivesScore() {

        // KNOWLEDGE 구역의 대표 칸
        int startCell = 13;

        Set<Integer> region = board.sameTypeRegion(startCell);
        assertThat(region)
                .containsExactlyInAnyOrder(13, 8, 4);

        int regionSize = region.size(); // = 3
        int beforeScore = player.getScore();


        // 순서대로 배치하기 위해 리스트로 변환
        List<Integer> order = new ArrayList<>(region);

        for (int cellId : order) {
            PersonalCell cell = layout.get(cellId);
            TileType type = cell.type();
            int die = cell.die();

            // 배치할 타일을 저장소에 넣는다
            player.addToStorage(BaseTile.of(type, cellId));

            // 저장소 index=0 의 타일을 해당 칸에 배치
            PlayerActions.placeTileFromStorage(player, 0, cellId, die);
        }

        int afterScore = player.getScore();

        assertThat(afterScore - beforeScore)
                .as("완성된 KNOWLEDGE 구역 크기(3)만큼 점수가 올라가야 한다")
                .isEqualTo(regionSize);
    }
}