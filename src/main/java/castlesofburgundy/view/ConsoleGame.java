package castlesofburgundy.view;

import castlesofburgundy.core.Command;
import castlesofburgundy.core.Decision;
import castlesofburgundy.core.Game;
import castlesofburgundy.core.GameEvent;
import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.BuildingKind;
import castlesofburgundy.tile.Tile;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Scanner;

/**
 * 콘솔에서 Game을 진행한다. 이 화면에는 진행 서술과 선택 메뉴만 두고,
 * 보드와 상세 상태는 StatusFile로 써서 두 번째 터미널에서 보게 한다.
 */
public final class ConsoleGame {
    private static final String BACK = "b";

    private final Scanner in;
    private final PrintStream out;
    private final StatusFile statusFile;
    private boolean statusFileFailed;

    // "=> 실행: ..."은 고른 행동이 후속 선택까지 끝난 뒤(ActionCompleted) 출력한다
    private String executedLine;

    public ConsoleGame(Scanner in, PrintStream out, Path statusFile) {
        this.in = in;
        this.out = out;
        this.statusFile = new StatusFile(statusFile);
    }

    public void run(long seed) {
        Game game = Game.start(seed, readPlayerNames());
        out.println("\n보드와 상세 상태는 " + statusFile.path() + " 에 계속 씁니다. 다른 터미널에서 이렇게 보세요:");
        out.println("while true; do clear; cat '" + statusFile.path() + "'; sleep 1; done");
        printEvents(game);

        while (!game.isOver()) {
            writeStatus(game);
            Decision decision = game.pending();
            printHeader(game, decision);

            Command command = choose(CommandMenu.build(game));
            String line = executedLine(decision, CommandText.describe(game, decision, command));
            if (line != null) {
                executedLine = line; // 효과의 대상 고르기는 앞서 고른 행동의 줄을 덮지 않는다
            }

            game.apply(command);
            printEvents(game);
        }
        writeStatus(game);

        out.println("\n=== 최종 게임 보드 ===");
        out.println(GameBoardConsoleView.render(game.board()));
        for (Player player : game.players()) {
            out.println("\n=== 최종 플레이어 보드 (" + player.getName() + ") ===");
            out.println(PlayerConsoleView.render(player));
        }
    }

