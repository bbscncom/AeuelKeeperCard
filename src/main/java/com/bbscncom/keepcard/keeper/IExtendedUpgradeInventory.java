package com.bbscncom.keepcard.keeper;



public interface IExtendedUpgradeInventory {
	int getInstalledUpgrades(Integer u);

	int getMaxInstalled(Integer u);

	void markDirty();
}
