package castlesofburgundy.player;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;


class PersonalGridTest {

    @Test
    void getNeighborsTest() {
        List<Integer> neighbors = PersonalGrid.getNeighbors(13);
        assertThat(neighbors).isEqualTo(List.of(7, 8, 12, 14, 19, 20));

        neighbors = PersonalGrid.getNeighbors(34);
        assertThat(neighbors).isEqualTo(List.of(29, 30, 35));

        neighbors = PersonalGrid.getNeighbors(22);
        assertThat(neighbors).isEqualTo(List.of(15, 21, 28));
    }

}