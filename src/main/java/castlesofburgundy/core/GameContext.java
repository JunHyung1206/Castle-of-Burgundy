package castlesofburgundy.core;

import castlesofburgundy.board.GameBoard;
import castlesofburgundy.goods.GoodsSupply;

record GameContext(
        long seed,
        GameBoard board,
        TileSupplier tileSupplier,
        Dice dice,
        GoodsSupply goodsSupply
) {
}
