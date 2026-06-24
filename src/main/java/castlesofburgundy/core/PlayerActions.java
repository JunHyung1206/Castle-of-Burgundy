package castlesofburgundy.core;

import castlesofburgundy.board.*;
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

    /**
     * 저장소의 타일을 영지에 배치하고, 그에 따른 즉시 효과(구역 완성 점수 + 타일 고유 보너스)를
     * 모두 player에 적용한다.
     *
     * @return 성 효과처럼 "추가 행동"을 줘야 하면 true. (그 외 점수/자원은 이미 적용됨)
     */
    public static boolean placeTileFromStorage(
            Player player,
            int storageId,
            int cellId,
            int dieUsed,
            Phase phase
    ) {
        // 배치(실제 보드에 놓기) — 놓인 타일을 그대로 돌려받는다
        Tile tile = player.placeTileFromStorage(storageId, cellId, dieUsed);

        // 1) 구역 완성 점수
        int regionSize = player.completedRegionSizeIfAny(cellId);
        if (regionSize > 0) {
            int regionScore = phaseBonus(phase) + regionSizeBonus(regionSize);
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

        // 4) 호출부 제어 흐름에 필요한 것만 반환 (점수/자원은 위에서 이미 적용)
        return tileResult.grantExtraAction();
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
