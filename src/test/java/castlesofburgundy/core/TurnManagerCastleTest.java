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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 성 추가 행동 흐름과 구역 완성 서술을, 콘솔 없이 RecordingGameUi로 검증한다.
 * (포트/어댑터 분리로 가능해진 테스트)
 */
class TurnManagerCastleTest {

    private RecordingGameUi ui;
    private GameContext ctx;
    private Player player;

    @BeforeEach
    void setUp() {
        GameBoardLayout layout = new GameBoardLayout();
        BoardState emptyBoard = new BoardState(layout); // 일부러 채우지 않음 → 보드에서 가져오기 옵션 배제
        TileSupplier supplier = new TileSupplier(1L);
        ctx = new GameContext(1L, layout, emptyBoard, supplier, new Dice(1L));

        ui = new RecordingGameUi();

        PersonalBoard board = new PersonalBoard(new PersonalLayout());
        board.setupInitialCastle(19);
        player = new Player(board, 3, "P1");
    }

    @Test
    @DisplayName("성 추가 행동 시 가능한 선택지가 없으면 그 사실을 알린다")
    void castleExtraAction_noOptions() {
        // 저장소 비어있고 보드도 비어있음 → 추가 행동 선택지 없음
        TurnManager tm = new TurnManager(ctx, ui);

        tm.runCastleExtraAction(player, Phase.A);

        assertThat(ui.messaged("가능한 선택지가 없습니다")).isTrue();
        assertThat(ui.chooseCalls).isZero();
    }

    @Test
    @DisplayName("성 추가 행동에서 저장소 타일을 골라 영지에 배치한다")
    void castleExtraAction_placesTile() {
        // cell 13(KNOWLEDGE/die1)은 시작 성과 인접 → die 1 루프에서 배치 옵션이 생긴다
        player.addToStorage(BaseTile.of(TileType.KNOWLEDGE, 100));
        ui.scriptChoices(0); // 첫 옵션 선택
        TurnManager tm = new TurnManager(ctx, ui);

        tm.runCastleExtraAction(player, Phase.A);

        assertThat(ui.messaged("추가로 한 번 더 행동할 수 있습니다")).isTrue();
        assertThat(ui.chooseCalls).isEqualTo(1);
        assertThat(player.getBoard().isOccupied(13)).isTrue();
        assertThat(player.isStorageEmpty()).isTrue();
    }

    @Test
    @DisplayName("배치로 구역을 완성하면 구역 완성 메시지가 나간다")
    void placementNarratesRegionCompletion() {
        // KNOWLEDGE 구역 {4,8,13}을 성(19)에서부터 연결해 채운다
        PersonalBoard board = player.getBoard();
        board.place(13, BaseTile.of(TileType.KNOWLEDGE, 1), 1);
        board.place(8, BaseTile.of(TileType.KNOWLEDGE, 2), 5);
        // 마지막 한 칸(4/die3)은 TurnManager 액션으로 채워 서술까지 확인
        player.addToStorage(BaseTile.of(TileType.KNOWLEDGE, 3));

        TurnManager tm = new TurnManager(ctx, ui);
        TurnManager.Action place4 = tm.buildOptionsForDie(player, 3, Phase.A).stream()
                .filter(a -> a.description().contains("cell 4 에 배치"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("cell 4 배치 옵션이 있어야 한다"));

        place4.apply().getAsBoolean();

        assertThat(ui.messaged("[구역 완성]")).isTrue();
        assertThat(board.isRegionCompleted(13)).isTrue();
    }
}
