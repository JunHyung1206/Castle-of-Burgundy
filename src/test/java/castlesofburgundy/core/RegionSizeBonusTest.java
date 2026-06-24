package castlesofburgundy.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegionSizeBonusTest {

    @ParameterizedTest(name = "크기 {0} → {1}점")
    @CsvSource({
            "1, 1",
            "2, 3",
            "3, 6",
            "4, 10",
            "5, 15",
            "6, 21",
            "7, 28",
            "8, 36"
    })
    @DisplayName("구역 크기별 점수 표가 정확하다")
    void regionSizeBonusTable(int size, int expectedScore) {
        assertThat(PlayerActions.regionSizeBonus(size)).isEqualTo(expectedScore);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 9})
    @DisplayName("지원하지 않는 크기는 예외를 던진다")
    void regionSizeBonusOutOfRange(int size) {
        assertThatThrownBy(() -> PlayerActions.regionSizeBonus(size))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
