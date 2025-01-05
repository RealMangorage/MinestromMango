package org.mangorage.servertest.block.placement;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mangorage.server.misc.Util;

public class AxisBlockPlacementRule extends BlockPlacementRule {

    public AxisBlockPlacementRule(@NotNull Block block) {
        super(block);
    }

    @Override
    public @Nullable Block blockPlace(@NotNull PlacementState placementState) {
        return placementState.block()
                .withProperty("axis", Util.getAxis(Util.getPlayerFacingDirection(placementState.playerPosition(), false)));
    }
}
