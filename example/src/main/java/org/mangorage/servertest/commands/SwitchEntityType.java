package org.mangorage.servertest.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import org.mangorage.server.core.MangoServer;
import org.mangorage.servertest.entities.MorphingPlayer;

public class SwitchEntityType extends MangoServerCommand {

    public SwitchEntityType(MangoServer server) {
        super(server, "switch");

        var type = ArgumentType
                .Word("level")
                .from(
                        EntityType.values()
                                .stream()
                                .map(id -> id.namespace().toString())
                                .toArray(String[]::new)
                );

        addSyntax(
                (sender, context) -> {
                    if (sender instanceof MorphingPlayer entity)
                        entity.morph(
                                new Entity(
                                        EntityType.fromNamespaceId(context.get(type))
                                )
                        );
                },
                type
        );
    }


}
