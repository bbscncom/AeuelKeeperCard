package com.bbscncom.keepcard.matchedoutputbus;

import appeng.api.AEApi;
import appeng.api.config.*;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.*;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IConfigManager;
import appeng.capabilities.Capabilities;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.core.sync.GuiBridge;
import appeng.helpers.IPriorityHost;
import appeng.helpers.Reflected;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.cache.GridStorageCache;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.ITickingMonitor;
import appeng.me.storage.MEInventoryHandler;
import appeng.parts.PartModel;
import appeng.parts.automation.PartSharedItemBus;
import appeng.parts.automation.PartUpgradeable;
import appeng.parts.misc.PartInterface;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.misc.TileInterface;
import appeng.tile.networking.TileCableBus;
import appeng.util.ConfigManager;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;
import appeng.util.prioritylist.FuzzyPriorityList;
import appeng.util.prioritylist.PrecisePriorityList;
import com.bbscncom.keepcard.Main;
import com.bbscncom.keepcard.keeper.ItemKeeperUpgrade;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.primitives.Ints;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import thaumicenergistics.util.ForgeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PartMatchedOutputBus extends PartSharedItemBus implements IGridTickable {
    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "part/export_bus_base");
    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, new ResourceLocation(AppEng.MOD_ID, "part/export_bus_off"));
    @PartModels
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, new ResourceLocation(AppEng.MOD_ID, "part/export_bus_on"));
    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, new ResourceLocation(AppEng.MOD_ID, "part/export_bus_has_channel"));
    protected final IActionSource mySrc;
    protected final AppEngInternalAEInventory Config = new AppEngInternalAEInventory(this, 63);
    protected int priority = 0;
    protected boolean cached = false;
    protected ITickingMonitor monitor = null;
    protected MEInventoryHandler<IAEItemStack> handler = null;
    protected int handlerHash = 0;
    private boolean wasActive = false;
    private byte resetCacheLogic = 0;
    private boolean accessChanged;
    private boolean readOncePass;
    private long itemToSend;
    private boolean didSomething;
    private int nextSlot;

    @Reflected
    public PartMatchedOutputBus(final ItemStack is) {
        super(is);
        this.mySrc = new MachineSource(this);
    }

    @Override
    @MENetworkEventSubscribe
    public void powerRender(final MENetworkPowerStatusChange c) {
        this.updateStatus();
    }

    private void updateStatus() {
        final boolean currentActive = this.getProxy().isActive();
        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
            //                this.getProxy().getGrid().postEvent(new MENetworkCellArrayUpdate());
            this.getHost().markForUpdate();
        }
    }

    @Override
    @MENetworkEventSubscribe
    public void chanRender(final MENetworkChannelsChanged changedChannels) {
        this.updateStatus();
    }

    @Override
    protected int getUpgradeSlots() {
        return 5;
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Enum settingName, final Enum newValue) {
        this.getHost().markForSave();
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack) {
        super.onChangeInventory(inv, slot, mc, removedStack, newStack);
    }

    @Override
    public void upgradesChanged() {
        super.upgradesChanged();
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.Config.readFromNBT(data, "config");
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        this.Config.writeToNBT(data, "config");
    }

    @Override
    public IItemHandler getInventoryByName(final String name) {
        if (name.equals("config")) {
            return this.Config;
        }

        return super.getInventoryByName(name);
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(3, 3, 15, 13, 13, 16);
        bch.addBox(2, 2, 14, 14, 14, 15);
        bch.addBox(5, 5, 12, 11, 11, 14);
    }

    @Override
    public void onNeighborChanged(IBlockAccess w, BlockPos pos, BlockPos neighbor) {
        if (pos.offset(this.getSide().getFacing()).equals(neighbor)) {
            final TileEntity te = w.getTileEntity(neighbor);
        }
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 4;
    }

    @Override
    public boolean onPartActivate(final EntityPlayer player, final EnumHand hand, final Vec3d pos) {
        if (player.isSneaking() && player.getHeldItem(hand) != ItemStack.EMPTY) {
            return false;
        } else {
            if (ForgeUtil.isServer()) {
                BlockPos pos1 = this.getTile().getPos();
                player.openGui(Main.instance, calculateOrdinal(this.getSide()), player.getEntityWorld(), pos1.getX(), pos1.getY(), pos1.getZ());
            }
            return true;
        }
    }

    public static int calculateOrdinal(AEPartLocation side) {
        if (side == null) {
            side = AEPartLocation.UP;
        }

        return 1 << 4 | side.ordinal();
    }


    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(5, 60, false, true);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        if (this.monitor != null) {
            return this.monitor.onTick();
        }
        return doBusWork();
    }

    private int getStartingSlot(SchedulingMode schedulingMode, int x) {
        if (schedulingMode == SchedulingMode.RANDOM) {
            return Platform.getRandom().nextInt(this.availableSlots());
        } else {
            return schedulingMode == SchedulingMode.ROUNDROBIN ? (this.nextSlot + x) % this.availableSlots() : x;
        }
    }

    protected TickRateModulation doBusWork() {
        if (!this.getProxy().isActive() || !this.canDoBusWork()) {
            return TickRateModulation.IDLE;
        }

        this.didSomething = false;

        try {
            final InventoryAdaptor destination = this.getHandler();
            final IMEMonitor<IAEItemStack> inv = this.getProxy().getStorage().getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
            final IEnergyGrid energy = this.getProxy().getEnergy();
            final ICraftingGrid cg = this.getProxy().getCrafting();
            final SchedulingMode schedulingMode = SchedulingMode.ROUNDROBIN;

            int shouldkeepNum=10;
            IItemHandler upgrades = this.getInventoryByName("upgrades");
            for (int i = 0; i < upgrades.getSlots(); i++) {
                ItemStack stackInSlot = upgrades.getStackInSlot(i);
                if (stackInSlot != ItemStack.EMPTY && stackInSlot.getItem() instanceof ItemKeeperUpgrade) {
                    shouldkeepNum= ItemKeeperUpgrade.getNums(stackInSlot)[0];
                }
            }

            if (destination != null) {
                int x;
                for (x = 0; x < this.availableSlots() / 2 ; x++) {
                    final int slotToExport = this.getStartingSlot(schedulingMode, x);
                    this.itemToSend = this.calculateItemsToSend();

                    IAEItemStack shouldexport = Config.getAEStackInSlot(2 * x );
                    IAEItemStack shouldkeep = Config.getAEStackInSlot(2 * x+1);
                    if (shouldkeep == null || shouldexport == null) continue;

                    final IAEItemStack o = inv.getStorageList().findPrecise(shouldkeep);
                    if(o==null || o.getStackSize()<shouldkeepNum){
                        IAEItemStack precise = inv.getStorageList().findPrecise(shouldexport);

                        if (precise != null && precise.getStackSize() > 0) {
                            this.pushItemIntoTarget(destination, energy, inv, precise);
                        }
                    }
                }
                this.updateSchedulingMode(schedulingMode, x);
            } else {
                return TickRateModulation.SLEEP;
            }
        } catch (final GridAccessException e) {
            // :P
        }

        return this.didSomething ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
    }

    private void pushItemIntoTarget(final InventoryAdaptor d, final IEnergyGrid energy, final IMEInventory<IAEItemStack> inv, IAEItemStack org) {
        ItemStack inputStack = org.getCachedItemStack(org.getStackSize());

        ItemStack remaining = d.simulateAdd(inputStack);

        // Store the stack in the cache for next time.
        if (!remaining.isEmpty()) {
            org.setCachedItemStack(remaining);
            if (remaining == inputStack) {
                return;
            }
        }

        final long canFit = Math.min(this.itemToSend, org.getStackSize() - remaining.getCount());

        if (canFit > 0) {
            IAEItemStack ais = org.copy();
            ais.setStackSize(canFit);
            final IAEItemStack itemsToAdd = Platform.poweredExtraction(energy, inv, ais, this.mySrc);

            if (itemsToAdd != null) {
                this.itemToSend -= itemsToAdd.getStackSize();

                inputStack.setCount(Ints.saturatedCast(itemsToAdd.getStackSize()));

                final ItemStack failed = d.addItems(inputStack);
                if (!failed.isEmpty()) {
                    ais.setStackSize(failed.getCount());
                    inv.injectItems(ais, Actionable.MODULATE, this.mySrc);
                } else {
                    this.didSomething = true;
                }
            } else {
                org.setCachedItemStack(inputStack);
            }
        }
    }

    private void updateSchedulingMode(SchedulingMode schedulingMode, int x) {
        if (schedulingMode == SchedulingMode.ROUNDROBIN) {
            this.nextSlot = (this.nextSlot + x) % availableSlots();
        }
    }

    @Override
    protected int availableSlots() {
        return Config.getSlots();
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }

    protected Iterable<IAEItemStack> filterChanges(Iterable<IAEItemStack> change) {
        var storageFilter = this.getConfigManager().getSetting(Settings.STORAGE_FILTER);
        if (storageFilter == StorageFilter.EXTRACTABLE_ONLY && handler != null) {
            var filteredList = new ArrayList<IAEItemStack>();
            for (final IAEItemStack stack : change) {
                if (this.handler.passesBlackOrWhitelist(stack)) {
                    filteredList.add(stack);
                }
            }

            return filteredList;
        }
        return change;
    }
}
