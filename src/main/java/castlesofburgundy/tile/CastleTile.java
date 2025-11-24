package castlesofburgundy.tile;

import castlesofburgundy.core.TileEffectContext;
import castlesofburgundy.core.TilePlacementResult;

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
    public TilePlacementResult onPlaced(TileEffectContext ctx) {
        return new TilePlacementResult(true, 0, 0, 0);
    }
}