package castlesofburgundy.goods;

import java.util.EnumMap;
import java.util.Map;

/**
 * 플레이어의 상품 저장소. 최대 3색까지 보관하며, 같은 색은 겹쳐 쌓는다.
 * 판매하면 해당 색 전체가 판매 더미로 넘어간다.
 */
public final class GoodsStorage {
    private static final int MAX_COLORS = 3;

    private final Map<GoodsColor, Integer> stored = new EnumMap<>(GoodsColor.class);
    private final Map<GoodsColor, Integer> sold = new EnumMap<>(GoodsColor.class);

    /**
     * 상품 한 개 저장 시도. 이미 보관 중인 색이거나 보관 색이 3색 미만이면 저장하고 true.
     * 3색 제한 때문에 새 색을 둘 수 없으면 저장하지 않고 false.
     */
    public boolean add(GoodsColor color) {
        if (!stored.containsKey(color) && stored.size() >= MAX_COLORS) {
            return false;
        }
        stored.merge(color, 1, Integer::sum);
        return true;
    }

    public int count(GoodsColor color) {
        return stored.getOrDefault(color, 0);
    }

    public int colorCount() {
        return stored.size();
    }

    public int totalStored() {
        return stored.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * 해당 색 전체를 판매(=판매 더미로 이동). 판매한 개수를 반환한다(0이면 판매 없음).
     */
    public int sell(GoodsColor color) {
        Integer n = stored.remove(color);
        if (n == null) {
            return 0;
        }
        sold.merge(color, n, Integer::sum);
        return n;
    }

    public int totalSold() {
        return sold.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int distinctSoldColors() {
        return sold.size();
    }
}
