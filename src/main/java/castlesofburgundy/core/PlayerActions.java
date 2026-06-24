package castlesofburgundy.core;

import castlesofburgundy.board.*;
import castlesofburgundy.goods.GoodsColor;
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
     * @return 일어난 일(추가 행동 여부, 완성된 구역 크기/점수)을 담은 결과. 화면 서술은 호출부가 한다.
     */
    public static PlacementOutcome placeTileFromStorage(
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
        int regionScore = 0;
        if (regionSize > 0) {
            regionScore = phaseBonus(phase) + regionSizeBonus(regionSize);
            player.addScore(regionScore);
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

        return new PlacementOutcome(tileResult.grantExtraAction(), regionSize, regionScore);
    }


    public static void takeWorkers(Player player) {
        player.gainWorkers(2);
    }

    /**
     * 해당 색 상품을 전부 판매한다.
     * 판매가 일어나면 은화 1개(개수 무관) + 타일당 playerCount(2/3/4인) 점수를 얻는다.
     *
     * @return 판매한 타일 수(0이면 그 색 상품이 없어 아무 일도 안 일어남)
     */
    public static int sellGoods(Player player, GoodsColor color, int playerCount) {
        int sold = player.goods().sell(color);
        if (sold > 0) {
            player.addSilver(1);
            player.addScore(sold * playerCount);
        }
        return sold;
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

    static int regionSizeBonus(int size) { // 같은 패키지 테스트에서 표 전체를 검증하기 위해 package-private
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
