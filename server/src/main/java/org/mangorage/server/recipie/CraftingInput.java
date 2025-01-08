package org.mangorage.server.recipie;

import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.item.ItemStack;

import java.util.Arrays;

public final class CraftingInput {
    private final AbstractInventory inventory;
    private final Type type;
    private final int count;

    public CraftingInput(Type type, AbstractInventory inventory) {
        this.type = type;
        this.inventory = inventory;
        this.count = (int) Arrays.stream(getStacks())
                .filter(stack -> !stack.isAir())
                .count();
    }

    public ItemStack[] getStacks() {
        return type.getStacks(inventory);
    }

    public Type getType() {
        return type;
    }

    /**
     * @return The Amount of Items are in the crafting grid, doesn't count {@link ItemStack#AIR}
     */
    public int getCount() {
        return count;
    }

    public enum Type {
        CRAFTING_BENCH(9, 1, 10, 0),
        PLAYER(4, 37, 41, 36);

        final int maxSize;
        final int from;
        final int to;
        final int output;

        Type(int maxSize, int from, int to, int output) {
            this.maxSize = maxSize;
            this.from = from;
            this.to = to;
            this.output = output;
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
