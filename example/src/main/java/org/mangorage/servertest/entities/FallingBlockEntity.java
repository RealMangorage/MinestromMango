package org.mangorage.servertest.entities;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class FallingBlockEntity extends Entity {


    public FallingBlockEntity(Block block) {
        super(EntityType.FALLING_BLOCK, UUID.randomUUID());
    }

    @Override
    public void tick(long time) {
        super.tick(time);
        var inst = getInstance();
        var pos = getPosition();
        var blockPos = new Pos(pos.blockX(), pos.blockY() - 1, pos.blockZ());
        if (getEntityMeta() instanceof FallingBlockMeta meta) {
            if (!inst.getBlock(blockPos).isAir() && entityType == EntityType.FALLING_BLOCK) {
                switchEntityType(EntityType.COW);
            }
        }
    }
}
