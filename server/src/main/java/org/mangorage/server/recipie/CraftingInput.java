package org.mangorage.server.recipie;

import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class CraftingInput {
    private final List<ItemStack> items;
    private final Type type;
    private final int count;

    public CraftingInput(Type type, AbstractInventory inventory) {
        this.type = type;
        this.items = List.of(
                type.getStacks(inventory)
        );
        this.count = (int) items.stream()
                .filter(s -> s != ItemStack.AIR)
                .count();
    }

    public CraftingInput(Type type, List<ItemStack> stacks) {
        this.type = type;
        this.items = stacks;
        this.count = (int) items.stream()
                .filter(s -> s != ItemStack.AIR)
                .count();
    }

    public List<ItemStack> getStacks() {
        return items;
    }

    public Type getType() {
        return type;
    }

    public ItemStack getItem(int row, int column) {
        if (row >= type.width)
            row = 0;
        if (column >= type.height)
            column = 0;
        return  this.items.get(Math.min(row + column * this.type.width, items.size()));
    }

    /**
     * @return The Amount of Items are in the crafting grid, doesn't count {@link ItemStack#AIR}
     */
    public int getCount() {
        return count;
    }

    public int getHeight() {
        return type.height;
    }

    public int getWidth() {
        return type.width;
    }

    public enum Type {
        CRAFTING_BENCH(9, 1, 10, 0, 3, 3),
        PLAYER(4, 37, 41, 36, 2, 2);

        final int maxSize;
        final int from;
        final int to;
        final int output;
        final int height;
        final int width;

        Type(int maxSize, int from, int to, int output, int height, int width) {
            this.maxSize = maxSize;
            this.from = from;
            this.to = to;
            this.output = output;
            this.height = height;
            this.width = width;
        }

        public ItemStack[] getStacks(AbstractInventory inventory) {
            return Arrays.copyOfRange(inventory.getItemStacks(), from, to);
        }

        public int getOutputSlot() {
            return output;
        }

        public int getMaxSize() {
            return maxSize;
        }
    }
}
