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

import java.awt.Rectangle;
import java.io.IOException;
import java.lang.reflect.Method;

public class GuiSupremePatternTerminal extends GuiContainer {
    private static final ResourceLocation SLOT_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private static final int PANEL_BG = 0xFFC6C6C6;
    private static final int PANEL_LIGHT = 0xFFFFFFFF;
    private static final int PANEL_DARK = 0xFF555555;
    private static final int CANVAS_BG = 0xFF373737;
    private static final int GRID_BORDER = 0xFFA0A0A0;
    private static final int SLOT_BRIGHTEN = 0x30FFFFFF;
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
    private static final double NAV_PAD = 12.0;
    private static final int GROUP_SIZE = 27;
    private static final int QUICK_BUTTON = 20;
    private static final int QUICK_GAP = 4;
    private static final int MOVE_BUTTON = 20;
    private static final int MOVE_GAP = 3;
    private static final int GROUP_BUTTON_ID = 100;
    private static final int MOVE_BUTTON_ID = 200;

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
        initNavigationButtons();
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
        drawCanvasStackTooltip(mouseX, mouseY);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, PANEL_BG);
        drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + 1, PANEL_LIGHT);
        drawRect(guiLeft, guiTop, guiLeft + 1, guiTop + ySize, PANEL_LIGHT);
        drawRect(guiLeft, guiTop + ySize - 1, guiLeft + xSize, guiTop + ySize, PANEL_DARK);
        drawRect(guiLeft + xSize - 1, guiTop, guiLeft + xSize, guiTop + ySize, PANEL_DARK);

        int cl = guiLeft + CANVAS_PAD;
        int ct = guiTop + TITLE_HEIGHT;
        int cr = cl + canvasWidth();
        int cb = ct + canvasHeight();
        drawRect(cl, ct, cr, cb, CANVAS_BG);

        renderGridChrome(cl, ct, cr, cb);
        drawSlotSprites();
    }

    private void renderGridChrome(int cl, int ct, int cr, int cb) {
        enableScissor(cl, ct, cr, cb);
        GlStateManager.disableTexture2D();
        int left = guiLeft + gridLineX(0);
        int top = guiTop + gridLineY(0);
        int right = guiLeft + gridLineX(SupremeTableInventory.WIDTH);
        int bottom = guiTop + gridLineY(SupremeTableInventory.HEIGHT);
        drawRect(left - 2, top - 2, right + 2, top, GRID_BORDER);
        drawRect(left - 2, bottom, right + 2, bottom + 2, GRID_BORDER);
        drawRect(left - 2, top, left, bottom, GRID_BORDER);
        drawRect(right, top, right + 2, bottom, GRID_BORDER);
        GlStateManager.enableTexture2D();
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
            int sy = guiTop + gridLineY(y);
            int height = Math.max(1, cellHeight(y));
            for (int x = firstX; x <= lastX; x++) {
                int sx = guiLeft + gridLineX(x);
                int width = Math.max(1, cellWidth(x));
                drawScaledCustomSizeModalRect(sx, sy, 7, 17, 18, 18, width, height, 256, 256);
                drawRect(sx + 1, sy + 1, sx + width - 1, sy + height - 1, SLOT_BRIGHTEN);
            }
        }
    }

    private void drawSlotSprites() {
        mc.getTextureManager().bindTexture(SLOT_TEXTURE);
        for (int i = SupremeTableInventory.SIZE; i < inventorySlots.inventorySlots.size(); i++) {
            Slot slot = inventorySlots.inventorySlots.get(i);
            drawTexturedModalRect(guiLeft + slot.xPos - 1, guiTop + slot.yPos - 1, 7, 17, 18, 18);
            drawRect(guiLeft + slot.xPos, guiTop + slot.yPos, guiLeft + slot.xPos + 16, guiTop + slot.yPos + 16, SLOT_BRIGHTEN);
        }
    }

    private Rectangle groupButtonBounds(int index) {
        int col = index % 3;
        int row = index / 3;
        int total = QUICK_BUTTON * 3 + QUICK_GAP * 2;
        int invLeft = guiLeft + (xSize - 9 * 18) / 2;
        int leftSpace = invLeft - guiLeft;
        int startX = guiLeft + Math.max(8, (leftSpace - total) / 2);
        int startY = guiTop + TITLE_HEIGHT + CANVAS_HEIGHT + PLAYER_INV_GAP + 3;
        return new Rectangle(startX + col * (QUICK_BUTTON + QUICK_GAP),
                startY + row * (QUICK_BUTTON + QUICK_GAP), QUICK_BUTTON, QUICK_BUTTON);
    }

    private Rectangle moveButtonBounds(int direction) {
        int invLeft = guiLeft + (xSize - 9 * 18) / 2;
        int invRight = invLeft + 9 * 18;
        int centerX = invRight + (guiLeft + xSize - invRight) / 2;
        Rectangle firstGroupButton = groupButtonBounds(0);
        int groupHeight = QUICK_BUTTON * 3 + QUICK_GAP * 2;
        int centerY = firstGroupButton.y + groupHeight / 2;
        int step = MOVE_BUTTON + MOVE_GAP;
        int x = centerX - MOVE_BUTTON / 2;
        int y = centerY - MOVE_BUTTON / 2;
        if (direction == 0) y -= step;
        if (direction == 1) x -= step;
        if (direction == 2) x += step;
        if (direction == 3) y += step;
        return new Rectangle(x, y, MOVE_BUTTON, MOVE_BUTTON);
    }

    private String directionLabel(int direction) {
        if (direction == 0) return "^";
        if (direction == 1) return "<";
        if (direction == 2) return ">";
        return "v";
    }

    private void initNavigationButtons() {
        for (int i = 0; i < 9; i++) {
            Rectangle bounds = groupButtonBounds(i);
            buttonList.add(new GuiButton(GROUP_BUTTON_ID + i, bounds.x, bounds.y, bounds.width, bounds.height,
                    String.valueOf(i + 1)));
        }
        for (int i = 0; i < 4; i++) {
            Rectangle bounds = moveButtonBounds(i);
            buttonList.add(new GuiButton(MOVE_BUTTON_ID + i, bounds.x, bounds.y, bounds.width, bounds.height,
                    directionLabel(i)));
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
        Slot hover = gridStackAt(mouseX, mouseY);
        if (hover != null) {
            int gx = SupremeTableInventory.xOf(hover.slotNumber);
            int gy = SupremeTableInventory.yOf(hover.slotNumber);
            Rectangle bounds = stackBounds(guiLeft + hover.xPos, guiTop + hover.yPos, cellWidth(gx), cellHeight(gy));
            drawGradientRect(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height, -2130706433, -2130706433);
        }
        disableScissor();
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    protected boolean isPointInRegion(int left, int top, int right, int bottom, int pointX, int pointY) {
        if (left == OFFSCREEN || top == OFFSCREEN) {
            return false;
        }
        int gx = gridXFromSlotLeft(left);
        int gy = gridYFromSlotTop(top);
        if (gx >= 0 && gy >= 0 && gx < SupremeTableInventory.WIDTH && gy < SupremeTableInventory.HEIGHT
                && gridLineX(gx) == left && gridLineY(gy) == top) {
            int px = pointX - guiLeft;
            int py = pointY - guiTop;
            return inCanvas(pointX, pointY)
                    && px >= left
                    && px < left + cellWidth(gx)
                    && py >= top
                    && py < top + cellHeight(gy);
        }
        return super.isPointInRegion(left, top, right, bottom, pointX, pointY);
    }

    private int gridXFromSlotLeft(int left) {
        return (int) Math.round((left - CANVAS_PAD - panOffsetX) / cellSize);
    }

    private int gridYFromSlotTop(int top) {
        return (int) Math.round((top - TITLE_HEIGHT - panOffsetY) / cellSize);
    }

    private Slot gridSlotAt(int mouseX, int mouseY) {
        if (!inCanvas(mouseX, mouseY)) return null;
        int relX = mouseX - guiLeft - CANVAS_PAD;
        int relY = mouseY - guiTop - TITLE_HEIGHT;
        int gx = (int) Math.floor((relX - panOffsetX) / cellSize);
        int gy = (int) Math.floor((relY - panOffsetY) / cellSize);
        if (gx < 0 || gy < 0 || gx >= SupremeTableInventory.WIDTH || gy >= SupremeTableInventory.HEIGHT) return null;
        Slot slot = inventorySlots.inventorySlots.get(SupremeTableInventory.indexOf(gx, gy));
        return slot.xPos == OFFSCREEN || slot.yPos == OFFSCREEN ? null : slot;
    }

    private Slot gridStackAt(int mouseX, int mouseY) {
        Slot slot = gridSlotAt(mouseX, mouseY);
        if (slot == null || !slot.getHasStack()) return null;
        int gx = SupremeTableInventory.xOf(slot.slotNumber);
        int gy = SupremeTableInventory.yOf(slot.slotNumber);
        Rectangle bounds = stackBounds(guiLeft + slot.xPos, guiTop + slot.yPos, cellWidth(gx), cellHeight(gy));
        return bounds.contains(mouseX, mouseY) ? slot : null;
    }

    private void drawCanvasStackTooltip(int mouseX, int mouseY) {
        Slot hover = gridStackAt(mouseX, mouseY);
        if (mc.player.inventory.getItemStack().isEmpty() && hover != null) {
            renderToolTip(hover.getStack(), mouseX, mouseY);
        }
    }

    private void drawScaledStack(ItemStack stack, int x, int y, int cellW, int cellH) {
        if (stack.isEmpty()) return;
        float scale = stackScale(cellW, cellH);
        Rectangle bounds = stackBounds(x, y, cellW, cellH);
        GlStateManager.pushMatrix();
        GlStateManager.translate(bounds.x, bounds.y, 100.0F);
        GlStateManager.scale(scale, scale, 1.0F);
        itemRender.zLevel = 100.0F;
        itemRender.renderItemAndEffectIntoGUI(mc.player, stack, 0, 0);
        itemRender.renderItemOverlayIntoGUI(fontRenderer, stack, 0, 0, null);
        itemRender.zLevel = 0.0F;
        GlStateManager.popMatrix();
    }

    private float stackScale(int cellW, int cellH) {
        return Math.max(0.25F, Math.min(cellW, cellH) / 18.0F);
    }

    private Rectangle stackBounds(int x, int y, int cellW, int cellH) {
        float scale = stackScale(cellW, cellH);
        int size = Math.max(1, Math.round(16.0F * scale));
        int drawX = Math.round(x + (cellW - size) / 2.0F);
        int drawY = Math.round(y + (cellH - size) / 2.0F);
        return new Rectangle(drawX, drawY, size, size);
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
        if (button.id >= GROUP_BUTTON_ID && button.id < GROUP_BUTTON_ID + 9) {
            int group = button.id - GROUP_BUTTON_ID;
            centerGroup(group % 3, group / 3);
            return;
        }
        if (button.id >= MOVE_BUTTON_ID && button.id < MOVE_BUTTON_ID + 4) {
            moveView(button.id - MOVE_BUTTON_ID);
            return;
        }
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

    private void centerGroup(int groupX, int groupY) {
        double availableWidth = Math.max(1.0, canvasWidth() - NAV_PAD * 2.0);
        double availableHeight = Math.max(1.0, canvasHeight() - NAV_PAD * 2.0);
        cellSize = Math.min(MAX_CELL, Math.min(availableWidth, availableHeight) / (double) GROUP_SIZE);
        centerOnGrid(groupX * GROUP_SIZE + GROUP_SIZE / 2.0, groupY * GROUP_SIZE + GROUP_SIZE / 2.0, NAV_PAD);
    }

    private void moveView(int direction) {
        double centerX = (canvasWidth() / 2.0 - panOffsetX) / cellSize;
        double centerY = (canvasHeight() / 2.0 - panOffsetY) / cellSize;
        if (direction == 0) centerY -= 1.0;
        if (direction == 1) centerX -= 1.0;
        if (direction == 2) centerX += 1.0;
        if (direction == 3) centerY += 1.0;
        centerOnGrid(centerX, centerY, NAV_PAD);
    }

    private void centerOnGrid(double gridX, double gridY, double edgePad) {
        panOffsetX = CanvasMath.clampPan(canvasWidth() / 2.0 - gridX * cellSize,
                canvasWidth(), cellSize, SupremeTableInventory.WIDTH, edgePad);
        panOffsetY = CanvasMath.clampPan(canvasHeight() / 2.0 - gridY * cellSize,
                canvasHeight(), cellSize, SupremeTableInventory.HEIGHT, edgePad);
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
