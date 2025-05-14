package com.bbscncom.keepcard;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.helpers.DualityInterface;
import appeng.items.misc.ItemEncodedPattern;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.MachineSource;
import appeng.tile.grid.AENetworkTile;
import appeng.tile.misc.TileInterface;
import com.google.common.collect.ImmutableSet;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;

public class TileKeeperController extends AENetworkTile implements IGridTickable, ICraftingRequester {
    public AENetworkProxy gridProxy = new AENetworkProxy(this, "MyTickingTile", this.getItemFromTile(this), true);

    public Optional<IGrid> aeGrid() {
        return Optional.ofNullable(getProxy().getNode()).map(IGridNode::getGrid);
    }

    public final CraftingTracker craftingTracker = new CraftingTracker(this);
    public MachineSource actionSource = new MachineSource(this);
    public TileKeeperController() {
    }


    @Nonnull
    @Override
    public IGridNode getGridNode(@Nullable AEPartLocation dir) {
        return this.gridProxy.getNode();
    }

    @NotNull
    @Override
    public AECableType getCableConnectionType(@NotNull AEPartLocation dir) {
        return AECableType.DENSE_SMART;
    }


    @Nonnull
    @Override
    public TickingRequest getTickingRequest(@Nonnull IGridNode node) {
        return new TickingRequest(400, 400, false, true);
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(@Nonnull IGridNode node, int ticksSinceLastCall) {
        IGrid grid = node.getGrid();
        IMachineSet machines = grid.getMachines(TileInterface.class);
        for (IGridNode machine : machines) {
            TileInterface controller = (TileInterface) machine.getMachine();
            GetInstalledUpgrades getInstalledUpgrades = (GetInstalledUpgrades) controller.getInterfaceDuality();
            int installedUpgrades = getInstalledUpgrades.getInstalledUpgrades(ItemKeeperUpgrade.typeId);
            if (installedUpgrades == 0) continue;

            DualityInterface interfaceDuality = controller.getInterfaceDuality();
            IItemHandler upgrades = interfaceDuality.getInventoryByName("upgrades");
            ItemStack keeper = null;
            for (int i = 0; i < 4; i++) {
                ItemStack upgradesStackInSlot = upgrades.getStackInSlot(i);
                if(upgradesStackInSlot.getItem()==ItemKeeperUpgrade.item)
                    keeper=upgradesStackInSlot;break;
            }

            IItemHandler patterns = controller.getInventoryByName("patterns");
            for (int i = 0; i < patterns.getSlots(); i++) {
                ItemStack stackInSlot = patterns.getStackInSlot(i);
                if (stackInSlot == ItemStack.EMPTY) continue;
                if (stackInSlot.getItem() instanceof ItemEncodedPattern encodedPattern) {
                    final ICraftingPatternDetails details = encodedPattern.getPatternForItem(stackInSlot, world);
                    IAEItemStack[] condensedOutputs = details.getCondensedOutputs();
                    tick(grid, condensedOutputs,keeper);
                }
            }

        }
        return TickRateModulation.SAME;
    }

    protected void tick(IGrid grid, IAEItemStack[] stacks, ItemStack stackInSlot) {
        ICraftingGrid crafting = (ICraftingGrid) grid.getCache(ICraftingGrid.class);
        IMEMonitor<IAEItemStack> storageGrid = ((IStorageGrid) grid.getCache(IStorageGrid.class)).getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));

        for (IAEItemStack stack : stacks) {
            IAEItemStack storage = storageGrid.getStorageList().findPrecise(stack);
            int[] nums = ItemKeeperUpgrade.getNums(stackInSlot);
            int shouldKeep = nums[0];
            int perCraft = nums[1];
            long storageNumber = storage.getStackSize();
            if(shouldKeep<=storageNumber)continue;
            long shouldCraft = Math.min(shouldKeep-storageNumber,  perCraft);
            if (shouldCraft == 0) continue;

            IAEItemStack copy = stack.copy();
            copy.setStackSize(shouldCraft);
            this.craftingTracker.requestCrafting(copy, world, grid, crafting, this.actionSource);
        }
    }

    @Override
    public AENetworkProxy getProxy() {
        return gridProxy;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    public void gridChanged() {
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return craftingTracker.getRequestedJobs();
    }

    @javax.annotation.Nullable
    @Override
    public IAEItemStack injectCraftedItems(ICraftingLink link, @javax.annotation.Nullable IAEItemStack stack, Actionable mode) {
        if (stack == null) {
            return null;
        }
        Item itemForJob = craftingTracker.getItemForJob(link);
        if (itemForJob == null) {
            return stack;
        }
        try {
            IMEMonitor<IAEItemStack> storageGrid = ((IStorageGrid) gridProxy.getGrid().getCache(IStorageGrid.class)).getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
            return storageGrid.injectItems(stack, mode, null);
        } catch (GridAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        if (craftingTracker.onJobStateChange(link)) {
        }
    }
}
