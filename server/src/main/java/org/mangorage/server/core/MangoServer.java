package org.mangorage.server.core;

import me.lucko.spark.minestom.SparkMinestom;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerProcess;
import net.minestom.server.command.builder.Command;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.mangorage.server.misc.PlayerUtil;
import org.mangorage.server.recipie.CraftingRecipeManager;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MangoServer {
    private static final Executor spinnner = Executors.newCachedThreadPool();

    public static void init(String id, Consumer<MangoServer> serverConsumer) {
        var inst = new MangoServer(id);
        serverConsumer.accept(inst);
    }


    private final Random random = new Random();
    private final Map<NamespaceID, InstanceContainer> levels = new HashMap<>();
    private final CraftingRecipeManager craftingRecipeManager = new CraftingRecipeManager();
    private final String id;
    private final Path serverPath;
    private final MinecraftServer server;
    private final ServerProcess serverProcess;
    private final InstanceManager instanceManager;
    private final MangoBlockManager manager;

    private MangoServer(String id) {
        this.id = id;
        this.serverPath = Path.of("data/server/%s".formatted(id));
        this.server = MinecraftServer.init();
        this.serverProcess = MinecraftServer.process();
        this.instanceManager = serverProcess.instance();
        this.manager = new MangoBlockManager(serverProcess);

        Path directory = Path.of("spark");
        SparkMinestom.builder(directory)
                .commands(true) // enables registration of Spark commands
                .permissionHandler((sender, permission) -> true) // allows all command senders to execute all commands
                .enable();
    }

    public Path getServerPath() {
        return serverPath;
    }

    public void setOnPlayerJoin(Consumer<AsyncPlayerConfigurationEvent> event) {
        serverProcess.eventHandler().addListener(AsyncPlayerConfigurationEvent.class, event);
    }

    public void saveAll()  {
        saveAll(s -> {});
    }

    public void saveAll(Consumer<String> consumer) {
        levels.forEach((k, l) -> {
            l.saveChunksToStorage();
            consumer.accept(k.asMinimalString());
        });
        serverProcess
                .connection()
                .getOnlinePlayers()
                .forEach(plr -> {
                    PlayerUtil.serialize(this, "players", plr);
                });
    }

    public @Nullable InstanceContainer getLevel(NamespaceID id) {
        return levels.get(id);
    }

    public void registerCommands(Command... commands) {
        getServerProcess()
                .command()
                .register(commands);
    }

    public List<String> getLevelList() {
        return levels
                .keySet()
                .stream()
                .map(NamespaceID::asString)
                .toList();
    }

    public void createLevel(NamespaceID id, BiConsumer<NamespaceID, InstanceContainer> consumer) {
        createLevel(id, DimensionType.OVERWORLD, consumer);
    }

    public void createLevel(NamespaceID id, DynamicRegistry.Key<DimensionType> dimensionType, BiConsumer<NamespaceID, InstanceContainer> consumer) {
        InstanceContainer container = instanceManager.createInstanceContainer(dimensionType);
        consumer.accept(id, container);
        levels.computeIfAbsent(id, k -> container);
    }

    public void start(int port) {
        start("0.0.0.0", port);
    }

    public void start(String ip, int port) {
        spinnner.execute(() ->  server.start(ip, port));
    }

    public MangoBlockManager getBlockManager() {
        return manager;
    }

    public CraftingRecipeManager getCraftingRecipeManager() {
        return craftingRecipeManager;
    }

    public AnvilLoader createLoader(NamespaceID id) {
        return new AnvilLoader(
                getServerPath()
                        .resolve("worlds")
                        .resolve(id.namespace())
                        .resolve(id.path())
        );
    }

    public ServerProcess getServerProcess() {
        return serverProcess;
    }

    public Random getRandom() {
        return random;
    }
}
