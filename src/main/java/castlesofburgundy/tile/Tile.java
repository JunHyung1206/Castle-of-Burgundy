package castlesofburgundy.tile;

import java.util.Map;

public interface Tile {
    int id();
    TileType type();
    Map<String, Object> props();
}