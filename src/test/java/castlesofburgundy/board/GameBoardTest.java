package castlesofburgundy.board;

import castlesofburgundy.tile.BaseTile;
import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameBoardTest {

    GameBoard board = new GameBoard();
    private int nextTileId;

    /** 칸이 요구한 타입의 타일을 매번 새로 만든다(공급원 역할). */
    private Optional<Tile> draw(TileType type) {
        return Optional.of(BaseTile.of(type, nextTileId++));
    }

    private Optional<Tile> drawAny() {
        return draw(TileType.SHIP);
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
        board.refillDepots(4, this::draw);

        // 슬롯의 허용 타입대로 채워지는지 확인 (창고1 슬롯1=SHIP, 창고5 슬롯1=KNOWLEDGE)
        assertThat(board.depot(1).tileAt(1)).map(Tile::type).contains(TileType.SHIP);
        assertThat(board.depot(5).tileAt(1)).map(Tile::type).contains(TileType.KNOWLEDGE);
    }

    @Test
    @DisplayName("다시 채우면 남아 있던 타일은 치워지고 새 타일이 놓인다")
    void refillReplacesLeftovers() {
        board.refillDepots(4, this::draw);
        Tile before = board.depot(1).tileAt(0).orElseThrow();

        board.refillDepots(4, this::draw);

        assertThat(board.depot(1).tileAt(0).orElseThrow()).isNotSameAs(before);
    }

    @Test
    @DisplayName("중앙 시장은 페이즈 시작 시 검은 타일 8개로 채워진다")
    void centralMarketFillTest() {
        assertThat(board.centralMarket()).isEmpty();

        board.refillCentralMarket(this::drawAny);

        assertThat(board.centralMarket()).hasSize(8);
        assertThat(board.centralMarket()).allMatch(Objects::nonNull);
    }

    @Test
    @DisplayName("칸에서 타일을 꺼내면 그 칸은 비워진다")
    void takeTileTest() {
        board.refillDepots(4, this::draw);

        Optional<Tile> tile = board.depot(1).takeTile(1);

        assertThat(tile).isPresent();
        assertThat(board.depot(1).tileAt(1)).isEmpty();
        assertThat(board.depot(1).takeTile(1)).isEmpty();
    }

    @Test
    @DisplayName("인원수에 맞는 칸만 채운다: 2인 12칸, 3인 18칸, 4인 24칸")
    void refillFillsSlotsByPlayerCount() {
        board.refillDepots(2, this::draw);
        assertThat(filledSlots()).isEqualTo(12);
        assertThat(board.depot(1).tileAt(2)).isEmpty();

        board.refillDepots(3, this::draw);
        assertThat(filledSlots()).isEqualTo(18);
        assertThat(board.depot(1).tileAt(3)).isEmpty();

        board.refillDepots(4, this::draw);
        assertThat(filledSlots()).isEqualTo(24);
    }

    @Test
    @DisplayName("1인 게임은 2인 칸만 채우고, 4인을 넘으면 거부한다")
    void refillPlayerCountBounds() {
        board.refillDepots(1, this::draw);
        assertThat(filledSlots()).isEqualTo(12);

        assertThrows(IllegalArgumentException.class, () -> board.refillDepots(5, this::draw));
        assertThrows(IllegalArgumentException.class, () -> board.refillDepots(0, this::draw));
    }

    private long filledSlots() {
        return board.depots().stream()
                .flatMap(depot -> java.util.stream.IntStream.range(0, depot.slotCount()).mapToObj(depot::tileAt))
                .filter(Optional::isPresent)
                .count();
    }
}
