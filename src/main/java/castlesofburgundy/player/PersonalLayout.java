package castlesofburgundy.player;

import castlesofburgundy.tile.TileType;

import java.util.*;

public final class PersonalLayout {
    private final Map<Integer, PersonalCell> cells = new LinkedHashMap<>();
    private final PersonalGrid grid = new PersonalGrid();

    public PersonalLayout() {
        add(new PersonalCell(1, TileType.ANIMAL, 6));
        add(new PersonalCell(2, TileType.CASTLE, 5));
        add(new PersonalCell(3, TileType.CASTLE, 4));
        add(new PersonalCell(4, TileType.KNOWLEDGE, 3));

        add(new PersonalCell(5, TileType.ANIMAL, 2));
        add(new PersonalCell(6, TileType.ANIMAL, 1));
        add(new PersonalCell(7, TileType.CASTLE, 6));
        add(new PersonalCell(8, TileType.KNOWLEDGE, 5));
        add(new PersonalCell(9, TileType.BUILDING, 4));

        add(new PersonalCell(10, TileType.ANIMAL, 5));
        add(new PersonalCell(11, TileType.ANIMAL, 4));
        add(new PersonalCell(12, TileType.BUILDING, 3));
        add(new PersonalCell(13, TileType.KNOWLEDGE, 1));
        add(new PersonalCell(14, TileType.BUILDING, 2));
        add(new PersonalCell(15, TileType.BUILDING, 3));

        add(new PersonalCell(16, TileType.SHIP, 6));
        add(new PersonalCell(17, TileType.SHIP, 1));
        add(new PersonalCell(18, TileType.SHIP, 2));
        add(new PersonalCell(19, TileType.CASTLE, 6));
        add(new PersonalCell(20, TileType.SHIP, 5));
        add(new PersonalCell(21, TileType.SHIP, 4));
        add(new PersonalCell(22, TileType.SHIP, 1));

        add(new PersonalCell(23, TileType.BUILDING, 2));
        add(new PersonalCell(24, TileType.BUILDING, 5));
        add(new PersonalCell(25, TileType.MINE, 4));
        add(new PersonalCell(26, TileType.BUILDING, 3));
        add(new PersonalCell(27, TileType.BUILDING, 1));
        add(new PersonalCell(28, TileType.ANIMAL, 2));

        add(new PersonalCell(29, TileType.BUILDING, 6));
        add(new PersonalCell(30, TileType.MINE, 1));
        add(new PersonalCell(31, TileType.KNOWLEDGE, 2));
        add(new PersonalCell(32, TileType.BUILDING, 5));
        add(new PersonalCell(33, TileType.BUILDING, 6));

        add(new PersonalCell(34, TileType.MINE, 3));
        add(new PersonalCell(35, TileType.KNOWLEDGE, 4));
        add(new PersonalCell(36, TileType.KNOWLEDGE, 1));
        add(new PersonalCell(37, TileType.BUILDING, 3));

        validateAgainstGrid();
    }

    /**
     * 이 레이아웃이 정의한 칸 집합이 PersonalGrid의 기하(이웃) 정의와 정확히 일치하는지 확인.
     * 한쪽만 수정해 두 정의가 어긋나는 것을 생성 시점에 막는다.
     */
    private void validateAgainstGrid() {
        Set<Integer> gridCells = grid.validCells();
        if (!cells.keySet().equals(gridCells)) {
            throw new IllegalStateException(
                    "PersonalLayout과 PersonalGrid의 칸 집합이 일치하지 않습니다. "
                            + "layout=" + cells.keySet() + ", grid=" + gridCells);
        }
    }

    private void add(PersonalCell c) {
        cells.put(c.id(), c);
    }

    public PersonalCell get(int id) {
        return cells.get(id);
    }

    public Collection<PersonalCell> all() {
        return cells.values();
    }

    /**
     * cellId에 인접한 칸들. (기하 객체를 밖으로 노출하지 않고 위임한다)
     */
    public List<Integer> getNeighbors(int cellId) {
        return grid.getNeighbors(cellId);
    }

    /**
     * 렌더링용 행 단위 그리드 모양(복사본).
     */
    public int[][] gridRows() {
        return grid.rows();
    }
}
