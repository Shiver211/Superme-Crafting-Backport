package com.shiver.supermecrafting.client;

import com.shiver.supermecrafting.ae2.ContainerSupremePatternTerminal;
import com.shiver.supermecrafting.table.SupremeTableInventory;
import com.shiver.supermecrafting.table.ViewportSlot;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.lang.reflect.Method;

public class GuiSupremePatternTerminal extends GuiContainer {
    private static final ResourceLocation SLOT_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private static final int CANVAS_PAD = 4;
    private static final int TITLE_HEIGHT = 17;
    private static final int CANVAS_HEIGHT = 220;
    private static final int SIDEBAR_WIDTH = 36;
    private static final int PLAYER_INV_GAP = 14;
    private static final int HOTBAR_GAP = 4;
    private static final int OFFSCREEN = -9999;
    private static final int ENCODE_BUTTON_ID = 10;
    private static final double DEFAULT_CELL = 18.0;
    private static final double MIN_CELL = 2.0;
    private static final double MAX_CELL = 36.0;
    private static final double ZOOM_STEP = 1.15;
    private static final double EDGE_PAD = 60.0;

    private double panOffsetX;
    private double panOffsetY;
    private double cellSize = DEFAULT_CELL;
    private boolean panning;
    private int dragLastX;
    private int dragLastY;

    public GuiSupremePatternTerminal(ContainerSupremePatternTerminal container) {
        super(container);
        this.xSize = 356;
        this.ySize = TITLE_HEIGHT + CANVAS_HEIGHT + PLAYER_INV_GAP + 3 * 18 + HOTBAR_GAP + 18 + 6;
        this.allowUserInput = true;
    }

    @Override
    public void initGui() {
        super.initGui();
        panOffsetX = CanvasMath.clampPan(canvasWidth() / 2.0 - SupremeTableInventory.WIDTH * cellSize / 2.0,
                canvasWidth(), cellSize, SupremeTableInventory.WIDTH, EDGE_PAD);
        panOffsetY = CanvasMath.clampPan(canvasHeight() / 2.0 - SupremeTableInventory.HEIGHT * cellSize / 2.0,
                canvasHeight(), cellSize, SupremeTableInventory.HEIGHT, EDGE_PAD);
        buttonList.add(new GuiButton(ENCODE_BUTTON_ID, guiLeft + 320, guiTop + 96, 30, 20, "=>"));
    }

    private int canvasWidth() {
        return xSize - 2 * CANVAS_PAD - SIDEBAR_WIDTH;
    }

    private int canvasHeight() {
        return CANVAS_HEIGHT;
    }

    private int gridLineX(int gx) {
        return CANVAS_PAD + (int) Math.round(panOffsetX + gx * cellSize);
    }

    private int gridLineY(int gy) {
        return TITLE_HEIGHT + (int) Math.round(panOffsetY + gy * cellSize);
    }

    private int cellWidth(int gx) {
        return gridLineX(gx + 1) - gridLineX(gx);
    }

    private int cellHeight(int gy) {
        return gridLineY(gy + 1) - gridLineY(gy);
    }

    private boolean inCanvas(int mouseX, int mouseY) {
        int cl = guiLeft + CANVAS_PAD;
        int ct = guiTop + TITLE_HEIGHT;
        return mouseX >= cl && mouseX < cl + canvasWidth() && mouseY >= ct && mouseY < ct + canvasHeight();
    }

