package castlesofburgundy.tile;

import castlesofburgundy.core.TileEffectContext;
import castlesofburgundy.core.TilePlacementResult;

/**
 * 건물 타일. 종류(kind)에 따라 배치 즉시 한 번 효과가 적용된다.
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

    @Override
    public TilePlacementResult onPlaced(TileEffectContext ctx) {
        return switch (kind) {
            case WATCHTOWER -> new TilePlacementResult(false, 0, 0, 4); // +4점
            case BANK -> new TilePlacementResult(false, 0, 2, 0);       // +은화2
            case BOARDING_HOUSE -> new TilePlacementResult(false, 4, 0, 0); // +일꾼4
            // 시장/목수/교회/창고/시청은 상품·상호작용이 필요해 아직 미구현
            default -> TilePlacementResult.NONE;
        };
    }
}
