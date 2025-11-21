package castlesofburgundy.player;

import castlesofburgundy.tile.BaseTile;
import castlesofburgundy.tile.TileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StorageTest {

    @Test
    @DisplayName("비어있는 저장소는 size=0, isEmpty=true, isFull=false 이다")
    void emptyStorage() {
        Storage s = new Storage(3);
        assertThat(s.size()).isEqualTo(0);
        assertThat(s.isEmpty()).isTrue();
        assertThat(s.isFull()).isFalse();
    }

    @Test
    @DisplayName("capacity까지는 추가 가능하고 그 이후에는 예외가 발생한다")
    void addAndFull() {
        Storage s = new Storage(2);

        s.add(BaseTile.of(TileType.ANIMAL, 1));
        s.add(BaseTile.of(TileType.BUILDING, 2));

        assertThat(s.size()).isEqualTo(2);
        assertThat(s.isFull()).isTrue();

        assertThrows(IllegalStateException.class,
                () -> s.add(BaseTile.of(TileType.SHIP, 3)));
    }

    @Test
    @DisplayName("removeAt은 해당 인덱스의 타일을 제거하고 반환한다")
    void removeAt() {
        Storage s = new Storage(3);
        s.add(BaseTile.of(TileType.ANIMAL, 1));
        s.add(BaseTile.of(TileType.BUILDING, 2));

        var removed = s.removeAt(0);
        assertThat(removed.id()).isEqualTo(1);
        assertThat(s.size()).isEqualTo(1);

        var left = s.view().get(0);
        assertThat(left.id()).isEqualTo(2);
    }

    @Test
    @DisplayName("removeAt 인덱스가 범위를 벗어나면 예외가 발생한다")
    void removeAtOutOfBounds() {
        Storage s = new Storage(3);
        s.add(BaseTile.of(TileType.ANIMAL, 1));
        s.add(BaseTile.of(TileType.BUILDING, 2));

        s.add(BaseTile.of(TileType.ANIMAL, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> s.removeAt(3));
        assertThrows(IndexOutOfBoundsException.class, () -> s.removeAt(-1));
    }
}
