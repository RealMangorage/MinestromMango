package org.mangorage.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;

public final class Listeners {

    private final Instance mainLevel;

    public Listeners(Instance mainLevel) {
        this.mainLevel = mainLevel;

        GlobalEventHandler handler = MinecraftServer.getGlobalEventHandler();
        handler.addListener(PlayerSpawnEvent.class, this::playerSpawn);
        handler.addListener(PlayerDisconnectEvent.class, this::playerDisconnect);
        handler.addListener(AsyncPlayerConfigurationEvent.class, this::playerConfiguration);
        handler.addListener(EntityAttackEvent.class, this::onEntityAttack);

    }

    public  void playerSpawn(PlayerSpawnEvent event) {
        if (event.isFirstSpawn()) {
            MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(p -> {
                p.sendMessage("%s joined the server!".formatted(event.getPlayer().getUsername()));
            });
        }

        //EntityCreature entity = new Mine(EntityType.ZOMBIE);
        //entity.setInstance(mainLevel, event.getEntity().getPosition());
    }

    public void playerDisconnect(PlayerDisconnectEvent event) {
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(p -> {
            p.sendMessage("%s Left the server!".formatted(event.getPlayer().getUsername()));
        });
    }

    public void playerConfiguration(AsyncPlayerConfigurationEvent event) {
        Player player = event.getPlayer();
        player.setGameMode(GameMode.CREATIVE);
        event.setSpawningInstance(mainLevel);
        player.setRespawnPoint(
                new Pos(0, 55, 0)
        );
    }

    public void onEntityAttack(EntityAttackEvent event) {
        if (event.getTarget() instanceof LivingEntity livingEntity) {
            livingEntity.damage(DamageType.MOB_ATTACK, 2f);
        }
    }
}
