package castlesofburgundy.board;

import castlesofburgundy.tile.TileType;

import java.util.*;


/**
 * 보드 칸을 식별하는 개체, 해당 칸에 들어갈 수 있는 TileType 하나
 */
public final class BoardSlot {
    private final int sectionId; // 1..6
    private final int index;     // 0..3 (섹션 내 칸 순번)

    public BoardSlot(int sectionId, int index) {
        if (sectionId < 1 || sectionId > 6) {
            throw new IllegalArgumentException("섹션 id는 1~6 사이입니다.");
        }
        if (index < 0 || index > 3) {
            throw new IllegalArgumentException("슬롯 index는 0~3 사이입니다.");
        }
        this.sectionId = sectionId;
        this.index = index;
    }

    public int getSectionId() {
        return sectionId;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardSlot boardSlot = (BoardSlot) o;
        return sectionId == boardSlot.sectionId && index == boardSlot.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sectionId, index);
    }

    @Override
    public String toString() {
        return "S" + sectionId + "#" + index ;
    }
}