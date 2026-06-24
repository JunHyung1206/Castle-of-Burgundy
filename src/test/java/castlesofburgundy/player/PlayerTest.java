package castlesofburgundy.player;

import castlesofburgundy.tile.BaseTile;
import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    Player player;

    @BeforeEach
    void setUp() {
        PersonalBoard personalBoard = new PersonalBoard(new PersonalLayout());
        personalBoard.setupInitialCastle(19);
        player = new Player(personalBoard, 3, "player1", 2);
    }

    @Test
    @DisplayName("저장소에 타일을 추가한다.")
    void addToStorageTest() {
        Tile tile = BaseTile.of(TileType.KNOWLEDGE, 0);
        player.addToStorage(tile);

        assertThat(player.getStorage().size()).isEqualTo(1);
    }


    @Test
    @DisplayName("저장소가 가득차면 더는 추가할 수 없다.")
    void addToStorageFullTest() {
        Tile tile1 = BaseTile.of(TileType.KNOWLEDGE, 0);
        Tile tile2 = BaseTile.of(TileType.SHIP, 1);
        Tile tile3 = BaseTile.of(TileType.CASTLE, 2);

        player.addToStorage(tile1);
        player.addToStorage(tile2);
        player.addToStorage(tile3);

        assertThat(player.getStorage().size()).isEqualTo(3);
        assertThat(player.getStorage().isFull()).isTrue();

        assertThrows(IllegalStateException.class, () -> player.addToStorage(tile3));
    }

    @Test
    @DisplayName("저장소에 있는 타일을 가져와서 배치한다.")
    void removeFromStorageTest() {
        Tile tile1 = BaseTile.of(TileType.KNOWLEDGE, 0);
        Tile tile2 = BaseTile.of(TileType.SHIP, 1);
        Tile tile3 = BaseTile.of(TileType.CASTLE, 2);

        player.addToStorage(tile1);
        player.addToStorage(tile2);
        player.addToStorage(tile3);

        // tile2(SHIP)를 cell 20(SHIP/die5)에 배치 — 놓인 타일을 반환받는다
        Tile placed = player.placeTileFromStorage(1, 20, 5);
        assertThat(placed).isEqualTo(tile2);
        assertThat(player.getStorage().size()).isEqualTo(2);
        assertThat(player.getBoard().isOccupied(20)).isTrue();

        // 남은 [tile1, tile3] 중 index0(tile1, KNOWLEDGE)을 cell 13(KNOWLEDGE/die1)에 배치
        player.placeTileFromStorage(0, 13, 1);
        assertThat(player.getStorage().size()).isEqualTo(1);
        assertThat(player.getBoard().isOccupied(13)).isTrue();
    }

    @Test
    @DisplayName("점수와 은화를 더하면 누적된다")
    void scoreAndSilverAccumulate() {
        assertThat(player.getScore()).isZero();
        assertThat(player.getSilver()).isZero();

        player.addScore(16);
        player.addScore(4);
        assertThat(player.getScore()).isEqualTo(20);

        player.addSilver(2);
        assertThat(player.getSilver()).isEqualTo(2);
    }

    @Test
    @DisplayName("저장소 상태 위임 메소드가 Storage와 일치한다")
    void storageDelegates() {
        assertThat(player.isStorageEmpty()).isTrue();
        assertThat(player.isStorageFull()).isFalse();
        assertThat(player.storedTiles()).isEmpty();

        player.addToStorage(BaseTile.of(TileType.ANIMAL, 1));
        player.addToStorage(BaseTile.of(TileType.SHIP, 2));
        player.addToStorage(BaseTile.of(TileType.MINE, 3));

        assertThat(player.isStorageEmpty()).isFalse();
        assertThat(player.isStorageFull()).isTrue();
        assertThat(player.storedTiles()).hasSize(3);
    }


    @Test
    @DisplayName("일꾼을 사용하면 그만큼 감소한다.")
    void spendWorkersSuccessTest() {
        // given
        assertThat(player.getWorkers()).isEqualTo(2);

        // when
        int remain = player.spendWorkers(1);

        // then
        assertThat(remain).isEqualTo(1);
        assertThat(player.getWorkers()).isEqualTo(1);

        // 한 번 더 사용
        remain = player.spendWorkers(1);
        assertThat(remain).isEqualTo(0);
        assertThat(player.getWorkers()).isEqualTo(0);
    }

    @Test
    @DisplayName("가지고 있는 일꾼보다 많이 쓰려고 하면 예외가 발생한다.")
    void spendWorkersTooManyTest() {
        // 현재 2개 있음
        assertThat(player.getWorkers()).isEqualTo(2);

        assertThrows(IllegalStateException.class, () -> player.spendWorkers(3));
        // 실패 후에도 일꾼 수는 그대로여야 함
        assertThat(player.getWorkers()).isEqualTo(2);
    }

    @Test
    @DisplayName("일꾼 사용 개수가 음수이면 예외가 발생한다.")
    void spendWorkersNegativeTest() {
        assertThrows(IllegalArgumentException.class, () -> player.spendWorkers(-1));
    }

    @Test
    @DisplayName("일꾼을 얻으면 증가한다.")
    void gainWorkersTest() {
        // 기본 2개
        assertThat(player.getWorkers()).isEqualTo(2);

        player.gainWorkers(2);
        assertThat(player.getWorkers()).isEqualTo(4);

        player.spendWorkers(1);
        assertThat(player.getWorkers()).isEqualTo(3);
    }

}
