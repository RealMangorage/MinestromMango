package org.mangorage.servertest.core;

import net.goldenstack.loot.LootContext;
import net.goldenstack.loot.LootEntry;
import net.goldenstack.loot.LootFunction;
import net.goldenstack.loot.LootPredicate;
import net.goldenstack.loot.LootTable;
import net.goldenstack.loot.Trove;
import net.goldenstack.loot.util.VanillaInterface;
import net.goldenstack.loot.util.nbt.NBTReference;
import net.goldenstack.loot.util.nbt.NBTUtils;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponent;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.gamedata.tags.TagManager;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.TransactionOption;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.network.packet.server.play.WorldBorderSizePacket;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mangorage.server.core.MangoServer;
import org.mangorage.server.misc.Util;
import org.mangorage.server.recipie.CraftingInput;
import org.mangorage.server.recipie.CraftingRecipeManager;
import org.mangorage.server.misc.PlayerUtil;
import org.mangorage.servertest.core.nodes.DefaultBlockBreakHandler;
import org.mangorage.servertest.core.nodes.DefaultInventoryHandler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.UnaryOperator;

public final class Listeners {
    private final CraftingRecipeManager manager;

    private static final Map<NamespaceID, LootTable> tables = LootTableHelper.loadInternalLootTable( "loot_tables/blocks");

    public Listeners(MangoServer server) {
        this.manager = server.getCraftingRecipeManager();
        GlobalEventHandler handler = MinecraftServer.getGlobalEventHandler();

        handler.addChild(DefaultBlockBreakHandler.register(server));
        handler.addChild(DefaultInventoryHandler.register(server));

        handler.addListener(
                PlayerDisconnectEvent.class,
                event -> {
                    PlayerUtil.serialize(server, "players", event.getPlayer());
                }
        );
    }




}
