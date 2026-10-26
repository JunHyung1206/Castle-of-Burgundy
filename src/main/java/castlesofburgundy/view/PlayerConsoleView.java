package castlesofburgundy.view;

import castlesofburgundy.core.GameState.PlayerState;
import castlesofburgundy.player.PersonalCell;
import castlesofburgundy.player.PersonalLayout;
import castlesofburgundy.tile.AnimalSpecies;
import castlesofburgundy.tile.AnimalTile;
import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;

import java.util.List;

public final class PlayerConsoleView {
    private PlayerConsoleView() {
    }

    // ===== 설정 =====
    private static final boolean COLORIZE = true;   // 색상 끄려면 false
    private static final int CELL_W = 12;          // 각 셀 박스의 총 폭(괄호 포함)
    private static final int INNER_W = CELL_W - 2;  // 괄호/대괄호 제외 내부 폭
    private static final String EMPTY = " ".repeat(CELL_W);
    private static final String GAP = " ";        // 셀 간 간격

    public static String render(PlayerState player) {
        PersonalLayout layout = player.layout();
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== Personal Board ===\n");

        int[][] g = layout.gridRows();
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

                boolean placed = player.placedAt(id).isPresent();
                String inner = player.placedAt(id)
                        .map(tile -> cellInner(cell, tile))
                        .orElseGet(() -> cell.id() + ":" + AsciiUtils.shortType(cell.type()) + "/" + cell.die());
                sb.append(box(inner, cell.type(), placed)).append(GAP);
            }
            sb.append("\n");
        }

        sb.append("\n[ id:type/die ]  placed: ()   empty: []  ");
        if (COLORIZE) sb.append(AsciiUtils.FG_GRAY).append("(색상=타입 배경)").append(AsciiUtils.RESET);
        sb.append("\n\n");

        String s = storageView(player.storage());
        sb.append(s);

        sb.append("\n일꾼 수: ").append(player.workers())
                .append(", 은화 수: ").append(player.silver())
                .append(", 점수: ").append(player.score())
                .append("\n");

        return sb.toString();
    }

    private static String box(String inner, TileType type, boolean placed) {
        String content = COLORIZE ? AsciiUtils.colorizeInner(inner, type, placed) : inner;
        int visible = AsciiUtils.visibleLen(content);
        int pad = Math.max(0, INNER_W - visible);
        String padded = content + " ".repeat(pad);
        return (placed ? "(" : "[") + padded + (placed ? ")" : "]");
    }

    private static String storageView(List<Tile> tiles) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Storage ===\n");

        if (tiles.isEmpty()) {
            sb.append("(empty)\n");
            return sb.toString();
        }

        for (int i = 0; i < tiles.size(); i++) {
            Tile t = tiles.get(i);
            sb.append("#")
                    .append(i)
                    .append(" : ")
                    .append(tileSummary(t))
                    .append(" (id=")
                    .append(t.id())
                    .append(")\n");
        }
        return sb.toString();
    }

    private static String cellInner(PersonalCell cell, Tile tile) {
        if (tile instanceof AnimalTile animal) {
            return cell.id() + ":" + animalShortName(animal.species()) + animal.count() + "/" + cell.die();
        }
        return cell.id() + ":" + AsciiUtils.shortType(cell.type()) + "/" + cell.die();
    }

    private static String tileSummary(Tile tile) {
        if (tile instanceof AnimalTile animal) {
            return tile.type() + " " + animal.species() + " x" + animal.count();
        }
        return tile.type().toString();
    }

    private static String animalShortName(AnimalSpecies species) {
        return switch (species) {
            case SHEEP -> "Sh";
            case COW -> "Co";
            case PIG -> "Pi";
            case CHICKEN -> "Ch";
        };
    }

    public static String renderBoardOnly(PlayerState player) {
        PersonalLayout layout = player.layout();
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== Personal Board ===\n");

        int[][] g = layout.gridRows();
        for (int r = 0; r < g.length; r++) {
            if ((r & 1) == 0) sb.append(" ".repeat(6)); // 비슷한 indentation

            for (int c = 0; c < g[r].length; c++) {
                int id = g[r][c];
                if (id <= 0) {
                    sb.append("              ");
                    continue;
                }

                PersonalCell cell = layout.get(id);
                boolean placed = player.placedAt(id).isPresent();

                String inner = player.placedAt(id)
                        .map(tile -> cellInner(cell, tile))
                        .orElseGet(() -> id + ":" + AsciiUtils.shortType(cell.type()) + "/" + cell.die());
                sb.append(box(inner, cell.type(), placed)).append(" ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
