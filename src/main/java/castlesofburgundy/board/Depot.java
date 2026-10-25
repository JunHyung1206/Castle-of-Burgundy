package castlesofburgundy.board;

import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * 공용 보드의 번호 창고(1~6) 하나: 육각 타일 칸들과 상품 더미.
 * 육각 타일은 페이즈마다 새로 채우고, 상품은 페이즈가 바뀌어도 남는다.
 */
public final class Depot {
    private final int number;
    private final List<TileType> slotTypes;
    private final Tile[] slots;
    private final List<GoodsColor> goods = new ArrayList<>();

    Depot(int number, List<TileType> slotTypes) {
        this.number = number;
        this.slotTypes = List.copyOf(slotTypes);
        this.slots = new Tile[slotTypes.size()];
    }

    public int number() {
        return number;
    }

    public int slotCount() {
        return slots.length;
    }

    public TileType slotType(int index) {
        requireSlot(index);
        return slotTypes.get(index);
    }

    public Optional<Tile> tileAt(int index) {
        requireSlot(index);
        return Optional.ofNullable(slots[index]);
    }

    /** 칸의 타일을 꺼낸다(=비운다). 비어 있으면 empty. */
    public Optional<Tile> takeTile(int index) {
        Optional<Tile> tile = tileAt(index);
        slots[index] = null;
        return tile;
    }

    /** 남은 타일을 모두 치우고, 칸마다 허용 타입의 타일을 새로 놓는다. */
    void refill(Function<TileType, Optional<Tile>> draw) {
        for (int i = 0; i < slots.length; i++) {
            slots[i] = draw.apply(slotTypes.get(i)).orElse(null);
        }
    }

    /** 상품 한 개를 쌓는다. (흰 주사위가 사용) */
    public void addGoods(GoodsColor color) {
        goods.add(color);
    }

    /** 쌓인 상품 목록(읽기용 복사본). */
    public List<GoodsColor> goods() {
        return List.copyOf(goods);
    }

    /** 상품을 전부 가져온다(=비운다). (배 타일이 사용) */
    public List<GoodsColor> takeAllGoods() {
        List<GoodsColor> taken = List.copyOf(goods);
        goods.clear();
        return taken;
    }

    private void requireSlot(int index) {
        if (index < 0 || index >= slots.length) {
            throw new IllegalArgumentException("창고 " + number + "의 칸 index 범위 초과: " + index);
        }
    }

    @Override
    public String toString() {
        return "Depot" + number;
    }
}
