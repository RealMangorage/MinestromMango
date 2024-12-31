package org.mangorage.server.recipie;

import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ShapedCraftingRecipe implements CraftingRecipe {
    public static Builder create() {
        return new Builder();
    }

    private final Material[][] materials; // Recipe grid (e.g., 2x2 or 3x3)
    private final Supplier<ItemStack> result; // Result of the recipe

    private ShapedCraftingRecipe(Material[][] materials, Supplier<ItemStack> result) {
        this.materials = materials;
        this.result = result;
    }

    @Override
    public ItemStack getResult(CraftingInventory inventory) {
        var inv = inventory.getStacks();
        if (inventory.getType() == CraftingInventory.Type.S_2X2) {
            return getResultShaped2x2(convertTo2D(inv));
        } else {
            if (Arrays.stream(inv).filter(i -> !i.isAir()).count() > 4 && materials.length * materials.length == 4) {
                return ItemStack.AIR;
            }
            return getResultShaped3x3(convertTo2D(inv));
        }
    }

    private ItemStack getResultShaped3x3(ItemStack[][] grid) {
        int recipeHeight = materials.length;
        int recipeWidth = materials[0].length;
        int gridHeight = grid.length;
        int gridWidth = grid[0].length;

        // Ensure the grid is at least as large as the recipe
        if (gridHeight < recipeHeight || gridWidth < recipeWidth) {
            return ItemStack.AIR;
        }

        // Slide the recipe over the grid
        for (int startRow = 0; startRow <= gridHeight - recipeHeight; startRow++) {
            for (int startCol = 0; startCol <= gridWidth - recipeWidth; startCol++) {
                if (matches(grid, startRow, startCol)) {
                    return result.get();
                }
            }
        }

        return ItemStack.AIR; // No match found
    }

    private ItemStack getResultShaped2x2(ItemStack[][] stacks) {
        // Check grid dimensions
        if (stacks == null || stacks.length != materials.length || stacks[0].length != materials[0].length) {
            return ItemStack.AIR;
        }

        for (int row = 0; row < materials.length; row++) {
            for (int col = 0; col < materials[row].length; col++) {
                Material expected = materials[row][col];
                ItemStack actual = stacks[row][col];

                if (expected != null) { // Ignore nulls in recipe
                    if (actual == null || actual.material() != expected) {
                        return ItemStack.AIR;
                    }
                }
            }
        }

        return result.get();
    }

    private boolean matches(ItemStack[][] grid, int startRow, int startCol) {
        for (int row = 0; row < materials.length; row++) {
            for (int col = 0; col < materials[row].length; col++) {
                Material expected = materials[row][col];
                ItemStack actual = grid[startRow + row][startCol + col];

                if (expected != null) { // Ignore nulls in the recipe
                    if (actual == null || actual.material() != expected) {
                        return false; // Mismatch
                    }
                }
            }
        }
        return true; // All slots matched
    }

    public static ItemStack[][] convertTo2D(ItemStack[] input) {
        int gridSize = (int) Math.sqrt(input.length);
        if (gridSize * gridSize != input.length) {
            throw new IllegalArgumentException("Input length must form a perfect square.");
        }

        ItemStack[][] result = new ItemStack[gridSize][gridSize];
        for (int i = 0; i < input.length; i++) {
            result[i / gridSize][i % gridSize] = input[i];
        }
        return result;
    }

    public static Material[][] convertTo2D(Material[] input) {
        int gridSize = (int) Math.sqrt(input.length);
        if (gridSize * gridSize != input.length) {
            throw new IllegalArgumentException("Input length must form a perfect square.");
        }

        Material[][] result = new Material[gridSize][gridSize];
        for (int i = 0; i < input.length; i++) {
            result[i / gridSize][i % gridSize] = input[i];
        }
        return result;
    }


    public static class Builder {
        private final Char2ObjectMap<Material> recipeMap = new Char2ObjectArrayMap<>();
        private Material[] materials = new Material[0];

        private Builder() {

        }

        public Builder setIngredient(Material material) {
            this.materials = Arrays.copyOf(materials, materials.length + 1);
            materials[materials.length - 1] = material;
            return this;
        }

        public Builder skip() {
            return setIngredient(Material.AIR);
        }

        public Builder buildAndRegisterFork(Supplier<ItemStack> output, Consumer<CraftingRecipe> craftingRecipe) {

            return this;
        }

        public CraftingRecipe build(Supplier<ItemStack> output) {
            // Return the ShapedRecipe
            return new ShapedCraftingRecipe(materials.length <= 4 ? convertTo2D(Arrays.copyOf(materials, 4)) : convertTo2D(Arrays.copyOf(materials, 9)), output); // Example result (change as needed)
        }
    }
}