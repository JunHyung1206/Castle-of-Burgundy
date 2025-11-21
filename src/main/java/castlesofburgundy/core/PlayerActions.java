package castlesofburgundy.core;

import castlesofburgundy.board.*;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.Tile;

public final class PlayerActions {
    private PlayerActions() {
    }

    public static void takeTileFromMarket(GameContext ctx, Player player, int dieValue, int slotIndex) {
        if (dieValue < 1 || dieValue > 6) {
            throw new IllegalArgumentException("주사위 눈은 1~6");
        }
        if (slotIndex < 0 || slotIndex >= 4) {
            throw new IllegalArgumentException("slotIndex 0~3");
        }

        BoardState boardState = ctx.boardState();

        BoardSlot slot = new BoardSlot(dieValue, slotIndex);
        Tile tile = boardState.removeAt(slot).orElseThrow(() -> new IllegalStateException("해당 슬롯에 타일이 없습니다: " + slot));

        player.addToStorage(tile);
    }

    public static void placeFromStorage(Player player, int storageId, int cellId, int dieUsed) {
        player.placeTileFromStorage(storageId, cellId, dieUsed);
        // TODO: 배치 효과(성/동물/건물/지식 등)는 이후 단계에서 훅 추가
    }

    public static void takeWorkers(Player player) {
        player.gainWorkers(2);
    }
}
