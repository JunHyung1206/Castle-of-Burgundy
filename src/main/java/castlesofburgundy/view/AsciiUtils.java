package castlesofburgundy.view;

import castlesofburgundy.tile.TileType;
import java.util.regex.Pattern;

public final class AsciiUtils {
    private AsciiUtils() {}

    // ANSI
    public static final String RESET    = "\u001B[0m";
    public static final String BOLD     = "\u001B[1m";
    public static final String FG_GRAY  = "\u001B[90m";
    public static final String FG_WHITE = "\u001B[97m";

    // 256-color 배경 (원하는 색으로 바꿔도 됨)
    public static final String BG_CASTLE = "\u001B[48;5;23m";   // 남색
    public static final String BG_SHIP   = "\u001B[48;5;27m";   // 파랑
    public static final String BG_MINE   = "\u001B[48;5;240m";  // 회색
    public static final String BG_ANIMAL = "\u001B[48;5;70m";   // 연녹
    public static final String BG_BUILD  = "\u001B[48;5;136m";  // 황토
    public static final String BG_KNOW   = "\u001B[48;5;100m";  // 올리브

    private static final Pattern ANSI = Pattern.compile("\\u001B\\[[;?0-9]*[ -/]*[@-~]");

    public static String shortType(TileType t) {
        return switch (t) {
            case CASTLE -> "Ca";
            case SHIP -> "Sh";
            case MINE -> "Mi";
            case ANIMAL -> "An";
            case BUILDING -> "Bu";
            case KNOWLEDGE -> "Kn";
        };
    }

    public static String bgFor(TileType t) {
        return switch (t) {
            case CASTLE    -> BG_CASTLE;
            case SHIP      -> BG_SHIP;
            case MINE      -> BG_MINE;
            case ANIMAL    -> BG_ANIMAL;
            case BUILDING  -> BG_BUILD;
            case KNOWLEDGE -> BG_KNOW;
        };
    }

    /** ANSI 제거 후 가시 길이 */
    public static int visibleLen(String s) {
        return ANSI.matcher(s).replaceAll("").length();
    }

    /** 타입별 배경 + 가독성(필요시 흰 글자) + 놓인 칸은 굵게 */
    public static String colorizeInner(String inner, TileType type, boolean placed) {
        String bg = bgFor(type);
        String fg = FG_WHITE;
        String bold = placed ? BOLD : "";
        return bg + fg + bold + " " + inner + " " + RESET; // 좌우 1칸 여백
    }
}