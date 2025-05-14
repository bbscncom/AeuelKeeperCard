package com.bbscncom.keepcard.mixins.base;

import appeng.parts.automation.UpgradeInventory;
import com.bbscncom.keepcard.IExtendedUpgradeInventory;
import com.bbscncom.keepcard.ItemKeeperUpgrade;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Mixin(value = UpgradeInventory.class, remap = false)
public abstract class MixinUpgradeInventory implements IExtendedUpgradeInventory {
	@Unique
	private final HashMap<Integer, Integer> installedUpgrades = new HashMap<>();
	@Shadow
	private boolean cached;

	@Shadow
	private void updateUpgradeInfo() {}

	@Override
	@Unique
	public int getInstalledUpgrades(Integer u) {
		if (!this.cached) {
			this.updateUpgradeInfo();
		}

		return this.installedUpgrades.getOrDefault(u, 0);
	}

	@Override
	@Unique
	public abstract int getMaxInstalled(Integer u);

	@Inject(method = "updateUpgradeInfo", at = @At("HEAD"))
	private void injectUpdateUpgradeInfo(CallbackInfo ci) {
		this.installedUpgrades.clear();
	}

	@Inject(method = "updateUpgradeInfo", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;",
		shift = At.Shift.AFTER,
		remap = true,
		ordinal = 0
	))
	private void injectUpdateUpgradeInfoIS(CallbackInfo ci, @Local ItemStack is) {
		var item = is.getItem();
		if (item instanceof ItemKeeperUpgrade niu) {
			var type = niu.getType(is);
			this.installedUpgrades.put(type, this.installedUpgrades.getOrDefault(type, 0) + 1);
		}
	}

	@Override
	public void markDirty() {
		this.cached = false;
	}
}
