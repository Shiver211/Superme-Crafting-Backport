package com.shiver.supermecrafting.net;

import com.shiver.supermecrafting.recipe.SupremeRecipeTransfer;
import com.shiver.supermecrafting.table.SupremeTableContainer;
import com.shiver.supermecrafting.table.SupremeTableTileEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TransferRecipePacket implements IMessage {

    private BlockPos tablePos;
    private ResourceLocation recipeId;

    public TransferRecipePacket() {}

    public TransferRecipePacket(BlockPos tablePos, ResourceLocation recipeId) {
        this.tablePos = tablePos;
        this.recipeId = recipeId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        tablePos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        recipeId = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(tablePos.getX());
        buf.writeInt(tablePos.getY());
        buf.writeInt(tablePos.getZ());
        ByteBufUtils.writeUTF8String(buf, recipeId.toString());
    }

    public BlockPos getTablePos() {
        return tablePos;
    }

    public ResourceLocation getRecipeId() {
        return recipeId;
    }

    public static class Handler implements IMessageHandler<TransferRecipePacket, IMessage> {
        @Override
        public IMessage onMessage(TransferRecipePacket message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            ((WorldServer) player.world).addScheduledTask(() -> {
                if (!player.world.isBlockLoaded(message.tablePos)) return;

                if (!(player.openContainer instanceof SupremeTableContainer)) return;
                SupremeTableContainer container = (SupremeTableContainer) player.openContainer;
                if (!container.getTablePos().equals(message.tablePos)) return;

                TileEntity te = player.world.getTileEntity(message.tablePos);
                if (!(te instanceof SupremeTableTileEntity)) return;

                IRecipe recipe = net.minecraft.item.crafting.CraftingManager.getRecipe(message.recipeId);
                if (recipe == null) return;

                SupremeRecipeTransfer.executeTransfer(
                        (SupremeTableTileEntity) te, player, recipe);
            });
            return null;
        }
    }
}
