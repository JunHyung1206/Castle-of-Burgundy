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

    public static TilePlacementResult placeTileFromStorage(
            Player player,
            int storageId,
            int cellId,
            int dieUsed,
            Phase phase
    ) {
        // 먼저 어떤 타일인지 꺼내고
        Tile tile = player.getStorage().view().get(storageId);

        // 배치(실제 보드에 놓기)
        player.placeTileFromStorage(storageId, cellId, dieUsed);

        // 1) 구역 완성 점수
        PersonalBoard board = player.getBoard();
        int regionSize = board.completedRegionSizeIfAny(cellId);

        int regionScore = 0;
        if (regionSize > 0) {
            regionScore = phaseBonus(phase) + regionSizeBonus(regionSize);
            player.addScore(regionScore);

            System.out.println(
                    "[구역 완성] " + player.getName() +
                            "이(가) 크기 " + regionSize + " 구역을 완성했습니다! (+" + regionScore + "점, 페이즈 " + phase + ")"
            );
        }

        // 2) 타일 고유 효과
        TileEffectContext effectCtx = new TileEffectContext(player, phase, cellId, dieUsed);
        TilePlacementResult tileResult = tile.onPlaced(effectCtx);

        // 3) 타일 효과 결과(보너스 스코어/일꾼/은화) 적용
        if (tileResult.bonusScore() != 0) {
            player.addScore(tileResult.bonusScore());
        }
        if (tileResult.bonusWorkers() != 0) {
            player.gainWorkers(tileResult.bonusWorkers());
        }
        if (tileResult.bonusSilver() != 0) {
            player.addSilver(tileResult.bonusSilver());
        }

        // 4) "구역 점수"와 "타일 자체 보너스"를 합친 결과를 리턴
        return new TilePlacementResult(
                tileResult.grantExtraAction(),
                tileResult.bonusWorkers(),
                tileResult.bonusSilver(),
                tileResult.bonusScore() + regionScore
        );
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
