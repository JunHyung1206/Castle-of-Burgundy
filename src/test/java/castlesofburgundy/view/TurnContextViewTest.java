package castlesofburgundy.view;

import castlesofburgundy.core.ActionOption;
import castlesofburgundy.core.Command;
import castlesofburgundy.core.GameState;
import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.tile.AnimalSpecies;
import castlesofburgundy.tile.AnimalTile;
import castlesofburgundy.tile.BaseTile;
import castlesofburgundy.tile.TileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TurnContextViewTest {

    @Test
    @DisplayName("타일 저장소, 보유 상품, 창고 상품을 함께 보여준다")
    void showsStorageAndGoods() {
        GameState state = ViewFixtures.state(ViewFixtures.player(0, "P1",
                List.of(BaseTile.of(TileType.SHIP, 100)), Map.of(GoodsColor.RED, 2), Map.of()));

        String rendered = TurnContextView.render(state, List.of());

        assertThat(rendered).contains("[현재 상태]", "타일 저장소: #0=SHIP(100)", "보유 상품: RED=2", "창고 상품");
    }

    @Test
    @DisplayName("팔 수 있는 상품은 Game이 계산한 일꾼 비용과 점수로 보여준다")
    void showsSellableGoodsFromOptions() {
        GameState state = ViewFixtures.state(ViewFixtures.player(0, "P1", List.of(), Map.of(GoodsColor.TEAL, 1), Map.of()));
        List<ActionOption> options = List.of(new ActionOption(new Command.SellGoods(6), 1, 0, 3, 1));

        String rendered = TurnContextView.render(state, options);

        assertThat(rendered).contains("일꾼 1개 사용 주사위 6 -> TEAL 1개 판매 가능 (+은화 1, +3점)");
    }

    @Test
    @DisplayName("가축 배치 후보는 저장소 칸 순서대로 Game이 계산한 점수로 보여준다")
    void showsAnimalCandidatesFromOptions() {
        GameState state = ViewFixtures.state(ViewFixtures.player(0, "P1",
                List.of(BaseTile.of(TileType.SHIP, 1), new AnimalTile(100, AnimalSpecies.SHEEP, 3)), Map.of(), Map.of()));
        List<ActionOption> options = List.of(
                new ActionOption(new Command.PlaceTile(5, 0, 20), 0, 0, 0, 0),
                new ActionOption(new Command.PlaceTile(2, 1, 28), 0, 0, 8, 0));

        String rendered = TurnContextView.render(state, options);

        assertThat(rendered)
                .contains("현재 주사위 2 -> 저장소[1] SHEEP x3 cell 28 배치 시 +8점")
                .doesNotContain("저장소[0] SHIP");
    }
}
