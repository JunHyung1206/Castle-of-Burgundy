package castlesofburgundy.core;

import castlesofburgundy.board.Depot;
import castlesofburgundy.board.GameBoard;
import castlesofburgundy.core.Command.BuyCentralMarket;
import castlesofburgundy.core.Command.ChooseBoardTile;
import castlesofburgundy.core.Command.ChooseDepotGoods;
import castlesofburgundy.core.Command.ChooseGoodsColor;
import castlesofburgundy.core.Command.EndTurn;
import castlesofburgundy.core.Command.PlaceTile;
import castlesofburgundy.core.Command.SellGoods;
import castlesofburgundy.core.Command.TakeTile;
import castlesofburgundy.core.Command.TakeWorkers;
import castlesofburgundy.core.Command.UseDie;
import castlesofburgundy.core.Decision.ChooseBuildingTile;
import castlesofburgundy.core.Decision.ChooseCastleAction;
import castlesofburgundy.core.Decision.ChooseDie;
import castlesofburgundy.core.Decision.ChooseDieAction;
import castlesofburgundy.core.Decision.ChooseGoodsToSell;
import castlesofburgundy.core.Decision.ChooseShipDepot;
import castlesofburgundy.core.Decision.GameOver;
import castlesofburgundy.core.Decision.OfferCentralMarket;
import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.goods.GoodsSupply;
import castlesofburgundy.player.PersonalBoard;
import castlesofburgundy.player.PersonalLayout;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.BuildingKind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 게임 한 판의 상태 기계. 진행 위치(페이즈·라운드·차례·남은 주사위)와 지금 기다리는 입력(pending)을 갖고,
 * 명령을 받아 검증·실행한 뒤 다음 입력이 필요한 곳까지 진행한다. 화면과 입출력은 모른다.
 * <p>
 * 사용법: pending()과 availableActions()를 보고 명령 하나를 apply()한 뒤, drainEvents()로 일어난 일을 읽는다.
 */
public final class Game {
    private static final int ROUNDS_PER_PHASE = 5;
    private static final int MAX_PLAYERS = 4;
    private static final int STARTING_GOODS = 3;
    private static final int STARTING_SILVER = 1;

    private final GameContext ctx;
    private final List<Player> players;
    private final PlayerActions actions;
    private final ActionRules rules;
    private final List<GameEvent> events = new ArrayList<>();

    private Phase phase;
    private int round;
    private List<Player> roundOrder = List.of();
    private int currentIndex;
    private Turn turn;
    private int actionNumber;
    private Decision pending;

    // 선택형 효과가 끝날 때까지 미뤄 둔 배치 결과
    private GameEvent.RegionCompleted deferredRegion;
    private boolean deferredExtraAction;

    /**
     * 새 게임: 시작 플레이어부터 일꾼 1·2·3·4개, 은화 1개, 무작위 상품 3개를 주고 성을 가운데 칸에 놓은 뒤
     * 페이즈 A 첫 턴의 첫 입력까지 진행한다.
     */
    public static Game start(long seed, List<String> playerNames) {
        if (playerNames.isEmpty() || playerNames.size() > MAX_PLAYERS) {
            throw new IllegalArgumentException("플레이어 수는 1~" + MAX_PLAYERS + ": " + playerNames.size());
        }
        GameContext ctx = new GameContext(seed, new GameBoard(), new TileSupplier(seed), new Dice(seed), new GoodsSupply(seed));

        PersonalLayout layout = new PersonalLayout(); // 모든 플레이어가 공유하는 정적 배치
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < playerNames.size(); i++) {
            PersonalBoard board = new PersonalBoard(layout);
            board.setupStartingCastle();

            Player player = new Player(board, playerNames.get(i), i + 1);
            player.addSilver(STARTING_SILVER);
            for (int g = 0; g < STARTING_GOODS; g++) {
                board.getGoods().add(ctx.goodsSupply().draw());
            }
            players.add(player);
        }

