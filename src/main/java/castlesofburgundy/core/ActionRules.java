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
import castlesofburgundy.goods.GoodsColor;
import castlesofburgundy.player.Player;
import castlesofburgundy.tile.Tile;
import castlesofburgundy.tile.TileType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 지금 기다리는 선택에서 플레이어가 낼 수 있는 명령과, 각 명령의 비용·즉시 이득을 계산한다.
 * 상태는 바꾸지 않는다. 행동 룰(주사위 조정, 구매 조건, 버릴 칸 등)이 바뀌면 여기를 고친다.
 */
final class ActionRules {
    private static final int MAX_WORKERS_PER_DIE = 3; // ±3은 같은 눈이라 3개면 모든 눈에 닿는다

    private final GameBoard board;
    private final PlayerActions actions;

    ActionRules(GameBoard board, PlayerActions actions) {
        this.board = board;
        this.actions = actions;
    }

    /** 낼 수 있는 명령 전부. 순서는 화면의 메뉴 순서다. 게임이 끝났으면 부르지 않는다. */
    List<Command> commands(Decision pending, Player player, Turn turn) {
        return switch (pending) {
            case Decision.ChooseDie d -> d.remainingDice().stream().distinct().<Command>map(UseDie::new).toList();
            case Decision.ChooseDieAction d -> dieActions(d.die(), player, turn);
            case Decision.ChooseCastleAction c -> castleActions(player);
            case Decision.ChooseShipDepot s -> actions.depotsWithGoods().stream()
                    .<Command>map(depot -> new ChooseDepotGoods(depot.number()))
                    .toList();
            case Decision.ChooseBuildingTile b -> boardTileChoices(player, b.allowedTypes());
            case Decision.ChooseGoodsToSell g -> goodsColorChoices(player);
            case Decision.OfferCentralMarket o -> {
                List<Command> options = new ArrayList<>();
                options.add(new EndTurn());
                options.addAll(purchaseActions(player, turn));
                yield options;
            }
            case Decision.GameOver g -> List.of();
        };
    }

    /** 명령 하나의 비용과, 적용하는 즉시 얻는 점수·은화. */
    ActionOption option(Command command, Decision pending, Player player, Phase phase) {
        return switch (command) {
            case TakeTile t -> new ActionOption(command, workersFor(pending, t.targetDie()), 0, 0, 0);
            case PlaceTile p -> {
                PlayerActions.Gain gain = actions.previewPlacement(player, p.storageIndex(), p.cellId(), phase);
                yield new ActionOption(command, workersFor(pending, p.targetDie()), 0, gain.score(), gain.silver());
            }
            case SellGoods s -> {
                PlayerActions.Gain gain = actions.previewSale(player, GoodsColor.forDie(s.targetDie()));
                yield new ActionOption(command, workersFor(pending, s.targetDie()), 0, gain.score(), gain.silver());
            }
            case ChooseGoodsColor c -> {
                PlayerActions.Gain gain = actions.previewSale(player, c.color());
                yield new ActionOption(command, 0, 0, gain.score(), gain.silver());
            }
            case BuyCentralMarket b -> new ActionOption(command, 0, PlayerActions.CENTRAL_MARKET_COST, 0, 0);
            default -> new ActionOption(command, 0, 0, 0, 0);
        };
    }

    /** 주사위 행동은 굴린 눈에서 목표 눈까지의 거리만큼 일꾼이 든다. 성 추가 행동은 원하는 눈을 고르는 것이라 들지 않는다. */
    static int workersFor(Decision pending, int targetDie) {
        if (pending instanceof Decision.ChooseDieAction d) {
            return workerCost(d.die(), targetDie);
        }
        return 0;
    }

    /** 주사위 눈 from을 to로 바꾸는 데 드는 일꾼 수(1↔6을 잇는 원형 거리). */
    static int workerCost(int from, int to) {
        int distance = Math.abs(from - to);
        return Math.min(distance, 6 - distance);
    }

