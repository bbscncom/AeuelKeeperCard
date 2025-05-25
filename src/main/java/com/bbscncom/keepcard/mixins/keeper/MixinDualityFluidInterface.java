package com.bbscncom.keepcard.mixins.keeper;

import appeng.fluids.helper.DualityFluidInterface;
import appeng.parts.automation.UpgradeInventory;
import com.bbscncom.keepcard.keeper.GetInstalledUpgrades;
import com.bbscncom.keepcard.keeper.IExtendedUpgradeInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = DualityFluidInterface.class, remap = false)
public class MixinDualityFluidInterface implements GetInstalledUpgrades {

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
