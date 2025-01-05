package org.mangorage.server.core;

import net.minestom.server.ServerProcess;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class MangoBlockManager {
    private final ServerProcess process;
    private final Map<Block, BlockHandler> blockHandlerMap = new HashMap<>();

    public MangoBlockManager(ServerProcess process) {
        this.process = process;
    }

    public void register(BlockHandler handler, Block... blocks) {
        for (Block block : blocks) {
            blockHandlerMap.put(block, handler);
            process.block()
                    .registerHandler(
                            handler.getNamespaceId(),
                            () -> handler
                    );
        }
    }

    public void registerPlacementRule(Function<Block, BlockPlacementRule> function, Block... blocks) {
        var blockManager = process.block();
        for (Block block : blocks) {
            blockManager.registerBlockPlacementRule(function.apply(block));
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
