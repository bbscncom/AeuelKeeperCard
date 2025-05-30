package com.bbscncom.keepcard.mixins.keeper;


import appeng.parts.automation.UpgradeInventory;
import com.bbscncom.keepcard.keeper.IExtendedUpgradeInventory;
import com.bbscncom.keepcard.keeper.ItemKeeperUpgrade;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = { "appeng.parts.automation.UpgradeInventory$UpgradeInvFilter" }, remap = false)
public class MixinUpgradeInvFilter {
	@Shadow
	@Final
	UpgradeInventory this$0;

	@Inject(method = "allowInsert", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;",
		shift = At.Shift.AFTER,
		remap = true
	), cancellable = true)
	private void injectAllowInsert(IItemHandler inv, int slot, ItemStack itemstack,
								   CallbackInfoReturnable<Boolean> cir) {
		if (this.this$0 instanceof IExtendedUpgradeInventory outer && itemstack.getItem() instanceof ItemKeeperUpgrade niu) {
			var u = niu.getType(itemstack);
			if (u == ItemKeeperUpgrade.typeId) {
				cir.setReturnValue(outer.getInstalledUpgrades(u) < outer.getMaxInstalled(u));
			}
		}
	}
}