    private List<String> readPlayerNames() {
        int playerCount = readPlayerCount();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            out.print("\n플레이어 " + (i + 1) + " 이름을 입력하세요: ");
            String name = in.nextLine().trim();
            names.add(name.isEmpty() ? "플레이어" + (i + 1) : name);
        }
        return names;
    }

    private int readPlayerCount() {
        while (true) {
            out.print("플레이어 수를 입력하세요 (1~4): ");
            String line = in.nextLine();
            try {
                int n = Integer.parseInt(line.trim());
                if (n >= 1 && n <= 4) {
                    return n;
                }
            } catch (NumberFormatException ignored) {
            }
            out.println("1~4 사이 숫자를 입력하세요.");
        }
    }

    /** 단계 메뉴를 따라 내려가 명령 하나를 고른다. 첫 단계가 아니면 b로 한 단계 위로 돌아간다. */
    private Command choose(List<CommandMenu.Item> top) {
        Deque<List<CommandMenu.Item>> parents = new ArrayDeque<>();
        List<CommandMenu.Item> items = top;
        while (true) {
            for (int i = 0; i < items.size(); i++) {
                out.println(i + ") " + items.get(i).label());
            }
            boolean canGoBack = !parents.isEmpty();
            if (canGoBack) {
                out.println(BACK + ") 뒤로");
            }

            Integer choice = readChoice(items.size(), canGoBack);
            if (choice == null) {
                items = parents.pop();
                continue;
            }
            switch (items.get(choice)) {
                case CommandMenu.Leaf leaf -> {
                    return leaf.command();
                }
                case CommandMenu.Group group -> {
                    parents.push(items);
                    items = group.items();
                    out.println("\n[" + group.label() + "]");
                }
            }
        }
    }

    /** @return 고른 번호, 뒤로면 null */
    private Integer readChoice(int size, boolean canGoBack) {
        while (true) {
            out.print("번호 선택(0~" + (size - 1) + (canGoBack ? ", " + BACK + "=뒤로" : "") + "): ");
            String line = in.nextLine().trim();
            if (canGoBack && line.equalsIgnoreCase(BACK)) {
                return null;
            }
            try {
                int v = Integer.parseInt(line);
                if (v >= 0 && v < size) {
                    return v;
                }
            } catch (NumberFormatException ignored) {
            }
            out.println("잘못된 입력입니다.");
        }
    }

    /** 상태 화면은 보조 수단이라, 쓰지 못해도 게임은 계속하고 한 번만 알린다. */
    private void writeStatus(Game game) {
        try {
            statusFile.write(game);
        } catch (IOException e) {
            if (!statusFileFailed) {
                out.println("[알림] 상태 파일을 쓰지 못했습니다: " + e.getMessage());
                statusFileFailed = true;
            }
        }
    }

    // ===== 선택 화면 =====

    private void printHeader(Game game, Decision decision) {
        switch (decision) {
            case Decision.ChooseDie d ->
                    out.println("\n사용할 주사위를 고르세요. (남은 주사위: " + d.remainingDice() + ")");
            case Decision.ChooseDieAction d -> out.println(compactStatus(game.currentPlayer()));
            case Decision.ChooseCastleAction c -> out.println("\n[성 효과] 추가로 한 번 더 행동할 수 있습니다.");
            case Decision.OfferCentralMarket o -> out.println("\n[턴 종료 전] 중앙 시장에서 한 번 구매할 수 있습니다.");
            case Decision.ChooseShipDepot s -> out.println("[배] 상품을 가져올 창고를 고르세요.");
            case Decision.ChooseBuildingTile b ->
                    out.println(CommandText.buildingLabel(b.kind()) + " 가져올 타일을 고르세요.");
            case Decision.ChooseGoodsToSell g -> out.println("[창고] 팔 상품을 고르세요.");
            case Decision.GameOver g -> {
            }
        }
    }

    /** 행동을 고를 때 필요한 것만 한 줄로. 자세한 요약은 상태 파일에 있다. */
    private static String compactStatus(Player player) {
        List<String> stored = new ArrayList<>();
        List<Tile> tiles = player.getBoard().getStorage().view();
        for (int i = 0; i < tiles.size(); i++) {
            stored.add("#" + i + "=" + tiles.get(i).type());
        }
        List<String> goods = new ArrayList<>();
        for (GoodsColor color : GoodsColor.values()) {
            int count = player.getBoard().getGoods().count(color);
            if (count > 0) {
                goods.add(color + "=" + count);
            }
        }
        return "일꾼 " + player.getWorkers() + " · 은화 " + player.getSilver() + " · 점수 " + player.getScore()
                + " · 저장소 " + (stored.isEmpty() ? "(비어 있음)" : String.join(", ", stored))
                + " · 상품 " + (goods.isEmpty() ? "(없음)" : String.join(", ", goods));
    }

    /** 행동을 고르는 선택만 "=> 실행" 줄을 남긴다. 주사위 고르기와 효과의 대상 고르기는 남기지 않는다. */
    private static String executedLine(Decision decision, String description) {
        return switch (decision) {
            case Decision.ChooseDieAction d -> "=> 실행: " + description;
            case Decision.OfferCentralMarket o -> "=> 실행: " + description;
            case Decision.ChooseCastleAction c -> "=> [성 추가 행동] 실행: " + description;
            default -> null;
        };
    }

    // ===== 일어난 일 =====

    private void printEvents(Game game) {
        for (GameEvent event : game.drainEvents()) {
            print(event);
        }
    }

    private void print(GameEvent event) {
        switch (event) {
            case GameEvent.PhaseStarted e -> {
                out.println("\n================================");
                out.println("=== Phase " + e.phase() + " 시작 ===");
                out.println("================================");
            }
            case GameEvent.RoundStarted e -> {
                out.println("\n--- Phase " + e.phase() + " / Round " + e.round() + " ---");
                out.println("흰 주사위 " + e.whiteDie() + " → 창고 " + e.whiteDie() + "에 상품(" + e.goods() + ") 배치");
            }
            case GameEvent.TurnStarted e -> {
                out.println("\n===" + e.player().getName() + "의 턴 ===");
                out.println("굴린 주사위: " + e.die1() + ", " + e.die2());
            }
            case GameEvent.DieActionStarted e ->
                    out.println("\n[" + e.actionNumber() + "번째 행동: 주사위 " + e.die() + "]");
            case GameEvent.GoodsSold e -> out.println((e.byWarehouse() ? "[창고] " : "[상품 판매] ")
                    + e.color() + " " + e.count() + "개 → 은화 +1, 점수 +" + e.score());
            case GameEvent.ShipGoodsTaken e -> {
                out.println("[배] 창고 " + e.depot() + "에서 상품 " + e.stored() + "개를 가져왔습니다.");
                if (e.dropped() > 0) {
                    out.println("[배] 상품 저장소 색 제한으로 " + e.dropped() + "개는 보관하지 못했습니다.");
                }
            }
            case GameEvent.ShipFoundNoGoods e -> out.println("[배] 가져올 상품이 있는 창고가 없습니다.");
            case GameEvent.TurnOrderAdvanced e ->
                    out.println("[다리] " + e.player().getName() + "의 턴 순서 토큰이 앞으로 이동했습니다.");
            case GameEvent.BuildingEffectSkipped e -> out.println(e.kind() == BuildingKind.WAREHOUSE
                    ? "[창고] 판매할 상품이 없습니다."
                    : CommandText.buildingLabel(e.kind()) + " 가져올 수 있는 타일이 없습니다.");
            case GameEvent.BuildingTileTaken e ->
                    out.println(CommandText.buildingLabel(e.kind()) + " 효과를 실행했습니다.");
            case GameEvent.RegionCompleted e -> out.println("[구역 완성] " + e.player().getName()
                    + "이(가) 크기 " + e.size()
                    + " 구역을 완성했습니다! (+" + e.score() + "점, 페이즈 " + e.phase() + ")");
            case GameEvent.ActionCompleted e -> {
                if (executedLine != null) {
                    out.println(executedLine);
                    executedLine = null;
                }
            }
            case GameEvent.CastleActionUnavailable e ->
                    out.println("\n[성 효과] 추가 행동이 가능하지만 현재 가능한 선택지가 없습니다.");
            case GameEvent.MinesPaid e ->
                    out.println(e.player().getName() + ": 광산 " + e.silver() + "개 → 은화 +" + e.silver());
            case GameEvent.PhaseEnded e -> out.println("\n=== Phase " + e.phase() + " 종료 ===");
            case GameEvent.GameEnded e -> out.println("\n=== 모든 페이즈 종료 ===");
        }
    }
}
