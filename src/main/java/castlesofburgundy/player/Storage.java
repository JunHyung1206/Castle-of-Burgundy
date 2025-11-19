package castlesofburgundy.player;

import castlesofburgundy.tile.Tile;

import java.util.*;

/**
 * 좌하단 저장소: 기본 3칸. 가득 차면 먼저 하나 버려야 새로 보관 가능.
 */
public final class Storage {
    private final int capacity;
    private final List<Tile> slots = new ArrayList<>();

    public Storage(int capacity) {
        this.capacity = capacity;
    }

    public int size() {
        return slots.size();
    }

    public boolean isFull() {
        return size() >= capacity;
    }

    public boolean isEmpty(){
        return slots.isEmpty();
    }

    public void add(Tile tile) {
        if (isFull()) {
            throw new IllegalStateException("저장소 가득참: 먼저 버려야 함");
        }
        slots.add(tile);
    }

    public Tile removeAt(int index) {
        if ((index < 0) || (index >= slots.size())) {
            throw new IndexOutOfBoundsException("index=" + index);
        }
        return slots.remove(index);
    }

    public List<Tile> view() {
        return List.copyOf(slots);
    }
}
