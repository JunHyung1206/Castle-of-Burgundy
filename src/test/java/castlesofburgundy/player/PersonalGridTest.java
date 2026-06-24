package castlesofburgundy.player;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;


class PersonalGridTest {

    @Test
    @DisplayName("이웃하여 있는 칸이 잘 나오는지 확인한다.")
    void getNeighborsTest() {
        PersonalGrid grid = new PersonalGrid();

        List<Integer> neighbors = grid.getNeighbors(13);
        assertThat(neighbors).isEqualTo(List.of(7, 8, 12, 14, 19, 20));

        neighbors = grid.getNeighbors(34);
        assertThat(neighbors).isEqualTo(List.of(29, 30, 35));

        neighbors = grid.getNeighbors(22);
        assertThat(neighbors).isEqualTo(List.of(15, 21, 28));

        neighbors = grid.getNeighbors(19);
        assertThat(neighbors).isEqualTo(List.of(12, 13, 18, 20, 25, 26));

    }

}
