package castlesofburgundy.core;

import castlesofburgundy.tile.AnimalTile;
import castlesofburgundy.tile.BuildingTile;
import castlesofburgundy.tile.CastleTile;
import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TileSupplierTest {

    private final TileSupplier supplier = new TileSupplier(42L);

    @Test
    @DisplayName("요청한 타입의 타일이 항상 나온다 (마르지 않음)")
    void alwaysSuppliesRequestedType() {
        for (int i = 0; i < 100; i++) {
            assertThat(supplier.draw(TileType.SHIP))
                    .get()
                    .extracting(Tile::type)
                    .isEqualTo(TileType.SHIP);
        }
    }

    @Test
    @DisplayName("가축·건물·성은 각 타입의 전용 타일로 공급된다")
    void suppliesTypedTiles() {
        assertThat(supplier.draw(TileType.ANIMAL)).get().isInstanceOf(AnimalTile.class);
        assertThat(supplier.draw(TileType.BUILDING)).get().isInstanceOf(BuildingTile.class);
        assertThat(supplier.draw(TileType.CASTLE)).get().isInstanceOf(CastleTile.class);
    }
}
