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

class BoardStateTest {

    GameBoardLayout gameBoardLayout = new GameBoardLayout();
    BoardState boardState = new BoardState(gameBoardLayout);
    TileSupplier supplier;

    @BeforeEach
    void setUp() {
        supplier = new TileSupplier(42);
    }

    @Test
    @DisplayName("처음 시작할때는 모두 빈 상태로 시작한다.")
    void initTest() {
        assertThat(boardState.hasTile(new BoardSlot(1, 1))).isFalse();
    }

    @Test
    @DisplayName("Supplier를 통해 전부 채워지는 기능이 동작한다.")
    void allFillBySupplierTest() {
        assertThat(boardState.hasTile(new BoardSlot(1, 1))).isFalse();

        boardState.fillAllFromSupply(supplier);
        assertThat(boardState.hasTile(new BoardSlot(1, 1))).isTrue();

        // 슬롯의 허용 타입대로 채워지는지 확인 (섹션1 슬롯1=SHIP, 섹션5 슬롯1=KNOWLEDGE)
        assertThat(boardState.get(new BoardSlot(1, 1)).get().type()).isEqualTo(TileType.SHIP);
        assertThat(boardState.get(new BoardSlot(5, 1)).get().type()).isEqualTo(TileType.KNOWLEDGE);
    }

    @Test
    @DisplayName("중앙 시장은 페이즈 시작 시 검은 타일 8개로 채워진다")
    void centralMarketFillTest() {
        assertThat(boardState.centralMarket()).isEmpty();

        boardState.fillCentralMarketFromSupply(supplier);

        assertThat(boardState.centralMarket()).hasSize(8);
        assertThat(boardState.centralMarket()).allMatch(Objects::nonNull);
    }

    @Test
    @DisplayName("지우는 동작을 확인한다.")
    void removeTest() {
        boardState.fillAllFromSupply(supplier);

        BoardSlot removeSlot = new BoardSlot(1, 1);
        Optional<Tile> tile = boardState.removeAt(removeSlot);
        assertThat(tile).isPresent();
        assertThat(boardState.hasTile(removeSlot)).isFalse();
    }
}
