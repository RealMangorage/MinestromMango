package org.mangorage.server.init;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.recipe.Ingredient;
import net.minestom.server.recipe.Recipe;
import net.minestom.server.recipe.RecipeBookCategory;
import net.minestom.server.recipe.RecipeProperty;
import net.minestom.server.recipe.display.RecipeDisplay;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.Difficulty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mangorage.server.Listeners;
import org.mangorage.server.MangoServer;
import org.mangorage.server.commands.GameModeCommand;
import org.mangorage.server.commands.SaveAllCommand;
import org.mangorage.server.commands.TeleportCommand;
import org.mangorage.server.commands.TransferCommand;
import org.mangorage.server.generators.BasicGrassGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        MangoServer.init("MangoServer", server -> {

            //MojangAuth.init();
            MinecraftServer.setBrandName("MangoServer");
            MinecraftServer.setDifficulty(Difficulty.HARD);

            new Listeners();

            server.getBlockManager().register(server.getServerProcess().eventHandler());


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
                            player.getInventory().addItemStack(ItemStack.of(Material.OAK_LOG, 64));
                        }));

                        level.setGenerator(new BasicGrassGenerator(server.getRandom().nextLong()));
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

                        level.setGenerator(new BasicGrassGenerator(server.getRandom().nextLong()));
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
