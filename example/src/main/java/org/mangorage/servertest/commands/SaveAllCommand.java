package org.mangorage.servertest.commands;

import org.mangorage.server.core.MangoServer;

public class SaveAllCommand extends MangoServerCommand {
    public SaveAllCommand(MangoServer server) {
        super(server, "saveAll");

        setDefaultExecutor(((sender, context) -> {
            sender.sendMessage("Saving All...");
            server.saveAll(sender::sendMessage);
        }));
    }
}
