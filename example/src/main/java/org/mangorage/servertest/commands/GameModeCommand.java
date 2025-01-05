package org.mangorage.servertest.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

public class GameModeCommand extends Command {
    public GameModeCommand() {
        super("gamemode");

        var gamemodeArgument = ArgumentType.Enum("gamemode", GameMode.class);
        gamemodeArgument.setFormat(ArgumentEnum.Format.LOWER_CASED);
        gamemodeArgument.setDefaultValue(GameMode.SURVIVAL);

        var playerArgument = ArgumentType.Entity("player");
        playerArgument.onlyPlayers(true);
        playerArgument.singleEntity(true);

        addSyntax((sender, context) -> {
            final GameMode mode = context.get(gamemodeArgument);
            if (sender instanceof Player player)
                player.setGameMode(mode);
        }, gamemodeArgument);

        addSyntax((sender, context) -> {
            var plr = context.get(playerArgument).findFirstPlayer(sender);
            if (plr != null)
                plr.setGameMode(context.get(gamemodeArgument));
        }, gamemodeArgument, playerArgument);
    }
}
