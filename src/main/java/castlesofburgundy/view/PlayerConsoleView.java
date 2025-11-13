package castlesofburgundy.view;

import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.PersonalGrid;
import castlesofburgundy.player.PersonalLayout;
import castlesofburgundy.player.PersonalCell;
import castlesofburgundy.tile.TileType;

public final class PlayerConsoleView {
    private PlayerConsoleView() {
    }

    // ===== 설정 =====
    private static final boolean COLORIZE = true;   // 색상 끄려면 false
    private static final int CELL_W = 12;          // 각 셀 박스의 총 폭(괄호 포함)
    private static final int INNER_W = CELL_W - 2;  // 괄호/대괄호 제외 내부 폭
    private static final String EMPTY = " ".repeat(CELL_W);
    private static final String GAP = " ";        // 셀 간 간격

    public static String render(PersonalLayout layout, PersonalBoard board) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== Personal Board ===\n");

        int[][] g = PersonalGrid.GRID;
        for (int r = 0; r < g.length; r++) {
            if ((r & 1) == 0) sb.append(" ".repeat(CELL_W / 2));

            for (int c = 0; c < g[r].length; c++) {
                int id = g[r][c];
                if (id <= 0) {
                    sb.append(EMPTY).append(GAP);
                    continue;
                }

                PersonalCell cell = layout.get(id);
                if (cell == null) {
                    sb.append(EMPTY).append(GAP);
                    continue;
                }

                String inner = id + ":" + AsciiUtils.shortType(cell.type()) + "/" + cell.die();
                boolean placed = board.isOccupied(id);
                sb.append(box(inner, cell.type(), placed)).append(GAP);
            }
            sb.append("\n");
        }

        sb.append("\n[ id:type/die ]  placed: ()   empty: []  ");
        if (COLORIZE) sb.append(AsciiUtils.FG_GRAY).append("(색상=타입 배경)").append(AsciiUtils.RESET);
        sb.append("\n");
        return sb.toString();
    }

    private static String box(String inner, TileType type, boolean placed) {
        String content = COLORIZE ? AsciiUtils.colorizeInner(inner, type, placed) : inner;
        int visible = AsciiUtils.visibleLen(content);
        int pad = Math.max(0, INNER_W - visible);
        String padded = content + " ".repeat(pad);
        return (placed ? "(" : "[") + padded + (placed ? ")" : "]");
    }
}