    private void updateSlotPositions() {
        int firstX = Math.max(0, (int) Math.ceil(-panOffsetX / cellSize));
        int lastX = Math.min(SupremeTableInventory.WIDTH - 1, (int) Math.floor((canvasWidth() - panOffsetX) / cellSize) - 1);
        int firstY = Math.max(0, (int) Math.ceil(-panOffsetY / cellSize));
        int lastY = Math.min(SupremeTableInventory.HEIGHT - 1, (int) Math.floor((canvasHeight() - panOffsetY) / cellSize) - 1);
        for (int i = 0; i < SupremeTableInventory.SIZE; i++) {
            Slot slot = inventorySlots.inventorySlots.get(i);
            slot.xPos = OFFSCREEN;
            slot.yPos = OFFSCREEN;
        }
        for (int y = firstY; y <= lastY; y++) {
            for (int x = firstX; x <= lastX; x++) {
                Slot slot = inventorySlots.inventorySlots.get(SupremeTableInventory.indexOf(x, y));
                slot.xPos = gridLineX(x);
                slot.yPos = gridLineY(y);
            }
        }
        int invLeft = (xSize - 9 * 18) / 2;
        int invTop = TITLE_HEIGHT + CANVAS_HEIGHT + PLAYER_INV_GAP;
        int index = SupremeTableInventory.SIZE;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                Slot slot = inventorySlots.inventorySlots.get(index++);
                slot.xPos = invLeft + col * 18;
                slot.yPos = invTop + row * 18;
            }
        }
        int hotbarTop = invTop + 3 * 18 + HOTBAR_GAP;
        for (int col = 0; col < 9; col++) {
            Slot slot = inventorySlots.inventorySlots.get(index++);
            slot.xPos = invLeft + col * 18;
            slot.yPos = hotbarTop;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        updateSlotPositions();
        ViewportSlot.setSuppressRender(true);
        try {
            super.drawScreen(mouseX, mouseY, partialTicks);
        } finally {
            ViewportSlot.setSuppressRender(false);
        }
        drawScaledGridContents(mouseX, mouseY);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, 0xFFC6C6C6);
        drawRect(guiLeft + CANVAS_PAD, guiTop + TITLE_HEIGHT,
                guiLeft + CANVAS_PAD + canvasWidth(), guiTop + TITLE_HEIGHT + canvasHeight(), 0xFF373737);
        renderGridChrome();
        drawSlotSprites();
    }

    private void renderGridChrome() {
        int cl = guiLeft + CANVAS_PAD;
        int ct = guiTop + TITLE_HEIGHT;
        int cr = cl + canvasWidth();
        int cb = ct + canvasHeight();
        enableScissor(cl, ct, cr, cb);
        drawCanvasSlotSprites();
        disableScissor();
    }

    private void drawCanvasSlotSprites() {
        int firstX = Math.max(0, (int) Math.floor(-panOffsetX / cellSize));
        int lastX = Math.min(SupremeTableInventory.WIDTH - 1, (int) Math.ceil((canvasWidth() - panOffsetX) / cellSize));
        int firstY = Math.max(0, (int) Math.floor(-panOffsetY / cellSize));
        int lastY = Math.min(SupremeTableInventory.HEIGHT - 1, (int) Math.ceil((canvasHeight() - panOffsetY) / cellSize));
        mc.getTextureManager().bindTexture(SLOT_TEXTURE);
        for (int y = firstY; y <= lastY; y++) {
            for (int x = firstX; x <= lastX; x++) {
                drawScaledCustomSizeModalRect(guiLeft + gridLineX(x), guiTop + gridLineY(y),
                        7, 17, 18, 18, Math.max(1, cellWidth(x)), Math.max(1, cellHeight(y)), 256, 256);
            }
        }
    }

    private void drawSlotSprites() {
        mc.getTextureManager().bindTexture(SLOT_TEXTURE);
        for (int i = SupremeTableInventory.SIZE; i < inventorySlots.inventorySlots.size(); i++) {
            Slot slot = inventorySlots.inventorySlots.get(i);
            drawTexturedModalRect(guiLeft + slot.xPos - 1, guiTop + slot.yPos - 1, 7, 17, 18, 18);
        }
    }

    private void drawScaledGridContents(int mouseX, int mouseY) {
        int cl = guiLeft + CANVAS_PAD;
        int ct = guiTop + TITLE_HEIGHT;
        int cr = cl + canvasWidth();
        int cb = ct + canvasHeight();
        enableScissor(cl, ct, cr, cb);
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
        for (int i = 0; i < SupremeTableInventory.SIZE; i++) {
            Slot slot = inventorySlots.inventorySlots.get(i);
            if (slot.xPos == OFFSCREEN || slot.yPos == OFFSCREEN || !slot.getHasStack()) continue;
            int gx = SupremeTableInventory.xOf(i);
            int gy = SupremeTableInventory.yOf(i);
            drawScaledStack(slot.getStack(), guiLeft + slot.xPos, guiTop + slot.yPos, cellWidth(gx), cellHeight(gy));
        }
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        disableScissor();
        RenderHelper.disableStandardItemLighting();
    }

    private void drawScaledStack(ItemStack stack, int x, int y, int cellW, int cellH) {
        if (stack.isEmpty()) return;
        float scale = Math.max(0.25F, Math.min(cellW, cellH) / 18.0F);
        int size = Math.max(1, Math.round(16.0F * scale));
        int drawX = Math.round(x + (cellW - size) / 2.0F);
        int drawY = Math.round(y + (cellH - size) / 2.0F);
        GlStateManager.pushMatrix();
        GlStateManager.translate(drawX, drawY, 100.0F);
        GlStateManager.scale(scale, scale, 1.0F);
        itemRender.zLevel = 100.0F;
        itemRender.renderItemAndEffectIntoGUI(mc.player, stack, 0, 0);
        itemRender.zLevel = 0.0F;
        GlStateManager.popMatrix();
    }

    private void enableScissor(int left, int top, int right, int bottom) {
        int x = left * mc.displayWidth / width;
        int y = mc.displayHeight - bottom * mc.displayHeight / height;
        int w = Math.max(0, (right - left) * mc.displayWidth / width);
        int h = Math.max(0, (bottom - top) * mc.displayHeight / height);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x, y, w, h);
    }

    private void disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == ENCODE_BUTTON_ID) {
            sendEncode();
            return;
        }
        super.actionPerformed(button);
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

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 2 && inCanvas(mouseX, mouseY)) {
            panning = true;
            dragLastX = mouseX;
            dragLastY = mouseY;
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 2) {
            panning = false;
            return;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (panning && clickedMouseButton == 2) {
            panOffsetX = CanvasMath.clampPan(panOffsetX + mouseX - dragLastX,
                    canvasWidth(), cellSize, SupremeTableInventory.WIDTH, EDGE_PAD);
            panOffsetY = CanvasMath.clampPan(panOffsetY + mouseY - dragLastY,
                    canvasHeight(), cellSize, SupremeTableInventory.HEIGHT, EDGE_PAD);
            dragLastX = mouseX;
            dragLastY = mouseY;
            return;
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void handleMouseInput() throws IOException {
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int mouseX = Mouse.getEventX() * width / mc.displayWidth;
            int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            if (inCanvas(mouseX, mouseY)) {
                double old = cellSize;
                cellSize = CanvasMath.clamp(cellSize * Math.pow(ZOOM_STEP, wheel > 0 ? 1 : -1), MIN_CELL, MAX_CELL);
                int cl = guiLeft + CANVAS_PAD;
                int ct = guiTop + TITLE_HEIGHT;
                panOffsetX = CanvasMath.zoomCenteredPan(panOffsetX, old, cellSize, mouseX - cl);
                panOffsetY = CanvasMath.zoomCenteredPan(panOffsetY, old, cellSize, mouseY - ct);
                panOffsetX = CanvasMath.clampPan(panOffsetX, canvasWidth(), cellSize, SupremeTableInventory.WIDTH, EDGE_PAD);
                panOffsetY = CanvasMath.clampPan(panOffsetY, canvasHeight(), cellSize, SupremeTableInventory.HEIGHT, EDGE_PAD);
                return;
            }
        }
        super.handleMouseInput();
    }
}
