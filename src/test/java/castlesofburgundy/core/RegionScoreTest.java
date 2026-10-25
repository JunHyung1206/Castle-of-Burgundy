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
        player = new Player(board, "P1");
    }


    @Test
    @DisplayName("KNOWLEDGE 구역(13-8-4)을 페이즈 A에서 완성하면 16점을 얻는다")
    void completedKnowledgeRegionGivesCorrectScore() {

        int startCell = 13;

        // 1) 구역 전체 확인 (13, 8, 4)
        Set<Integer> region = board.sameTypeRegion(startCell);
        assertThat(region)
                .containsExactlyInAnyOrder(13, 8, 4);

        int beforeScore = player.getScore();

        // 2) 구역의 모든 셀에 Knowledge 타일 배치
        List<Integer> order = new ArrayList<>(region);
        for (int cellId : order) {
            PersonalCell cell = layout.get(cellId);
            TileType type = cell.type();
            int die = cell.die();

            // 배치할 타일을 저장소에 넣고
            player.getBoard().getStorage().add(BaseTile.of(type, cellId));

            // 페이즈 A 라고 가정, workerUsed는 0
            TestGames.actions(2).placeTileFromStorage(player, 0, 0, cellId, die, Phase.A);
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

        // cell 12는 단독 BUILDING 구역(시작 성 19와 인접)
        Set<Integer> region = board.sameTypeRegion(startCell);
        assertThat(region).containsExactlyInAnyOrder(12);

        int beforeScore = player.getScore();

        for (int cellId : region) {
            PersonalCell cell = layout.get(cellId);
            player.getBoard().getStorage().add(BaseTile.of(cell.type(), cellId));
            TestGames.actions(2).placeTileFromStorage(player, 0, 0, cellId, cell.die(), Phase.B);
        }

        // 페이즈 B(+8) / 구역크기 1(+1) → 총 9점
        assertThat(player.getScore() - beforeScore)
                .as("페이즈 B에서 크기 1 구역 완성 시 9점")
                .isEqualTo(8 + 1);
    }

    @Test
    @DisplayName("BUILDING 구역(26-27-32-33-37, 크기 5)을 페이즈 A에서 완성하면 25점을 얻는다")
    void completedSize5RegionGivesCorrectScore() {
        // 성(19)과 인접한 26부터 연결되도록 순서대로 채운다
        int[][] cellAndDie = {{26, 3}, {27, 1}, {32, 5}, {33, 6}, {37, 3}};

        int beforeScore = player.getScore();
        for (int[] cd : cellAndDie) {
            player.getBoard().getStorage().add(BaseTile.of(TileType.BUILDING, cd[0]));
            TestGames.actions(2).placeTileFromStorage(player, 0, 0, cd[0], cd[1], Phase.A);
        }

        // 페이즈 A(+10) / 구역크기 5(+15) → 총 25점
        assertThat(player.getScore() - beforeScore)
                .as("페이즈 A에서 크기 5 구역 완성 시 25점")
                .isEqualTo(10 + 15);
    }
}