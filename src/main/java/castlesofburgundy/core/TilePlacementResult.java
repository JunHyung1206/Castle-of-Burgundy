package castlesofburgundy.core;

public record TilePlacementResult(
        boolean grantExtraAction,  // 성 : true
        int bonusWorkers,          // 일꾼 추가
        int bonusSilver,           // 은화 추가
        int bonusScore             // 즉시 점수
) {
    public static final TilePlacementResult NONE =
            new TilePlacementResult(false, 0, 0, 0);

    public TilePlacementResult add(TilePlacementResult other) {
        return new TilePlacementResult(
                this.grantExtraAction || other.grantExtraAction,
                this.bonusWorkers + other.bonusWorkers,
                this.bonusSilver + other.bonusSilver,
                this.bonusScore + other.bonusScore
        );
    }
}
