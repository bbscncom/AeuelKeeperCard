package com.bbscncom.keepcard.mixins.base;

import appeng.me.cluster.implementations.CraftingCPUCluster;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = CraftingCPUCluster.class, remap = false)
public class MixinKeeper {
}
