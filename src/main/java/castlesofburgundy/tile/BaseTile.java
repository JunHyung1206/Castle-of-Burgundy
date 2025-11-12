package castlesofburgundy.tile;

import java.util.Map;

public record BaseTile(TileType type, Map<String, Object> props) implements Tile {
    public BaseTile {
        if (type == null) {
            throw new IllegalArgumentException("type");
        }
        if (props == null) {
            props = Map.of();
        } else {
            props = Map.copyOf(props);
        }

    }

    public static BaseTile of(TileType type) {
        return new BaseTile(type, Map.of());
    }
}