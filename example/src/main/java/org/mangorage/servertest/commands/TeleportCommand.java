package org.mangorage.servertest.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.location.RelativeVec;
import org.mangorage.server.core.MangoServer;

public class TeleportCommand extends MangoServerCommand {
    public TeleportCommand(MangoServer server) {
        super(server, "tp");

        var positionArgument = ArgumentType.RelativeVec3("pos");
        var levelArgument = ArgumentType
                .Word("level")
                .from(
                        server.getLevelList().toArray(String[]::new)
                );

        addSyntax(
                this::onPositionTeleport,
                positionArgument, levelArgument
        );
    }

    private void onPositionTeleport(CommandSender sender, CommandContext context) {
        if (sender instanceof Player player) {
            final RelativeVec relativeVec = context.get("pos");
            final String levelId = context.get("level");
            final Instance instance = getServer().getLevel(NamespaceID.from(levelId));

            final Vec rel = relativeVec.from(new Pos(0, 0, 0));

            final Pos playerPos = player.getPosition();

            final Pos position = new Pos(
                    relativeVec.isRelativeX() ? playerPos.x() + rel.x() : rel.x(),
                    relativeVec.isRelativeY() ? playerPos.y() + rel.y() : rel.y(),
                    relativeVec.isRelativeZ() ? playerPos.z() + rel.z() : rel.z(),
                    playerPos.yaw(),
                    playerPos.pitch()
            );

            if (player.getInstance() == instance) {
                player.teleport(position);
                player.sendMessage(Component.text("You have been teleported to " + toString(position)));
            } else {
                player.teleport(position);
                player.sendMessage(Component.text("You have been teleported to " + toString(position) + " " + levelId));
            }
        }
    }

    private static String ofDouble(double d) {
        return ((Double) d).toString();
    }

    private static String toString(Pos pos) {
        return "X: %s Y: %s Z: %s".formatted(
                pos.blockX(),
                pos.blockY(),
                pos.blockZ()
        );
    }
}
