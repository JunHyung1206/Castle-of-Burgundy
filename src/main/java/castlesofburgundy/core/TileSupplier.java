package castlesofburgundy.core;

import castlesofburgundy.tile.AnimalSpecies;
import castlesofburgundy.tile.AnimalTile;
import castlesofburgundy.tile.BaseTile;
import castlesofburgundy.tile.BuildingKind;
import castlesofburgundy.tile.BuildingTile;
import castlesofburgundy.tile.CastleTile;
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
                Tile tile;
                if (t == TileType.CASTLE) {
                    tile = new CastleTile(i);
                } else if (t == TileType.ANIMAL) {
                    // TODO: 실제 가축 구성으로 교체. 지금은 4종 순환 + 수 2~4 순환의 임시 구성.
                    tile = new AnimalTile(i, AnimalSpecies.values()[i % 4], 2 + (i % 3));
                } else if (t == TileType.BUILDING) {
                    // 8종 순환 (40장 → 종류별 5장)
                    tile = new BuildingTile(i, BuildingKind.values()[i % 8]);
                } else {
                    tile = BaseTile.of(t, i);
                }
                tmp.add(tile);
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
