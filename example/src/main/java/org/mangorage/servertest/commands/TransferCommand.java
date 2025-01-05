package org.mangorage.servertest.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.common.TransferPacket;

public class TransferCommand extends Command {
    public TransferCommand() {
        super("transfer");

        var portArgument = ArgumentType.Integer("port");

        addSyntax((sender, context) -> {
            if (sender instanceof Player player) {
                player.sendPacket(
                        new TransferPacket(
                                "127.0.0.1", 25566
                        )
                );
                player.getPlayerConnection().disconnect();
            }
        }, portArgument);
    }
}
