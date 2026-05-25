package com.shiver.supermecrafting.furnace;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;

public final class FurnaceTick {
    public static final int FUEL_PER_ITEM = 800;

    private FurnaceTick() {
    }

    public static void tickAll(World world) {
        MultiblockRegions data = MultiblockRegions.get(world);
        for (Region region : new java.util.ArrayList<>(data.all())) {
            tick(world, data, region);
        }
    }

    private static void tick(World world, MultiblockRegions data, Region region) {
        ItemStack input = region.items().get(Region.SLOT_INPUT);
        ItemStack result = input.isEmpty() ? ItemStack.EMPTY : FurnaceRecipes.instance().getSmeltingResult(input);
        boolean cooked = false;
        boolean changed = false;
        if (!result.isEmpty() && outputRoom(region, result) > 0) {
            int need = region.throughput() * FUEL_PER_ITEM;
            while (region.litTime() < need && !region.items().get(Region.SLOT_FUEL).isEmpty()) {
                ItemStack fuel = region.items().get(Region.SLOT_FUEL);
                int burn = TileEntityFurnace.getItemBurnTime(fuel);
                if (burn <= 0) break;
                fuel.shrink(1);
                region.setLitTime(region.litTime() + burn);
                changed = true;
            }
            int batch = Math.min(region.throughput(),
                    Math.min(region.litTime() / FUEL_PER_ITEM, Math.min(input.getCount(), outputRoom(region, result))));
            if (batch > 0) {
                ItemStack out = region.items().get(Region.SLOT_OUTPUT);
                if (out.isEmpty()) {
                    ItemStack copy = result.copy();
                    copy.setCount(batch);
                    region.items().set(Region.SLOT_OUTPUT, copy);
                } else {
                    out.grow(batch);
                }
                input.shrink(batch);
                region.setLitTime(region.litTime() - batch * FUEL_PER_ITEM);
                cooked = true;
                changed = true;
            }
        }
        if (region.isLit() != cooked) {
            region.setLit(cooked);
            MultiblockSync.add(world, region);
            changed = true;
        }
        if (changed) data.markDirty();
    }

    private static int outputRoom(Region region, ItemStack result) {
        ItemStack out = region.items().get(Region.SLOT_OUTPUT);
        if (out.isEmpty()) return result.getMaxStackSize();
        if (!ItemStack.areItemsEqual(out, result) || !ItemStack.areItemStackTagsEqual(out, result)) return 0;
        return out.getMaxStackSize() - out.getCount();
    }
}
