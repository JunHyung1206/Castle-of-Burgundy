package castlesofburgundy.core;

import castlesofburgundy.board.BoardState;
import castlesofburgundy.board.GameBoardLayout;
import castlesofburgundy.goods.GoodsSupply;

public record GameContext(
        long seed,
        GameBoardLayout layout,
        BoardState boardState,
        TileSupplier tileSupplier,
        Dice dice,
        GoodsSupply goodsSupply
) {
}
