package castlesofburgundy.core;

import castlesofburgundy.board.*;
import castlesofburgundy.player.Player;
import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.PersonalLayout;
import castlesofburgundy.tile.BaseTile;
import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerActionsTest {

    private GameContext simpleContext() {
        GameBoardLayout layout = new GameBoardLayout();
        Map<TileType, Integer> counts = Map.of(
                TileType.CASTLE, 10,
                TileType.SHIP, 10,
                TileType.MINE, 10,
                TileType.ANIMAL, 10,
                TileType.BUILDING, 10,
                TileType.KNOWLEDGE, 10
        );
        TileSupplier supplier = new TileSupplier(counts, 42L);
        BoardState boardState = new BoardState(layout);
        boardState.fillAllFromSupply(supplier);
        return new GameContext(1L, layout, boardState, supplier);
    }

    private Player newPlayer() {
        PersonalBoard pb = new PersonalBoard(new PersonalLayout());
        pb.setupInitialCastle(19);
        return new Player(pb, 3, "p");
    }

    @Test
    @DisplayName("공용 보드에서 타일을 하나 가져와서 플레이어 저장소에 넣는다")
    void takeTileFromMarket() {
        GameContext ctx = simpleContext();
        Player p = newPlayer();

        BoardSlot slot = new BoardSlot(1, 0);
        assertThat(ctx.boardState().hasTile(slot)).isTrue();
        int beforeSize = p.getStorage().size();
        Tile expectedTile = ctx.boardState().get(slot).get();

        PlayerActions.takeTileFromBoard(ctx, p, 1, 0);

        assertThat(p.getStorage().size()).isEqualTo(beforeSize + 1); // 개인 보드의 저장소에 한개 추가된다.
        assertThat(ctx.boardState().hasTile(slot)).isFalse();  // 가져온 후 공용보드에는 타일이 없다.

        assertThat(p.getStorage().view().get(0)).isEqualTo(expectedTile);
    }

    @Test
    @DisplayName("저장소가 가득 차 있으면 시장에서 더 가져올 수 없다 (예외)")
    void takeTileFromMarketWhenStorageFull() {
        GameContext ctx = simpleContext();
        Player p = newPlayer();

        // 저장소를 가득 채우기
        p.addToStorage(BaseTile.of(TileType.ANIMAL, 1));
        p.addToStorage(BaseTile.of(TileType.ANIMAL, 2));
        p.addToStorage(BaseTile.of(TileType.ANIMAL, 3));
        assertThat(p.getStorage().isFull()).isTrue();

        assertThrows(IllegalStateException.class,
                () -> PlayerActions.takeTileFromBoard(ctx, p, 1, 0));
    }
}
