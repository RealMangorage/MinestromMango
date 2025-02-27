package org.mangorage.server.recipie;

import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.item.ItemStack;

import java.util.Arrays;

public final class CraftingInventory {
    private final AbstractInventory inventory;
    private final Type type;

    public CraftingInventory(Type type, AbstractInventory inventory) {
        this.type = type;
        this.inventory = inventory;
    }

    public ItemStack[] getStacks() {
        return type.getStacks(inventory);
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        S_3X3(9, 1, 10, 0),
        S_2X2(4, 37, 41, 36);

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
