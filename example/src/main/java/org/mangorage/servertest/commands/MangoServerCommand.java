package org.mangorage.servertest.commands;

import net.minestom.server.command.builder.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mangorage.server.core.MangoServer;

public class MangoServerCommand extends Command {
    private final MangoServer server;

    public MangoServerCommand(MangoServer server, @NotNull String name, @Nullable String... aliases) {
        super(name, aliases);
        this.server = server;
    }

    public MangoServerCommand(MangoServer server, @NotNull String name) {
        super(name);
        this.server = server;
    }

    public final MangoServer getServer() {
        return server;
    }
}
