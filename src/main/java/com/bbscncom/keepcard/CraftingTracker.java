package com.bbscncom.keepcard;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.*;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import io.github.phantamanta44.libnine.util.ImpossibilityRealizedException;
import net.minecraft.item.Item;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class CraftingTracker {
    private final ICraftingRequester owner;
    private final HashBiMap<Item,Future<ICraftingJob>> jobs;
    private final HashBiMap<Item,ICraftingLink> links;

    @SuppressWarnings("unchecked")
    public CraftingTracker(ICraftingRequester owner) {
        this.owner = owner;
        this.jobs = HashBiMap.create();
        this.links = HashBiMap.create();
    }

    public boolean requestCrafting(IAEItemStack item, World world, IGrid grid, ICraftingGrid crafting, IActionSource actionSrc) {
        if (links.get(item.getItem()) != null) {
            return false;
        }
        Future<ICraftingJob> jobTask = jobs.get(item.getItem());
        if (jobTask == null) {
            jobs.put(item.getItem(),crafting.beginCraftingJob(world, grid, actionSrc, item.copy(), null));
        } else if (jobTask.isDone()) {
            try {
                ICraftingJob job = jobTask.get();
                if (job != null) {
                    ICraftingLink link = crafting.submitJob(job, owner, null, false, actionSrc);
                    jobs.put(item.getItem(),null);
                    if (link != null) {
                        links.put(item.getItem(),link);
                        updateLinks();
                        return true;
                    }
                }
            } catch (ExecutionException e) {
                return false;
            } catch (InterruptedException e) {
                throw new ImpossibilityRealizedException(e);
            }
        }
        return false;
    }


    private void updateLinks() {
        Iterator<Map.Entry<Item, ICraftingLink>> iterator = links.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<Item, ICraftingLink> next = iterator.next();
            if(next.getValue().isCanceled()||next.getValue().isDone()) {
                iterator.remove();
            }
        }
    }

    public ImmutableSet<ICraftingLink> getRequestedJobs() {
       return  ImmutableSet.copyOf(links.values());
    }

    public boolean onJobStateChange(final ICraftingLink link) {
        links.inverse().keySet().remove(link);
        updateLinks();
        return true;
    }

    public Item getItemForJob(ICraftingLink link) {
        return links.inverse().get(link);
    }
}
