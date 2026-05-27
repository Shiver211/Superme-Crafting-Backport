package com.shiver.supremecrafting.furnace;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

public class HatchItemHandler implements IItemHandler {
    private final World world;
    private final BlockPos pos;
    private final HatchRole role;

    public HatchItemHandler(World world, BlockPos pos, HatchRole role) {
        this.world = world;
        this.pos = pos;
        this.role = role;
    }

    private Region region() {
        return MultiblockRegions.get(world).findContaining(pos);
    }

    private int slot() {
        if (role == HatchRole.INPUT) return Region.SLOT_INPUT;
        if (role == HatchRole.FUEL) return Region.SLOT_FUEL;
        return Region.SLOT_OUTPUT;
    }

    @Override public int getSlots() { return 1; }

    @Override
    public ItemStack getStackInSlot(int slot) {
        Region r = region();
        return r == null ? ItemStack.EMPTY : r.items().get(slot());
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        Region r = region();
        if (r == null || role == HatchRole.OUTPUT || stack.isEmpty()) return stack;
        if (role == HatchRole.FUEL && TileEntityFurnace.getItemBurnTime(stack) <= 0) return stack;
        ItemStack target = r.items().get(slot());
        if (target.isEmpty()) {
            int move = Math.min(stack.getMaxStackSize(), stack.getCount());
            if (!simulate) {
                ItemStack copy = stack.copy();
                copy.setCount(move);
                r.items().set(slot(), copy);
                MultiblockRegions.get(world).markDirty();
            }
            ItemStack remain = stack.copy();
            remain.shrink(move);
            return remain;
        }
        if (!ItemStack.areItemsEqual(target, stack) || !ItemStack.areItemStackTagsEqual(target, stack)) return stack;
        int move = Math.min(target.getMaxStackSize() - target.getCount(), stack.getCount());
        if (move <= 0) return stack;
        if (!simulate) {
            target.grow(move);
            MultiblockRegions.get(world).markDirty();
        }
        ItemStack remain = stack.copy();
        remain.shrink(move);
        return remain;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        Region r = region();
        if (r == null || role != HatchRole.OUTPUT || amount <= 0) return ItemStack.EMPTY;
        ItemStack target = r.items().get(Region.SLOT_OUTPUT);
        if (target.isEmpty()) return ItemStack.EMPTY;
        ItemStack out = target.copy();
        out.setCount(Math.min(amount, target.getCount()));
        if (!simulate) {
            target.shrink(out.getCount());
            MultiblockRegions.get(world).markDirty();
        }
        return out;
    }

    @Override public int getSlotLimit(int slot) { return 64; }
}
