package org.mangorage.server.misc;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.utils.Direction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

    public static <T> boolean isSymmetrical(int pWidth, int pHeight, List<T> pList) {
        if (pWidth == 1) {
            return true;
        } else {
            int i = pWidth / 2;

            for (int j = 0; j < pHeight; j++) {
                for (int k = 0; k < i; k++) {
                    int l = pWidth - 1 - k;
                    T t = pList.get(k + j * pWidth);
                    T t1 = pList.get(l + j * pWidth);
                    if (!t.equals(t1)) {
                        return false;
                    }
                }
            }

            return true;
        }
    }
}
