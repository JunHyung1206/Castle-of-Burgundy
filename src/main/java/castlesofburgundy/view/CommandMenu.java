package castlesofburgundy.view;

import castlesofburgundy.core.ActionOption;
import castlesofburgundy.core.Command;
import castlesofburgundy.core.Decision;
import castlesofburgundy.core.GameState;
import castlesofburgundy.tile.Tile;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Game.options()를 단계 메뉴로 묶는다: 행동 종류 → 대상 → (저장소가 가득 찼으면) 버릴 칸.
 * 보기가 하나뿐인 단계는 건너뛰어, 짧은 선택은 한 번에 끝난다. 모든 명령은 정확히 한 번씩 메뉴 끝(Leaf)에 나온다.
 */
final class CommandMenu {
    private CommandMenu() {
    }

    sealed interface Item {
        String label();
    }

    /** 고르면 이 명령을 Game에 보낸다. */
    record Leaf(String label, Command command) implements Item {
    }

    /** 고르면 아래 보기로 들어간다. */
    record Group(String label, List<Item> items) implements Item {
    }

    /** 메뉴에서 행동 종류를 나누는 기준. 선언 순서가 첫 단계 보기 순서다. */
    private enum Kind {
        TAKE_TILE("타일 가져오기"),
        PLACE_TILE("저장소 타일 배치"),
        SELL_GOODS("상품 판매"),
        END_TURN("턴 종료"),
        BUY_CENTRAL_MARKET("[주사위 미사용] 중앙 시장 구매"),
        TAKE_WORKERS("일꾼 받기"),
        OTHER("선택");

        private final String label;

        Kind(String label) {
            this.label = label;
        }

        static Kind of(Command command) {
            return switch (command) {
                case Command.TakeTile t -> TAKE_TILE;
                case Command.PlaceTile p -> PLACE_TILE;
                case Command.SellGoods s -> SELL_GOODS;
                case Command.EndTurn e -> END_TURN;
                case Command.BuyCentralMarket b -> BUY_CENTRAL_MARKET;
                case Command.TakeWorkers t -> TAKE_WORKERS;
                default -> OTHER; // 주사위 고르기, 배·건물 효과의 대상 고르기
            };
        }
    }

    static List<Item> build(GameState state, Decision decision, List<ActionOption> options) {
        // 1) 버릴 칸만 다른 명령을 대상 하나로 묶는다
        Map<Command, List<ActionOption>> byTarget = new LinkedHashMap<>();
        for (ActionOption option : options) {
            byTarget.computeIfAbsent(withoutDiscard(option.command()), key -> new ArrayList<>()).add(option);
        }

        // 2) 대상을 행동 종류로 묶는다
        Map<Kind, List<Item>> byKind = new EnumMap<>(Kind.class);
        for (Map.Entry<Command, List<ActionOption>> entry : byTarget.entrySet()) {
            byKind.computeIfAbsent(Kind.of(entry.getKey()), key -> new ArrayList<>())
                    .add(targetItem(state, decision, entry.getKey(), entry.getValue()));
        }

        if (byKind.size() == 1) {
            return byKind.values().iterator().next();
        }
        List<Item> items = new ArrayList<>();
        for (Map.Entry<Kind, List<Item>> entry : byKind.entrySet()) {
            List<Item> targets = entry.getValue();
            items.add(targets.size() == 1
                    ? targets.get(0)
                    : new Group(entry.getKey().label + " (" + targets.size() + ")", targets));
        }
        return items;
    }

    /** 대상 하나: 버릴 칸 선택이 없으면 바로 실행, 있으면 버릴 칸을 고르는 단계로. */
    private static Item targetItem(GameState state, Decision decision, Command target, List<ActionOption> variants) {
        ActionOption first = variants.get(0);
        String label = CommandText.describe(state, decision, target, first.workerCost()) + gain(first);
        if (variants.size() == 1 && first.command().equals(target)) {
            return new Leaf(label, target);
        }

        List<Item> discards = new ArrayList<>();
        for (ActionOption variant : variants) {
            int discardIndex = discardIndex(variant.command());
            Tile discarded = CommandText.storedTile(state.current(), discardIndex);
            discards.add(new Leaf("저장소[" + discardIndex + "] " + discarded.type() + " 버리기", variant.command()));
        }
        return new Group(label + " - 저장소가 가득 차 버릴 칸 선택", discards);
    }

    /** 고르기 전에 알 수 있는 이득(Game이 계산한 값): 판매, 구역 완성·가축·망루·은행 배치. */
    private static String gain(ActionOption option) {
        List<String> parts = new ArrayList<>();
        if (option.silverGain() > 0) {
            parts.add("은화 +" + option.silverGain());
        }
        if (option.scoreGain() > 0) {
            parts.add("점수 +" + option.scoreGain());
        }
        return parts.isEmpty() ? "" : " → " + String.join(", ", parts);
    }

    private static Command withoutDiscard(Command command) {
        return switch (command) {
            case Command.TakeTile t -> new Command.TakeTile(t.targetDie(), t.slot(), null);
            case Command.BuyCentralMarket b -> new Command.BuyCentralMarket(b.marketIndex(), null);
            case Command.ChooseBoardTile c -> new Command.ChooseBoardTile(c.depot(), c.slot(), null);
            default -> command;
        };
    }

    private static int discardIndex(Command command) {
        return switch (command) {
            case Command.TakeTile t -> t.discardIndex();
            case Command.BuyCentralMarket b -> b.discardIndex();
            case Command.ChooseBoardTile c -> c.discardIndex();
            default -> throw new IllegalArgumentException("버릴 칸이 없는 명령: " + command);
        };
    }

    /** 메뉴 끝에 있는 모든 명령(검증용). */
    static List<Command> leaves(List<Item> items) {
        List<Command> commands = new ArrayList<>();
        for (Item item : items) {
            switch (item) {
                case Leaf leaf -> commands.add(leaf.command());
                case Group group -> commands.addAll(leaves(group.items()));
            }
        }
        return commands;
    }
}
