package com.shiver.supremecrafting.client;

import com.shiver.supremecrafting.net.PacketReturnTableItems;
import com.shiver.supremecrafting.net.SCNetwork;
import com.shiver.supremecrafting.table.ContainerSupremeTable;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import java.io.IOException;

public class GuiSupremeTable extends GuiSupremeCraftingBase {
    private static final int RETURN_BUTTON_ID = 300;

    private final ContainerSupremeTable tableContainer;

    public GuiSupremeTable(ContainerSupremeTable container, InventoryPlayer playerInv) {
        super(container);
        this.tableContainer = container;
    }

    @Override
    protected String titleKey() {
        return "tile.supreme_crafting.supreme_table.name";
    }

    @Override
    protected void refreshCraftingResult() {
        tableContainer.refreshCraftingResult();
    }

    @Override
    protected void initExtraButtons() {
        int x = guiLeft + ContainerSupremeTable.RESULT_SLOT_X - 2;
        int y = guiTop + ContainerSupremeTable.RESULT_SLOT_Y + 24;
        buttonList.add(new GuiButton(RETURN_BUTTON_ID, x, y, 20, 20, "E"));
    }

    @Override
    protected boolean handleExtraButton(GuiButton button) throws IOException {
        if (button.id != RETURN_BUTTON_ID) {
            return false;
        }
        SCNetwork.CHANNEL.sendToServer(new PacketReturnTableItems());
        return true;
    }
}
