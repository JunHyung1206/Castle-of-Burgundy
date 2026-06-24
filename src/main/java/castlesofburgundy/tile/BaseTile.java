package castlesofburgundy.tile;

/**
 * 고유 효과가 아직 없는(또는 필요 없는) 일반 육각 타일. 타입과 id만 가진다.
 */
public record BaseTile(TileType type, int id) implements Tile {
    public BaseTile {
        if (type == null) {
            throw new IllegalArgumentException("type");
        }
    }

    public static BaseTile of(TileType type, int id) {
        return new BaseTile(type, id);
    }
}
