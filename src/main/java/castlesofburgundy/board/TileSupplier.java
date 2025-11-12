package castlesofburgundy.board;

import castlesofburgundy.tile.BaseTile;
import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;

import java.util.*;

public class TileSupplier {
    private final EnumMap<TileType, Deque<Tile>> pool = new EnumMap<>(TileType.class);

    public TileSupplier(Map<TileType, Integer> counts, long seed) {
        Random random = new Random(seed);
        for (TileType t : TileType.values()) {
            int n = counts.getOrDefault(t, 0);
            List<Tile> tmp = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                tmp.add(BaseTile.of(t, i));
            }
            Collections.shuffle(tmp, random);
            pool.put(t, new ArrayDeque<>(tmp));
        }
    }

    public Optional<Tile> draw(TileType type) {
        Deque<Tile> q = pool.get(type);
        if (q == null || q.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(q.pop());
    }

    public int remaining(TileType type) {
        Deque<Tile> q = pool.get(type);
        if (q == null) {
            return 0;
        }
        return q.size();
    }
}
