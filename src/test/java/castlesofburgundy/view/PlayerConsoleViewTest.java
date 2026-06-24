package castlesofburgundy.view;

import castlesofburgundy.core.Phase;
import castlesofburgundy.core.PlayerActions;
import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.PersonalLayout;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.AnimalSpecies;
import castlesofburgundy.tile.AnimalTile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerConsoleViewTest {

    @Test
    @DisplayName("저장소의 동물 타일은 종과 마리 수를 보여준다")
    void storageShowsAnimalSpeciesAndCount() {
        Player player = newPlayer();
        player.addToStorage(new AnimalTile(100, AnimalSpecies.SHEEP, 3));

        String rendered = PlayerConsoleView.render(player);

        assertThat(rendered).contains("SHEEP x3");
    }

    @Test
    @DisplayName("개인판에 놓인 동물 타일은 칸 안에 종과 마리 수를 축약해서 보여준다")
    void boardShowsPlacedAnimalSpeciesAndCount() {
        Player player = newPlayer();
        player.addToStorage(new AnimalTile(100, AnimalSpecies.SHEEP, 3));
        PlayerActions.placeTileFromStorage(player, 0, 1, 6, Phase.A);

        String rendered = PlayerConsoleView.render(player);

        assertThat(rendered).contains("1:Sh3/6");
    }

    private Player newPlayer() {
        PersonalBoard board = new PersonalBoard(new PersonalLayout());
        board.setupInitialCastle(2);
        return new Player(board, 3, "P1");
    }
}
