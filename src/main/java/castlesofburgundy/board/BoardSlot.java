package castlesofburgundy.board;

/**
 * 보드 칸을 식별하는 개체, 해당 칸에 들어갈 수 있는 TileType 하나
 *
 * @param sectionId 1..6
 * @param index     0..3 (섹션 내 칸 순번)
 */
public record BoardSlot(int sectionId, int index) {
    public BoardSlot {
        if (sectionId < 1 || sectionId > 6) {
            throw new IllegalArgumentException("섹션 id는 1~6 사이입니다.");
        }
        if (index < 0 || index > 3) {
            throw new IllegalArgumentException("슬롯 index는 0~3 사이입니다.");
        }
    }

    @Override
    public String toString() {
        return "S" + sectionId + "#" + index;
    }
}
