package castlesofburgundy.board;

import castlesofburgundy.goods.GoodsColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BoardGoodsTest {

    private BoardState boardState;

    @BeforeEach
    void setUp() {
        boardState = new BoardState(new GameBoardLayout());
    }

    @Test
    @DisplayName("창고에 상품이 쌓이고, 순서대로 조회된다")
    void goodsAccumulate() {
        boardState.addGoods(3, GoodsColor.RED);
        boardState.addGoods(3, GoodsColor.PINK);

        assertThat(boardState.goodsAt(3)).containsExactly(GoodsColor.RED, GoodsColor.PINK);
        assertThat(boardState.goodsAt(1)).isEmpty();
    }

    @Test
    @DisplayName("창고 상품을 전부 가져오면 그 창고는 비워진다")
    void takeAllEmptiesWarehouse() {
        boardState.addGoods(2, GoodsColor.BROWN);
        boardState.addGoods(2, GoodsColor.BROWN);

        List<GoodsColor> taken = boardState.takeAllGoods(2);

        assertThat(taken).containsExactly(GoodsColor.BROWN, GoodsColor.BROWN);
        assertThat(boardState.goodsAt(2)).isEmpty();
    }

    @Test
    @DisplayName("창고 번호는 1~6만 허용한다")
    void warehouseRangeValidated() {
        assertThrows(IllegalArgumentException.class, () -> boardState.addGoods(0, GoodsColor.RED));
        assertThrows(IllegalArgumentException.class, () -> boardState.addGoods(7, GoodsColor.RED));
    }
}
