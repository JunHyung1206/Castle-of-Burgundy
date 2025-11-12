package castlesofburgundy.tile;

import java.util.*;

public class TileSupply {
    private final EnumMap<TileType, Deque<Tile>> pool = new EnumMap<>(TileType.class);

    public TileSupply(Map<TileType, Integer> counts, long seed) {
        Random random = new Random(seed);
        for (TileType t : TileType.values()) {
            int n = counts.getOrDefault(t, 0);
            List<Tile> tmp = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                tmp.add(BaseTile.of(t));
            }
            Collections.shuffle(tmp, random);
            pool.put(t, new ArrayDeque<>(tmp));
        }
    }

    public Optional<Tile> draw(TileType type) {
        Deque<Tile> q = pool.get(type);
        return (q == null || q.isEmpty()) ? Optional.empty() : Optional.of(q.pop());
    }

    public int remaining(TileType type) {
        Deque<Tile> q = pool.get(type);
        return (q == null) ? 0 : q.size();
    }
}