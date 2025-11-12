package castlesofburgundy.board;

import castlesofburgundy.tile.TileType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameBoardLayout {
    private final List<SectionLayout> sections;  // 각 섹션에 대한 타입
    private final int marketSlots;  // 시장의 타일 개수
    private final int phaseSlots;  // 페이즈 개수
    private final int roundSlots;  // 라운드 개수

    public GameBoardLayout() {
        sections = List.of(
                new SectionLayout(1, List.of(TileType.BUILDING, TileType.SHIP, TileType.KNOWLEDGE, TileType.ANIMAL)),
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

    public List<BoardSlot> asSlots() {
        List<BoardSlot> all = new ArrayList<>();
        for (SectionLayout sec : sections) {
            List<TileType> types = sec.getSlotTypes();
            for (int i = 0; i < types.size(); i++) {
                all.add(new BoardSlot(sec.getSectionId(), i, types.get(i)));
            }
        }
        return Collections.unmodifiableList(all);
    }


    public List<SectionLayout> getSections() {
        return sections;
    }

    public int getMarketSlots() {
        return marketSlots;
    }

    public int getPhaseSlots() {
        return phaseSlots;
    }

    public int getRoundSlots() {
        return roundSlots;
    }
}
