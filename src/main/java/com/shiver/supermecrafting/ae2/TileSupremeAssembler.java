package com.shiver.supermecrafting.ae2;

import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;

public class TileSupremeAssembler extends TileEntity implements ITickable {
    private static final int CRAFT_TICKS = 20;

    private final NonNullList<ItemStack> inputs = NonNullList.create();
    private final NonNullList<ItemStack> outputs = NonNullList.create();
    private int remainingTicks;
    private long interfacePos;
    private boolean crafted;

    public boolean isBusy() {
        return remainingTicks > 0 || !outputs.isEmpty();
    }

    public boolean start(TileSupremeInterface owner, NonNullList<ItemStack> taskInputs, NonNullList<ItemStack> taskOutputs) {
        if (isBusy() || taskOutputs.isEmpty()) {
            return false;
        }
        inputs.clear();
        for (ItemStack stack : taskInputs) {
            if (!stack.isEmpty()) {
                inputs.add(stack.copy());
            }
        }
        outputs.clear();
        for (ItemStack stack : taskOutputs) {
            if (!stack.isEmpty()) {
                outputs.add(stack.copy());
            }
        }
        remainingTicks = CRAFT_TICKS;
        interfacePos = owner.getPos().toLong();
        crafted = false;
        markDirty();
        return true;
    }

    @Override
    public void update() {
        if (world == null || world.isRemote || remainingTicks <= 0) {
            return;
        }
        remainingTicks--;
        if (remainingTicks <= 0) {
            crafted = true;
            TileEntity tile = world.getTileEntity(net.minecraft.util.math.BlockPos.fromLong(interfacePos));
            if (tile instanceof TileSupremeInterface && ((TileSupremeInterface) tile).receiveAssemblerOutputs(copyOutputs())) {
                inputs.clear();
                outputs.clear();
                crafted = false;
                markDirty();
            } else if (!(tile instanceof TileSupremeInterface)) {
                dropOutputs();
                inputs.clear();
                outputs.clear();
                crafted = false;
                markDirty();
            } else {
                remainingTicks = 1;
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("RemainingTicks", remainingTicks);
        compound.setLong("InterfacePos", interfacePos);
        compound.setBoolean("Crafted", crafted);
        NBTTagList list = new NBTTagList();
        for (ItemStack stack : inputs) {
            list.appendTag(stack.writeToNBT(new NBTTagCompound()));
        }
        compound.setTag("Inputs", list);
        NBTTagList outputList = new NBTTagList();
        for (ItemStack stack : outputs) {
            outputList.appendTag(stack.writeToNBT(new NBTTagCompound()));
        }
        compound.setTag("Outputs", outputList);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        remainingTicks = compound.getInteger("RemainingTicks");
        interfacePos = compound.getLong("InterfacePos");
        crafted = compound.getBoolean("Crafted");
        inputs.clear();
        NBTTagList list = compound.getTagList("Inputs", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            ItemStack stack = new ItemStack(list.getCompoundTagAt(i));
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }
        outputs.clear();
        NBTTagList outputList = compound.getTagList("Outputs", 10);
        for (int i = 0; i < outputList.tagCount(); i++) {
            ItemStack stack = new ItemStack(outputList.getCompoundTagAt(i));
            if (!stack.isEmpty()) {
                outputs.add(stack);
            }
        }
        if (outputs.isEmpty() && compound.hasKey("Output", 10)) {
            ItemStack oldOutput = new ItemStack(compound.getCompoundTag("Output"));
            if (!oldOutput.isEmpty()) {
                outputs.add(oldOutput);
            }
        }
    }

    public void dropContents() {
        if (world == null) {
            return;
        }
        for (ItemStack stack : inputs) {
            if (!crafted && !stack.isEmpty()) {
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
        if (crafted) {
            for (ItemStack stack : outputs) {
                if (!stack.isEmpty()) {
                    InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
                }
            }
        }
    }

    private void dropOutputs() {
        for (ItemStack stack : outputs) {
            if (!stack.isEmpty()) {
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
    }

    private NonNullList<ItemStack> copyOutputs() {
        NonNullList<ItemStack> copy = NonNullList.create();
        for (ItemStack stack : outputs) {
            if (!stack.isEmpty()) {
                copy.add(stack.copy());
            }
        }
        return copy;
    }
}
