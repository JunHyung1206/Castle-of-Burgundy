package castlesofburgundy.goods;

import java.util.Random;

/**
 * 상품 공급. 색끼리 교체 가능하므로 요청 시 랜덤 색을 준다(무한, 안 마름).
 */
public final class GoodsSupply {
    private final Random random;

    public GoodsSupply(long seed) {
        this.random = new Random(seed);
    }

    public GoodsColor draw() {
        GoodsColor[] colors = GoodsColor.values();
        return colors[random.nextInt(colors.length)];
    }
}
