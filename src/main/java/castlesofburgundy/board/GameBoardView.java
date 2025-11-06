package castlesofburgundy.board;

public class GameBoardView {
    private static final String GAME_BOARD_HEADER = "=== CASTLES OF BURGUNDY — CENTRAL BOARD ===";
    private static final String TILE_TYPE = "타입: B=건물, S=선박, K=지식, A=동물, M=광산, C=성";
    private static final String TILE_TYPE_DETAILS =
            """
                    동물: \uD83D\uDC14=닭, \uD83D\uDC11=양, \uD83D\uDC04=소, \uD83D\uDC16=돼지
                    건물: WR=창고, CW=목수, CH=교회, MK=시장, IN=하숙, BK=은행, TH=시청, TW=망루
                    지식(세부): K01~K26
                    """;

    private static final String BUILDING_DESCRIPTION =
            """
                    [건물 요약]
                    창고(WR) : 상품을 판매합니다.
                    목수(CW) : 건물 타일을 하나 가져옵니다.
                    교회(CH) : 광산, 지식, 성 타일 중 하나를 가져옵니다.
                    시장(MK) : 선박, 동물 타일 중 하나를 가져옵니다.
                    하숙(IN) : 일꾼 타일 4개를 받습니다.
                    은행(BK) : 2실버링을 받습니다.
                    시청(TH) : 자신의 창고에 있는 타일을 개인 소유의 땅 위에 놓을 수 있습니다.
                    망루(TW) : 4점을 얻습니다.
                    """;
    private static final String KNOWLEDGE_DESCRIPTION =
            """
                    [지식 타일 요약]
                    K01 동일도시 건물중복 허용   K02 페이즈종료 일꾼+1     K03 판매시 2코인       K04 판매시 일꾼+1
                    K05 선박 시 상품2곳 획득     K06 중앙외 6구역 구매     K07 동물 점수 +1        K08 일꾼 사용시 ±2
                    K09 건물 배치 굴림 ±1        K10 선박/동물 굴림 ±1     K11 성/광산/지식 ±1     K12 보드타일획득 ±1
                    K13 일꾼액션+코인1           K14 일꾼액션 4개 받기     K15 종료: 상품종류당+3  K16 종료: 창고당+4
                    K17 종료: 망루당+4           K18 종료: 목수당+4        K19 종료: 교회당+4      K20 종료: 시장당+4
                    K21 종료: 하숙당+4           K22 종료: 은행당+4        K23 종료: 시청당+4      K24 종료: 동물종류당+4
                    K25 종료: 판 상품타일당+1    K26 종료: 보너스타일당+2
                    """;


    public static void main(String[] args) {
        System.out.println(

                GAME_BOARD_HEADER +
                        TILE_TYPE +
                        TILE_TYPE_DETAILS +
                        "\n\n" +
                        "[Turn Order]  P1\n" +
                        "[Phase]  [□][■][□][□][□]\n" +
                        "[Round]  [■][□][□][□][□]\n" +
                        "\n" +
                        "[Sections]\n" +
                        "+------------------------------+  +------------------------------+  +------------------------------+\n" +
                        "|          Section 1           |  |          Section 2           |  |          Section 3           |\n" +
                        "|Types  | B S K A              |  |Types  | K C B B              |  |Types  | A B S K              |\n" +
                        "|Slots  | IN S K03 \uD83D\uDC11x3        |  |Slots  | K01 C BK WR          |  |Slots  | \uD83D\uDC14x2 TH S K12        |\n" +
                        "+------------------------------+  +------------------------------+  +------------------------------+\n" +
                        "+------------------------------+  +------------------------------+  +------------------------------+\n" +
                        "|          Section 4           |  |          Section 5           |  |          Section 6           |\n" +
                        "|Types  | S B A M              |  |Types  | M K B B              |  |Types  | B A C S              |\n" +
                        "|Slots  | S CW \uD83D\uDC04x4 M          |  |Slots  | M K07 TW CH          |  |Slots  | MK \uD83D\uDC16x2 C K06        |\n" +
                        "+------------------------------+  +------------------------------+  +------------------------------+\n" +
                        "\n" +
                        "[Market]\n" +
                        "+------------------------------+\n" +
                        "| K04  WR  □   □               |\n" +
                        "| □    \uD83D\uDC04x2  K10  IN           |\n" +
                        "+------------------------------+\n" +
                        "Index:   0   1   2   3 | 4   5   6   7\n" +
                        "\n" +
                        KNOWLEDGE_DESCRIPTION +
                        "\n" +
                        BUILDING_DESCRIPTION);


    }


}