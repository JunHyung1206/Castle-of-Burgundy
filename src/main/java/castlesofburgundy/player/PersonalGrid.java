package castlesofburgundy.player;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


public class PersonalGrid {
    public static final int[][] GRID = new int[][]{
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


    private static final Map<Integer, int[]> INDEX_BY_CELL = new HashMap<>();

    static {
        for (int r = 0; r < GRID.length; r++) {
            for (int c = 0; c < GRID[r].length; c++) {
                putIfValid(r, c);
            }
        }
    }

    private static void putIfValid(int r, int c) {
        int cell = GRID[r][c];
        if (isValidCell(r, c)) {
            INDEX_BY_CELL.put(cell, new int[]{r, c});
        }
    }

    public static List<Integer> getNeighbors(int target) {
        int[] index = INDEX_BY_CELL.get(target);
        if (index == null) {
            return List.of();
        }

        int row = index[0];
        int col = index[1];

        int[][] deltas;
        if (row % 2 == 0) {
            deltas = EVEN_DELTAS;
        } else {
            deltas = ODD_DELTAS;
        }

        List<Integer> neighbors = new ArrayList<>(6);
        for (int[] d : deltas) {
            int nr = row + d[0];
            int nc = col + d[1];
            if (isInBounds(nr, nc) && isValidCell(nr, nc)) {
                neighbors.add(GRID[nr][nc]);
            }
        }
        return neighbors;
    }

    private static boolean isInBounds(int r, int c) {
        if (r < 0 || r >= GRID.length) {
            return false;
        }
        return c >= 0 && c < GRID[r].length;
    }

    private static boolean isValidCell(int r, int c) {
        return GRID[r][c] > 0; // 유효한 셀 번호만
    }
}
