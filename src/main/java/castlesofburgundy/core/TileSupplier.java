package castlesofburgundy.core;

import castlesofburgundy.tile.AnimalSpecies;
import castlesofburgundy.tile.AnimalTile;
import castlesofburgundy.tile.BaseTile;
import castlesofburgundy.tile.BuildingKind;
import castlesofburgundy.tile.BuildingTile;
import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;

import java.util.Optional;
import java.util.Random;

/**
 * 창고에 채울 타일을 공급한다.
 * 가축·건물·배·광산·성은 같은 타입끼리 서로 바꿔도 무방하므로 요청 시 랜덤으로 생성한다(무한, 안 마름).
 * <p>
 * TODO(수도원): KNOWLEDGE는 고유 효과 26종(대략 1장씩)이라, 구현 시 유한 고유 덱
 * (정해진 셋에서 중복 없이 뽑기)으로 바꿔야 한다. 그때는 draw가 Optional.empty를 줄 수 있다.
 */
public class TileSupplier {
    private final Random random;
    private int nextId = 0;

    public TileSupplier(long seed) {
        this.random = new Random(seed);
    }

    public Optional<Tile> draw(TileType type) {
        return Optional.of(create(type));
    }

    public Optional<Tile> drawAny() {
        TileType[] types = TileType.values();
        return draw(types[random.nextInt(types.length)]);
    }

    private Tile create(TileType type) {
        int id = nextId++;
        return switch (type) {
            case ANIMAL -> new AnimalTile(id, pick(AnimalSpecies.values()), 2 + random.nextInt(3)); // 2~4
            case BUILDING -> new BuildingTile(id, pick(BuildingKind.values()));
            case CASTLE, MINE, SHIP, KNOWLEDGE -> BaseTile.of(type, id);
        };
    }

    private <T> T pick(T[] values) {
        return values[random.nextInt(values.length)];
    }
}
