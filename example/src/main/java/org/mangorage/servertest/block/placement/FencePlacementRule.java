package org.mangorage.servertest.block.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FencePlacementRule extends BlockPlacementRule {

    public FencePlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        var inst = placementState.instance();
        var block = placementState.block();
        var pos = placementState.placePosition();
        for (BlockFace face : BlockFace.values()) {
            if (face == BlockFace.TOP || face == BlockFace.BOTTOM) continue;
            if (inst.getBlock(pos.relative(face)).isSolid()) {
                block = block.withProperty(face.name().toLowerCase(), "true");
            }
        }
        return block;
    }

    @Override
    public @NotNull Block blockUpdate(@NotNull UpdateState updateState) {
        var inst = updateState.instance();
        var block = updateState.currentBlock();
        var pos = updateState.blockPosition();
        var face = updateState.fromFace();
        if (face == BlockFace.TOP || face == BlockFace.BOTTOM) return block;
        if (!inst.getBlock(pos.relative(face)).isSolid()) {
            return block
                    .withProperty(face.name().toLowerCase(), "false");
        }
        return block
                .withProperty(face.name().toLowerCase(), "true");
    }

    @Override
    public boolean isSelfReplaceable(@NotNull Replacement replacement) {
        return true;
    }
}
