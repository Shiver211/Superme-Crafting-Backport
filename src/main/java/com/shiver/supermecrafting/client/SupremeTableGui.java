package com.shiver.supermecrafting.client;

import com.shiver.supermecrafting.Tags;
import com.shiver.supermecrafting.table.SupremeTableContainer;
import com.shiver.supermecrafting.table.SupremeTableInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

/**
 * Google-Maps 风格的画布，覆盖整个 81x81 网格。
 * 每个网格单元都是一个真实的原版 {@link Slot}（共 6561 个）；
 * 我们只是每帧重新定位它们，并将不可见的单元格停放在画布外。
 * 中键拖动平移，鼠标滚轮缩放（以光标为中心）。
 *
 * <p>完全程序化渲染（不使用纹理文件），与原版实现方式一致。
 */
public class SupremeTableGui extends GuiContainer {

    // 颜色常量（与原版一致）
    private static final int PANEL_BG = 0xFFC6C6C6;
    private static final int PANEL_LIGHT = 0xFFFFFFFF;
    private static final int PANEL_DARK = 0xFF555555;
    private static final int CANVAS_BG = 0xFF373737;
    private static final int GRID_BORDER = 0xFFA0A0A0;
    private static final int GRID_BORDER_PX = 2;
    private static final int CANVAS_BEVEL_DARK = 0xFF373737;
    private static final int CANVAS_BEVEL_LIGHT = 0xFFFFFFFF;

    private static final int ARROW_SIZE = 16;
    private static final int ARROW_RIGHT_INSET = 6;
    private static final int ARROW_TOP_INSET = 4;
    private static final int ARROW_BG = 0xFF8B8B8B;
    private static final int ARROW_BG_HOVER = 0xFFFFD040;
    private static final int ARROW_FG = 0xFFFFFFFF;

    private static final int CANVAS_PAD = 4;
    private static final int TITLE_HEIGHT = 17;
    private static final int CANVAS_HEIGHT = 220;
    private static final int PLAYER_INV_GAP = 14;
    private static final int HOTBAR_GAP = 4;
    private static final int PLAYER_INV_ROWS = 3;
    private static final int PLAYER_INV_COLS = 9;

    private static final double DEFAULT_CELL = 18.0;
    private static final double MIN_CELL = 2.0;
    private static final double MAX_CELL = 36.0;
    private static final double ZOOM_STEP = 1.15;
    private static final double EDGE_PAD = 60.0;

    private static final int OFFSCREEN = -9999;

    // 平移/缩放状态
    private double panOffsetX;
    private double panOffsetY;
    private double cellSize = DEFAULT_CELL;

    private boolean panning;
    private double dragLastX;
    private double dragLastY;

    public SupremeTableGui(SupremeTableContainer container) {
        super(container);
        this.xSize = 356;
        int playerInvHeight = PLAYER_INV_ROWS * 18 + HOTBAR_GAP + 18;
        this.ySize = TITLE_HEIGHT + CANVAS_HEIGHT + PLAYER_INV_GAP + playerInvHeight + 6;
    }

    @Override
    public void initGui() {
        super.initGui();
        // 初始化平移：居中显示网格
        panOffsetX = CanvasMath.clampPan(
                canvasWidth() / 2.0 - SupremeTableInventory.WIDTH * cellSize / 2.0,
                canvasWidth(), cellSize, SupremeTableInventory.WIDTH, EDGE_PAD);
        panOffsetY = CanvasMath.clampPan(
                canvasHeight() / 2.0 - SupremeTableInventory.HEIGHT * cellSize / 2.0,
                canvasHeight(), cellSize, SupremeTableInventory.HEIGHT, EDGE_PAD);
    }

    private int canvasLeftRel() { return CANVAS_PAD; }
    private int canvasTopRel() { return TITLE_HEIGHT; }
    private int canvasWidth() { return xSize - 2 * CANVAS_PAD - 36; }
    private int canvasHeight() { return CANVAS_HEIGHT; }

    private int arrowXRel() { return xSize - ARROW_SIZE - ARROW_RIGHT_INSET; }
    private int arrowYRel() { return ARROW_TOP_INSET; }

