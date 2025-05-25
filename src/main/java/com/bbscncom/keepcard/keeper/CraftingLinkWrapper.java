package com.bbscncom.keepcard.keeper;

import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.CraftingLink;
import appeng.crafting.CraftingLinkNexus;
import com.bbscncom.keepcard.Main;
import net.minecraft.nbt.NBTTagCompound;

import java.lang.reflect.Field;

public class CraftingLinkWrapper extends CraftingLink {
    public CraftingLink link;

    public static NBTTagCompound forNew;
    static {
        forNew =new NBTTagCompound();
        forNew.setBoolean("req",true);
    }

    public CraftingLinkWrapper(ICraftingRequester req,CraftingLink link){
        this(forNew.copy(), req,link);
        this.link=link;
    }
    public CraftingLinkWrapper(NBTTagCompound data, ICraftingRequester req,CraftingLink link) {
        super(data, req);
    }

    private final static Field reqField;

    static {
        try {
            reqField = CraftingLinkWrapper.class.getDeclaredField("reqField");
            reqField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void markDone() {
        link.markDone();
        ICraftingRequester req = null;
        try {
            req = (ICraftingRequester) reqField.get(link);
        } catch (IllegalAccessException e) {
            Main.LOGGER.error("CraftingLinkWrapper reflect read req field error, should not but happen: {}", e.getMessage());
        }
        if(req instanceof TileKeeperController tileKeeperController){
            tileKeeperController.craftingTracker.beginCraftingJobs(1);
        }
    }

    @Override
    public boolean isCanceled() {
        return link.isCanceled();
    }

    @Override
    public boolean isDone() {
        return link.isDone();
    }

    @Override
    public void cancel() {
        link.cancel();
    }

    @Override
    public boolean isStandalone() {
        return link.isStandalone();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        link.writeToNBT(tag);
    }

    @Override
    public String getCraftingID() {
        return link.getCraftingID();
    }

    @Override
    public void setNexus(CraftingLinkNexus n) {
        link.setNexus(n);
    }

    @Override
    public IAEItemStack injectItems(IAEItemStack input, Actionable mode) {
        return link.injectItems(input, mode);
    }
}
