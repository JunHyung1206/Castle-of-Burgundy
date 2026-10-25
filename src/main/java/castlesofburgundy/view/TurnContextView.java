package castlesofburgundy.view;

import castlesofburgundy.board.Depot;
import castlesofburgundy.core.Game;
import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.AnimalTile;
import castlesofburgundy.tile.Tile;

import java.util.ArrayList;
import java.util.List;

/**
 * 주사위 행동을 고르기 전에 보여 주는 현재 상태 요약(저장소, 상품, 이번 주사위로 팔 수 있는 상품, 가축 점수 후보).
 */
public final class TurnContextView {
    private TurnContextView() {
    }

    public static String render(Game game, int baseDie) {
        Player player = game.currentPlayer();
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
                sellableGoodsSummary(game, player, baseDie),
                animalScoreCandidatesSummary(player, baseDie),
                depotGoodsSummary(game),
                player.getSilver(),
                player.getScore()
        );
    }

    private static String storageSummary(Player player) {
        List<Tile> stored = player.getBoard().getStorage().view();
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

    private static String goodsSummary(Player player) {
        List<String> parts = new ArrayList<>();
        for (GoodsColor color : GoodsColor.values()) {
            int count = player.getBoard().getGoods().count(color);
            if (count > 0) {
                parts.add(color + "=" + count);
            }
        }
        return parts.isEmpty() ? "(empty)" : String.join(", ", parts);
    }

    private static String sellableGoodsSummary(Game game, Player player, int baseDie) {
        List<String> parts = new ArrayList<>();
        for (int die : Game.reachableDice(baseDie, player.getWorkers())) {
            GoodsColor color = GoodsColor.forDie(die);
            int count = player.getBoard().getGoods().count(color);
            if (count > 0) {
                parts.add(dieLabel(baseDie, die) + " 주사위 " + die
                        + " -> " + color + " " + count + "개 판매 가능"
                        + " (+은화 1, +" + game.scoreForSelling(count) + "점)");
            }
        }
        return parts.isEmpty() ? "(없음)" : "\n- " + String.join("\n- ", parts);
    }

    private static String animalScoreCandidatesSummary(Player player, int baseDie) {
        List<String> parts = new ArrayList<>();
        List<Tile> stored = player.getBoard().getStorage().view();
        for (int storageIndex = 0; storageIndex < stored.size(); storageIndex++) {
            if (!(stored.get(storageIndex) instanceof AnimalTile animal)) {
                continue;
            }

            for (int die : Game.reachableDice(baseDie, player.getWorkers())) {
                for (int cellId : player.getBoard().legalPlacements(animal, die)) {
                    int score = animal.scoreIn(player.getBoard().placedTilesInRegion(cellId));
                    parts.add(dieLabel(baseDie, die) + " 주사위 " + die
                            + " -> 저장소[" + storageIndex + "] "
                            + animal.species() + " x" + animal.count()
                            + " cell " + cellId + " 배치 시 +" + score + "점");
                }
            }
        }
        return parts.isEmpty() ? "(없음)" : "\n- " + String.join("\n- ", parts);
    }

    private static String dieLabel(int baseDie, int die) {
        int cost = Game.workerCost(baseDie, die);
        return cost == 0 ? "현재" : "일꾼 " + cost + "개 사용";
    }

    private static String depotGoodsSummary(Game game) {
        List<String> parts = new ArrayList<>();
        for (Depot depot : game.board().depots()) {
            List<GoodsColor> goods = depot.goods();
            if (!goods.isEmpty()) {
                parts.add(depot.number() + "=" + goods);
            }
        }
        return parts.isEmpty() ? "(empty)" : String.join(", ", parts);
    }
}
