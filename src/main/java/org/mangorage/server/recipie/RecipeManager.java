package org.mangorage.server.recipie;

import net.minestom.server.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class RecipeManager implements Recipe {
    private final List<Recipe> recipes = new ArrayList<>();

    public void register(Recipe recipe) {
        this.recipes.add(recipe);
    }

    @Override
    public ItemStack getResult(ItemStack[] stacks) {
        for (Recipe recipe : recipes) {
            var result = recipe.getResult(stacks);
            if (!result.isAir())
                return result;
        }
        return ItemStack.AIR;
    }
}
