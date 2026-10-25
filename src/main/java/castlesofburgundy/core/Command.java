package castlesofburgundy.core;

import castlesofburgundy.goods.GoodsColor;

/**
 * 플레이어가 Game에 내는 명령. 화면·요청과 무관한 데이터이며, Game.availableActions()에 있는 것만 받아들인다.
 * targetDie는 일꾼으로 조정한 뒤의 눈이고, 필요한 일꾼 수는 Game이 계산한다(Game.workerCost).
 * discardIndex는 저장소가 가득 찼을 때 버릴 칸이며, 그렇지 않으면 null이다.
 */
public sealed interface Command {

    record UseDie(int die) implements Command {
    }

    /** 번호 창고(= targetDie)의 칸에서 타일을 가져온다. */
    record TakeTile(int targetDie, int slot, Integer discardIndex) implements Command {
    }

    record PlaceTile(int targetDie, int storageIndex, int cellId) implements Command {
    }

    /** targetDie에 해당하는 색 상품을 모두 판다. */
    record SellGoods(int targetDie) implements Command {
    }

    record TakeWorkers() implements Command {
    }

    /** 은화 2개로 중앙 시장 타일을 산다. 주사위를 쓰지 않고 턴당 한 번이다. */
    record BuyCentralMarket(int marketIndex, Integer discardIndex) implements Command {
    }

    /** 턴 끝의 중앙 시장 구매 제안을 거절하고 턴을 마친다. */
    record EndTurn() implements Command {
    }

    /** 배 효과: 상품을 가져올 창고. */
    record ChooseDepotGoods(int depot) implements Command {
    }

    /** 시장·목수·교회 효과: 가져올 번호 창고 타일. */
    record ChooseBoardTile(int depot, int slot, Integer discardIndex) implements Command {
    }

    /** 창고 건물 효과: 팔 상품 색. */
    record ChooseGoodsColor(GoodsColor color) implements Command {
    }
}
