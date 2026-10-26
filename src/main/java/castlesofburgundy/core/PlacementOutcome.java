package castlesofburgundy.core;

import castlesofburgundy.tile.BuildingKind;
import castlesofburgundy.tile.TileType;

import java.util.Set;

/**
 * 저장소 타일을 영지에 배치한 결과(점수·즉시 효과는 이미 적용됨).
 *
 * @param grantsExtraAction   성 효과처럼 추가 행동을 줘야 하면 true
 * @param completedRegionSize 이 배치로 완성된 구역의 크기(0이면 완성 없음)
 * @param regionScore         완성으로 얻은 점수(이미 player에 가산됨)
 * @param followUp            배치 직후 플레이어 선택이 필요한 효과
 */
record PlacementOutcome(
        boolean grantsExtraAction,
        int completedRegionSize,
        int regionScore,
        FollowUp followUp
) {
    /** 배치 직후 플레이어 선택이 필요한 효과. 선택은 Game이 받아 PlayerActions로 실행한다. */
    public sealed interface FollowUp {
        record None() implements FollowUp {
        }

        /** 배: 상품이 있는 창고 하나를 골라 상품을 가져오고 턴 순서가 앞으로 간다. */
        record ShipGoods() implements FollowUp {
        }

        /** 시장·목수·교회: 번호 창고에서 허용 타입 타일 하나를 가져온다. */
        record BoardTile(BuildingKind kind, Set<TileType> allowedTypes) implements FollowUp {
        }

        /** 창고 건물: 상품 한 색을 판다. */
        record WarehouseSale() implements FollowUp {
        }
    }
}
