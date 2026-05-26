package com.shiver.supermecrafting.ae2;

import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class SupremeCraftingPatternDetails implements ICraftingPatternDetails {
    private final ItemStack pattern;
    private final IAEItemStack[] inputs;
    private final IAEItemStack[] condensedInputs;
    private final IAEItemStack[] outputs;
    private int priority;

    public SupremeCraftingPatternDetails(ItemStack pattern) {
        this.pattern = pattern.copy();
        List<ItemStack> inputStacks = SupremePatternData.readInputs(pattern);
        this.inputs = new IAEItemStack[inputStacks.size()];
        this.condensedInputs = new IAEItemStack[inputStacks.size()];
        for (int i = 0; i < inputStacks.size(); i++) {
            IAEItemStack stack = AEApi.instance().storage()
                    .getStorageChannel(IItemStorageChannel.class).createStack(inputStacks.get(i));
            this.inputs[i] = stack;
            this.condensedInputs[i] = stack == null ? null : stack.copy();
        }
        IAEItemStack output = AEApi.instance().storage()
                .getStorageChannel(IItemStorageChannel.class).createStack(SupremePatternData.readOutput(pattern));
        this.outputs = output == null ? new IAEItemStack[0] : new IAEItemStack[] { output };
    }

    @Override public ItemStack getPattern() { return pattern.copy(); }
    @Override public boolean isValidItemForSlot(int slotIndex, ItemStack itemStack, World world) { return true; }
    @Override public boolean isCraftable() { return false; }
    @Override public IAEItemStack[] getInputs() { return copy(inputs); }
    @Override public IAEItemStack[] getCondensedInputs() { return copy(condensedInputs); }
    @Override public IAEItemStack[] getCondensedOutputs() { return copy(outputs); }
    @Override public IAEItemStack[] getOutputs() { return copy(outputs); }
    @Override public boolean canSubstitute() { return false; }
    @Override public ItemStack getOutput(InventoryCrafting craftingInv, World world) { return SupremePatternData.readOutput(pattern); }
    @Override public int getPriority() { return priority; }
    @Override public void setPriority(int priority) { this.priority = priority; }

    private static IAEItemStack[] copy(IAEItemStack[] stacks) {
        IAEItemStack[] out = new IAEItemStack[stacks.length];
        for (int i = 0; i < stacks.length; i++) {
            out[i] = stacks[i] == null ? null : stacks[i].copy();
        }
        return out;
    }
}
