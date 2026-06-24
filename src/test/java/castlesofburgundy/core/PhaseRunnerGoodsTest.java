package castlesofburgundy.core;

import castlesofburgundy.board.BoardState;
import castlesofburgundy.board.GameBoardLayout;
import castlesofburgundy.goods.GoodsSupply;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhaseRunnerGoodsTest {

    @Test
    @DisplayName("흰 주사위 단계는 나온 눈의 창고에 상품 1개를 놓는다")
    void placeRoundGoodsAddsOneGoodsToWhiteDieWarehouse() {
        GameBoardLayout layout = new GameBoardLayout();
        BoardState boardState = new BoardState(layout);
        TileSupplier supplier = new TileSupplier(1L);
        GameContext ctx = new GameContext(1L, layout, boardState, supplier, new Dice(1L), new GoodsSupply(1L));
        RecordingGameUi ui = new RecordingGameUi();
        TurnManager tm = new TurnManager(ctx, ui);
        PhaseRunner runner = new PhaseRunner(ctx, new RoundManager(), tm, ui);

        int warehouse = runner.placeRoundGoods();

        assertThat(boardState.goodsAt(warehouse)).hasSize(1);
        assertThat(ui.messaged("흰 주사위 " + warehouse)).isTrue();
    }
}
