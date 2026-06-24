package castlesofburgundy.core;

import castlesofburgundy.board.BoardState;
import castlesofburgundy.board.GameBoardLayout;
import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.PersonalLayout;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.BaseTile;
import castlesofburgundy.tile.TileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PhaseEndTest {

    private PhaseRunner phaseRunner;
    private RecordingGameUi ui;

    @BeforeEach
    void setUp() {
        GameBoardLayout layout = new GameBoardLayout();
        BoardState boardState = new BoardState(layout);
        TileSupplier supplier = new TileSupplier(Map.of(), 1L);
        GameContext ctx = new GameContext(1L, layout, boardState, supplier, new Dice(1L));

        ui = new RecordingGameUi();
        TurnManager tm = new TurnManager(ctx, ui);
        phaseRunner = new PhaseRunner(ctx, new RoundManager(), tm, ui);
    }

    @Test
    @DisplayName("단계 종료 시 광산 1개당 은화 1개를 받는다")
    void minesGiveSilverAtPhaseEnd() {
        PersonalBoard board = new PersonalBoard(new PersonalLayout());
        board.setupInitialCastle(19);
        // 광산 {25, 30}을 성(19)에서부터 연결해 배치
        board.place(25, BaseTile.of(TileType.MINE, 1), 4);
        board.place(30, BaseTile.of(TileType.MINE, 2), 1);
        Player player = new Player(board, 3, "P1");

        assertThat(player.getSilver()).isZero();

        phaseRunner.endPhase(new Player[]{player});

        assertThat(player.getSilver()).isEqualTo(2);
    }

    @Test
    @DisplayName("광산이 없으면 은화 정산도 없다")
    void noMinesNoSilver() {
        PersonalBoard board = new PersonalBoard(new PersonalLayout());
        board.setupInitialCastle(19);
        Player player = new Player(board, 3, "P1");

        phaseRunner.endPhase(new Player[]{player});

        assertThat(player.getSilver()).isZero();
    }
}
