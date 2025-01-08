package org.mangorage.servertest.core;
import net.goldenstack.loot.LootTable;
import net.goldenstack.loot.Trove;
import net.minestom.server.utils.NamespaceID;
import java.nio.file.Path;
import java.util.Map;

public class LootTableHelper {
    public static Map<NamespaceID, LootTable> loadInternalLootTable(String path) {
        return Trove.readTables(Path.of(path));
    }
}
