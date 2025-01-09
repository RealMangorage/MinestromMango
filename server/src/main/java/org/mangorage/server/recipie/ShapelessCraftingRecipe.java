package org.mangorage.server.recipie;

import net.minestom.server.item.ItemStack;

import java.util.ArrayList;
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

        // Ensure no AIR is present in the ingredients
        for (Ingredient ingredient : ingredients) {
            if (ingredient.hasAir()) {
                throw new IllegalArgumentException("Ingredients cannot include AIR");
            }
        }
    }

    @Override
    public ItemStack getResult(CraftingInput input) {
        List<ItemStack> inputStacks = new ArrayList<>(input.getStacks()); // Make a mutable copy of the input stacks
        List<Ingredient> unmatchedIngredients = new ArrayList<>(ingredients); // Track unmatched ingredients

        // Attempt to match each input stack to an ingredient
        for (ItemStack stack : inputStacks) {
            if (stack.isAir()) continue; // Skip AIR stacks

            boolean matched = unmatchedIngredients.removeIf(ingredient -> ingredient.is(stack));
            if (!matched) {
                return ItemStack.AIR; // If a stack doesn't match any ingredient, return AIR
            }
        }

        // If all ingredients are matched, return the result
        return unmatchedIngredients.isEmpty() ? result.get() : ItemStack.AIR;
    }
}