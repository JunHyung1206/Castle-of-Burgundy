package castlesofburgundy.tile;

import java.util.Map;

public interface Tile {
    TileType type();
    Map<String, Object> props();
}