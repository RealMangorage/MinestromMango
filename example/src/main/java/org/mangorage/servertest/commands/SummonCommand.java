package org.mangorage.servertest.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import org.mangorage.server.core.MangoServer;
import org.mangorage.server.misc.Util;

import java.util.function.Function;

public class SummonCommand extends MangoServerCommand {
    public enum Type {
        LIVING_ENTITY(LivingEntity::new),
        ENTITY(Entity::new);

        private final Function<EntityType, Entity> function;

        Type(Function<EntityType, Entity> function) {
            this.function = function;
        }

        public Entity create(EntityType type) {
            return function.apply(type);
        }
    }

    public SummonCommand(MangoServer server) {
        super(server, "summon");

        var location = ArgumentType.RelativeVec3("pos");
        var entityType = ArgumentType.EntityType("entityType");
        var typeEntity = ArgumentType.Enum("type", Type.class);

        addSyntax(
                (sender, context) -> {
                    if (sender instanceof Player player) {
                        var pos = Util.getRelatiivePos(context.get(location), player.getPosition());
                        var type = context.get(entityType);
                        var entity = context.get(typeEntity).create(type);

                        entity.setInstance(player.getInstance(), pos);
                    }
                },
                location, entityType, typeEntity
        );
    }
}
