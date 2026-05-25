package com.shiver.supermecrafting.client;

import com.shiver.supermecrafting.table.ContainerSupremeTable;
import com.shiver.supermecrafting.table.SupremeTableInventory;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class GuiSupremeTable extends GuiContainer {
    private static final ResourceLocation SLOT_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private static final int PANEL_BG = 0xFFC6C6C6;
    private static final int PANEL_LIGHT = 0xFFFFFFFF;
    private static final int PANEL_DARK = 0xFF555555;
    private static final int CANVAS_BG = 0xFF373737;
    private static final int GRID_BORDER = 0xFFA0A0A0;
    private static final int CANVAS_PAD = 4;
    private static final int TITLE_HEIGHT = 17;
    private static final int CANVAS_HEIGHT = 220;
    private static final int SIDEBAR_WIDTH = 36;
    private static final int PLAYER_INV_GAP = 14;
    private static final int HOTBAR_GAP = 4;
    private static final int OFFSCREEN = -9999;
    private static final double DEFAULT_CELL = 18.0;
    private static final double MIN_CELL = 2.0;
    private static final double MAX_CELL = 36.0;
    private static final double ZOOM_STEP = 1.15;
    private static final double EDGE_PAD = 60.0;

    private final ContainerSupremeTable tableContainer;
    private double panOffsetX;
    private double panOffsetY;
    private double cellSize = DEFAULT_CELL;
    private boolean panning;
    private int dragLastX;
    private int dragLastY;

    public GuiSupremeTable(ContainerSupremeTable container, InventoryPlayer playerInv) {
        super(container);
        this.tableContainer = container;
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

    private boolean overArrow(int mouseX, int mouseY) {
        int ax = guiLeft + xSize - 22;
        int ay = guiTop + 4;
        return mouseX >= ax && mouseX < ax + 16 && mouseY >= ay && mouseY < ay + 16;
    }

    private void updateSlotPositions() {
        int firstX = Math.max(0, (int) Math.floor(-panOffsetX / cellSize));
        int lastX = Math.min(SupremeTableInventory.WIDTH - 1, (int) Math.ceil((canvasWidth() - panOffsetX) / cellSize));
        int firstY = Math.max(0, (int) Math.floor(-panOffsetY / cellSize));
        int lastY = Math.min(SupremeTableInventory.HEIGHT - 1, (int) Math.ceil((canvasHeight() - panOffsetY) / cellSize));
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
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (overArrow(mouseX, mouseY)) {
            drawHoveringText(java.util.Collections.singletonList("Show recipes for this table"), mouseX, mouseY);
        }
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

        int ax = guiLeft + xSize - 22;
        int ay = guiTop + 4;
        drawRect(ax, ay, ax + 16, ay + 16, overArrow(mouseX, mouseY) ? 0xFFFFD040 : 0xFF8B8B8B);
        fontRenderer.drawString("?", ax + 6, ay + 4, 0xFFFFFF);

        renderGridChrome(cl, ct, cr, cb);
        drawSlotSprites();
    }

    private void renderGridChrome(int cl, int ct, int cr, int cb) {
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
    }

    private void drawSlotSprites() {
        mc.getTextureManager().bindTexture(SLOT_TEXTURE);
        for (int i = SupremeTableInventory.SIZE; i < inventorySlots.inventorySlots.size(); i++) {
            Slot slot = inventorySlots.inventorySlots.get(i);
            drawTexturedModalRect(guiLeft + slot.xPos - 1, guiTop + slot.yPos - 1, 7, 17, 18, 18);
        }
    }

    @Override
    protected boolean isPointInRegion(int left, int top, int right, int bottom, int pointX, int pointY) {
        if (left == OFFSCREEN || top == OFFSCREEN) {
            return false;
        }
        int slotIndex = getSlotUnderMouse() == null ? -1 : getSlotUnderMouse().slotNumber;
        return super.isPointInRegion(left, top, right, bottom, pointX, pointY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0 && overArrow(mouseX, mouseY)) {
            RecipeViewerHooks.invokeFirst();
            return;
        }
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
                double direction = wheel > 0 ? 1 : -1;
                cellSize = CanvasMath.clamp(cellSize * Math.pow(ZOOM_STEP, direction), MIN_CELL, MAX_CELL);
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
