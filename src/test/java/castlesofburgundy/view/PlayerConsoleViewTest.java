package castlesofburgundy.view;

import castlesofburgundy.core.GameState.PlayerState;
import castlesofburgundy.tile.AnimalSpecies;
import castlesofburgundy.tile.AnimalTile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerConsoleViewTest {

    @Test
    @DisplayName("저장소의 동물 타일은 종과 마리 수를 보여준다")
    void storageShowsAnimalSpeciesAndCount() {
        PlayerState player = ViewFixtures.player(0, "P1", List.of(new AnimalTile(100, AnimalSpecies.SHEEP, 3)), Map.of(), Map.of());

        String rendered = PlayerConsoleView.render(player);

        assertThat(rendered).contains("SHEEP x3");
    }

    @Test
    @DisplayName("개인판에 놓인 동물 타일은 칸 안에 종과 마리 수를 축약해서 보여준다")
    void boardShowsPlacedAnimalSpeciesAndCount() {
        PlayerState player = ViewFixtures.player(0, "P1", List.of(), Map.of(), Map.of(1, new AnimalTile(100, AnimalSpecies.SHEEP, 3)));

        String rendered = PlayerConsoleView.render(player);

        assertThat(rendered).contains("1:Sh3/6");
    }
}
