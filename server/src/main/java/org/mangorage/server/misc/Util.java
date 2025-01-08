package org.mangorage.server.misc;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class Util {
    public static Direction getPlayerFacingDirection(Pos playerPos, boolean skip) {
        float yaw = playerPos.yaw();
        float pitch = playerPos.pitch(); // Assuming Pos has a pitch method.

        if (!skip) {
            // Check for UP/DOWN based on pitch
            if (pitch <= -45) {
                return Direction.UP;
            } else if (pitch >= 45) {
                return Direction.DOWN;
            }
        }

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

    public static String getAxis(Direction direction) {
        return switch (direction) {
            case UP:
            case DOWN:
                yield "y";
            case EAST:
            case WEST:
                yield "x";
            case NORTH:
            case SOUTH:
                yield "z";
        };
    }

}
