package castlesofburgundy.board;

import castlesofburgundy.tile.TileType;

import java.util.List;
import java.util.Objects;

// 각 섹션의 타입만 저장합니다.
public class SectionLayout {
    private final int id;
    private final List<TileType> slotTypes;

    public SectionLayout(int id, List<TileType> slotTypes) {
        validate(id, slotTypes);
        this.id = id;
        this.slotTypes = slotTypes;
    }

    private void validate(int id, List<TileType> slotTypes) {
        if (id < 1 || id > 6) {
            throw new IllegalArgumentException("Section id must be 1..6");
        }
        Objects.requireNonNull(slotTypes, "slotTypes");
        if (slotTypes.size() != 4) {
            throw new IllegalArgumentException("Each section must have 4 tiles");
        }
    }

    public int getId() {
        return id;
    }

    public List<TileType> getSlotTypes() {
        return slotTypes;
    }
}
