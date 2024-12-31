package org.mangorage.server.misc;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Direction;

public final class Util {
    public static Direction getPlayerFacingDirection(Pos playerPos) {
        float yaw = playerPos.yaw();

        // Normalize yaw to a range of 0-360
        yaw = (yaw % 360 + 360) % 360;

        // Determine the direction
        if (yaw >= 45 && yaw < 135) {
            return Direction.EAST;
        } else if (yaw >= 135 && yaw < 225) {
            return Direction.SOUTH;
        } else if (yaw >= 225 && yaw < 315) {
            return Direction.WEST;
        } else {
            return Direction.NORTH;
        }
    }
}
