package org.mangorage.server.recipie;

import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class ShapedRecipe implements Recipe {
    public static Builder create() {
        return new Builder();
    }

    private final Material[][] materials; // Recipe grid (e.g., 2x2 or 3x3)
    private final Supplier<ItemStack> result; // Result of the recipe

    private ShapedRecipe(Material[][] materials, Supplier<ItemStack> result) {
        this.materials = materials;
        this.result = result;
    }

    @Override
    public ItemStack getResult(ItemStack[] stacks) {
        if (materials.length == 2) {
            if (Arrays.stream(stacks).filter(c -> !c.isAir()).count() > 4) {
                return ItemStack.AIR;
            } else {
                return getResultShaped2x2(convertTo2D(stacks));
            }
        } else {
            return getResultShaped3x3(convertTo2D(stacks));
        }
    }

    private ItemStack getResultShaped2x2(ItemStack[][] grid) {
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

    private ItemStack getResultShaped3x3(ItemStack[][] stacks) {
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

    public static class Builder {
        private final Char2ObjectMap<Material> recipeMap = new Char2ObjectArrayMap<>();

        private Builder() {}

        public Builder setChar(char ch, Material material) {
            this.recipeMap.put(ch, material);
            return this;
        }

        public ShapedRecipe build(String pattern, Supplier<ItemStack> output) {
            String[] rows = pattern.split("\n");
            int height = rows.length;
            int width = rows[0].length();
            Material[][] materials = new Material[height][width];

            // Fill the material grid based on the pattern and ingredient map
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    char symbol = rows[i].charAt(j);
                    if (recipeMap.containsKey(symbol)) {
                        materials[i][j] = recipeMap.get(symbol);
                    } else {
                        materials[i][j] = null; // Empty slot
                    }
                }
            }

            // Return the ShapedRecipe
            return new ShapedRecipe(materials, output); // Example result (change as needed)
        }
    }
}