package castlesofburgundy.view;

import castlesofburgundy.core.Command;
import castlesofburgundy.core.Decision;
import castlesofburgundy.core.GameState;
import castlesofburgundy.core.GameState.PlayerState;
import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.tile.BuildingKind;
import castlesofburgundy.tile.Tile;

/**
 * 명령을 콘솔 문장으로 바꾼다. 메뉴 보기와 "=> 실행: ..." 줄이 같은 문장을 쓴다.
 */
final class CommandText {
    private CommandText() {
    }

    /**
     * 명령 전체(버릴 저장소 칸 포함)를 한 문장으로.
     *
     * @param workerCost 이 명령에 드는 일꾼 수(Game.options()가 알려 준 값)
     */
    static String describe(GameState state, Decision decision, Command command, int workerCost) {
        PlayerState player = state.current();
        GameState.BoardState board = state.board();
        return switch (command) {
            case Command.UseDie u -> "주사위 " + u.die() + " 사용";
            case Command.TakeTile t -> dieActionPrefix(decision, t.targetDie(), workerCost)
                    + takeTileText(player, board.depot(t.targetDie()), t.slot(), t.discardIndex(), "");
            case Command.PlaceTile p -> dieActionPrefix(decision, p.targetDie(), workerCost)
                    + "저장소[" + p.storageIndex() + "]의 " + storedTile(player, p.storageIndex()).type()
                    + " 를 cell " + p.cellId() + " 에 배치";
            case Command.SellGoods s -> dieActionPrefix(decision, s.targetDie(), workerCost)
                    + sellText(player, GoodsColor.forDie(s.targetDie()));
            case Command.TakeWorkers t -> "일꾼 2개 받기";
            case Command.BuyCentralMarket b -> {
                String type = board.centralMarket().get(b.marketIndex()).type().toString();
                yield b.discardIndex() == null
                        ? "[주사위 미사용] 은화 2개로 중앙 시장[" + b.marketIndex() + "]의 " + type + " 구매"
                        : "[주사위 미사용] 저장소[" + b.discardIndex() + "] 버리고 은화 2개로 중앙 시장["
                        + b.marketIndex() + "]의 " + type + " 구매"
                        + " (버림: " + storedTile(player, b.discardIndex()).type() + ")";
            }
            case Command.EndTurn e -> "구매하지 않고 턴 종료";
            case Command.ChooseDepotGoods c ->
                    "창고 " + c.depot() + " 상품 가져오기 " + board.depot(c.depot()).goods();
            case Command.ChooseBoardTile c -> {
                BuildingKind kind = ((Decision.ChooseBuildingTile) decision).kind();
                yield takeTileText(player, board.depot(c.depot()), c.slot(), c.discardIndex(), buildingLabel(kind) + " ");
            }
            case Command.ChooseGoodsColor c -> sellText(player, c.color());
        };
    }

    /** 주사위 행동은 쓴 일꾼 수를, 성 추가 행동은 고른 눈을 앞에 붙인다. */
    private static String dieActionPrefix(Decision decision, int targetDie, int workerCost) {
        return switch (decision) {
            case Decision.ChooseDieAction d -> workerCost == 0 ? "" : "[일꾼 " + workerCost + "개 소모] ";
            case Decision.ChooseCastleAction c -> "[성 추가 행동][주사위 " + targetDie + "] ";
            default -> "";
        };
    }

    private static String takeTileText(PlayerState player, GameState.DepotState depot, int slot, Integer discardIndex, String label) {
        String type = depot.tileAt(slot).orElseThrow().type().toString();
        String take = "섹션 " + depot.number() + " 슬롯 " + slot + "에서 타일 " + type + " 가져오기";
        if (discardIndex == null) {
            return label + take;
        }
        return label + "저장소[" + discardIndex + "] 버리고 " + take
                + " (버림: " + storedTile(player, discardIndex).type() + ")";
    }

    private static String sellText(PlayerState player, GoodsColor color) {
        return color + " 상품 판매 (" + player.goodsCount(color) + "개)";
    }

    static Tile storedTile(PlayerState player, int storageIndex) {
        return player.storage().get(storageIndex);
    }

    static String buildingLabel(BuildingKind kind) {
        return switch (kind) {
            case MARKET -> "[시장]";
            case CARPENTER -> "[목수]";
            case CHURCH -> "[교회]";
            case WAREHOUSE -> "[창고]";
            default -> "[" + kind + "]";
        };
    }
}
