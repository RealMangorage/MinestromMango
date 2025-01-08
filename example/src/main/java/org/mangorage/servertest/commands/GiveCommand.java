package org.mangorage.servertest.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mangorage.server.core.MangoServer;

public class GiveCommand extends MangoServerCommand {
    public GiveCommand(MangoServer server) {
        super(server, "give");

        var player = ArgumentType.Entity("player")
                .onlyPlayers(true)
                .singleEntity(true);

        var item = ArgumentType.ItemStack("item");

        addSyntax(
                (sender, context) -> {
                    context.get(player).findFirstPlayer(sender).getInventory().addItemStack(context.get(item));
                },
                player, item
        );
    }
}
