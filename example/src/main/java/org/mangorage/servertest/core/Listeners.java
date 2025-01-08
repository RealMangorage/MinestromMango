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

    public void shrinkCrafting(AbstractInventory inv, boolean is3x3) {
        int from = is3x3 ? 1 : 37;
        int to = is3x3 ? 9 : 40;
        for (int i = from; i <= to; i++) {
            var item = inv.getItemStack(i);
            if (item.isAir() || item.amount() == 1) {
                inv.setItemStack(i, ItemStack.AIR, true);
            } else {
                inv.setItemStack(
                        i,
                        item.withAmount(
                                item.amount() - 1
                        ),
                        true
                );
            }
        }
        inv.update();
    }

    public ItemStack updateCraftingView(AbstractInventory inventory, boolean is3x3) {
        var type = is3x3 ? CraftingInput.Type.CRAFTING_BENCH : CraftingInput.Type.PLAYER;
        var result = manager.getResult(new CraftingInput(type, inventory));
        inventory.setItemStack(type.getOutputSlot(), result, true);
        return result;
    }

    private static final Map<NamespaceID, LootTable> tables = LootTableHelper.loadInternalLootTable( "loot_tables/blocks");

    public Listeners(MangoServer server) {
        this.manager = server.getCraftingRecipeManager();
        GlobalEventHandler handler = MinecraftServer.getGlobalEventHandler();


        handler.addListener(PickupItemEvent.class, event -> {
            var entity = event.getEntity();
            if (entity instanceof Player player) {
                var ret = player.getInventory().addItemStacks(List.of(event.getItemStack()), TransactionOption.ALL);
                if (ret.stream().anyMatch(i -> i != ItemStack.AIR))
                    event.setCancelled(true);
            } else {
                event.setCancelled(true);
            }
        });

        handler.addListener(ItemDropEvent.class, event -> {
            Player player = event.getPlayer();
            ItemStack droppedItem = event.getItemStack();

            ItemEntity itemEntity = new ItemEntity(droppedItem);
            itemEntity.setPickupDelay(500, TimeUnit.MILLISECOND);
            itemEntity.setInstance(player.getInstance());
            itemEntity.teleport(player.getPosition().add(0, 1.5f, 0));

            Vec velocity = player.getPosition().direction().mul(6);
            itemEntity.setVelocity(velocity);
        });

        handler.addListener(PlayerBlockBreakEvent.class, event -> {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;

            var material = Material.fromNamespaceId(event.getBlock().namespace());
            if (material != null) {
                var held = event.getPlayer().getItemInMainHand();
                var enchs = held.get(ItemComponent.ENCHANTMENTS);
                var tool = held.get(ItemComponent.TOOL);

                if (tool.isCorrectForDrops(event.getBlock())) {
                    Map<LootContext.Key<?>, Object> l = new HashMap<>();
                    l.put(LootContext.TOOL, event.getPlayer().getItemInMainHand());
                    l.put(LootContext.BLOCK_STATE, event.getBlock());
                    l.put(LootContext.RANDOM, server.getRandom());

                    if (enchs.has(Enchantment.FORTUNE)) {
                        l.put(LootContext.ENCHANTMENT_ACTIVE, false);
                        l.put(LootContext.ENCHANTMENT_LEVEL, enchs.level(Enchantment.FORTUNE));

                    }

                    tables.get(event.getBlock().namespace()).blockDrop(
                            LootContext.from(
                                    VanillaInterface.defaults(),
                                    l
                            ),
                            event.getInstance(),
                            event.getBlockPosition()
                    );
                }

                if (held.has(ItemComponent.DAMAGE)) {
                    var maxDmg = held.get(ItemComponent.MAX_DAMAGE);
                    var dmg = held.get(ItemComponent.DAMAGE) + tool.damagePerBlock();

                    if (dmg >= maxDmg) {
                        event.getPlayer().setItemInMainHand(ItemStack.AIR);
                        event.getPlayer().playSound(
                                Sound.sound()
                                        .type(SoundEvent.ENTITY_ITEM_BREAK)
                                        .build()
                        );
                    } else {
                        event.getPlayer().setItemInMainHand(
                                held.with(ItemComponent.DAMAGE, dmg)
                        );
                    }
                }

            }
        });

        handler.addListener(InventoryPreClickEvent.class, event -> {
            if (event.getClickType() == ClickType.SHIFT_CLICK)
                event.setCancelled(true);
        });

        handler.addListener(InventoryCloseEvent.class, event -> {
            if (event.getInventory() instanceof Inventory inventory) {
                for (@NotNull ItemStack stack : inventory.getItemStacks()) {
                    event.getPlayer().getInventory().addItemStack(stack);
                }
            }
        });

        handler.addListener(
                PlayerDisconnectEvent.class,
                event -> {
                    PlayerUtil.serialize(server, "players", event.getPlayer());
                }
        );

        handler.addListener(InventoryClickEvent.class, event -> {
            // 37 -> 40

            // 36 Output
            if (event.getInventory() instanceof PlayerInventory playerInventory) {
                if (event.getSlot() >= 37 && event.getSlot() <= 40) {
                    updateCraftingView(playerInventory, false);
                } else if (event.getSlot() == 36 && !event.getClickedItem().isAir()) {
                    var result = updateCraftingView(playerInventory, false); // Verify
                    if (event.getCursorItem().material() != result.material() && !event.getCursorItem().isAir()) {
                        playerInventory.setItemStack(36, event.getClickedItem(), true);
                        playerInventory.setCursorItem(event.getCursorItem());
                        return;
                    } else if (!event.getCursorItem().isAir()) {
                        if ( (event.getCursorItem().amount() + result.amount()) > result.material().maxStackSize()) {
                            playerInventory.setItemStack(36, event.getClickedItem(), true);
                            playerInventory.setCursorItem(event.getCursorItem());
                            return;
                        } else {
                            playerInventory.setCursorItem(
                                    ItemStack.of(
                                            result.material(),
                                            event.getCursorItem().amount() + result.amount()
                                    )
                            );
                        }
                    }
                    shrinkCrafting(playerInventory, false);
                    playerInventory.setItemStack(36, ItemStack.AIR, true);
                    updateCraftingView(playerInventory, false);
                }
            } else if (event.getInventory() instanceof Inventory inventory) {
                if (inventory.getInventoryType().equals(InventoryType.CRAFTING)) {
                    if (event.getSlot() != 0) {
                        updateCraftingView(inventory, true);
                    } else if (event.getSlot() == 0 && !event.getClickedItem().isAir()) {
                        var result = updateCraftingView(inventory, true); // Verify
                        if (event.getCursorItem().material() != result.material() && !event.getCursorItem().isAir()) {
                            inventory.setItemStack(0, event.getClickedItem(), true);
                            inventory.setCursorItem(event.getPlayer(), event.getCursorItem());
                            return;
                        } else if (!event.getCursorItem().isAir()) {
                            if ( (event.getCursorItem().amount() + result.amount()) > result.material().maxStackSize()) {
                                inventory.setItemStack(0, event.getClickedItem(), true);
                                inventory.setCursorItem(event.getPlayer(), event.getCursorItem());
                                return;
                            } else {
                                inventory.setCursorItem(
                                        event.getPlayer(),
                                        ItemStack.of(
                                                result.material(),
                                                event.getCursorItem().amount() + result.amount()
                                        )
                                );
                            }
                        }
                        shrinkCrafting(inventory, true);
                        inventory.setItemStack(0, ItemStack.AIR, true);
                        updateCraftingView(inventory, true);
                    }
                }
            }
        });
    }




}
