package castlesofburgundy.board;

import castlesofburgundy.core.TileSupplier;
import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameBoardTest {

    GameBoard board = new GameBoard();
    TileSupplier supplier;

    @BeforeEach
    void setUp() {
        supplier = new TileSupplier(42);
    }

    @Test
    @DisplayName("처음 시작할때는 모두 빈 상태로 시작한다.")
    void initTest() {
        assertThat(board.depot(1).tileAt(1)).isEmpty();
    }

    @Test
    @DisplayName("창고 번호는 1~6이고, 각 창고는 4칸이다")
    void depotsAndSlots() {
        assertThat(board.depots()).extracting(Depot::number).containsExactly(1, 2, 3, 4, 5, 6);
        assertThat(board.depots()).allMatch(depot -> depot.slotCount() == 4);

        assertThrows(IllegalArgumentException.class, () -> board.depot(0));
        assertThrows(IllegalArgumentException.class, () -> board.depot(7));
    }

    @Test
    @DisplayName("Supplier를 통해 전부 채워지는 기능이 동작한다.")
    void refillDepotsTest() {
        board.refillDepots(supplier::draw);

        // 슬롯의 허용 타입대로 채워지는지 확인 (창고1 슬롯1=SHIP, 창고5 슬롯1=KNOWLEDGE)
        assertThat(board.depot(1).tileAt(1)).map(Tile::type).contains(TileType.SHIP);
        assertThat(board.depot(5).tileAt(1)).map(Tile::type).contains(TileType.KNOWLEDGE);
    }

    @Test
    @DisplayName("다시 채우면 남아 있던 타일은 치워지고 새 타일이 놓인다")
    void refillReplacesLeftovers() {
        board.refillDepots(supplier::draw);
        Tile before = board.depot(1).tileAt(0).orElseThrow();

        board.refillDepots(supplier::draw);

        assertThat(board.depot(1).tileAt(0).orElseThrow()).isNotSameAs(before);
    }

    @Test
    @DisplayName("중앙 시장은 페이즈 시작 시 검은 타일 8개로 채워진다")
    void centralMarketFillTest() {
        assertThat(board.centralMarket()).isEmpty();

        board.refillCentralMarket(supplier::drawAny);

        assertThat(board.centralMarket()).hasSize(8);
        assertThat(board.centralMarket()).allMatch(Objects::nonNull);
    }

    @Test
    @DisplayName("칸에서 타일을 꺼내면 그 칸은 비워진다")
    void takeTileTest() {
        board.refillDepots(supplier::draw);

        Optional<Tile> tile = board.depot(1).takeTile(1);

        assertThat(tile).isPresent();
        assertThat(board.depot(1).tileAt(1)).isEmpty();
        assertThat(board.depot(1).takeTile(1)).isEmpty();
    }
}
