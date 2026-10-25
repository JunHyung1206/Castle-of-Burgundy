package castlesofburgundy.tile;

public enum BuildingKind {
    WATCHTOWER,     // 망루: 즉시 +4점
    BANK,           // 은행: 즉시 은화 +2
    BOARDING_HOUSE, // 하숙집: 즉시 일꾼 +4
    MARKET,         // 시장: 보드에서 배/가축 타일 획득
    CARPENTER,      // 목수 작업장: 보드에서 건물 타일 획득
    CHURCH,         // 교회: 보드에서 광산/수도원/성 타일 획득
    WAREHOUSE,      // 창고: 상품 판매
    TOWN_HALL       // 시청: (미구현) 타일 추가 배치
}
