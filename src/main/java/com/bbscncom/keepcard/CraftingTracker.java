package com.bbscncom.keepcard;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.item.Item;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class CraftingTracker {
    private final ICraftingRequester owner;
    private final HashBiMap<IAEItemStack,Future<ICraftingJob>> jobs;
    private final HashBiMap<IAEItemStack,ICraftingLink> links;

    @SuppressWarnings("unchecked")
    public CraftingTracker(ICraftingRequester owner) {
        this.owner = owner;
        this.jobs = HashBiMap.create();
        this.links = HashBiMap.create();
    }

    public boolean requestCrafting(IAEItemStack item, World world, IGrid grid, ICraftingGrid crafting, IActionSource actionSrc) {
        if (links.get(item) != null) {
            return false;
        }
        Future<ICraftingJob> jobTask = jobs.get(item);
        if (jobTask == null) {
            jobs.put(item,crafting.beginCraftingJob(world, grid, actionSrc, item.copy(), null));
        } else if (jobTask.isDone()) {
            try {
                ICraftingJob job = jobTask.get();
                if (job != null) {
                    ICraftingLink link = crafting.submitJob(job, owner, null, false, actionSrc);
                    jobs.remove(item);
                    if (link != null) {
                        links.put(item,link);
                        updateLinks();
                        return true;
                    }
                }
            } catch (ExecutionException e) {
                return false;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }


    private void updateLinks() {
        Iterator<Map.Entry<IAEItemStack, ICraftingLink>> iterator = links.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<IAEItemStack, ICraftingLink> next = iterator.next();
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

    public IAEItemStack getItemForJob(ICraftingLink link) {
        return links.inverse().get(link);
    }
}
