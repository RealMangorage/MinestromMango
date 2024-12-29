package org.mangorage.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.recipe.Ingredient;
import net.minestom.server.recipe.Recipe;
import net.minestom.server.recipe.RecipeBookCategory;
import net.minestom.server.recipe.RecipeProperty;
import net.minestom.server.recipe.display.RecipeDisplay;
import net.minestom.server.recipe.display.SlotDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mangorage.server.recipie.RecipeManager;
import org.mangorage.server.recipie.ShapedRecipe;
import org.mangorage.server.recipie.ShapelessRecipe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class Listeners {
    private final RecipeManager manager = new RecipeManager();


    public ItemStack[] getItems(AbstractInventory inv, boolean is3x3) {
        if (!is3x3) {
            return Arrays.copyOfRange(inv.getItemStacks(), 37, 41);
        } else {
            return Arrays.copyOfRange(inv.getItemStacks(), 1, 10);
        }
    }

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
        var result = manager.getResult(getItems(inventory, is3x3));
        inventory.setItemStack(is3x3 ? 0 : 36, result, true);
        return result;
    }

    public Listeners() {
        manager.register(
                new ShapelessRecipe(
                        List.of(
                                Material.OAK_LOG
                        ),
                        () -> ItemStack.of(Material.OAK_PLANKS, 4)
                )
        );

        manager.register(
                new ShapelessRecipe(
                        List.of(
                                Material.WHITE_WOOL,
                                Material.RED_DYE
                        ),
                        () -> ItemStack.of(Material.RED_WOOL)
                )
        );

        manager.register(
                ShapedRecipe.create()
                        .setChar('X', Material.OAK_PLANKS)
                        .build(
                               """
                               XX
                               XX
                               """,
                                () -> ItemStack.of(Material.OAK_PLANKS, 4)
                        )
        );

        manager.register(
                ShapedRecipe.create()
                        .setChar('I', Material.IRON_INGOT)
                        .setChar('A', Material.AIR)
                        .setChar('S', Material.STICK)
                        .build(
                                """
                                III
                                ASA
                                ASA
                                """,
                                () -> ItemStack.of(Material.IRON_PICKAXE)
                        )
        );

        manager.register(
                ShapedRecipe.create()
                        .setChar('I', Material.IRON_INGOT)
                        .setChar('A', Material.AIR)
                        .build(
                                """
                                IA
                                AI
                                """,
                                () -> ItemStack.of(Material.SHEARS)
                        )
        );

        MinecraftServer.process().recipe()
                .addRecipe(
                        new Recipe() {
                            @Override
                            public @Nullable String recipeBookGroup() {
                                return "test";
                            }

                            @Override
                            public @Nullable RecipeBookCategory recipeBookCategory() {
                                return RecipeBookCategory.CRAFTING_MISC;
                            }

                            @Override
                            public @Nullable List<Ingredient> craftingRequirements() {
                                return List.of(
                                        new Ingredient(
                                                List.of(
                                                        Material.OAK_LOG
                                                )
                                        )
                                );
                            }

                            @Override
                            public @NotNull List<RecipeDisplay> createRecipeDisplays() {
                                return List.of(
                                        new RecipeDisplay.CraftingShapeless(
                                                List.of(
                                                        new SlotDisplay.Item(Material.OAK_LOG)
                                                ),
                                                new SlotDisplay.ItemStack(ItemStack.of(Material.OAK_PLANKS, 4)),
                                                new SlotDisplay.Item(Material.CRAFTING_TABLE)
                                        )
                                );
                            }

                            @Override
                            public @NotNull Map<RecipeProperty, List<Material>> itemProperties() {
                                Map<RecipeProperty, List<Material>> l = new HashMap<>();
                                l.put(RecipeProperty.FURNACE_INPUT, List.of(Material.RAW_IRON));
                                return l;
                            }
                        }
                );

        GlobalEventHandler handler = MinecraftServer.getGlobalEventHandler();

        handler.addListener(PlayerBlockBreakEvent.class, event -> {
            event.getPlayer().getInventory().addItemStack(
                   ItemStack.of(
                           Material.fromNamespaceId(event.getBlock().namespace()),
                           1
                   )
           );
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
