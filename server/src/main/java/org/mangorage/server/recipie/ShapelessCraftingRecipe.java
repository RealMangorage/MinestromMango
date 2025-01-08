package org.mangorage.server.recipie;

import net.minestom.server.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

public class ShapelessCraftingRecipe implements CraftingRecipe {
    private final List<Ingredient> ingredients;
    private final Supplier<ItemStack> result;
    private final int height;
    private final int width;

    public ShapelessCraftingRecipe(List<Ingredient> ingredients, Supplier<ItemStack> result, int height, int width) {
        this.ingredients = List.copyOf(ingredients);
        this.result = result;
        this.height = height;
        this.width = width;
    }


    @Override
    public ItemStack getResult(CraftingInput input) {
        if (input.getHeight() < height || input.getWidth() < width || input.getCount() == 0 || input.getCount() > ingredients.size())
            return ItemStack.AIR;

        int matches = 0;
        int amount = 0;

        for (ItemStack stack : input.getStacks()) {
            for (Ingredient ingredient : ingredients) {
                if (ingredient.is(stack))
                    matches++;
            }
            if (!stack.isAir())
                amount++;
        }

        return matches == amount ? result.get() : ItemStack.AIR;
    }
}
