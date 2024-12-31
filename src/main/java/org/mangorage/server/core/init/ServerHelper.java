package org.mangorage.server.core.init;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.exception.ExceptionHandler;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.Difficulty;
import net.minestom.server.world.DimensionType;
import org.mangorage.server.block.handlers.LavaBlockHandler;
import org.mangorage.server.core.Listeners;
import org.mangorage.server.core.MangoServer;
import org.mangorage.server.block.handlers.CraftingTableBlockHandler;
import org.mangorage.server.block.handlers.FurnaceBlockHandler;
import org.mangorage.server.block.placement.FacingBlockPlacementRule;
import org.mangorage.server.commands.GameModeCommand;
import org.mangorage.server.commands.SaveAllCommand;
import org.mangorage.server.commands.TeleportCommand;
import org.mangorage.server.commands.TransferCommand;
import org.mangorage.server.data.PlayerData;
import org.mangorage.server.generators.GeneratorList;
import org.mangorage.server.generators.SimpleTerrainGenerator;

import java.util.Timer;
import java.util.TimerTask;

public class ServerHelper {
    public static void startServer(int port, String sid) {
        MangoServer.init("MangoServer", server -> {
            MinecraftServer.setBrandName(sid);
            MinecraftServer.setDifficulty(Difficulty.HARD);

            new Listeners();

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

            server.getServerProcess()
                    .exception()
                    .setExceptionHandler(e -> {

                    });

            server.getBlockManager()
                    .register(
                            new LavaBlockHandler(),
                            Block.LAVA
                    );

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
                                                    Component.text("Saved %s...".formatted(s))
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
                    10_000 // 10s
            );


            server.createLevel(
                    NamespaceID.from("mangorage:main"),
                    (id, level) -> {
                        server.setOnPlayerJoin((e -> {
                            var plr = e.getPlayer();

                            var data = PlayerData.deserialize(e.getPlayer());
                            if (data != null) {
                                data.itemStacks().forEach((s, i) -> e.getPlayer().getInventory().setItemStack(s, i));
                                plr.setGameMode(GameMode.CREATIVE);
                                plr.setRespawnPoint(
                                        data.pos()
                                );
                                plr.getInventory().addItemStack(ItemStack.of(Material.OAK_LOG, 64));
                                e.setSpawningInstance(level);
                            } else {
                                plr.setGameMode(GameMode.CREATIVE);
                                plr.setRespawnPoint(
                                        new Pos(0, 50, 0)
                                );
                                plr.getInventory().addItemStack(ItemStack.of(Material.OAK_LOG, 64));
                                e.setSpawningInstance(level);
                            }

                        }));

                        level.setGenerator(new GeneratorList()
                                .add(new SimpleTerrainGenerator())
                        );
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
                            new TeleportCommand(server)
                    );

            server.start(port);
        });
    }
}
