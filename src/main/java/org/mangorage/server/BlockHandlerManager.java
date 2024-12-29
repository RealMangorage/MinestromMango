package org.mangorage.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import java.util.HashMap;
import java.util.Map;

public final class BlockHandlerManager {
    private final Map<Block, BlockHandler> blockHandlerMap = new HashMap<>();

    public void register(Block block, BlockHandler handler) {
        blockHandlerMap.put(block, handler);
        MinecraftServer.getBlockManager()
                .registerHandler(
                        handler.getNamespaceId(),
                        () -> handler
                );
    }

    public void register(BlockHandler handler, Block... blocks) {
        for (Block block : blocks) {
            register(block, handler);
        }
    }

    public void register(GlobalEventHandler handler) {
        handler.addListener(PlayerBlockPlaceEvent.class, event -> {
            var blockHandler = blockHandlerMap.get(event.getBlock());
            if (blockHandler != null)
                event.setBlock(
                        event.getBlock()
                                .withHandler(
                                        blockHandler
                                )
                );
        });
    }
}
