package com.shiver.supermecrafting.client;

import com.shiver.supermecrafting.table.SupremeTableTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

/**
 * 动画化 Supreme Table —— 三轴平移抖动 + 缓慢旋转摇摆。
 * 方块本身返回 INVISIBLE 渲染类型，所以 BER 是方块视觉的唯一来源。
 */
public class SupremeTableRenderer extends TileEntitySpecialRenderer<SupremeTableTileEntity> {

    private static final float TRANSLATE_AMP = 0.04F;
    private static final float ROTATE_AMP_DEG = 4.0F;

    @Override
    public void render(SupremeTableTileEntity te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        World world = te.getWorld();
        if (world == null) return;

        // 基于方块位置哈希的相位，使相邻的桌子不同步振荡
        BlockPos pos = te.getPos();
        double phase = (pos.hashCode() & 0xFF) * 0.0245;
        double t = (world.getTotalWorldTime() + partialTicks) / 20.0 + phase;

        float dx = (float) (TRANSLATE_AMP * Math.sin(t * 7.0));
        float dy = (float) (TRANSLATE_AMP * Math.sin(t * 11.0));
        float dz = (float) (TRANSLATE_AMP * Math.cos(t * 5.0));
        float yaw = (float) (ROTATE_AMP_DEG * Math.sin(t * 4.0));

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5F + dx, y + 0.5F + dy, z + 0.5F + dz);
        GlStateManager.rotate(yaw, 0, 1, 0);
        GlStateManager.translate(-0.5F, -0.5F, -0.5F);

        // 渲染方块模型
        IBlockState state = world.getBlockState(pos);
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderHelper.disableStandardItemLighting();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        buffer.setTranslation(0, 0, 0);

        mc.getBlockRendererDispatcher().getBlockModelRenderer().renderModel(
                world,
                mc.getBlockRendererDispatcher().getModelForState(state),
                state,
                pos,
                buffer,
                false,
                0L
        );

        tessellator.draw();
        RenderHelper.enableStandardItemLighting();

        GlStateManager.popMatrix();
    }
}
