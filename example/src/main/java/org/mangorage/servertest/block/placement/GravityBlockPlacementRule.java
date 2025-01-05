package org.mangorage.servertest.block.placement;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mangorage.servertest.entities.FallingBlockEntity;

public class GravityBlockPlacementRule extends BlockPlacementRule {
    public GravityBlockPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @NotNull Block blockUpdate(@NotNull UpdateState updateState) {
        var inst = updateState.instance();
        var pos = updateState.blockPosition();
        if (inst.getBlock(pos.relative(BlockFace.BOTTOM)).isAir()) {
            Entity entity = new FallingBlockEntity(updateState.currentBlock());
            entity.editEntityMeta(FallingBlockMeta.class, e -> {
                e.setBlock(updateState.currentBlock());
            });

            entity.setInstance((Instance) inst, pos.add(0.5, 0.5, 0.5));
            return Block.AIR;
        }
        return updateState.currentBlock();
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        return placementState.block();
    }
}
