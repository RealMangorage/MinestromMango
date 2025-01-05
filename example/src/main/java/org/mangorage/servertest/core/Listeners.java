package org.mangorage.servertest.core;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.mangorage.server.core.MangoServer;
import org.mangorage.server.recipie.CraftingInventory;
import org.mangorage.server.recipie.CraftingRecipeManager;
import org.mangorage.server.recipie.ShapedCraftingRecipe;
import org.mangorage.server.recipie.ShapelessCraftingRecipe;
import org.mangorage.server.misc.PlayerUtil;

import java.util.List;


public final class Listeners {
    private final CraftingRecipeManager manager = new CraftingRecipeManager();


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
        var type = is3x3 ? CraftingInventory.Type.S_3X3 : CraftingInventory.Type.S_2X2;
        var result = manager.getResult(new CraftingInventory(type, inventory));
        inventory.setItemStack(type.getOutputSlot(), result, true);
        return result;
    }

    public Listeners(MangoServer server) {

        manager.register(
                ShapedCraftingRecipe.create()
                        .setIngredient(Material.IRON_INGOT).setIngredient(Material.IRON_INGOT).setIngredient(Material.IRON_INGOT)
                        .skip().setIngredient(Material.STICK).skip()
                        .skip().setIngredient(Material.STICK).skip()
                        .build(() -> ItemStack.of(Material.IRON_PICKAXE))
        );

        manager.register(
                ShapedCraftingRecipe.create()
                        .setIngredient(Material.OAK_PLANKS).setIngredient(Material.OAK_PLANKS)
                        .setIngredient(Material.OAK_PLANKS).setIngredient(Material.OAK_PLANKS)
                        .build(() -> ItemStack.of(Material.CRAFTING_TABLE))
        );


        manager.register(
                new ShapelessCraftingRecipe(
                        List.of(
                                s -> s.material() == Material.WHITE_WOOL,
                                s -> s.material() == Material.RED_DYE
                        ),
                        () -> ItemStack.of(Material.RED_WOOL, 1)
                )
        );

        GlobalEventHandler handler = MinecraftServer.getGlobalEventHandler();

        handler.addListener(PlayerBlockBreakEvent.class, event -> {
            var material = Material.fromNamespaceId(event.getBlock().namespace());
            if (material != null) {
                event.getPlayer().getInventory().addItemStack(
                        ItemStack.of(
                                material,
                                1
                        )
                );
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
                } else if (event.getSlot() == 36) {
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
                    } else if (event.getSlot() == 0) {
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
