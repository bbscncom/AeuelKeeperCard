package com.bbscncom.keepcard.matchedoutputbus;


import appeng.api.config.AccessRestriction;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.core.sync.packets.PacketSwitchGuis;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class GuiMatchedOutputBus extends GuiUpgradeable {
    public GuiMatchedOutputBus(InventoryPlayer inventoryPlayer, PartMatchedOutputBus te) {
        super(new ContainerMatchedOutputBus(inventoryPlayer, te));
        this.ySize = 251;
    }

    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.fontRenderer.drawString(this.getGuiDisplayName(GuiText.StorageBus.getLocal()), 8, 6, 4210752);
        this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);
    }

    protected String getBackground() {
        return "guis/storagebus.png";
    }

    protected void actionPerformed(GuiButton btn) throws IOException {
        super.actionPerformed(btn);
        boolean backwards = Mouse.isButtonDown(1);
    }
}
