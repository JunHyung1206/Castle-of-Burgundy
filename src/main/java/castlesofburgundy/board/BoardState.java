package castlesofburgundy.board;

import castlesofburgundy.core.TileSupplier;
import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;

import java.util.*;

/**
 * 특정 GameBoardLayout에 대한 동적 상태. 내부는 Map<BoardSlot, Tile>로 “칸 → 타일”을 보관.
 * 추가로 각 창고(1~6)에 쌓인 상품 타일을 보관한다(육각 타일과 달리 페이즈가 바뀌어도 유지).
 **/
public class BoardState {
    private final GameBoardLayout layout;
    private final Map<BoardSlot, Tile> placed = new HashMap<>();
    private final Map<Integer, List<GoodsColor>> warehouseGoods = new HashMap<>();

    public BoardState(GameBoardLayout layout) {
        this.layout = Objects.requireNonNull(layout);
    }

    /** 창고(1~6)에 상품 한 개를 쌓는다. (흰 주사위가 사용) */
    public void addGoods(int warehouse, GoodsColor color) {
        requireWarehouse(warehouse);
        warehouseGoods.computeIfAbsent(warehouse, k -> new ArrayList<>()).add(color);
    }

    /** 창고에 쌓인 상품 목록(읽기용 복사본). */
    public List<GoodsColor> goodsAt(int warehouse) {
        requireWarehouse(warehouse);
        return List.copyOf(warehouseGoods.getOrDefault(warehouse, List.of()));
    }

    /** 창고의 상품을 전부 가져온다(=비운다). (배 타일이 사용) */
    public List<GoodsColor> takeAllGoods(int warehouse) {
        requireWarehouse(warehouse);
        List<GoodsColor> taken = warehouseGoods.remove(warehouse);
        return taken == null ? List.of() : List.copyOf(taken);
    }

    private static void requireWarehouse(int warehouse) {
        if (warehouse < 1 || warehouse > 6) {
            throw new IllegalArgumentException("창고 번호는 1~6: " + warehouse);
        }
    }

    public int size() {
        return placed.size();
    }

    public void fillAllFromSupply(TileSupplier supply) {
        Objects.requireNonNull(supply, "supply");
        removeAll();
        for (BoardSlot slot : layout.asSlots()) {
            TileType type = layout.getAllowedType(slot);
            Optional<Tile> drawn = supply.draw(type);
            drawn.ifPresent(tile -> placed.put(slot, tile));
        }
    }

    public void removeAll() {
        placed.clear();
    }

    public Optional<Tile> removeAt(BoardSlot slot) {
        return Optional.ofNullable(placed.remove(slot));
    }

    public Optional<Tile> get(BoardSlot slot) {
        return Optional.ofNullable(placed.get(slot));
    }

    public boolean hasTile(BoardSlot slot) {
        return placed.containsKey(slot);
    }

    public Map<BoardSlot, Tile> snapshot() {
        return Collections.unmodifiableMap(placed);
    }

    public GameBoardLayout getLayout() {
        return layout;
    }
}
