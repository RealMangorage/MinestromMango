package org.mangorage.servertest.block.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.mangorage.server.misc.Util;

public final class FacingBlockPlacementRule extends BlockPlacementRule {
    public FacingBlockPlacementRule(@NotNull Block block) {
        super(block);
        if (block.getProperty("facing") == null)
            throw new IllegalStateException("Missing 'Facing' Property");
    }

    @Override
    public Block blockPlace(@NotNull PlacementState placementState) {
        if (placementState.block().defaultState() != getBlock()) return placementState.block();

        return placementState.block()
                .withProperty("facing", Util.getPlayerFacingDirection(placementState.playerPosition(), true).name().toLowerCase());
    }

    @Override
    public @NotNull Block getBlock() {
        return super.getBlock();
    }

    @Override
    public @NotNull Block blockUpdate(@NotNull UpdateState updateState) {
        return super.blockUpdate(updateState);
    }

    @Override
    public boolean isSelfReplaceable(@NotNull Replacement replacement) {
        return super.isSelfReplaceable(replacement);
    }

    @Override
    public int maxUpdateDistance() {
        return super.maxUpdateDistance();
    }
}
