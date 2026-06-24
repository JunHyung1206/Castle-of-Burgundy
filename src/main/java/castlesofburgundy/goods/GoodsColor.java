package castlesofburgundy.goods;

/**
 * 교역 상품 6색. 순서가 주사위 눈 1~6에 대응한다(판매 액션에서 사용).
 */
public enum GoodsColor {
    RED,
    PURPLE,
    PINK,
    ORANGE,
    BROWN,
    TEAL;

    public static GoodsColor forDie(int die) {
        if (die < 1 || die > 6) {
            throw new IllegalArgumentException("주사위 눈은 1~6: " + die);
        }
        return values()[die - 1];
    }
}
