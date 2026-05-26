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
    private ItemStack output = ItemStack.EMPTY;
    private int remainingTicks;
    private long interfacePos;

    public boolean isBusy() {
        return remainingTicks > 0 || !output.isEmpty();
    }

    public boolean start(TileSupremeInterface owner, NonNullList<ItemStack> taskInputs, ItemStack taskOutput) {
        if (isBusy() || taskOutput.isEmpty()) {
            return false;
        }
        inputs.clear();
        for (ItemStack stack : taskInputs) {
            if (!stack.isEmpty()) {
                inputs.add(stack.copy());
            }
        }
        output = taskOutput.copy();
        remainingTicks = CRAFT_TICKS;
        interfacePos = owner.getPos().toLong();
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
            TileEntity tile = world.getTileEntity(net.minecraft.util.math.BlockPos.fromLong(interfacePos));
            if (tile instanceof TileSupremeInterface && ((TileSupremeInterface) tile).receiveAssemblerOutput(output.copy())) {
                inputs.clear();
                output = ItemStack.EMPTY;
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
        compound.setTag("Output", output.writeToNBT(new NBTTagCompound()));
        NBTTagList list = new NBTTagList();
        for (ItemStack stack : inputs) {
            list.appendTag(stack.writeToNBT(new NBTTagCompound()));
        }
        compound.setTag("Inputs", list);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        remainingTicks = compound.getInteger("RemainingTicks");
        interfacePos = compound.getLong("InterfacePos");
        output = new ItemStack(compound.getCompoundTag("Output"));
        inputs.clear();
        NBTTagList list = compound.getTagList("Inputs", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            ItemStack stack = new ItemStack(list.getCompoundTagAt(i));
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }
    }

    public void dropContents() {
        if (world == null) {
            return;
        }
        for (ItemStack stack : inputs) {
            if (!stack.isEmpty()) {
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
        if (!output.isEmpty()) {
            InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), output);
        }
    }
}
