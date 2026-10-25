package castlesofburgundy.core;

import castlesofburgundy.tile.BuildingKind;
import castlesofburgundy.tile.TileType;

import java.util.List;
import java.util.Set;

/**
 * Game이 지금 기다리는 입력. 어느 순간이든 하나만 있고, 낼 수 있는 명령은 Game.availableActions()가 준다.
 * 모두 현재 플레이어(Game.currentPlayer())의 선택이다.
 */
public sealed interface Decision {

    /** 서로 다른 주사위가 두 개 남았다: 이번 행동에 쓸 주사위를 고른다. */
    record ChooseDie(List<Integer> remainingDice) implements Decision {
    }

    /** 고른 주사위로 할 행동을 고른다. 중앙 시장 구매(주사위 미사용)도 여기서 할 수 있다. */
    record ChooseDieAction(int die) implements Decision {
    }

    /** 성을 놓아 얻은 추가 행동: 원하는 눈 하나로 일꾼 없이 행동한다. */
    record ChooseCastleAction() implements Decision {
    }

    /** 배를 놓았다: 상품을 가져올 창고를 고른다. */
    record ChooseShipDepot() implements Decision {
    }

    /** 시장·목수·교회를 놓았다: 번호 창고에서 가져올 타일을 고른다. */
    record ChooseBuildingTile(BuildingKind kind, Set<TileType> allowedTypes) implements Decision {
    }

    /** 창고 건물을 놓았다: 팔 상품 색을 고른다. */
    record ChooseGoodsToSell() implements Decision {
    }

    /** 주사위를 다 썼고 이번 턴에 아직 사지 않았다: 턴을 끝내기 전에 중앙 시장에서 살지 고른다. */
    record OfferCentralMarket() implements Decision {
    }

    record GameOver() implements Decision {
    }
}
