package com.shiver.supermecrafting.client;

import com.shiver.supermecrafting.ae2.ContainerSupremePatternTerminal;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.lang.reflect.Method;

public class GuiSupremePatternTerminal extends GuiSupremeCraftingBase {
    private static final ResourceLocation STATES_TEXTURE =
            new ResourceLocation("supreme_crafting", "textures/gui/states.png");
    private static final int ENCODE_BUTTON_ID = 10;
    private static final int CLEAR_BUTTON_ID = 11;

    private final ContainerSupremePatternTerminal patternContainer;

    public GuiSupremePatternTerminal(ContainerSupremePatternTerminal container) {
        super(container);
        this.patternContainer = container;
    }

    @Override
    protected String titleKey() {
        return "tile.supreme_crafting.supreme_pattern_terminal.name";
    }

    @Override
    protected void refreshCraftingResult() {
        patternContainer.refreshCraftingResult();
    }

    @Override
    protected void initExtraButtons() {
        buttonList.add(new GuiButton(ENCODE_BUTTON_ID, guiLeft + 327, guiTop + 187, 20, 20, "->"));
        buttonList.add(new GuiButton(CLEAR_BUTTON_ID, guiLeft + 327, guiTop + 95, 20, 20, "C"));
    }

    @Override
    protected void drawExtraSlotSprites() {
        Slot slot = inventorySlots.inventorySlots.get(ContainerSupremePatternTerminal.BLANK_SLOT_INDEX);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        mc.getTextureManager().bindTexture(STATES_TEXTURE);
        drawScaledCustomSizeModalRect(guiLeft + slot.xPos, guiTop + slot.yPos,
                0, 0, 16, 16, 16, 16, 16, 16);
    }

    @Override
    protected boolean handleExtraButton(GuiButton button) throws IOException {
        if (button.id == ENCODE_BUTTON_ID) {
            sendEncode();
            return true;
        }
        if (button.id == CLEAR_BUTTON_ID) {
            sendClearPatternTerminal();
            return true;
        }
        return false;
    }

    private static void sendEncode() {
        try {
            Class<?> bridge = Class.forName("com.shiver.supermecrafting.ae2.AE2NetworkBridge");
            Method method = bridge.getMethod("sendEncode");
            method.invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to send encode packet", e);
        }
    }

    private static void sendClearPatternTerminal() {
        try {
            Class<?> bridge = Class.forName("com.shiver.supermecrafting.ae2.AE2NetworkBridge");
            Method method = bridge.getMethod("sendClearPatternTerminal");
            method.invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to send clear packet", e);
        }
    }
}
