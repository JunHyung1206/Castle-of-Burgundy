package castlesofburgundy.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * 한 공작 영지(개인 보드)의 기하: 어떤 칸이 존재하고, 칸끼리 어떻게 인접하는지.
 * 칸의 타입/주사위 같은 속성은 PersonalLayout이 담당한다.
 * (보드별로 기하가 다르므로 인스턴스로 둔다. 현재는 보드 1번만 제공)
 */
public final class PersonalGrid {
    private static final int[][] BOARD_ONE = new int[][]{
            {-1, 1, 2, 3, 4, -1, -1},
            {-1, 5, 6, 7, 8, 9, -1},
            {10, 11, 12, 13, 14, 15, -1},
            {16, 17, 18, 19, 20, 21, 22},
            {23, 24, 25, 26, 27, 28, -1},
            {-1, 29, 30, 31, 32, 33, -1},
            {-1, 34, 35, 36, 37, -1, -1}
    };

    private static final int[][] EVEN_DELTAS = {
            {-1, 0}, {-1, 1},
            {0, -1}, {0, 1},
            {1, 0}, {1, 1}
    };

    private static final int[][] ODD_DELTAS = {
            {-1, -1}, {-1, 0},
            {0, -1}, {0, 1},
            {1, -1}, {1, 0}
    };

    private final int[][] grid;
    private final Map<Integer, int[]> indexByCell = new HashMap<>();

    public PersonalGrid() {
        this(BOARD_ONE);
    }

    private PersonalGrid(int[][] grid) {
        this.grid = grid;
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                putIfValid(r, c);
            }
        }
    }

    private void putIfValid(int r, int c) {
        if (isValidCell(r, c)) {
            indexByCell.put(grid[r][c], new int[]{r, c});
        }
    }

    /**
     * 그리드에 정의된 유효한 칸(양수) id 전체. PersonalLayout과의 정합성 검증에 사용.
     */
    public Set<Integer> validCells() {
        return Set.copyOf(indexByCell.keySet());
    }

    /**
     * 행 단위 그리드 모양(렌더링용). 음수는 빈 자리를 뜻한다.
     * 내부 배열을 보호하기 위해 복사본을 돌려준다.
     */
    public int[][] rows() {
        int[][] copy = new int[grid.length][];
        for (int r = 0; r < grid.length; r++) {
            copy[r] = grid[r].clone();
        }
        return copy;
    }

    public List<Integer> getNeighbors(int target) {
        int[] index = indexByCell.get(target);
        if (index == null) {
            return List.of();
        }

        int row = index[0];
        int col = index[1];

        int[][] deltas = ODD_DELTAS;
        if (row % 2 == 0) {
            deltas = EVEN_DELTAS;
        }

        List<Integer> neighbors = new ArrayList<>(6);
        for (int[] d : deltas) {
            int nr = row + d[0];
            int nc = col + d[1];
            if (isInBounds(nr, nc) && isValidCell(nr, nc)) {
                neighbors.add(grid[nr][nc]);
            }
        }
        return neighbors;
    }

    private boolean isInBounds(int r, int c) {
        if (r < 0 || r >= grid.length) {
            return false;
        }
        return c >= 0 && c < grid[r].length;
    }

    private boolean isValidCell(int r, int c) {
        return grid[r][c] > 0; // 유효한 셀 번호만
    }
}
