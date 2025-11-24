package castlesofburgundy.player;

import castlesofburgundy.tile.BaseTile;
import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;
import castlesofburgundy.view.PlayerConsoleView;
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

        System.out.println(PlayerConsoleView.render(player));


        player.placeTileFromStorage(1, 20, 5);
        assertThat(player.getStorage().size()).isEqualTo(2);
        System.out.println(PlayerConsoleView.render(player));

        player.placeTileFromStorage(0, 13, 1);
        assertThat(player.getStorage().size()).isEqualTo(1);
        System.out.println(PlayerConsoleView.render(player));
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
