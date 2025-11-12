package castlesofburgundy.section;

import castlesofburgundy.board.*;
import castlesofburgundy.tile.*;

import java.util.*;

public class SectionBoard {
    private final GameBoardLayout layout;
    private final Map<BoardSlot, Tile> grid = new HashMap<>();

    public SectionBoard(GameBoardLayout layout) {
        this.layout = Objects.requireNonNull(layout);
    }

    /**
     * 1) 전부 채우기: 보드를 깨끗이 비우고, 모든 슬롯을 타입에 맞게 공급원에서 채움
     */
    public void fillAllFromSupply(TileSupply supply) {
        Objects.requireNonNull(supply, "supply");
        grid.clear();
        for (BoardSlot slot : layout.asSlots()) {
            supply.draw(slot.getAllowedType()).ifPresent(tile -> grid.put(slot, tile));
        }
    }

    /**
     * (옵션) 비어있는 칸만 채우고 싶다면 이 메서드를 사용
     */
    public void fillEmptyFromSupply(TileSupply supply) {
        Objects.requireNonNull(supply, "supply");
        for (BoardSlot slot : layout.asSlots()) {
            if (!grid.containsKey(slot)) {
                supply.draw(slot.getAllowedType()).ifPresent(tile -> grid.put(slot, tile));
            }
        }
    }

    /**
     * 2) 하나 없애기
     */
    public Optional<Tile> removeAt(BoardSlot slot) {
        return Optional.ofNullable(grid.remove(slot));
    }

    /**
     * 전체 없애기
     */
    public List<Tile> removeAll() {
        List<Tile> removed = new ArrayList<>(grid.values());
        grid.clear();
        return removed;
    }


    public Optional<Tile> get(BoardSlot slot) {
        return Optional.ofNullable(grid.get(slot));
    }

    public boolean isFilled(BoardSlot slot) {
        return grid.containsKey(slot);
    }

    public Map<BoardSlot, Tile> snapshot() {
        return Collections.unmodifiableMap(grid);
    }

    public GameBoardLayout layout() {
        return layout;
    }
}