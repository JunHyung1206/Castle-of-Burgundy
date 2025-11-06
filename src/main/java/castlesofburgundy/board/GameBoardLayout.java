package castlesofburgundy.board;

import castlesofburgundy.tile.TileType;

import java.util.List;

public class GameBoardLayout {
    private final List<SectionLayout> sections;
    private final int marketSlots;
    private final int phaseSlots;
    private final int roundSlots;

    public GameBoardLayout(){
        sections = List.of(
                new SectionLayout(1, List.of(TileType.BUILDING, TileType.BUILDING, TileType.KNOWLEDGE, TileType.ANIMAL)),
                new SectionLayout(2, List.of(TileType.KNOWLEDGE, TileType.CASTLE, TileType.BUILDING, TileType.BUILDING)),
                new SectionLayout(3, List.of(TileType.ANIMAL, TileType.BUILDING, TileType.SHIP, TileType.KNOWLEDGE)),
                new SectionLayout(4, List.of(TileType.SHIP, TileType.BUILDING, TileType.ANIMAL, TileType.MINE)),
                new SectionLayout(5, List.of(TileType.MINE, TileType.KNOWLEDGE, TileType.BUILDING, TileType.BUILDING)),
                new SectionLayout(6, List.of(TileType.BUILDING, TileType.ANIMAL, TileType.CASTLE, TileType.SHIP))
        );
        marketSlots = 8;
        phaseSlots = 5;
        roundSlots = 5;
    }
}
