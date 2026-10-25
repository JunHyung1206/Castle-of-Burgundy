package castlesofburgundy.tile;

/**
 * 육각 타일. 종류와 그 종류에 딸린 데이터(가축 종·수, 건물 종류)만 가진다.
 * 배치 효과는 플레이어·보드를 함께 바꾸므로 core.PlayerActions가 한 곳에서 적용한다.
 */
public interface Tile {
    int id();
    TileType type();
}
