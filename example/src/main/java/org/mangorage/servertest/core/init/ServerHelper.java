package org.mangorage.servertest.core.init;

import com.dfsek.terra.minestom.world.TerraMinestomWorldBuilder;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.monster.zombie.ZombieMeta;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import net.minestom.server.network.packet.client.play.ClientPickItemFromBlockPacket;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.Difficulty;
import net.minestom.server.world.DimensionType;
import org.mangorage.server.core.MangoServer;
import org.mangorage.server.misc.PlayerUtil;
import org.mangorage.servertest.block.handlers.CraftingTableBlockHandler;
import org.mangorage.servertest.block.handlers.FurnaceBlockHandler;
import org.mangorage.servertest.block.handlers.LavaBlockHandler;
import org.mangorage.servertest.block.placement.AxisBlockPlacementRule;
import org.mangorage.servertest.block.placement.FacingBlockPlacementRule;
import org.mangorage.servertest.block.placement.FencePlacementRule;
import org.mangorage.servertest.block.placement.GravityBlockPlacementRule;
import org.mangorage.servertest.commands.GameModeCommand;
import org.mangorage.servertest.commands.SaveAllCommand;
import org.mangorage.servertest.commands.SwitchEntityType;
import org.mangorage.servertest.commands.TeleportCommand;
import org.mangorage.servertest.commands.TransferCommand;
import org.mangorage.servertest.core.Listeners;
import org.mangorage.servertest.core.registrations.Recipes;
import org.mangorage.servertest.entities.MorphingPlayer;

import java.util.Timer;
import java.util.TimerTask;

public class ServerHelper {

    public static void startServer(int port, String sid) {
        MangoServer.init(sid, server -> {
            MinecraftServer.setBrandName(sid);
            MinecraftServer.setDifficulty(Difficulty.HARD);

            Recipes.register(server.getCraftingRecipeManager());
            new Listeners(server);

            server.getBlockManager().register(server.getServerProcess().eventHandler());
            server.getServerProcess().packetListener().setListener(ConnectionState.HANDSHAKE, ClientHandshakePacket.class, Handshake::listener);

            server.getBlockManager()
                            .register(
                                    new CraftingTableBlockHandler(),
                                    Block.CRAFTING_TABLE
                            );
            server.getBlockManager()
                            .register(
                                    new FurnaceBlockHandler(),
                                    Block.FURNACE
                            );

            server.getBlockManager()
                    .registerPlacementRule(
                            FacingBlockPlacementRule::new,
                            Block.FURNACE,
                            Block.BLAST_FURNACE,
                            Block.CHEST
                    );

            server.getBlockManager()
                    .registerPlacementRule(
                            GravityBlockPlacementRule::new,
                            Block.SAND,
                            Block.RED_SAND
                    );

            server.getBlockManager()
                    .registerPlacementRule(
                            FencePlacementRule::new,
                            Block.ACACIA_FENCE
                    );
            server.getBlockManager()
                    .registerPlacementRule(
                            AxisBlockPlacementRule::new,
                            Block.OAK_LOG
                    );

            server.getServerProcess()
                    .exception()
                    .setExceptionHandler(e -> {

                    });

            server.getBlockManager()
                    .register(
                            new LavaBlockHandler(),
                            Block.LAVA
                    );


            server.getServerProcess()
                    .connection()
                    .setPlayerProvider(MorphingPlayer::new);

            server.getServerProcess()
                    .eventHandler()
                    .addListener(PlayerMoveEvent.class, playerEvent -> {
                        if (playerEvent.getPlayer() instanceof MorphingPlayer player)
                            player.update();
                    });

            server.getServerProcess()
                    .packetListener()
                    .setPlayListener(ClientPickItemFromBlockPacket.class, (p, plr) -> {
                        var block = plr.getInstance().getBlock(p.pos());
                        var material = Material.fromNamespaceId(block.namespace());
                        if (material == null) return;

                        plr.getInventory()
                                .setItemStack(
                                        plr.getHeldSlot(),
                                        ItemStack.of(material, 1)
                                );
                    });

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(
                    new TimerTask() {
                        @Override
                        public void run() {
                            server.getServerProcess()
                                    .connection()
                                    .getOnlinePlayers()
                                    .forEach(plr -> {
                                        plr.sendMessage(
                                                Component.text("Saving Server...")
                                        );
                                    });
                            server.saveAll(s -> {
                                server.getServerProcess()
                                        .connection()
                                        .getOnlinePlayers()
                                        .forEach(plr -> {
                                            plr.sendMessage(
                                                    Component.text("Saving -> %s...".formatted(s))
                                            );
                                        });
                            });
                            server.getServerProcess()
                                    .connection()
                                    .getOnlinePlayers()
                                    .forEach(plr -> {
                                        plr.sendMessage(
                                                Component.text("Saved Server...")
                                        );
                                    });
                        }
                    },
                    0,
                    100_000 // 100s
            );

            server.createLevel(
                    NamespaceID.from("mangorage:main"),
                    (id, level) -> {

                        TerraMinestomWorldBuilder
                                .from(level)
                                .seed(0)
                                .defaultPack()
                                .attach();

                        server.setOnPlayerJoin((e -> {


                            PlayerUtil.deserialize(server, "players", e.getPlayer(), (plr, result) -> {
                                if (result == PlayerUtil.DeserializationResult.SUCCESS) {

                                } else if (result == PlayerUtil.DeserializationResult.NO_DATA || result == PlayerUtil.DeserializationResult.FAILED) {
                                    plr.setRespawnPoint(
                                            new Pos(0, 50, 0)
                                    );
                                }

                                plr.setGameMode(GameMode.CREATIVE);
                                plr.getInventory().addItemStack(ItemStack.of(Material.OAK_LOG, 64));
                                e.setSpawningInstance(level);
                            });

                        }));

                        level.setChunkSupplier(LightingChunk::new);
                        level.setChunkLoader(server.createLoader(id));
                    }
            );


            server.createLevel(
                    NamespaceID.from("mangorage:other"),
                    DimensionType.THE_END,
                    (id, level) -> {

                    }
            );

            server.getServerProcess().command()
                    .register(
                            new TransferCommand(),
                            new GameModeCommand(),
                            new SaveAllCommand(server),
                            new TeleportCommand(server),
                            new SwitchEntityType(server)
                    );

            server.start(port);
        });
    }
}
