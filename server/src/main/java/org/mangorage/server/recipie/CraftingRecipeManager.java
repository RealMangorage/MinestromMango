package org.mangorage.server.recipie;

import net.minestom.server.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class CraftingRecipeManager implements CraftingRecipe {
    private final List<CraftingRecipe> recipes = new ArrayList<>();

    public void register(CraftingRecipe recipe) {
        this.recipes.add(recipe);
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