    private boolean isMouseOverArrow(int mouseX, int mouseY) {
        int ax = guiLeft + arrowXRel();
        int ay = guiTop + arrowYRel();
        return mouseX >= ax && mouseX < ax + ARROW_SIZE
                && mouseY >= ay && mouseY < ay + ARROW_SIZE;
    }

    private int gridLineX(int gx) {
        return canvasLeftRel() + (int) Math.round(panOffsetX + gx * cellSize);
    }

    private int gridLineY(int gy) {
        return canvasTopRel() + (int) Math.round(panOffsetY + gy * cellSize);
    }

    private boolean inCanvas(int mouseX, int mouseY) {
        int cl = guiLeft + canvasLeftRel();
        int ct = guiTop + canvasTopRel();
        return mouseX >= cl && mouseX < cl + canvasWidth()
                && mouseY >= ct && mouseY < ct + canvasHeight();
    }

    private void updateSlotPositions() {
        int firstX = Math.max(0, (int) Math.floor(-panOffsetX / cellSize));
        int lastX = Math.min(SupremeTableInventory.WIDTH - 1,
                (int) Math.ceil((canvasWidth() - panOffsetX) / cellSize));
        int firstY = Math.max(0, (int) Math.floor(-panOffsetY / cellSize));
        int lastY = Math.min(SupremeTableInventory.HEIGHT - 1,
                (int) Math.ceil((canvasHeight() - panOffsetY) / cellSize));

        // 将所有网格槽位停放在画布外
        for (int i = 0; i < SupremeTableInventory.SIZE; i++) {
            Slot s = inventorySlots.getSlot(i);
            s.xPos = OFFSCREEN;
            s.yPos = OFFSCREEN;
        }
        // 将可见槽位放到正确位置
        for (int gy = firstY; gy <= lastY; gy++) {
            int sy = gridLineY(gy);
            for (int gx = firstX; gx <= lastX; gx++) {
                int idx = SupremeTableInventory.indexOf(gx, gy);
                Slot s = inventorySlots.getSlot(idx);
                s.xPos = gridLineX(gx);
                s.yPos = sy;
            }
        }

        // 玩家物品栏 + 快捷栏
        int invLeft = (xSize - PLAYER_INV_COLS * 18) / 2;
        int invTop = TITLE_HEIGHT + CANVAS_HEIGHT + PLAYER_INV_GAP;
        int hotbarTop = invTop + PLAYER_INV_ROWS * 18 + HOTBAR_GAP;
        int slotIndex = SupremeTableInventory.SIZE;
        for (int row = 0; row < PLAYER_INV_ROWS; row++) {
            for (int col = 0; col < PLAYER_INV_COLS; col++) {
                Slot s = inventorySlots.getSlot(slotIndex++);
                s.xPos = invLeft + col * 18;
                s.yPos = invTop + row * 18;
            }
        }
        for (int col = 0; col < PLAYER_INV_COLS; col++) {
            Slot s = inventorySlots.getSlot(slotIndex++);
            s.xPos = invLeft + col * 18;
            s.yPos = hotbarTop;
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        updateSlotPositions();

        int x = guiLeft;
        int y = guiTop;

        // 面板背景
        Gui.drawRect(x, y, x + xSize, y + ySize, PANEL_BG);
        // 面板边框（亮色顶/左，暗色底/右）
        Gui.drawRect(x, y, x + xSize, y + 1, PANEL_LIGHT);
        Gui.drawRect(x, y, x + 1, y + ySize, PANEL_LIGHT);
        Gui.drawRect(x, y + ySize - 1, x + xSize, y + ySize, PANEL_DARK);
        Gui.drawRect(x + xSize - 1, y, x + xSize, y + ySize, PANEL_DARK);

        // 画布背景
        int cl = x + canvasLeftRel();
        int ct = y + canvasTopRel();
        int cr = cl + canvasWidth();
        int cb = ct + canvasHeight();
        Gui.drawRect(cl, ct, cr, cb, CANVAS_BG);

        // 1px 凹槽边框：暗色顶+左，亮色底+右
        Gui.drawRect(cl - 1, ct - 1, cr + 1, ct, CANVAS_BEVEL_DARK);
        Gui.drawRect(cl - 1, ct, cl, cb, CANVAS_BEVEL_DARK);
        Gui.drawRect(cl - 1, cb, cr + 1, cb + 1, CANVAS_BEVEL_LIGHT);
        Gui.drawRect(cr, ct, cr + 1, cb, CANVAS_BEVEL_LIGHT);

        // "?" 配方查看按钮
        int ax = x + arrowXRel();
        int ay = y + arrowYRel();
        boolean hover = isMouseOverArrow(mouseX, mouseY);
        Gui.drawRect(ax, ay, ax + ARROW_SIZE, ay + ARROW_SIZE, hover ? ARROW_BG_HOVER : ARROW_BG);
        Gui.drawRect(ax, ay, ax + ARROW_SIZE, ay + 1, PANEL_LIGHT);
        Gui.drawRect(ax, ay, ax + 1, ay + ARROW_SIZE, PANEL_LIGHT);
        Gui.drawRect(ax, ay + ARROW_SIZE - 1, ax + ARROW_SIZE, ay + ARROW_SIZE, PANEL_DARK);
        Gui.drawRect(ax + ARROW_SIZE - 1, ay, ax + ARROW_SIZE, ay + ARROW_SIZE, PANEL_DARK);
        String label = "?";
        int tw = fontRenderer.getStringWidth(label);
        fontRenderer.drawString(label, ax + (ARROW_SIZE - tw) / 2 + 1, ay + 4, ARROW_FG, false);

        // 玩家物品栏槽位精灵
        int invLeft = x + (xSize - PLAYER_INV_COLS * 18) / 2;
        int invTop = y + TITLE_HEIGHT + CANVAS_HEIGHT + PLAYER_INV_GAP;
        drawSlotSprites(invLeft, invTop, PLAYER_INV_COLS, PLAYER_INV_ROWS);
        drawSlotSprites(invLeft, invTop + PLAYER_INV_ROWS * 18 + HOTBAR_GAP, PLAYER_INV_COLS, 1);

        // 渲染网格（带裁剪）
        renderGridChrome(cl, ct, cr, cb);
    }

    private void drawSlotSprites(int x, int y, int cols, int rows) {
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                int sx = x + c * 18;
                int sy = y + r * 18;
                // 绘制凹槽槽位（暗色顶/左，亮色底/右）
                Gui.drawRect(sx, sy, sx + 18, sy + 18, 0xFF8B8B8B);
                Gui.drawRect(sx, sy, sx + 18, sy + 1, 0xFF373737);
                Gui.drawRect(sx, sy, sx + 1, sy + 18, 0xFF373737);
                Gui.drawRect(sx, sy + 17, sx + 18, sy + 18, 0xFFFFFFFF);
                Gui.drawRect(sx + 17, sy, sx + 18, sy + 18, 0xFFFFFFFF);
            }
        }
    }

    private void renderGridChrome(int cl, int ct, int cr, int cb) {
        // 启用裁剪
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        // 转换为屏幕坐标（OpenGL 坐标系 Y 轴翻转）
        int scaleFactor = new net.minecraft.client.gui.ScaledResolution(mc).getScaleFactor();
        GL11.glScissor(
                cl * scaleFactor,
                (mc.displayHeight - cb * scaleFactor),
                (cr - cl) * scaleFactor,
                (cb - ct) * scaleFactor
        );

        int gridLeft = guiLeft + gridLineX(0);
        int gridTop = guiTop + gridLineY(0);
        int gridRight = guiLeft + gridLineX(SupremeTableInventory.WIDTH);
        int gridBottom = guiTop + gridLineY(SupremeTableInventory.HEIGHT);

        // 网格边缘边框
        int b = GRID_BORDER_PX;
        Gui.drawRect(gridLeft - b, gridTop - b, gridRight + b, gridTop, GRID_BORDER);
        Gui.drawRect(gridLeft - b, gridBottom, gridRight + b, gridBottom + b, GRID_BORDER);
        Gui.drawRect(gridLeft - b, gridTop, gridLeft, gridBottom, GRID_BORDER);
        Gui.drawRect(gridRight, gridTop, gridRight + b, gridBottom, GRID_BORDER);

        // 为每个可见单元格绘制槽位精灵
        int firstX = Math.max(0, (int) Math.floor(-panOffsetX / cellSize));
        int lastX = Math.min(SupremeTableInventory.WIDTH - 1,
                (int) Math.ceil((canvasWidth() - panOffsetX) / cellSize));
        int firstY = Math.max(0, (int) Math.floor(-panOffsetY / cellSize));
        int lastY = Math.min(SupremeTableInventory.HEIGHT - 1,
                (int) Math.ceil((canvasHeight() - panOffsetY) / cellSize));
        for (int gy = firstY; gy <= lastY; gy++) {
            int sy = guiTop + gridLineY(gy);
            int height = gridLineY(gy + 1) - gridLineY(gy);
            for (int gx = firstX; gx <= lastX; gx++) {
                int sx = guiLeft + gridLineX(gx);
                int width = gridLineX(gx + 1) - gridLineX(gx);
                // 凹槽效果
                int color = 0xFF8B8B8B;
                Gui.drawRect(sx, sy, sx + width, sy + height, color);
                Gui.drawRect(sx, sy, sx + width, sy + 1, 0xFF373737);
                Gui.drawRect(sx, sy, sx + 1, sy + height, 0xFF373737);
                Gui.drawRect(sx, sy + height - 1, sx + width, sy + height, 0xFFFFFFFF);
                Gui.drawRect(sx + width - 1, sy, sx + width, sy + height, 0xFFFFFFFF);
            }
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(I18n.format("tile." + Tags.MOD_ID + ".supreme_table.name"), 8, 6, 0x404040);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
        // "?" 按钮悬停提示
        if (isMouseOverArrow(mouseX, mouseY)) {
            this.drawHoveringText(I18n.format("supreme_crafting.tooltip.show_recipes"), mouseX, mouseY);
        }
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        if (inCanvas(mouseX, mouseY)) {
            super.renderHoveredToolTip(mouseX, mouseY);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        int dWheel = Mouse.getEventDWheel();
        if (dWheel != 0 && inCanvas(Mouse.getEventX() * this.width / this.mc.displayWidth,
                this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1)) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

            int cl = guiLeft + canvasLeftRel();
            int ct = guiTop + canvasTopRel();

            double oldCell = cellSize;
            double newCell = CanvasMath.clamp(
                    cellSize * Math.pow(ZOOM_STEP, dWheel > 0 ? 1 : -1),
                    MIN_CELL, MAX_CELL);
            if (newCell != oldCell) {
                panOffsetX = CanvasMath.zoomCenteredPan(panOffsetX, oldCell, newCell, mouseX - cl);
                panOffsetY = CanvasMath.zoomCenteredPan(panOffsetY, oldCell, newCell, mouseY - ct);
                cellSize = newCell;
                panOffsetX = CanvasMath.clampPan(panOffsetX, canvasWidth(), cellSize, SupremeTableInventory.WIDTH, EDGE_PAD);
                panOffsetY = CanvasMath.clampPan(panOffsetY, canvasHeight(), cellSize, SupremeTableInventory.HEIGHT, EDGE_PAD);
            }
        }
        super.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0 && isMouseOverArrow(mouseX, mouseY)) {
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
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (panning && clickedMouseButton == 2) {
            panOffsetX = CanvasMath.clampPan(
                    panOffsetX + (mouseX - dragLastX),
                    canvasWidth(), cellSize, SupremeTableInventory.WIDTH, EDGE_PAD);
            panOffsetY = CanvasMath.clampPan(
                    panOffsetY + (mouseY - dragLastY),
                    canvasHeight(), cellSize, SupremeTableInventory.HEIGHT, EDGE_PAD);
            dragLastX = mouseX;
            dragLastY = mouseY;
        } else {
            super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (panning && state == 2) {
            panning = false;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }
}
