package com.shiver.supermecrafting.client;

import com.shiver.supermecrafting.block.BlockSupremeFurnaceCasing;
import com.shiver.supermecrafting.block.FurnaceRenderRegion;
import com.shiver.supermecrafting.furnace.FaceMath;
import com.shiver.supermecrafting.furnace.MultiblockFace;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class FormedCasingModel implements IBakedModel {
    private static final FaceBakery BAKERY = new FaceBakery();
    private static final Vector3f FROM = new Vector3f(0.0F, 0.0F, 0.0F);
    private static final Vector3f TO = new Vector3f(16.0F, 16.0F, 16.0F);

    private final IBakedModel fallback;
    private final TextureAtlasSprite top;
    private final TextureAtlasSprite side;
    private final TextureAtlasSprite front;
    private final TextureAtlasSprite frontOn;
    private final TextureAtlasSprite interior;

    public FormedCasingModel(IBakedModel fallback) {
        this.fallback = fallback;
        this.top = sprite("supreme_crafting:blocks/furnace_top");
        this.side = sprite("supreme_crafting:blocks/furnace_side");
        this.front = sprite("supreme_crafting:blocks/furnace_front_off");
        this.frontOn = sprite("supreme_crafting:blocks/furnace_front_on");
        this.interior = sprite("supreme_crafting:blocks/coal_block");
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (state == null || side == null || !state.getValue(BlockSupremeFurnaceCasing.FORMED)
                || !(state instanceof IExtendedBlockState)) {
            return fallback.getQuads(state, side, rand);
        }
        FurnaceRenderRegion region = ((IExtendedBlockState) state).getValue(BlockSupremeFurnaceCasing.RENDER_REGION);
        if (region == null) {
            return fallback.getQuads(state, side, rand);
        }
        FaceMath.FaceTexel texel = FaceMath.texelFor(region.pos(), region, side);
        if (texel == null) {
            return Collections.singletonList(quad(side, interior, 0, 0, 16, 16));
        }
        return Collections.singletonList(quad(side, spriteFor(texel.face(), region.lit()), texel.u(), texel.v(), texel.u() + 1, texel.v() + 1));
    }

    private TextureAtlasSprite spriteFor(MultiblockFace face, boolean lit) {
        if (face == MultiblockFace.TOP || face == MultiblockFace.BOTTOM) return top;
        if (face == MultiblockFace.FRONT) return lit ? frontOn : front;
        return side;
    }

    private static BakedQuad quad(EnumFacing facing, TextureAtlasSprite sprite, float u0, float v0, float u1, float v1) {
        BlockFaceUV uv = new BlockFaceUV(new float[]{u0, v0, u1, v1}, 0);
        BlockPartFace face = new BlockPartFace(null, -1, "", uv);
        return BAKERY.makeBakedQuad(FROM, TO, face, sprite, facing, ModelRotation.X0_Y0, null, true, true);
    }

    private static TextureAtlasSprite sprite(String name) {
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(name);
    }

    @Override public boolean isAmbientOcclusion() { return fallback.isAmbientOcclusion(); }
    @Override public boolean isGui3d() { return fallback.isGui3d(); }
    @Override public boolean isBuiltInRenderer() { return fallback.isBuiltInRenderer(); }
    @Override public TextureAtlasSprite getParticleTexture() { return fallback.getParticleTexture(); }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return fallback.getItemCameraTransforms(); }
    @Override public ItemOverrideList getOverrides() { return fallback.getOverrides(); }

}
