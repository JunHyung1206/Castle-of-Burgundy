package castlesofburgundy.player;

import castlesofburgundy.tile.Tile;

import java.util.*;

/**
 * 좌하단 저장소: 기본 3칸. 가득 차면 먼저 하나 버려야 새로 보관 가능.
 */
public final class Storage {
    private final int capacity;
    private final Deque<Tile> slots = new ArrayDeque<>();

    public Storage(int capacity) {
        this.capacity = capacity;
    }

    public int size() {
        return slots.size();
    }

    public boolean isFull() {
        return size() >= capacity;
    }

    public void add(Tile t) {
        if (isFull()) throw new IllegalStateException("저장소 가득참: 먼저 버려야 함");
        slots.addLast(t);
    }

    public Tile removeFirst() {
        Tile t = slots.pollFirst();
        if (t == null) throw new NoSuchElementException("비어있음");
        return t;
    }

    public List<Tile> view() {
        return List.copyOf(slots);
    }
}
