package castlesofburgundy.view;

import castlesofburgundy.board.Depot;
import castlesofburgundy.core.Command;
import castlesofburgundy.core.Decision;
import castlesofburgundy.core.Game;
import castlesofburgundy.core.GameEvent;
import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.BuildingKind;
import castlesofburgundy.tile.Tile;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 콘솔에서 Game을 진행한다: 지금 기다리는 선택과 가능한 명령을 메뉴로 보여 주고,
 * 고른 명령을 Game에 넘긴 뒤 일어난 일(GameEvent)을 문장으로 출력한다.
 */
public final class ConsoleGame {
    private final Scanner in;
    private final PrintStream out;

    // "=> 실행: ..."은 고른 행동이 후속 선택까지 끝난 뒤(ActionCompleted) 출력한다
    private String executedLine;

    public ConsoleGame(Scanner in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    public void run(long seed) {
        Game game = Game.start(seed, readPlayerNames());
        for (Player player : game.players()) {
            out.println("\n--- 개인 보드 초기 상태 (" + player.getName()
                    + ", 시작 성: " + player.getBoard().getLayout().startingCastleCell() + "번 칸) ---");
            out.println(PlayerConsoleView.renderBoardOnly(player.getBoard()));
        }
        printEvents(game);

        while (!game.isOver()) {
            Decision decision = game.pending();
            List<Command> options = game.availableActions();
            printHeader(game, decision);

            List<String> descriptions = new ArrayList<>(options.size());
            for (Command option : options) {
                descriptions.add(describe(game, decision, option));
            }
            int choice = chooseIndex(descriptions);
            String line = executedLine(decision, descriptions.get(choice));
            if (line != null) {
                executedLine = line; // 효과의 대상 고르기는 앞서 고른 행동의 줄을 덮지 않는다
            }

            game.apply(options.get(choice));
            printEvents(game);
        }

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

    private int chooseIndex(List<String> options) {
        for (int i = 0; i < options.size(); i++) {
            out.println(i + ") " + options.get(i));
        }
        while (true) {
            out.print("번호 선택(0~" + (options.size() - 1) + "): ");
            String line = in.nextLine();
            try {
                int v = Integer.parseInt(line);
                if (v >= 0 && v < options.size()) {
                    return v;
                }
            } catch (NumberFormatException ignored) {
            }
            out.println("잘못된 입력입니다.");
        }
    }

    // ===== 선택 화면 =====

    private void printHeader(Game game, Decision decision) {
        switch (decision) {
            case Decision.ChooseDie d ->
                    out.println("\n사용할 주사위를 고르세요. (남은 주사위: " + d.remainingDice() + ")");
            case Decision.ChooseDieAction d -> {
                out.println("현재 일꾼 수 : " + game.currentPlayer().getWorkers());
                out.println(TurnContextView.render(game, d.die()));
            }
            case Decision.ChooseCastleAction c -> out.println("\n[성 효과] 추가로 한 번 더 행동할 수 있습니다.");
            case Decision.OfferCentralMarket o -> out.println("\n[턴 종료 전] 중앙 시장에서 한 번 구매할 수 있습니다.");
            case Decision.ChooseShipDepot s -> {
            }
            case Decision.ChooseBuildingTile b -> {
            }
            case Decision.ChooseGoodsToSell g -> {
            }
            case Decision.GameOver g -> {
            }
        }
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

    private static String describe(Game game, Decision decision, Command command) {
        Player player = game.currentPlayer();
        return switch (command) {
            case Command.UseDie u -> "주사위 " + u.die() + " 사용";
            case Command.TakeTile t -> dieActionPrefix(decision, t.targetDie())
                    + takeTileText(player, game.board().depot(t.targetDie()), t.slot(), t.discardIndex(), "");
            case Command.PlaceTile p -> dieActionPrefix(decision, p.targetDie())
                    + "저장소[" + p.storageIndex() + "]의 " + storedTile(player, p.storageIndex()).type()
                    + " 를 cell " + p.cellId() + " 에 배치";
            case Command.SellGoods s -> {
                GoodsColor color = GoodsColor.forDie(s.targetDie());
                yield dieActionPrefix(decision, s.targetDie()) + sellText(player, color);
            }
            case Command.TakeWorkers t -> "일꾼 2개 받기";
            case Command.BuyCentralMarket b -> {
                String type = game.board().centralMarket().get(b.marketIndex()).type().toString();
                yield b.discardIndex() == null
                        ? "[주사위 미사용] 은화 2개로 중앙 시장[" + b.marketIndex() + "]의 " + type + " 구매"
                        : "[주사위 미사용] 저장소[" + b.discardIndex() + "] 버리고 은화 2개로 중앙 시장["
                        + b.marketIndex() + "]의 " + type + " 구매"
                        + " (버림: " + storedTile(player, b.discardIndex()).type() + ")";
            }
            case Command.EndTurn e -> "구매하지 않고 턴 종료";
            case Command.ChooseDepotGoods c ->
                    "창고 " + c.depot() + " 상품 가져오기 " + game.board().depot(c.depot()).goods();
            case Command.ChooseBoardTile c -> {
                BuildingKind kind = ((Decision.ChooseBuildingTile) decision).kind();
                yield takeTileText(player, game.board().depot(c.depot()), c.slot(), c.discardIndex(), buildingLabel(kind) + " ");
            }
            case Command.ChooseGoodsColor c -> sellText(player, c.color());
        };
    }

    /** 주사위 행동은 쓴 일꾼 수를, 성 추가 행동은 고른 눈을 앞에 붙인다. */
    private static String dieActionPrefix(Decision decision, int targetDie) {
        return switch (decision) {
            case Decision.ChooseDieAction d -> {
                int cost = Game.workerCost(d.die(), targetDie);
                yield cost == 0 ? "" : "[일꾼 " + cost + "개 소모] ";
            }
            case Decision.ChooseCastleAction c -> "[성 추가 행동][주사위 " + targetDie + "] ";
            default -> "";
        };
    }

    private static String takeTileText(Player player, Depot depot, int slot, Integer discardIndex, String label) {
        String type = depot.tileAt(slot).orElseThrow().type().toString();
        String take = "섹션 " + depot.number() + " 슬롯 " + slot + "에서 타일 " + type + " 가져오기";
        if (discardIndex == null) {
            return label + take;
        }
        return label + "저장소[" + discardIndex + "] 버리고 " + take
                + " (버림: " + storedTile(player, discardIndex).type() + ")";
    }

    private static String sellText(Player player, GoodsColor color) {
        return color + " 상품 판매 (" + player.getBoard().getGoods().count(color) + "개)";
    }

    private static Tile storedTile(Player player, int storageIndex) {
        return player.getBoard().getStorage().get(storageIndex);
    }

    private static String buildingLabel(BuildingKind kind) {
        return switch (kind) {
            case MARKET -> "[시장]";
            case CARPENTER -> "[목수]";
            case CHURCH -> "[교회]";
            case WAREHOUSE -> "[창고]";
            default -> "[" + kind + "]";
        };
    }

    // ===== 일어난 일 =====

    private void printEvents(Game game) {
        for (GameEvent event : game.drainEvents()) {
            print(game, event);
        }
    }

    private void print(Game game, GameEvent event) {
        switch (event) {
            case GameEvent.PhaseStarted e -> {
                out.println("\n================================");
                out.println("=== Phase " + e.phase() + " 시작 ===");
                out.println("================================");
            }
            case GameEvent.RoundStarted e -> {
                out.println("\n--- Phase " + e.phase() + " / Round " + e.round() + " ---");
                out.println("흰 주사위 " + e.whiteDie() + " → 창고 " + e.whiteDie() + "에 상품(" + e.goods() + ") 배치");
                out.println(GameBoardConsoleView.render(game.board()));
            }
            case GameEvent.TurnStarted e -> {
                out.println("\n현재 보드 상태 (" + e.player().getName() + ")");
                out.println(PlayerConsoleView.render(e.player()));
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
                    : buildingLabel(e.kind()) + " 가져올 수 있는 타일이 없습니다.");
            case GameEvent.BuildingTileTaken e -> out.println(buildingLabel(e.kind()) + " 효과를 실행했습니다.");
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
