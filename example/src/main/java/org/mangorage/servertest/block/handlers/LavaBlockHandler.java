package org.mangorage.servertest.block.handlers;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

public class LavaBlockHandler implements BlockHandler {
    private final NamespaceID ID = NamespaceID.from("minecraft:water");
    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return ID;
    }

    boolean ticked = false;
    boolean finished = false;

    @Override
    public void tick(@NotNull Tick tick) {
        if (ticked && !finished) {
            tick.getInstance().setBlock(tick.getBlockPosition(), Block.LAVA);
            this.finished = true;
        }
        if (ticked) return;

        BlockFace[] faces = new BlockFace[]{
                BlockFace.EAST,
                BlockFace.NORTH,
                BlockFace.WEST,
                BlockFace.SOUTH
        };
        for (BlockFace face : faces) {
            var inst = tick.getInstance();
            var pos = tick.getBlockPosition().relative(face);
            var above = tick.getBlockPosition().relative(BlockFace.TOP);

            if (!inst.getBlock(above).isAir()) {
                inst.setBlock(tick.getBlockPosition(), Block.STONE);
            } else {
                if (ChunkUtils.isLoaded(inst.getChunkAt(pos))) {
                    if (inst.getBlock(pos) == Block.AIR) {
                        inst.setBlock(pos, Block.LAVA.withHandler(new LavaBlockHandler()));
                        this.ticked = true;
                    }
                }
            }
        }

    }

    @Override
    public boolean isTickable() {
        return !(finished && ticked);
    }
}
