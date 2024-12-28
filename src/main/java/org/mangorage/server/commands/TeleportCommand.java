package org.mangorage.server.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.NamespaceID;
import org.mangorage.server.MangoServer;

public class TeleportCommand extends MangoServerCommand {
    public TeleportCommand(MangoServer server) {
        super(server, "tp");

        var positionArgument = ArgumentType.RelativeVec3("pos");
        var levelArgument = ArgumentType
                .Word("levels")
                .from(
                        server.getLevelList().toArray(String[]::new)
                );

        addSyntax(
                (sender, context) -> {
                    sender.sendMessage("Teleported Player");
                    if (sender instanceof Player player) {
                        var pos = context.get(positionArgument);
                        var lvl = server.getLevel(NamespaceID.from(context.get(levelArgument)));

                        if (lvl != player.getInstance()) {
                            player.setInstance(lvl);
                        }

                        player.teleport(
                                player.getPosition()
                                        .withCoord(pos.from(player))
                        );
                    }
                },
                positionArgument, levelArgument
        );
    }
}
