package castlesofburgundy.tile;

import castlesofburgundy.core.TileEffectContext;
import castlesofburgundy.core.TilePlacementResult;

/**
 * 가축 타일. 그려진 가축 수(count)가 곧 기본 승점이다.
 * 추가로, 같은 농장(=같은 타입 연결 구역)에 이미 놓인 같은 종 가축 타일이 있으면
 * 그 타일들의 수만큼 점수를 더 얻는다.
 */
public final class AnimalTile implements Tile {
    private final int id;
    private final AnimalSpecies species;
    private final int count;

    public AnimalTile(int id, AnimalSpecies species, int count) {
        this.id = id;
        this.species = species;
        this.count = count;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public TileType type() {
        return TileType.ANIMAL;
    }

    public AnimalSpecies species() {
        return species;
    }

    public int count() {
        return count;
    }

    @Override
    public TilePlacementResult onPlaced(TileEffectContext ctx) {
        int score = count;
        for (Tile t : ctx.placedTilesInSameRegion()) {
            if (t != this && t instanceof AnimalTile other && other.species == species) {
                score += other.count;
            }
        }
        return new TilePlacementResult(false, 0, 0, score);
    }
}
