package org.mangorage.servertest.entities;

import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

public class MorphingPlayer extends Player {
    private volatile Entity morphed = null;

    public MorphingPlayer(@NotNull PlayerConnection playerConnection, @NotNull GameProfile gameProfile) {
        super(playerConnection, gameProfile);
        this.collidesWithEntities = false;
        setTeam(
                MinecraftServer.getTeamManager().createBuilder(gameProfile.uuid().toString())
                        .collisionRule(TeamsPacket.CollisionRule.NEVER)
                        .build()
        );
        getTeam().addMember(gameProfile.name());
        setPermissionLevel(3);
    }


    public void update() {
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

    @Override
    public void tick(long time) {
        super.tick(time);
        update();
    }

    public void morph(Entity entity) {
        if (entity.getEntityType() == EntityType.PLAYER) {
            this.morphed.remove();
            switchEntityType(EntityType.PLAYER);
            setInvisible(false);
        } else {
            if (this.morphed != null) {
                morphed.remove();
                this.morphed = null;
            }
            this.morphed = entity;
            entity.setInstance(getInstance(), getPosition());
            entity.setNoGravity(true);
            entity.setBoundingBox(0, 0, 0);

            getTeam()
                    .addMember(entity.getUuid().toString());

            switchEntityType(entity.getEntityType());
            setInvisible(true);
        }
    }
}