        Game game = new Game(ctx, players);
        game.begin();
        return game;
    }

    /** 플레이어를 앉히기만 한다. 진행은 begin()부터. (테스트에서 원하는 상태의 플레이어로 시작할 때 사용) */
    Game(GameContext ctx, List<Player> players) {
        this.ctx = ctx;
        this.players = List.copyOf(players);
        this.actions = new PlayerActions(ctx, players.size());
        this.rules = new ActionRules(ctx.board(), actions);
        ctx.board().turnOrder().seat(this.players);
    }

    void begin() {
        startPhase(Phase.A);
    }

    // ===== 조회 =====

    public Decision pending() {
        return pending;
    }

    public boolean isOver() {
        return pending instanceof GameOver;
    }

    // 아래 조회들은 실제 객체를 돌려주므로 패키지 안(테스트 포함)에서만 쓴다. 바깥은 state()를 읽는다.

    /** 자리 순서(시작 플레이어부터). 턴 순서는 board().turnOrder(). */
    List<Player> players() {
        return players;
    }

    GameBoard board() {
        return ctx.board();
    }

    Player currentPlayer() {
        if (isOver()) {
            throw new IllegalStateException("게임이 끝났습니다.");
        }
        return roundOrder.get(currentIndex);
    }

    Phase phase() {
        return phase;
    }

    int round() {
        return round;
    }

    /** 지금 상태의 읽기 전용 스냅샷. */
    public GameState state() {
        List<GameState.PlayerState> playerStates = new ArrayList<>();
        for (int seat = 0; seat < players.size(); seat++) {
            playerStates.add(playerState(seat, players.get(seat)));
        }
        List<Integer> order = board().turnOrder().currentOrder().stream().map(this::seatOf).toList();
        int currentSeat = isOver() ? -1 : seatOf(currentPlayer());
        return new GameState(phase, round, currentSeat, order, List.copyOf(playerStates), boardState());
    }

    private GameState.PlayerState playerState(int seat, Player player) {
        PersonalBoard board = player.getBoard();
        Map<GoodsColor, Integer> goods = new EnumMap<>(GoodsColor.class);
        for (GoodsColor color : GoodsColor.values()) {
            int count = board.getGoods().count(color);
            if (count > 0) {
                goods.put(color, count);
            }
        }
        return new GameState.PlayerState(seat, player.getName(), player.getWorkers(), player.getSilver(), player.getScore(),
                board.getStorage().view(), Collections.unmodifiableMap(goods), board.getLayout(), board.placedTiles());
    }

    private GameState.BoardState boardState() {
        List<GameState.DepotState> depots = new ArrayList<>();
        for (Depot depot : board().depots()) {
            List<GameState.SlotState> slots = new ArrayList<>();
            for (int slot = 0; slot < depot.slotCount(); slot++) {
                slots.add(new GameState.SlotState(depot.slotType(slot), depot.tileAt(slot).orElse(null)));
            }
            depots.add(new GameState.DepotState(depot.number(), List.copyOf(slots), depot.goods()));
        }
        return new GameState.BoardState(List.copyOf(depots), board().centralMarket());
    }

    private int seatOf(Player player) {
        return players.indexOf(player);
    }

    /** 지금 낼 수 있는 명령과 각각의 비용·즉시 이득. 순서는 화면의 메뉴 순서다. */
    public List<ActionOption> options() {
        return availableActions().stream()
                .map(command -> rules.option(command, pending, currentPlayer(), phase))
                .toList();
    }

    /** 마지막으로 읽은 뒤 일어난 일을 순서대로 돌려주고 비운다. */
    public List<GameEvent> drainEvents() {
        List<GameEvent> drained = List.copyOf(events);
        events.clear();
        return drained;
    }

    /** 지금 낼 수 있는 명령 전부(options()의 명령과 같은 순서). apply가 이 목록으로 검증한다. */
    List<Command> availableActions() {
        return isOver() ? List.of() : rules.commands(pending, currentPlayer(), turn);
    }

    // ===== 명령 실행 =====

    public void apply(Command command) {
        if (!availableActions().contains(command)) {
            throw new IllegalArgumentException("지금 할 수 없는 명령입니다: " + command + " (대기 중: " + pending + ")");
        }

        Player player = currentPlayer();
        switch (command) {
            case UseDie u -> useDie(u.die());
            case TakeTile t -> {
                int cost = workersFor(t.targetDie());
                if (t.discardIndex() == null) {
                    actions.takeTileFromBoard(player, cost, t.targetDie(), t.slot());
                } else {
                    actions.replaceTileFromBoard(player, cost, t.targetDie(), t.slot(), t.discardIndex());
                }
                completeAction(false);
            }
            case PlaceTile p -> {
                PlacementOutcome outcome = actions.placeTileFromStorage(
                        player, workersFor(p.targetDie()), p.storageIndex(), p.cellId(), p.targetDie(), phase);
                handlePlacement(player, outcome);
            }
            case SellGoods s -> {
                GoodsColor color = GoodsColor.forDie(s.targetDie());
                PlayerActions.Gain sale = actions.previewSale(player, color);
                int sold = actions.sellGoods(player, workersFor(s.targetDie()), color);
                events.add(new GameEvent.GoodsSold(seatOf(player), color, sold, sale.score(), false));
                completeAction(false);
            }
            case TakeWorkers t -> {
                actions.takeWorkers(player);
                completeAction(false);
            }
            case BuyCentralMarket b -> {
                if (b.discardIndex() == null) {
                    actions.buyTileFromCentralMarket(player, b.marketIndex());
                } else {
                    actions.replaceTileFromCentralMarket(player, b.marketIndex(), b.discardIndex());
                }
                turn.markCentralMarketBought();
                events.add(new GameEvent.ActionCompleted());
                if (pending instanceof OfferCentralMarket) {
                    endTurn();
                }
                // 주사위 행동 중이었다면 주사위를 쓰지 않았으므로 같은 선택을 계속 기다린다
            }
            case EndTurn e -> {
                events.add(new GameEvent.ActionCompleted());
                endTurn();
            }
            case ChooseDepotGoods c -> {
                PlayerActions.ShipGoods taken = actions.takeShipGoods(player, c.depot());
                events.add(new GameEvent.ShipGoodsTaken(seatOf(player), c.depot(), taken.stored(), taken.dropped()));
                advanceTurnOrder(player);
                finishPlacement();
            }
            case ChooseBoardTile c -> {
                if (c.discardIndex() == null) {
                    actions.takeTileFromBoard(player, 0, c.depot(), c.slot());
                } else {
                    actions.replaceTileFromBoard(player, 0, c.depot(), c.slot(), c.discardIndex());
                }
                events.add(new GameEvent.BuildingTileTaken(((ChooseBuildingTile) pending).kind()));
                finishPlacement();
            }
            case ChooseGoodsColor c -> {
                PlayerActions.Gain sale = actions.previewSale(player, c.color());
                int sold = actions.sellGoods(player, 0, c.color());
                events.add(new GameEvent.GoodsSold(seatOf(player), c.color(), sold, sale.score(), true));
                finishPlacement();
            }
        }
    }

    private int workersFor(int targetDie) {
        return ActionRules.workersFor(pending, targetDie);
    }

    private void handlePlacement(Player player, PlacementOutcome outcome) {
        deferredExtraAction = outcome.grantsExtraAction();
        deferredRegion = outcome.completedRegionSize() > 0
                ? new GameEvent.RegionCompleted(seatOf(player), outcome.completedRegionSize(), outcome.regionScore(), phase)
                : null;

        switch (outcome.followUp()) {
            case PlacementOutcome.FollowUp.None n -> finishPlacement();
            case PlacementOutcome.FollowUp.ShipGoods s -> {
                if (actions.depotsWithGoods().isEmpty()) {
                    events.add(new GameEvent.ShipFoundNoGoods(seatOf(player)));
                    advanceTurnOrder(player);
                    finishPlacement();
                } else {
                    pending = new ChooseShipDepot();
                }
            }
            case PlacementOutcome.FollowUp.BoardTile b -> {
                if (rules.boardTileChoices(player, b.allowedTypes()).isEmpty()) {
                    events.add(new GameEvent.BuildingEffectSkipped(b.kind()));
                    finishPlacement();
                } else {
                    pending = new ChooseBuildingTile(b.kind(), b.allowedTypes());
                }
            }
            case PlacementOutcome.FollowUp.WarehouseSale w -> {
                if (rules.goodsColorChoices(player).isEmpty()) {
                    events.add(new GameEvent.BuildingEffectSkipped(BuildingKind.WAREHOUSE));
                    finishPlacement();
                } else {
                    pending = new ChooseGoodsToSell();
                }
            }
        }
    }

    private void advanceTurnOrder(Player player) {
        actions.advanceTurnOrder(player);
        events.add(new GameEvent.TurnOrderAdvanced(seatOf(player)));
    }

    /** 배치와 그 후속 선택이 모두 끝났다. 구역 완성은 효과 서술 뒤에 알린다. */
    private void finishPlacement() {
        if (deferredRegion != null) {
            events.add(deferredRegion);
            deferredRegion = null;
        }
        boolean extraAction = deferredExtraAction;
        deferredExtraAction = false;
        completeAction(extraAction);
    }

    private void completeAction(boolean grantsExtraAction) {
        events.add(new GameEvent.ActionCompleted());
        if (grantsExtraAction) {
            if (!rules.castleActions(currentPlayer()).isEmpty()) {
                pending = new ChooseCastleAction();
                return;
            }
            events.add(new GameEvent.CastleActionUnavailable(seatOf(currentPlayer())));
        }
        continueTurn();
    }

    // ===== 진행 =====

    private void continueTurn() {
        List<Integer> remaining = turn.remainingDice();
        if (!remaining.isEmpty()) {
            List<Integer> distinct = remaining.stream().distinct().toList();
            if (distinct.size() == 1) {
                useDie(distinct.get(0));
            } else {
                pending = new ChooseDie(remaining);
            }
            return;
        }
        if (!rules.purchaseActions(currentPlayer(), turn).isEmpty()) {
            pending = new OfferCentralMarket();
            return;
        }
        endTurn();
    }

    private void useDie(int die) {
        turn.useDie(die);
        events.add(new GameEvent.DieActionStarted(actionNumber++, die));
        pending = new ChooseDieAction(die);
    }

    private void endTurn() {
        currentIndex++;
        if (currentIndex < roundOrder.size()) {
            startTurn();
        } else if (round < ROUNDS_PER_PHASE) {
            startRound(round + 1);
        } else {
            endPhase();
        }
    }

    private void startPhase(Phase next) {
        phase = next;
        board().refillDepots(players.size(), ctx.tileSupplier()::draw);
        board().refillCentralMarket(ctx.tileSupplier()::drawAny);
        events.add(new GameEvent.PhaseStarted(phase));
        startRound(1);
    }

    /** 흰 주사위 눈의 창고에 상품 하나를 놓고, 이 라운드의 차례를 턴 순서 트랙에서 정한다. */
    private void startRound(int next) {
        round = next;
        int whiteDie = ctx.dice().roll();
        GoodsColor goods = ctx.goodsSupply().draw();
        board().depot(whiteDie).addGoods(goods);
        events.add(new GameEvent.RoundStarted(phase, round, whiteDie, goods));

        // 라운드 도중 배로 순서가 바뀌어도 다음 라운드부터 반영된다
        roundOrder = board().turnOrder().currentOrder();
        currentIndex = 0;
        startTurn();
    }

    private void startTurn() {
        Player player = roundOrder.get(currentIndex);
        int d1 = ctx.dice().roll();
        int d2 = ctx.dice().roll();
        events.add(new GameEvent.TurnStarted(seatOf(player), d1, d2));

        turn = new Turn(d1, d2);
        actionNumber = 1;
        continueTurn();
    }

    /** 페이즈 종료: 정산(광산 등)은 PlayerActions가 정하고, 여기서는 순서만 진행한다. */
    private void endPhase() {
        for (Player player : board().turnOrder().currentOrder()) {
            int silver = actions.payMines(player);
            if (silver > 0) {
                events.add(new GameEvent.MinesPaid(seatOf(player), silver));
            }
        }
        events.add(new GameEvent.PhaseEnded(phase));

        if (phase == Phase.E) {
            events.add(new GameEvent.GameEnded());
            pending = new GameOver();
            return;
        }
        startPhase(Phase.values()[phase.ordinal() + 1]);
    }
}
