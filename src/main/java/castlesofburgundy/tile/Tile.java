package castlesofburgundy.tile;

import castlesofburgundy.core.TileEffectContext;
import castlesofburgundy.core.TilePlacementResult;

public interface Tile {
    int id();
    TileType type();
    /**
     * 이 타일이 보드에 놓였을 때 발동되는 효과.
     * 기본 구현은 아무 효과 없음.
     */
    default TilePlacementResult onPlaced(TileEffectContext ctx) {
        return TilePlacementResult.NONE;
    }
}