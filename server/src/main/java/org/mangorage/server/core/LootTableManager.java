package org.mangorage.server.core;

import net.goldenstack.loot.LootTable;
import net.goldenstack.loot.Trove;
import net.minestom.server.utils.NamespaceID;

import java.nio.file.Path;
import java.util.Map;

public final class LootTableManager {
    private final Map<NamespaceID, LootTable> tables;

    public LootTableManager(Path path) {
        this.tables = Trove.readTables(path);
    }

    public LootTable get(NamespaceID id) {
        return tables.get(id);
    }

}
