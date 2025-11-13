package castlesofburgundy.player;

import castlesofburgundy.tile.TileType;

public record PersonalCell(
        int id,               // 고유 id
        TileType type,        // 이 칸에 놓일 수 있는 색/타입
        int die              // 1..6, 배치 시 필요한 주사위 눈
) {
}
