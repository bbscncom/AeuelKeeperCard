package com.bbscncom.keepcard;



public interface IExtendedUpgradeInventory {
	int getInstalledUpgrades(Integer u);

	int getMaxInstalled(Integer u);

	void markDirty();
}
