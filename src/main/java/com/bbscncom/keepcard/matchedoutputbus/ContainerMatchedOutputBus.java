package com.bbscncom.keepcard.matchedoutputbus;

import appeng.api.AEApi;
import appeng.api.config.*;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerUpgradeable;
import appeng.container.slot.OptionalSlotFakeTypeOnly;
import appeng.container.slot.SlotFakeTypeOnly;
import appeng.container.slot.SlotRestrictedInput;
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.iterators.NullIterator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.Iterator;

public class ContainerMatchedOutputBus extends ContainerUpgradeable {
    private final PartMatchedOutputBus storageBus;
    public ContainerMatchedOutputBus(InventoryPlayer ip, PartMatchedOutputBus te) {
        super(ip, te);
        this.storageBus = te;
    }

    protected int getHeight() {
        return 251;
    }

    protected void setupConfig() {
        IItemHandler config = this.getUpgradeable().getInventoryByName("config");

        for(int y = 0; y < 7; ++y) {
            for(int x = 0; x < 9; ++x) {
                if (y < 2) {
                    this.addSlotToContainer(new SlotFakeTypeOnly(config, y * 9 + x, 8 + x * 18, 29 + y * 18));
                } else {
                    this.addSlotToContainer(new OptionalSlotFakeTypeOnly(config, this, y * 9 + x, 8, 29, x, y, y - 2));
                }
            }
        }

        IItemHandler upgrades = this.getUpgradeable().getInventoryByName("upgrades");
        this.addSlotToContainer((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 0, 187, 8, this.getInventoryPlayer())).setNotDraggable());
        this.addSlotToContainer((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 1, 187, 26, this.getInventoryPlayer())).setNotDraggable());
        this.addSlotToContainer((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 2, 187, 44, this.getInventoryPlayer())).setNotDraggable());
        this.addSlotToContainer((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 3, 187, 62, this.getInventoryPlayer())).setNotDraggable());
        this.addSlotToContainer((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 4, 187, 80, this.getInventoryPlayer())).setNotDraggable());
    }

    protected boolean supportCapacity() {
        return true;
    }

    public int availableUpgrades() {
        return 5;
    }

    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        this.standardDetectAndSendChanges();
    }

    @Override
    public boolean isSlotEnabled(int idx) {
//        int upgrades = this.getUpgradeable().getInstalledUpgrades(Upgrades.CAPACITY);
//        return upgrades > idx;
        return true;
    }

}
