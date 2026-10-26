package castlesofburgundy.view;

import castlesofburgundy.core.ActionOption;
import castlesofburgundy.core.Command;
import castlesofburgundy.core.GameState;
import castlesofburgundy.core.GameState.PlayerState;
import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.tile.AnimalTile;
import castlesofburgundy.tile.Tile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 주사위 행동을 고를 때의 상태 요약(저장소, 상품, 이번 주사위로 팔 수 있는 상품, 가축 점수 후보).
 * 비용과 점수는 Game.options()가 계산한 값을 그대로 보여 준다.
 */
final class TurnContextView {
    private TurnContextView() {
    }

    static String render(GameState state, List<ActionOption> options) {
        PlayerState player = state.current();
        return """
                [현재 상태]
                타일 저장소: %s
                보유 상품: %s
                판매 가능: %s
                동물 점수 후보: %s
                창고 상품: %s
                은화/점수: %d/%d
                """.formatted(
                storageSummary(player),
                goodsSummary(player),
                sellableGoodsSummary(player, options),
                animalScoreCandidatesSummary(player, options),
                depotGoodsSummary(state),
                player.silver(),
                player.score()
        );
    }

    private static String storageSummary(PlayerState player) {
        List<Tile> stored = player.storage();
        if (stored.isEmpty()) {
            return "(empty)";
        }

        List<String> parts = new ArrayList<>();
        for (int i = 0; i < stored.size(); i++) {
            parts.add("#" + i + "=" + tileSummary(stored.get(i)));
        }
        return String.join(", ", parts);
    }

    private static String tileSummary(Tile tile) {
        if (tile instanceof AnimalTile animal) {
            return tile.type() + ":" + animal.species() + " x" + animal.count() + "(" + tile.id() + ")";
        }
        return tile.type() + "(" + tile.id() + ")";
    }

    private static String goodsSummary(PlayerState player) {
        List<String> parts = new ArrayList<>();
        player.goods().forEach((color, count) -> parts.add(color + "=" + count));
        return parts.isEmpty() ? "(empty)" : String.join(", ", parts);
    }

    private static String sellableGoodsSummary(PlayerState player, List<ActionOption> options) {
        List<String> parts = new ArrayList<>();
        for (ActionOption option : options) {
            if (option.command() instanceof Command.SellGoods sell) {
                GoodsColor color = GoodsColor.forDie(sell.targetDie());
                parts.add(dieLabel(option) + " 주사위 " + sell.targetDie()
                        + " -> " + color + " " + player.goodsCount(color) + "개 판매 가능"
                        + " (+은화 " + option.silverGain() + ", +" + option.scoreGain() + "점)");
            }
        }
        return parts.isEmpty() ? "(없음)" : "\n- " + String.join("\n- ", parts);
    }

    /** 저장소 칸 순서대로, 같은 칸 안에서는 메뉴 순서(주사위 조정 순서)대로. */
    private static String animalScoreCandidatesSummary(PlayerState player, List<ActionOption> options) {
        List<ActionOption> animalPlacements = new ArrayList<>();
        for (ActionOption option : options) {
            if (option.command() instanceof Command.PlaceTile place
                    && player.storage().get(place.storageIndex()) instanceof AnimalTile) {
                animalPlacements.add(option);
            }
        }
        animalPlacements.sort(Comparator.comparingInt(option -> ((Command.PlaceTile) option.command()).storageIndex()));

        List<String> parts = new ArrayList<>();
        for (ActionOption option : animalPlacements) {
            Command.PlaceTile place = (Command.PlaceTile) option.command();
            AnimalTile animal = (AnimalTile) player.storage().get(place.storageIndex());
            parts.add(dieLabel(option) + " 주사위 " + place.targetDie()
                    + " -> 저장소[" + place.storageIndex() + "] "
                    + animal.species() + " x" + animal.count()
                    + " cell " + place.cellId() + " 배치 시 +" + option.scoreGain() + "점");
        }
        return parts.isEmpty() ? "(없음)" : "\n- " + String.join("\n- ", parts);
    }

    private static String dieLabel(ActionOption option) {
        return option.workerCost() == 0 ? "현재" : "일꾼 " + option.workerCost() + "개 사용";
    }

    private static String depotGoodsSummary(GameState state) {
        List<String> parts = new ArrayList<>();
        for (GameState.DepotState depot : state.board().depots()) {
            if (!depot.goods().isEmpty()) {
                parts.add(depot.number() + "=" + depot.goods());
            }
        }
        return parts.isEmpty() ? "(empty)" : String.join(", ", parts);
    }
}
