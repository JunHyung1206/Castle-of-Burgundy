package castlesofburgundy.goods;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GoodsStorageTest {

    private GoodsStorage storage;

    @BeforeEach
    void setUp() {
        storage = new GoodsStorage();
    }

    @Test
    @DisplayName("같은 색은 겹쳐 쌓이고, 색 수는 늘지 않는다")
    void sameColorStacks() {
        assertThat(storage.add(GoodsColor.RED)).isTrue();
        assertThat(storage.add(GoodsColor.RED)).isTrue();

        assertThat(storage.count(GoodsColor.RED)).isEqualTo(2);
        assertThat(storage.colorCount()).isEqualTo(1);
        assertThat(storage.totalStored()).isEqualTo(2);
    }

    @Test
    @DisplayName("4번째 새 색은 저장할 수 없다(3색 제한). 기존 색 추가는 가능하다")
    void fourthColorRejected() {
        assertThat(storage.add(GoodsColor.RED)).isTrue();
        assertThat(storage.add(GoodsColor.PURPLE)).isTrue();
        assertThat(storage.add(GoodsColor.PINK)).isTrue();

        // 4번째 새 색 → 거부
        assertThat(storage.add(GoodsColor.ORANGE)).isFalse();
        assertThat(storage.colorCount()).isEqualTo(3);

        // 이미 가진 색은 계속 쌓을 수 있다
        assertThat(storage.add(GoodsColor.RED)).isTrue();
        assertThat(storage.count(GoodsColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("판매하면 해당 색 전체가 판매 더미로 넘어간다")
    void sellMovesAllOfColorToSold() {
        storage.add(GoodsColor.RED);
        storage.add(GoodsColor.RED);
        storage.add(GoodsColor.PINK);

        int soldCount = storage.sell(GoodsColor.RED);

        assertThat(soldCount).isEqualTo(2);
        assertThat(storage.count(GoodsColor.RED)).isZero();
        assertThat(storage.colorCount()).isEqualTo(1); // PINK만 남음
        assertThat(storage.totalSold()).isEqualTo(2);
        assertThat(storage.distinctSoldColors()).isEqualTo(1);
    }

    @Test
    @DisplayName("없는 색을 판매하면 0이고 아무 변화도 없다")
    void sellEmptyColorNoop() {
        assertThat(storage.sell(GoodsColor.BROWN)).isZero();
        assertThat(storage.totalSold()).isZero();
    }
}
