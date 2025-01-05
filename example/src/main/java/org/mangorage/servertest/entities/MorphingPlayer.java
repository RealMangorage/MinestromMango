package org.mangorage.servertest.entities;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

public class MorphingPlayer extends Player {
    private volatile Entity morphed = null;

    public MorphingPlayer(@NotNull PlayerConnection playerConnection, @NotNull GameProfile gameProfile) {
        super(playerConnection, gameProfile);
        this.collidesWithEntities = false;
    }

    @Override
    public void tick(long time) {
        super.tick(time);

        if (morphed != null) {
            if (isRemoved()) {
                morphed.remove();
            } else {
                if (morphed.getInstance() == getInstance()) {
                    morphed.teleport(getPosition());
                } else {
                    morphed.setInstance(getInstance(), getPosition());
                }
            }
        } else {
            switchEntityType(EntityType.PLAYER);
        }
    }

    public void morph(Entity entity) {
        if (entity.getEntityType() == EntityType.PLAYER) {
            this.morphed.remove();
            switchEntityType(EntityType.PLAYER);
        } else {
            this.morphed = entity;
            entity.setInstance(getInstance(), getPosition());
            entity.setNoGravity(true);
            entity.setBoundingBox(0, 0, 0);

            switchEntityType(entity.getEntityType());
            setInvisible(true);
        }
    }
}
