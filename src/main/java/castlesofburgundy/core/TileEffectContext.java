package castlesofburgundy.core;

import castlesofburgundy.player.Player;
import castlesofburgundy.tile.Tile;

import java.util.List;

public record TileEffectContext(
        Player player,
        Phase phase,
        int cellId,     // 어느 칸에 놓였는지
        int dieUsed     // 어떤 주사위 눈으로 배치했는지 (필요하면)
) {
    /**
     * 이 칸이 속한 같은 타입 구역에 이미 놓인 타일들(방금 놓인 자기 자신 포함).
     * 가축 농장 보너스처럼 "주변 같은 구역"을 봐야 하는 효과에서 사용.
     */
    public List<Tile> placedTilesInSameRegion() {
        return player.getBoard().placedTilesInRegion(cellId);
    }
}
