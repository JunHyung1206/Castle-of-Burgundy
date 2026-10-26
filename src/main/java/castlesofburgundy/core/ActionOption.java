package castlesofburgundy.core;

/**
 * 지금 낼 수 있는 명령 하나와, 그 명령을 내기 전에 알 수 있는 비용·이득.
 * 이득은 명령을 적용하는 즉시 생기는 것만 센다(판매, 구역 완성·가축·망루·은행 배치).
 * 배·건물처럼 이어서 고르는 효과의 결과는 포함하지 않는다.
 */
public record ActionOption(
        Command command,
        int workerCost,
        int silverCost,
        int scoreGain,
        int silverGain
) {
}
