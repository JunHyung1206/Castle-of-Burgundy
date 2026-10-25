package castlesofburgundy.view;

import castlesofburgundy.core.Game;
import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.AnimalSpecies;
import castlesofburgundy.tile.AnimalTile;
import castlesofburgundy.tile.BaseTile;
import castlesofburgundy.tile.TileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TurnContextViewTest {

    private final Game game = Game.start(42L, List.of("P1", "P2"));
    private final Player player = game.currentPlayer();

    @Test
    @DisplayName("행동을 고르기 전에 타일 저장소, 보유 상품, 창고 상품을 함께 보여준다")
    void showsStorageAndGoods() {
        player.getBoard().getStorage().add(BaseTile.of(TileType.SHIP, 100));

        String rendered = TurnContextView.render(game, 1);

        assertThat(rendered).contains("[현재 상태]", "타일 저장소: #0=SHIP(100)", "보유 상품", "창고 상품");
    }

    @Test
    @DisplayName("현재 주사위와 일꾼으로 팔 수 있는 상품을 점수와 함께 보여준다")
    void showsSellableGoods() {
        for (GoodsColor color : GoodsColor.values()) {
            player.getBoard().getGoods().sell(color); // 시작 상품(무작위 3개)을 비워 3색 제한을 피한다
        }
        player.getBoard().getGoods().add(GoodsColor.TEAL);

        String rendered = TurnContextView.render(game, 6);

        assertThat(rendered).contains("판매 가능", "현재 주사위 6 -> TEAL");
    }

    @Test
    @DisplayName("저장소의 가축 타일을 놓을 수 있는 칸과 얻을 점수를 보여준다")
    void showsAnimalScoreCandidates() {
        AnimalTile sheep = new AnimalTile(100, AnimalSpecies.SHEEP, 3);
        player.getBoard().getStorage().add(sheep);
        // cell 28(ANIMAL/die2)은 성(19) → 20 → 21을 거쳐야 인접하므로 배 칸 둘을 먼저 채운다
        player.getBoard().place(20, BaseTile.of(TileType.SHIP, 1), 5);
        player.getBoard().place(21, BaseTile.of(TileType.SHIP, 2), 4);
        int cell = 28;

        String rendered = TurnContextView.render(game, 2);

        assertThat(rendered).contains("동물 점수 후보", "저장소[0] SHEEP x3", "cell " + cell + " 배치 시 +3점");
    }
}
