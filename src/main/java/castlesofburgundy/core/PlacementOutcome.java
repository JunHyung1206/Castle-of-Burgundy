package castlesofburgundy.core;

/**
 * 저장소 타일을 영지에 배치한 결과(이미 적용됨). 화면 서술/흐름 제어용 정보다.
 *
 * @param grantsExtraAction   성 효과처럼 추가 행동을 줘야 하면 true
 * @param completedRegionSize 이 배치로 완성된 구역의 크기(0이면 완성 없음)
 * @param regionScore         완성으로 얻은 점수(이미 player에 가산됨)
 */
public record PlacementOutcome(
        boolean grantsExtraAction,
        int completedRegionSize,
        int regionScore
) {
}
