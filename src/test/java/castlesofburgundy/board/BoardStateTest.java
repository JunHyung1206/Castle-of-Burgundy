package castlesofburgundy.board;

import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;
import castlesofburgundy.view.GameBoardConsoleView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BoardStateTest {

    GameBoardLayout gameBoardLayout = new GameBoardLayout();
    BoardState boardState = new BoardState(gameBoardLayout);
    TileSupplier supplier;
    @BeforeEach
    void setUp() {
        Map<TileType, Integer> counts = Map.of(
                TileType.CASTLE,    8,
                TileType.SHIP,      100,
                TileType.MINE,      100,
                TileType.ANIMAL,    200,
                TileType.BUILDING,  280,
                TileType.KNOWLEDGE, 180
        );
        supplier = new TileSupplier(counts, 42);
    }
    @Test
    @DisplayName("처음 시작할때는 모두 빈 상태로 시작한다.")
    void initTest() {
        boolean empty = boardState.isEmpty(new BoardSlot(1, 1));
        assertThat(empty).isTrue();

        System.out.println(GameBoardConsoleView.render(gameBoardLayout, boardState));
    }

    @Test
    @DisplayName("Supplier를 통해 전부 채워지는 기능이 동작한다.")
    void allFillBySupplierTest(){
        boolean empty = boardState.isEmpty(new BoardSlot(1, 1));
        assertThat(empty).isTrue();

        boardState.fillAllFromSupply(supplier);
        empty = boardState.isEmpty(new BoardSlot(1, 1));
        assertThat(empty).isFalse();

        Tile tile = boardState.get(new BoardSlot(1, 1)).get();
        Tile tile2 = boardState.get(new BoardSlot(5, 1)).get();

        assertThat(tile).isInstanceOf(Tile.class);
        assertThat(tile2).isInstanceOf(Tile.class);

        assertThat(tile.type()).isEqualTo(TileType.SHIP);
        assertThat(tile2.type()).isEqualTo(TileType.KNOWLEDGE);

        System.out.println(GameBoardConsoleView.render(gameBoardLayout, boardState));
    }

    @Test
    @DisplayName("지우는 동작을 확인한다.")
    void removeTest(){
        boardState.fillAllFromSupply(supplier);

        BoardSlot removeSlot = new BoardSlot(1, 1);
        Optional<Tile> tile = boardState.removeAt(removeSlot);
        assertThat(tile.get()).isInstanceOf(Tile.class);
        assertThat(boardState.isEmpty(removeSlot)).isTrue();

        System.out.println(GameBoardConsoleView.render(gameBoardLayout, boardState));
    }
}
