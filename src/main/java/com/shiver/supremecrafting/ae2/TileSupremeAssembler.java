package com.shiver.supremecrafting.ae2;

import appeng.api.AEApi;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;

public class TileSupremeAssembler extends TileEntity implements ITickable, IGridHost {
    private static final String NODE_TAG = "ae2Node";
    private static final int CRAFT_TICKS = 20;
    private static final int AE_REFRESH_TICKS = 20;

    private final NonNullList<ItemStack> inputs = NonNullList.create();
    private final NonNullList<ItemStack> outputs = NonNullList.create();
    private int remainingTicks;
    private long interfacePos;
    private boolean crafted;
    private int aeRefreshTicks;
    private Object ae2Node;
    private NBTTagCompound ae2NodeData;

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
        if (world == null || world.isRemote) {
            return;
        }
        refreshAeNode();
        if (remainingTicks <= 0) {
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

    private void refreshAeNode() {
        if (aeRefreshTicks-- > 0) {
            IGridNode node = getGridNode(AEPartLocation.INTERNAL);
            if (node != null) {
                node.updateState();
            }
        }
    }

    @Override
    public IGridNode getGridNode(AEPartLocation dir) {
        IGridNode node = (IGridNode) ae2Node;
        if (node == null && world != null && !world.isRemote && !isInvalid()) {
            node = AEApi.instance().grid().createGridNode(new SupremeAssemblerGridBlock(this));
            ae2Node = node;
            if (ae2NodeData != null) {
                node.loadFromNBT(NODE_TAG, ae2NodeData);
                ae2NodeData = null;
            }
            node.updateState();
        }
        return node;
    }

    @Override
    public AECableType getCableConnectionType(AEPartLocation dir) {
        return AECableType.COVERED;
    }

    @Override
    public void securityBreak() {
        if (world != null) {
            world.destroyBlock(pos, true);
        }
    }

    @Override
    public void validate() {
        super.validate();
        if (world != null && !world.isRemote) {
            aeRefreshTicks = AE_REFRESH_TICKS;
            getGridNode(AEPartLocation.INTERNAL);
        }
    }

    @Override
    public void invalidate() {
        destroyNode();
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        destroyNode();
        super.onChunkUnload();
    }

    private void destroyNode() {
        IGridNode node = (IGridNode) ae2Node;
        if (node != null) {
            node.destroy();
            ae2Node = null;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        IGridNode node = (IGridNode) ae2Node;
        if (node != null) {
            node.saveToNBT(NODE_TAG, compound);
        }
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
        ae2NodeData = compound;
        aeRefreshTicks = AE_REFRESH_TICKS;
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
