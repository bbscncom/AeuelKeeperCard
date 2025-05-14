package com.bbscncom.keepcard.mixins.base.machine;

import appeng.api.definitions.IItemDefinition;
import appeng.parts.automation.StackUpgradeInventory;
import com.bbscncom.keepcard.ItemKeeperUpgrade;
import com.bbscncom.keepcard.mixins.base.MixinUpgradeInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(value = StackUpgradeInventory.class, remap = false)
public class MixinStackUpgradeInventory extends MixinUpgradeInventory {
    @Shadow
    @Final
    private ItemStack stack;

    @Unique
    @Override
    public int getMaxInstalled(Integer upgrades) {
        AtomicInteger max = new AtomicInteger();

        List<IItemDefinition> allow = ItemKeeperUpgrade.allow;
        for (IItemDefinition iItemDefinition : allow) {
            if (!iItemDefinition.maybeStack(1).isPresent()) continue;
            if (ItemStack.areItemsEqual(this.stack, iItemDefinition.maybeStack(1).get())) {
                max.set(1);
                return max.get();
            }
        }
        return max.get();
    }
}
