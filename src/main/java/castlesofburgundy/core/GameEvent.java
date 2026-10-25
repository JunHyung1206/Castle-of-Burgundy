package castlesofburgundy.core;

import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.BuildingKind;

/**
 * Game.apply/시작 중에 일어난 일. 문장으로 바꾸는 것은 화면(콘솔·웹)의 몫이다.
 */
public sealed interface GameEvent {

    record PhaseStarted(Phase phase) implements GameEvent {
    }

    /** 라운드 시작: 흰 주사위 눈의 창고에 상품 하나가 놓였다. */
    record RoundStarted(Phase phase, int round, int whiteDie, GoodsColor goods) implements GameEvent {
    }

    record TurnStarted(Player player, int die1, int die2) implements GameEvent {
    }

    /** 이번 턴의 actionNumber번째 행동에 die를 쓰기로 했다. */
    record DieActionStarted(int actionNumber, int die) implements GameEvent {
    }

    record GoodsSold(Player player, GoodsColor color, int count, int score, boolean byWarehouse) implements GameEvent {
    }

    record ShipGoodsTaken(Player player, int depot, int stored, int dropped) implements GameEvent {
    }

    record ShipFoundNoGoods(Player player) implements GameEvent {
    }

    record TurnOrderAdvanced(Player player) implements GameEvent {
    }

    /** 건물 효과로 고를 대상이 없어 건너뛰었다. */
    record BuildingEffectSkipped(BuildingKind kind) implements GameEvent {
    }

    /** 시장·목수·교회 효과로 번호 창고 타일을 가져왔다. */
    record BuildingTileTaken(BuildingKind kind) implements GameEvent {
    }

    record RegionCompleted(Player player, int size, int score, Phase phase) implements GameEvent {
    }

    /** 고른 행동이 후속 선택까지 모두 끝났다. */
    record ActionCompleted() implements GameEvent {
    }

    /** 성의 추가 행동을 얻었지만 할 수 있는 행동이 없다. */
    record CastleActionUnavailable(Player player) implements GameEvent {
    }

    record MinesPaid(Player player, int silver) implements GameEvent {
    }

    record PhaseEnded(Phase phase) implements GameEvent {
    }

    record GameEnded() implements GameEvent {
    }
}
