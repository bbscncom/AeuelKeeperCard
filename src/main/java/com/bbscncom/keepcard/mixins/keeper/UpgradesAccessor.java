package com.bbscncom.keepcard.mixins.keeper;

import appeng.api.config.Upgrades;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = Upgrades.class,remap = false)
public interface UpgradesAccessor {
    @Accessor(value = "supportedMax",remap = false)
    @Mutable
    @Final
    Map<ItemStack, Integer> getSupportedMax();
}
