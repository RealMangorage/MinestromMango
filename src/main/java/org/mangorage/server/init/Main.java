package org.mangorage.server.init;


import de.articdive.jnoise.JNoise;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.Difficulty;
import org.mangorage.server.MangoServer;
import org.mangorage.server.commands.GameModeCommand;
import org.mangorage.server.commands.SaveAllCommand;
import org.mangorage.server.commands.TeleportCommand;
import org.mangorage.server.commands.TransferCommand;

public class Main {
    public static void main(String[] args) {
        MangoServer.init("MangoServer", server -> {

            MojangAuth.init();
            MinecraftServer.setBrandName("MangoServer");
            MinecraftServer.setDifficulty(Difficulty.HARD);

            server.createLevel(
                    NamespaceID.from("mangorage:main"),
                    (id, level) -> {

                        server.setOnPlayerJoin((e -> {
                            Player player = e.getPlayer();
                            player.setGameMode(GameMode.CREATIVE);
                            e.setSpawningInstance(level);
                            player.setRespawnPoint(
                                    new Pos(0, 55, 0)
                            );
                        }));

                        JNoise noise = JNoise.newBuilder()
                                .superSimplex()
                                .setFrequency(0.01) // Low frequency for smooth terrain
                                .setSeed(server.getRandom().nextLong())
                                .build();

                        level.setGenerator(unit -> {
                            Point start = unit.absoluteStart();
                            for (int x = 0; x < unit.size().x(); x++) {
                                for (int z = 0; z < unit.size().z(); z++) {
                                    Point bottom = start.add(x, 0, z);

                                    synchronized (noise) { // Synchronization is necessary for JNoise
                                        double height = noise.getNoise(bottom.x(), bottom.z()) * 16;
                                        // * 16 means the height will be between -16 and +16
                                        unit.modifier().fill(bottom, bottom.add(1, 0, 1).withY(height), Block.STONE);
                                    }
                                }
                            }
                        });

                        level.setChunkSupplier(LightingChunk::new);
                        level.setChunkLoader(server.createLoader(id));
                    }
            );

            server.createLevel(
                    NamespaceID.from("mangorage:other"),
                    (id, level) -> {

                        server.setOnPlayerJoin((e -> {
                            Player player = e.getPlayer();
                            player.setGameMode(GameMode.CREATIVE);
                            e.setSpawningInstance(level);
                            player.setRespawnPoint(
                                    new Pos(0, 55, 0)
                            );
                        }));

                        JNoise noise = JNoise.newBuilder()
                                .superSimplex()
                                .setFrequency(0.01) // Low frequency for smooth terrain
                                .setSeed(server.getRandom().nextLong())
                                .build();

                        level.setGenerator(unit -> {
                            Point start = unit.absoluteStart();
                            for (int x = 0; x < unit.size().x(); x++) {
                                for (int z = 0; z < unit.size().z(); z++) {
                                    Point bottom = start.add(x, 0, z);

                                    synchronized (noise) { // Synchronization is necessary for JNoise
                                        double height = noise.getNoise(bottom.x(), bottom.z()) * 16;
                                        // * 16 means the height will be between -16 and +16
                                        unit.modifier().fill(bottom, bottom.add(1, 0, 1).withY(height), Block.STONE);
                                    }
                                }
                            }
                        });

                        level.setChunkSupplier(LightingChunk::new);
                        level.setChunkLoader(server.createLoader(id));
                    }
            );

            server.getServerProcess().command()
                    .register(
                            new TransferCommand(),
                            new GameModeCommand(),
                            new SaveAllCommand(server),
                            new TeleportCommand(server)
                    );

            server.start(25565);
        });

    }
}
