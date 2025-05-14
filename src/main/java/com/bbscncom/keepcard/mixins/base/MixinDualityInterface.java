package com.bbscncom.keepcard.mixins.base;

import appeng.helpers.DualityInterface;
import appeng.parts.automation.UpgradeInventory;
import com.bbscncom.keepcard.GetInstalledUpgrades;
import com.bbscncom.keepcard.IExtendedUpgradeInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = DualityInterface.class, remap = false)
public class MixinDualityInterface implements GetInstalledUpgrades {

	@Shadow
	private UpgradeInventory upgrades;

	@Unique
	@Override
	public int getInstalledUpgrades(Integer u) {
		if (this.upgrades == null) {
			return 0;
		}
		IExtendedUpgradeInventory upgrades1 = (IExtendedUpgradeInventory) this.upgrades;
		return upgrades1.getInstalledUpgrades(u);
	}

}
