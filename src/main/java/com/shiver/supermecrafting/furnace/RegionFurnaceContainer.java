package com.shiver.supermecrafting.furnace;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;

import java.util.UUID;

/**
 * Adapts a Region's 3-slot inventory to IInventory so vanilla ContainerFurnace
 * can drive it as if it were a vanilla furnace TE.
 *
 * IInventory in 1.12.2 includes getField/setField/getFieldCount for GUI data:
 * [0] litTime, [1] litDuration, [2] cookTime, [3] cookTimeTotal
 */
public class RegionFurnaceContainer implements IInventory {
    private final WorldServer level;
    private final UUID regionId;

    public RegionFurnaceContainer(WorldServer level, UUID regionId) {
        this.level = level;
        this.regionId = regionId;
    }

    private Region region() {
        return MultiblockRegions.get(level).byId(regionId);
    }

    // --- IInventory ---

    @Override
    public int getSizeInventory() {
        return Region.SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        Region r = region();
        if (r == null) return true;
        for (ItemStack s : r.getItems()) if (!s.isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        Region r = region();
        return r == null ? ItemStack.EMPTY : r.getItems().get(slot);
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        Region r = region();
        if (r == null) return ItemStack.EMPTY;
        ItemStack stack = r.getItems().get(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (stack.getCount() <= amount) {
            r.getItems().set(slot, ItemStack.EMPTY);
            markDirty();
            return stack;
        }
        ItemStack split = stack.splitStack(amount);
        if (stack.isEmpty()) r.getItems().set(slot, ItemStack.EMPTY);
        markDirty();
        return split;
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        Region r = region();
        if (r == null) return ItemStack.EMPTY;
        ItemStack stack = r.getItems().get(slot);
        r.getItems().set(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        Region r = region();
        if (r == null) return;
        r.getItems().set(slot, stack);
        if (stack.getCount() > getInventoryStackLimit()) stack.setCount(getInventoryStackLimit());
        markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return region() != null;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == Region.SLOT_OUTPUT) return false;
        if (slot == Region.SLOT_INPUT) return true;
        // 燃料槽：允许有燃烧时间的物品，以及空桶（用于湿海绵技巧）
        if (stack.getItem() == net.minecraft.init.Items.BUCKET) return true;
        return net.minecraft.tileentity.TileEntityFurnace.getItemBurnTime(stack) > 0;
    }

    @Override
    public int getField(int id) {
        Region r = region();
        if (r == null) return 0;
        switch (id) {
            case 0: return Math.min(r.getLitTime(), FurnaceTick.FUEL_PER_ITEM);
            case 1: return FurnaceTick.FUEL_PER_ITEM;
            case 2: return 0;
            case 3: return 1;
            default: return 0;
        }
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 4;
    }

    @Override
    public void markDirty() {
        MultiblockRegions.get(level).markDirty();
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public void clear() {
        Region r = region();
        if (r == null) return;
        r.getItems().clear();
        markDirty();
    }

    @Override
    public String getName() {
        return "container.supreme_crafting.supreme_furnace";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(getName());
    }
}
