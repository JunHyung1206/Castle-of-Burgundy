package castlesofburgundy.board;

import castlesofburgundy.tile.TileType;

import java.util.List;
import java.util.Objects;

// 각 섹션의 타입만 저장합니다.
public class SectionLayout {
    private final int sectionId;
    private final List<TileType> slotTypes;

    public SectionLayout(int sectionId, List<TileType> slotTypes) {
        validate(sectionId, slotTypes);
        this.sectionId = sectionId;
        this.slotTypes = List.copyOf(slotTypes);
    }

    private void validate(int sectionId, List<TileType> slotTypes) {
        if (sectionId < 1 || sectionId > 6) {
            throw new IllegalArgumentException("섹션 id는 1~6 사이 입니다.");
        }
        Objects.requireNonNull(slotTypes, "slotTypes");
        if (slotTypes.size() != 4) {
            throw new IllegalArgumentException("각 섹션은 4칸이어야 합니다.");
        }
    }

    public int getSectionId() {
        return sectionId;
    }

    public List<TileType> getSlotTypes() {
        return slotTypes;
    }
}
