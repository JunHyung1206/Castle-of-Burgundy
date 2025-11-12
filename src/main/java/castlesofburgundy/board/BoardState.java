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

    public boolean isEmpty(BoardSlot slot) {
        return !placed.containsKey(slot);
    }

    public boolean canPlace(BoardSlot slot, Tile tile) {
        // 1) 슬롯 존재 & 비어있음
        boolean exists = layout.asSlots().contains(slot);
        if (!exists || !isEmpty(slot)) {
            return false;
        }

        // 2) 타입 일치
        TileType allowed = slot.getAllowedType();
        if (tile == null || tile.type() != allowed) {
            return false;
        }
        return true;
    }

    public void place(BoardSlot slot, Tile tile) {
        if (!canPlace(slot, tile)) {
            throw new IllegalArgumentException("해당 슬롯에 배치할 수 없습니다: " + slot + " tile=" + tile);
        }
        placed.put(slot, tile);
    }

    public int size() {
        return placed.size();
    }
}