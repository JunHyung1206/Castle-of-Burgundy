package castlesofburgundy.core;

import castlesofburgundy.tile.CastleTile;
import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TileSupplierTest {

    @Test
    @DisplayName("count만큼 뽑을 수 있고, 다 떨어지면 Optional.empty를 준다")
    void drawUntilEmpty() {
        TileSupplier supplier = new TileSupplier(Map.of(TileType.KNOWLEDGE, 2), 42L);

        assertThat(supplier.remaining(TileType.KNOWLEDGE)).isEqualTo(2);

        assertThat(supplier.draw(TileType.KNOWLEDGE)).isPresent();
        assertThat(supplier.draw(TileType.KNOWLEDGE)).isPresent();
        assertThat(supplier.remaining(TileType.KNOWLEDGE)).isZero();

        assertThat(supplier.draw(TileType.KNOWLEDGE)).isEmpty();
    }

    @Test
    @DisplayName("공급 수량이 0인(또는 미지정) 타입은 바로 비어 있다")
    void unsuppliedTypeIsEmpty() {
        TileSupplier supplier = new TileSupplier(Map.of(TileType.KNOWLEDGE, 1), 42L);

        assertThat(supplier.remaining(TileType.MINE)).isZero();
        assertThat(supplier.draw(TileType.MINE)).isEmpty();
    }

    @Test
    @DisplayName("CASTLE 타입은 CastleTile로 공급된다")
    void castleTypeYieldsCastleTile() {
        TileSupplier supplier = new TileSupplier(Map.of(TileType.CASTLE, 1), 42L);

        Optional<Tile> drawn = supplier.draw(TileType.CASTLE);

        assertThat(drawn).isPresent();
        assertThat(drawn.get()).isInstanceOf(CastleTile.class);
        assertThat(drawn.get().type()).isEqualTo(TileType.CASTLE);
    }
}
