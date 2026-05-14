package com.shiver.supermecrafting.furnace;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.Objects;
import java.util.UUID;

/**
 * A formed Supreme Furnace.
 * Identity + geometry are immutable; smelt state is one int (litTime) plus the 3-slot inventory.
 * Layout: 0 = input, 1 = fuel, 2 = output (matches vanilla FurnaceMenu).
 */
public class Region {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_COUNT = 3;

    private final UUID id;
    private final int[] bounds; // minX, minY, minZ, maxX, maxY, maxZ
    private final EnumFacing front;

    private boolean lit;
    private int litTime;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    public Region(UUID id, int[] bounds, EnumFacing front) {
        if (front.getAxis().isVertical()) {
            throw new IllegalArgumentException("front must be horizontal, got " + front);
        }
        this.id = id;
        this.bounds = bounds;
        this.front = front;
    }

    public UUID getId() { return id; }
    public int[] getBounds() { return bounds; }
    public EnumFacing getFront() { return front; }

    public int getMinX() { return bounds[0]; }
    public int getMinY() { return bounds[1]; }
    public int getMinZ() { return bounds[2]; }
    public int getMaxX() { return bounds[3]; }
    public int getMaxY() { return bounds[4]; }
    public int getMaxZ() { return bounds[5]; }

    public boolean isLit() { return lit; }
    public void setLit(boolean lit) { this.lit = lit; }

    public int getLitTime() { return litTime; }
    public void setLitTime(int v) { this.litTime = Math.max(0, v); }

    public NonNullList<ItemStack> getItems() { return items; }

    /** Cube edge length — 32, 64, or 128. */
    public int size() {
        return bounds[3] - bounds[0] + 1;
    }

    /**
     * Items smelted per tick when fully fueled — scales with volume:
     * 32³ → 1, 64³ → 8, 128³ → 64.
     */
    public int throughput() {
        int s = size() / 32;
        return s * s * s;
    }

    public boolean contains(BlockPos pos) {
        return pos.getX() >= bounds[0] && pos.getX() <= bounds[3]
            && pos.getY() >= bounds[1] && pos.getY() <= bounds[4]
            && pos.getZ() >= bounds[2] && pos.getZ() <= bounds[5];
    }

    public NBTTagCompound save() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("id", id.toString());
        tag.setIntArray("bounds", bounds);
        tag.setByte("front", (byte) front.getHorizontalIndex());
        tag.setInteger("litTime", litTime);

        NBTTagList itemsList = new NBTTagList();
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (!items.get(i).isEmpty()) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setByte("Slot", (byte) i);
                items.get(i).writeToNBT(itemTag);
                itemsList.appendTag(itemTag);
            }
        }
        tag.setTag("items", itemsList);
        return tag;
    }

    public static Region load(NBTTagCompound tag) {
        UUID id = UUID.fromString(tag.getString("id"));
        int[] b = tag.getIntArray("bounds");
        EnumFacing front = EnumFacing.byHorizontalIndex(tag.getByte("front"));
        Region r = new Region(id, b, front);
        r.litTime = tag.getInteger("litTime");

        if (tag.hasKey("items", 9)) { // 9 = TAG_LIST
            NBTTagList itemsList = tag.getTagList("items", 10); // 10 = TAG_COMPOUND
            for (int i = 0; i < itemsList.tagCount(); i++) {
                NBTTagCompound itemTag = itemsList.getCompoundTagAt(i);
                int slot = itemTag.getByte("Slot") & 255;
                if (slot >= 0 && slot < SLOT_COUNT) {
                    r.items.set(slot, new ItemStack(itemTag));
                }
            }
        }
        return r;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Region && Objects.equals(id, ((Region) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
