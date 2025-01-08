package org.mangorage.server.recipie;

import net.minestom.server.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

public class ShapelessCraftingRecipe implements CraftingRecipe {
    private final List<Ingredient> ingredients;
    private final Supplier<ItemStack> result;

    public ShapelessCraftingRecipe(List<Ingredient> ingredients, Supplier<ItemStack> result) {
        this.ingredients = List.copyOf(ingredients);
        this.result = result;
    }

    @Override
    public ItemStack getResult(CraftingInput inventory) {
        int matches = 0;
        int amount = 0;
        for (ItemStack stack : inventory.getStacks()) {
            for (Ingredient ingredient : ingredients) {
                if (ingredient.is(stack))
                    matches++;
            }
            if (!stack.isAir())
                amount++;
        }

        if (matches == 0 || matches != amount || matches > ingredients.size())
            return ItemStack.AIR;

        return result.get();
    }
}
