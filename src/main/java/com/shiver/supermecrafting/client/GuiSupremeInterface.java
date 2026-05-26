package com.shiver.supermecrafting.client;

import com.shiver.supermecrafting.ae2.ContainerSupremeInterface;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

public class GuiSupremeInterface extends GuiContainer {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("supreme_crafting", "textures/gui/supreme_interface.png");
    private static final ResourceLocation STATES_TEXTURE =
            new ResourceLocation("supreme_crafting", "textures/gui/states.png");
    private static final int PATTERN_SLOTS = 36;
    private static final int TOP_CAP = 7;
    private static final int CROP_TOP = 79;

    public GuiSupremeInterface(ContainerSupremeInterface container) {
        super(container);
        this.xSize = 177;
        this.ySize = TOP_CAP + 256 - CROP_TOP;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, TOP_CAP);
        drawTexturedModalRect(guiLeft, guiTop + TOP_CAP, 0, CROP_TOP, xSize, ySize - TOP_CAP);
        drawPatternSlotStates();
    }

    private void drawPatternSlotStates() {
        net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        net.minecraft.client.renderer.GlStateManager.enableBlend();
        mc.getTextureManager().bindTexture(STATES_TEXTURE);
        for (int i = 0; i < PATTERN_SLOTS; i++) {
            Slot slot = inventorySlots.inventorySlots.get(i);
            drawScaledCustomSizeModalRect(guiLeft + slot.xPos, guiTop + slot.yPos,
                    0, 0, 16, 16, 16, 16, 16, 16);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(I18n.format("tile.supreme_crafting.supreme_interface.name"), 8, 8, 4210752);
    }
}
