package org.mangorage.servertest.core;

import net.goldenstack.loot.LootTable;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.utils.NamespaceID;
import org.mangorage.server.core.MangoServer;
import org.mangorage.server.recipie.CraftingRecipeManager;
import org.mangorage.server.misc.PlayerUtil;
import org.mangorage.servertest.core.nodes.DefaultBlockBreakHandler;
import org.mangorage.servertest.core.nodes.DefaultInventoryHandler;
import java.util.Map;

public final class Listeners {
    private final CraftingRecipeManager manager;

    private static final Map<NamespaceID, LootTable> tables = LootTableHelper.loadInternalLootTable( "loot_tables/blocks");

    public Listeners(MangoServer server) {
        this.manager = server.getCraftingRecipeManager();
        GlobalEventHandler handler = MinecraftServer.getGlobalEventHandler();

        handler.addChild(DefaultBlockBreakHandler.register(server));
        handler.addChild(DefaultInventoryHandler.register(server));

        handler.addListener(
                PlayerDisconnectEvent.class,
                event -> {
                    PlayerUtil.serialize(server, "players", event.getPlayer());
                }
        );
    }




}
