package castlesofburgundy.core;

import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.PersonalLayout;
import castlesofburgundy.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** 중앙 시장 구매: 주사위를 쓰지 않고, 턴당 한 번, 주사위 액션 전·사이·후 어느 때나. */
class TurnCentralMarketTest {

    private GameContext ctx;
    private RecordingGameUi ui;
    private TurnManager tm;
    private Player player;

    @BeforeEach
    void setUp() {
        ctx = TestGames.emptyContext(); // 번호 창고는 비워 두어 선택지를 단순하게
        ctx.board().refillCentralMarket(ctx.tileSupplier()::drawAny);
        ui = new RecordingGameUi();
        tm = new TurnManager(ctx, ui, 2);

        PersonalBoard board = new PersonalBoard(new PersonalLayout());
        board.setupInitialCastle(19);
        player = new Player(board, "P1", 0);
        player.addSilver(4);
    }

    @Test
    @DisplayName("주사위 액션 전에 구매해도 그 주사위로 다시 액션을 고른다")
    void purchaseDoesNotUseDie() {
        ui.scriptOptionsContaining("중앙 시장[0]", "일꾼 2개 받기", "일꾼 2개 받기");

        tm.playerTurn(player, Phase.A);

        assertThat(player.getSilver()).isEqualTo(2);
        assertThat(player.getBoard().getStorage().size()).isEqualTo(1);
        assertThat(player.getWorkers()).isEqualTo(4); // 주사위 두 개 모두 일꾼 받기에 썼다
        assertThat(ui.chooseCalls).isEqualTo(3);
    }

    @Test
    @DisplayName("한 턴에 두 번은 살 수 없다")
    void purchaseOncePerTurn() {
        ui.scriptOptionsContaining("중앙 시장[0]", "일꾼 2개 받기", "일꾼 2개 받기");

        tm.playerTurn(player, Phase.A);

        List<List<String>> afterPurchase = ui.menus.subList(1, ui.menus.size());
        assertThat(afterPurchase).allSatisfy(menu ->
                assertThat(menu).noneMatch(option -> option.contains("중앙 시장")));
        assertThat(ui.messaged("[턴 종료 전]")).isFalse();
    }

    @Test
    @DisplayName("주사위를 다 쓴 뒤에도 사지 않았다면 턴 종료 전에 한 번 살 수 있다")
    void purchaseAfterDice() {
        ui.scriptOptionsContaining("일꾼 2개 받기", "일꾼 2개 받기", "중앙 시장[0]");

        tm.playerTurn(player, Phase.A);

        assertThat(ui.messaged("[턴 종료 전]")).isTrue();
        assertThat(ui.menus.get(2).get(0)).isEqualTo("구매하지 않고 턴 종료");
        assertThat(player.getSilver()).isEqualTo(2);
        assertThat(player.getBoard().getStorage().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("턴 종료 전 구매를 건너뛸 수 있다")
    void skipPurchaseAfterDice() {
        ui.scriptOptionsContaining("일꾼 2개 받기", "일꾼 2개 받기", "구매하지 않고 턴 종료");

        tm.playerTurn(player, Phase.A);

        assertThat(player.getSilver()).isEqualTo(4);
        assertThat(player.getBoard().getStorage().isEmpty()).isTrue();
    }
}
