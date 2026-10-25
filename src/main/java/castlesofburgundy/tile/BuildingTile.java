package castlesofburgundy.tile;

/**
 * 건물 타일. 종류(kind)에 따라 배치 즉시 한 번 효과가 적용된다(효과는 core.PlayerActions).
 * 한 도시(연결된 베이지 영역)에는 같은 종류 건물을 하나만 둘 수 있다(배치 제약은 PersonalBoard).
 */
public final class BuildingTile implements Tile {
    private final int id;
    private final BuildingKind kind;

    public BuildingTile(int id, BuildingKind kind) {
        this.id = id;
        this.kind = kind;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public TileType type() {
        return TileType.BUILDING;
    }

    public BuildingKind kind() {
        return kind;
    }
}