    /** 일꾼 workers개 이하로 만들 수 있는 눈을 메뉴 순서(그대로, -1, +1, -2, +2, -3)로 돌려준다. */
    static List<Integer> reachableDice(int baseDie, int workers) {
        List<Integer> dice = new ArrayList<>();
        dice.add(baseDie);
        int max = Math.min(MAX_WORKERS_PER_DIE, workers);
        for (int used = 1; used <= max; used++) {
            dice.add(wrapAdd(baseDie, -used));
            if (used == MAX_WORKERS_PER_DIE) {
                break;
            }
            dice.add(wrapAdd(baseDie, used));
        }
        return dice;
    }

    private static int wrapAdd(int face, int delta) {
        return ((face - 1 + delta) % 6 + 6) % 6 + 1;
    }

    private List<Command> dieActions(int baseDie, Player player, Turn turn) {
        List<Command> options = new ArrayList<>();
        for (int die : reachableDice(baseDie, player.getWorkers())) {
            options.addAll(actionsForDie(die, player));
        }
        options.addAll(purchaseActions(player, turn));
        options.add(new TakeWorkers());
        return options;
    }

    List<Command> castleActions(Player player) {
        List<Command> options = new ArrayList<>();
        for (int die = 1; die <= 6; die++) {
            options.addAll(actionsForDie(die, player));
        }
        return options;
    }

    /** 주사위 눈 하나로 할 수 있는 행동: 그 번호 창고에서 가져오기, 그 눈 칸에 배치, 그 색 판매. */
    private List<Command> actionsForDie(int die, Player player) {
        List<Command> options = new ArrayList<>();

        Depot depot = board.depot(die);
        for (int slot = 0; slot < depot.slotCount(); slot++) {
            if (depot.tileAt(slot).isEmpty()) {
                continue;
            }
            for (Integer discard : discardChoices(player)) {
                options.add(new TakeTile(die, slot, discard));
            }
        }

        List<Tile> stored = player.getBoard().getStorage().view();
        for (int storageIndex = 0; storageIndex < stored.size(); storageIndex++) {
            for (int cellId : player.getBoard().legalPlacements(stored.get(storageIndex), die)) {
                options.add(new PlaceTile(die, storageIndex, cellId));
            }
        }

        if (player.getBoard().getGoods().count(GoodsColor.forDie(die)) > 0) {
            options.add(new SellGoods(die));
        }
        return options;
    }

    List<Command> purchaseActions(Player player, Turn turn) {
        if (!turn.canBuyFromCentralMarket() || player.getSilver() < PlayerActions.CENTRAL_MARKET_COST) {
            return List.of();
        }
        List<Command> options = new ArrayList<>();
        for (int marketIndex = 0; marketIndex < board.centralMarket().size(); marketIndex++) {
            for (Integer discard : discardChoices(player)) {
                options.add(new BuyCentralMarket(marketIndex, discard));
            }
        }
        return options;
    }

    List<Command> boardTileChoices(Player player, Set<TileType> allowedTypes) {
        List<Command> options = new ArrayList<>();
        for (Depot depot : board.depots()) {
            for (int slot = 0; slot < depot.slotCount(); slot++) {
                boolean allowed = depot.tileAt(slot).map(tile -> allowedTypes.contains(tile.type())).orElse(false);
                if (!allowed) {
                    continue;
                }
                for (Integer discard : discardChoices(player)) {
                    options.add(new ChooseBoardTile(depot.number(), slot, discard));
                }
            }
        }
        return options;
    }

    List<Command> goodsColorChoices(Player player) {
        List<Command> options = new ArrayList<>();
        for (GoodsColor color : GoodsColor.values()) {
            if (player.getBoard().getGoods().count(color) > 0) {
                options.add(new ChooseGoodsColor(color));
            }
        }
        return options;
    }

    /** 저장소에 자리가 있으면 버릴 필요가 없고(null), 가득 찼으면 버릴 칸마다 선택지가 생긴다. */
    private static List<Integer> discardChoices(Player player) {
        int size = player.getBoard().getStorage().size();
        if (!player.getBoard().getStorage().isFull()) {
            List<Integer> keepAll = new ArrayList<>();
            keepAll.add(null);
            return keepAll;
        }
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            indexes.add(i);
        }
        return indexes;
    }
}
