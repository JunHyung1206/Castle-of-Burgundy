package castlesofburgundy.core;

import castlesofburgundy.player.Player;

public record TileEffectContext(
        Player player,
        Phase phase,
        int cellId,     // 어느 칸에 놓였는지
        int dieUsed     // 어떤 주사위 눈으로 배치했는지 (필요하면)
) {
}
