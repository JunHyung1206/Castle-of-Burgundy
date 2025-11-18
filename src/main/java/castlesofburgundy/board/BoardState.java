package castlesofburgundy.board;

import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;

import java.util.*;

/**
 * 특정 GameBoardLayout에 대한 동적 상태. 내부는 Map<BoardSlot, Tile>로 “칸 → 타일”을 보관.
 **/
public class BoardState {
    private final GameBoardLayout layout;
    private final Map<BoardSlot, Tile> placed = new HashMap<>();

    public BoardState(GameBoardLayout layout) {
        this.layout = Objects.requireNonNull(layout);
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

    public boolean isExistTile(BoardSlot slot) {
        return placed.containsKey(slot);
    }

    public boolean isEmptyTile(BoardSlot slot) {
        return !isExistTile(slot);
    }

    public Map<BoardSlot, Tile> snapshot() {
        return Collections.unmodifiableMap(placed);
    }

    public GameBoardLayout getLayout() {
        return layout;
    }
}