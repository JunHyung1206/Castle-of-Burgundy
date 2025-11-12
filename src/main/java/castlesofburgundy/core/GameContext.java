package castlesofburgundy.core;

import castlesofburgundy.board.BoardState;
import castlesofburgundy.board.GameBoardLayout;
import castlesofburgundy.board.TileSupplier;

public record GameContext(
        long seed,
        GameBoardLayout layout,
        BoardState boardState,
        TileSupplier tileSupplier
) {}