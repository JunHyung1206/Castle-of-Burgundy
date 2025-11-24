package castlesofburgundy.tile;

import castlesofburgundy.core.TileEffectContext;
import castlesofburgundy.core.TilePlacementResult;

import java.util.Map;

public final class CastleTile implements Tile {
    private final int id;

    public CastleTile(int id) {
        this.id = id;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public TileType type() {
        return TileType.CASTLE;
    }

    @Override
    public Map<String, Object> props() {
        return Map.of();
    }

    @Override
    public TilePlacementResult onPlaced(TileEffectContext ctx) {
        return new TilePlacementResult(true, 0, 0, 0);
    }
}