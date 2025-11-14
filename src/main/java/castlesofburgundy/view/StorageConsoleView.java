package castlesofburgundy.view;

import castlesofburgundy.player.Storage;
import castlesofburgundy.tile.Tile;

import java.util.List;

public final class StorageConsoleView {
    private StorageConsoleView() { }

    public static String render(Storage storage) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Storage ===\n");

        List<Tile> tiles = storage.view();
        if (tiles.isEmpty()) {
            sb.append("(empty)\n");
            return sb.toString();
        }

        for (int i = 0; i < tiles.size(); i++) {
            Tile t = tiles.get(i);
            sb.append("#")
                    .append(i)
                    .append(" : ")
                    .append(t.type())
                    .append(" (id=")
                    .append(t.id())
                    .append(")\n");
        }
        return sb.toString();
    }
}