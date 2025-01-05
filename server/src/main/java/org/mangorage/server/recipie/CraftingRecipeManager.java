package org.mangorage.server.recipie;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public final class CraftingRecipeManager implements CraftingRecipe {
    private final List<CraftingRecipe> recipes = new ArrayList<>();

    public void register(CraftingRecipe recipe) {
        this.recipes.add(recipe);
    }

    public void registerShapeless(Material result, int amount, Material... materials) {
        register(
                new ShapelessCraftingRecipe(
                        Arrays.stream(materials)
                                .map(m -> (Predicate<ItemStack>) itemStack -> itemStack.material() == m)
                                .toList(),
                        () -> ItemStack.of(result, amount)
                )
        );
    }

    public CraftingRecipe createShapeless(Material result, int amount, Material... materials) {
        return new ShapelessCraftingRecipe(
                        Arrays.stream(materials)
                                .map(m -> (Predicate<ItemStack>) itemStack -> itemStack.material() == m)
                                .toList(),
                        () -> ItemStack.of(result, amount)
                );
    }

    @Override
    public ItemStack getResult(CraftingInventory inventory) {
        for (CraftingRecipe recipe : recipes) {
            var result = recipe.getResult(inventory);
            if (!result.isAir())
                return result;
        }
        return ItemStack.AIR;
    }
}
