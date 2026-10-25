package castlesofburgundy.board;

import castlesofburgundy.goods.GoodsColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DepotTest {

    private final GameBoard board = new GameBoard();

    @Test
    @DisplayName("창고에 상품이 쌓이고, 순서대로 조회된다")
    void goodsAccumulate() {
        board.depot(3).addGoods(GoodsColor.RED);
        board.depot(3).addGoods(GoodsColor.PINK);

        assertThat(board.depot(3).goods()).containsExactly(GoodsColor.RED, GoodsColor.PINK);
        assertThat(board.depot(1).goods()).isEmpty();
    }

    @Test
    @DisplayName("창고 상품을 전부 가져오면 그 창고는 비워진다")
    void takeAllEmptiesDepot() {
        board.depot(2).addGoods(GoodsColor.BROWN);
        board.depot(2).addGoods(GoodsColor.BROWN);

        List<GoodsColor> taken = board.depot(2).takeAllGoods();

        assertThat(taken).containsExactly(GoodsColor.BROWN, GoodsColor.BROWN);
        assertThat(board.depot(2).goods()).isEmpty();
    }

    @Test
    @DisplayName("육각 타일을 다시 채워도 상품은 남는다")
    void refillKeepsGoods() {
        board.depot(4).addGoods(GoodsColor.TEAL);

        board.refillDepots(4, type -> Optional.empty());

        assertThat(board.depot(4).goods()).containsExactly(GoodsColor.TEAL);
    }

    @Test
    @DisplayName("칸 index가 창고 칸 수를 벗어나면 오류가 발생한다")
    void slotIndexValidated() {
        Depot depot = board.depot(1);
        assertThrows(IllegalArgumentException.class, () -> depot.tileAt(4));
        assertThrows(IllegalArgumentException.class, () -> depot.tileAt(-1));
    }
}
