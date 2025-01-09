package org.mangorage.servertest.commands;

import com.dfsek.terra.api.command.CommandSender;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.entity.Player;

public final class Conditions {
    public static final CommandCondition CHEATS_NEEDED = (sender, commandString) -> sender instanceof CommandSender cmd || sender instanceof Player player && player.getPermissionLevel() >= 3;
}
