package castlesofburgundy.core;

import castlesofburgundy.board.BoardState;
import castlesofburgundy.board.GameBoardLayout;
import castlesofburgundy.goods.GoodsSupply;

public final class GameContextFactory {

    public static GameContext startNewGame(long seed) {
        GameBoardLayout layout = new GameBoardLayout();

        TileSupplier supplier = new TileSupplier(seed);
        // 보드 채우기는 페이즈 시작(PhaseRunner)에서 하므로 여기서는 비워둔 채로 둔다.
        BoardState boardState = new BoardState(layout);

        return new GameContext(seed, layout, boardState, supplier, new Dice(seed), new GoodsSupply(seed));
    }
}
