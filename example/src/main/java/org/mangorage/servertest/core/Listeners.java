package org.mangorage.servertest.core;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import org.mangorage.server.core.MangoServer;
import org.mangorage.server.recipie.CraftingRecipeManager;
import org.mangorage.server.misc.PlayerUtil;
import org.mangorage.servertest.core.nodes.DefaultBlockBreakHandler;
import org.mangorage.servertest.core.nodes.DefaultInventoryHandler;

public final class Listeners {
    private final CraftingRecipeManager manager;

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
