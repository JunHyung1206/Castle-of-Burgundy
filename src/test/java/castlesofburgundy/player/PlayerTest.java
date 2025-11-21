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
        player = new Player(personalBoard, 3, "player1");
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
    @DisplayName("일꾼을 사용해 주사위 눈을 ±1, ±2 조정한다")
    void adjustDieWithWorkers() {
        PersonalBoard board = new PersonalBoard(new PersonalLayout());
        board.setupInitialCastle(19);
        Player p = new Player(board, 3, "p");

        p.gainWorkers(3);

        int r1 = p.spendWorkersToAdjust(3, 1, +1); // +1
        assertThat(r1).isEqualTo(4);

        int r2 = p.spendWorkersToAdjust(6, 2, +1); // +2, 6→2(랩)
        assertThat(r2).isEqualTo(2);
    }


    @Test
    @DisplayName("일꾼이 부족하다면 주사위 눈을 조정할 수 없다.")
    void failUseWorkers() {
        PersonalBoard board = new PersonalBoard(new PersonalLayout());
        board.setupInitialCastle(19);
        Player p = new Player(board, 3, "p");

        p.gainWorkers(2);

        assertThrows(IllegalStateException.class,
                () -> p.spendWorkersToAdjust(6, 3, +1));
    }

}
