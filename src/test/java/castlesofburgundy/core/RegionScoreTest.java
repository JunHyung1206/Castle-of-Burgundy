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
    @DisplayName("KNOWLEDGE 구역(13-8-4)을 페이즈 A에서 완성하면 16점을 얻는다")
    void completedKnowledgeRegionGivesCorrectScore() {

        int startCell = 13;

        // 1) 구역 전체 확인 (13, 8, 4)
        Set<Integer> region = board.sameTypeRegion(startCell);
        assertThat(region)
                .containsExactlyInAnyOrder(13, 8, 4);

        int regionSize = region.size(); // 3
        int beforeScore = player.getScore();

        // 2) 구역의 모든 셀에 Knowledge 타일 배치
        List<Integer> order = new ArrayList<>(region);
        for (int cellId : order) {
            PersonalCell cell = layout.get(cellId);
            TileType type = cell.type();
            int die = cell.die();

            // 배치할 타일을 저장소에 넣고
            player.addToStorage(BaseTile.of(type, cellId));

            // 페이즈 A 라고 가정, workerUsed는 0
            PlayerActions.placeTileFromStorage(player, 0, cellId, die, Phase.A);
        }

        int afterScore = player.getScore();

        // 페이즈 A: +10 / 구역크기 3: +6 → 총 16점
        int expected = 10 + 6;
        assertThat(afterScore - beforeScore)
                .as("페이즈 A에서 크기 3 구역 완성 시 16점")
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("BUILDING 구역(12)을 페이즈 B에서 완성하면 9점을 얻는다")
    void completedBuildingRegionGivesCorrectScore() {

        int startCell = 12;


        Set<Integer> region = board.sameTypeRegion(startCell);
        assertThat(region)
                .containsExactlyInAnyOrder(12);

        int regionSize = region.size(); // 3
        int beforeScore = player.getScore();

        // 2) 구역의 모든 셀에 Knowledge 타일 배치
        List<Integer> order = new ArrayList<>(region);
        for (int cellId : order) {
            PersonalCell cell = layout.get(cellId);
            TileType type = cell.type();
            int die = cell.die();

            // 배치할 타일을 저장소에 넣고
            player.addToStorage(BaseTile.of(type, cellId));

            // 페이즈 A 라고 가정, workerUsed는 0
            PlayerActions.placeTileFromStorage(player, 0, cellId, die, Phase.B);
        }

        int afterScore = player.getScore();

        // 페이즈 A: +10 / 구역크기 3: +6 → 총 16점
        int expected = 8 + 1;
        assertThat(afterScore - beforeScore)
                .as("페이즈 B에서 크기 1 구역 완성 시 9점")
                .isEqualTo(expected);
    }
}