package com.bbscncom.keepcard.mixins.base.machine;

import appeng.api.definitions.IItemDefinition;
import appeng.parts.automation.BlockUpgradeInventory;
import com.bbscncom.keepcard.ItemKeeperUpgrade;
import com.bbscncom.keepcard.mixins.base.MixinUpgradeInventory;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(value = BlockUpgradeInventory.class, remap = false)
public abstract class MixinBlockUpgradeInventory extends MixinUpgradeInventory {
    @Shadow
    @Final
    private Block block;

    @Unique
    @Override
    public int getMaxInstalled(Integer upgrades) {
        AtomicInteger max = new AtomicInteger();

//        var encodedItem = Api.INSTANCE.definitions().blocks().iface();
//
        List<IItemDefinition> allow = ItemKeeperUpgrade.allow;
        for (IItemDefinition iItemDefinition : allow) {
            Item item = iItemDefinition.maybeStack(1).get().getItem();
            if (item instanceof ItemBlock && Block.getBlockFromItem(item) == this.block) {
                max.set(1);
            }
        }

        return max.get();
    }
}
