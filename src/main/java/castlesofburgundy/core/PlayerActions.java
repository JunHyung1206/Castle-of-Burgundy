package castlesofburgundy.core;

import castlesofburgundy.board.*;
import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;

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

    public static boolean placeTileFromStorage(Player player, int storageId, int cellId, int dieUsed, Phase phase) {
        Tile tile = player.getStorage().view().get(storageId);
        TileType type = tile.type();

        player.placeTileFromStorage(storageId, cellId, dieUsed);
        // 구역 완성 체크
        PersonalBoard board = player.getBoard();
        int regionSize = board.completedRegionSizeIfAny(cellId);
        if (regionSize > 0) {
            int gained = phaseBonus(phase) + regionSizeBonus(regionSize);
            player.addScore(gained);

            System.out.println(
                    "[구역 완성] " + player.getName() +
                            "이(가) 크기 " + regionSize + " 구역을 완성했습니다! (+" + gained + "점, 페이즈 " + phase + ")"
            );
        }

        // TODO: 배치 효과(성/동물/건물/지식 등)는 이후 단계에서 훅 추가

        return (type == TileType.CASTLE);
    }



    public static void takeWorkers(Player player) {
        player.gainWorkers(2);
    }

    private static int phaseBonus(Phase phase) {
        return switch (phase) {
            case A -> 10;
            case B -> 8;
            case C -> 6;
            case D -> 4;
            case E -> 2;
        };
    }

    private static int regionSizeBonus(int size) {
        return switch (size) {
            case 1 -> 1;
            case 2 -> 3;
            case 3 -> 6;
            case 4 -> 10;
            case 5 -> 15;
            case 6 -> 21;
            case 7 -> 28;
            case 8 -> 36;
            default -> throw new IllegalArgumentException("지원하지 않는 구역 크기: " + size);
        };
    }
}
