package castlesofburgundy.core;

import castlesofburgundy.board.*;
import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.Tile;

public final class PlayerActions {
    private PlayerActions() {
    }

    public static void takeTileFromBoard(GameContext ctx, Player player, int dieValue, int slotIndex) {
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

    public static void placeTileFromStorage(Player player, int storageId, int cellId, int dieUsed) {
        player.placeTileFromStorage(storageId, cellId, dieUsed);
        // 구역 완성 체크
        PersonalBoard board = player.getBoard();
        int regionSize = board.completedRegionSizeIfAny(cellId);

        if (regionSize > 0) {
            // 임시로 구역 크기만큼 점수를 준다.
            player.addScore(regionSize);

            System.out.println("[구역 완성] " + player.getName() + "이(가) 크기 " + regionSize + " 구역을 완성했습니다! (+" + regionSize + "점)"
            );
        }
        // TODO: 배치 효과(성/동물/건물/지식 등)는 이후 단계에서 훅 추가
    }

    public static void takeWorkers(Player player) {
        player.gainWorkers(2);
    }
}
