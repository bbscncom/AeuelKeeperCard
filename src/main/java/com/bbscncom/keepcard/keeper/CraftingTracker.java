package com.bbscncom.keepcard.keeper;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.CraftingLink;
import com.bbscncom.keepcard.Main;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;


public class CraftingTracker {
    private final ICraftingRequester owner;
    private final BiMap<IAEItemStack, JobContent> jobs;
    private final LinkedBlockingDeque<IAEItemStack> jobQueue;
    private final BiMap<IAEItemStack, ICraftingLink> links;
    private final Map<ICraftingLink, IActionSource> actionsources;

    public CraftingTracker(ICraftingRequester owner) {
        this.owner = owner;

        this.jobs = Maps.synchronizedBiMap(HashBiMap.create());
        jobQueue = new LinkedBlockingDeque<>(1000);

        this.links = Maps.synchronizedBiMap(HashBiMap.create());
        this.actionsources = new HashMap<>();
    }

    public boolean requestCrafting(IAEItemStack item, World world, IGrid grid, ICraftingGrid crafting, IActionSource actionSrc) {
        if (links.get(item) != null) {
            return false;
        }
        if (jobs.get(item) == null) {
            Future<ICraftingJob> job = crafting.beginCraftingJob(world, grid, actionSrc, item.copy(), null);

            boolean offer = jobQueue.offer(item);
            if (offer) {
                jobs.put(item, new JobContent(
                        job,
                        world,
                        grid,
                        crafting,
                        actionSrc));
            }
        }
        return false;
    }

    public void beginCraftingJobs(int num) {
        updateLinks();
        ArrayList<IAEItemStack> shouldInsertBack = new ArrayList<>();
        try {
            for (int i = 0; i < num; i++) {
                IAEItemStack peek = jobQueue.peek();
                if (peek == null) return;
                if (links.get(peek) != null) {
                    jobQueue.poll();
                    jobs.remove(peek);
                    continue;
                }

                IAEItemStack item = jobQueue.poll();
                JobContent jobContent = jobs.remove(item);
                if(jobContent==null){
                    Main.LOGGER.warn("crafting jobs handle's concurrent bug, jobContent should not null");
                    continue;
                }

                if(jobContent.future.isCancelled()) continue;
                if (!jobContent.future.isDone()) {
                    shouldInsertBack.add(item);
                    continue;
                }

                CraftingLink link = (CraftingLink) jobContent.crafting.submitJob(jobContent.future.get(),
                        owner,
                        null,
                        false,
                        jobContent.actionSrc);
                if (link != null) {
                    CraftingLinkWrapper craftingLinkWrapper = new CraftingLinkWrapper(owner, link);
                    links.put(item, craftingLinkWrapper);
                    actionsources.put(craftingLinkWrapper, jobContent.actionSrc);
                }
            }

            for (IAEItemStack iaeItemStack : shouldInsertBack) {
                jobQueue.offerFirst(iaeItemStack);
            }

        } catch (ExecutionException ignored) {

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateLinks() {
        Iterator<Map.Entry<IAEItemStack, ICraftingLink>> iterator = links.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<IAEItemStack, ICraftingLink> next = iterator.next();
            if (next.getValue().isCanceled() || next.getValue().isDone()) {
                iterator.remove();
            }
        }
        var iterator2 = actionsources.entrySet().iterator();
        while (iterator2.hasNext()) {
            var next = iterator2.next();
            if (next.getKey().isCanceled() || next.getKey().isDone()) {
                iterator2.remove();
            }
        }
    }

    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return ImmutableSet.copyOf(links.values());
    }

    public boolean onJobStateChange(final ICraftingLink link) {
        links.inverse().keySet().remove(link);
        actionsources.keySet().remove(link);
        return true;
    }

    public IAEItemStack getItemForJob(ICraftingLink link) {
        return links.inverse().get(link);
    }

    public IActionSource getActionSourceForJob(ICraftingLink link) {
        return actionsources.get(link);
    }

    public static class JobContent {
        Future<ICraftingJob> future;
        World world;
        IGrid grid;
        ICraftingGrid crafting;
        IActionSource actionSrc;

        public JobContent(Future<ICraftingJob> future, World world, IGrid grid, ICraftingGrid crafting, IActionSource actionSrc) {
            this.future = future;
            this.world = world;
            this.grid = grid;
            this.crafting = crafting;
            this.actionSrc = actionSrc;
        }
    }

}
