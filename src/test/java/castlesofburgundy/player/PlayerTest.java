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
        player = new Player(personalBoard,3);
    }

    @Test
    @DisplayName("저장소에 타일을 추가한다.")
    void addToStorageTest(){
        Tile tile = BaseTile.of(TileType.KNOWLEDGE, 0);
        player.addToStorage(tile);

        assertThat(player.getStorage().size()).isEqualTo(1);
    }


    @Test
    @DisplayName("저장소가 가득차면 더는 추가할 수 없다.")
    void addToStorageFullTest(){
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
    void removeFromStorageTest(){
        Tile tile1 = BaseTile.of(TileType.KNOWLEDGE, 0);
        Tile tile2 = BaseTile.of(TileType.SHIP, 1);
        Tile tile3 = BaseTile.of(TileType.CASTLE, 2);

        player.addToStorage(tile1);
        player.addToStorage(tile2);
        player.addToStorage(tile3);

        System.out.println(PlayerConsoleView.render(player));


        player.placeTileFromStorage(1,20,5);
        assertThat(player.getStorage().size()).isEqualTo(2);
        System.out.println(PlayerConsoleView.render(player));

        player.placeTileFromStorage(0,13,1);
        assertThat(player.getStorage().size()).isEqualTo(1);
        System.out.println(PlayerConsoleView.render(player));
    }
}
