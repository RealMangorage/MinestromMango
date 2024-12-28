package org.mangorage.server.commands;

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
                player.getPlayerConnection().sendPacket(
                        new TransferPacket("localhost", context.get(portArgument))
                );
            }
        }, portArgument);
    }
}
