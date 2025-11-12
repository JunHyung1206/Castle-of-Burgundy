package castlesofburgundy.core;

import castlesofburgundy.board.GameBoardLayout;
import castlesofburgundy.section.SectionBoard;
import castlesofburgundy.tile.TileSupply;

public record GameContext(
        long seed,
        GameBoardLayout layout,
        SectionBoard sectionBoard,
        TileSupply supply
) {}