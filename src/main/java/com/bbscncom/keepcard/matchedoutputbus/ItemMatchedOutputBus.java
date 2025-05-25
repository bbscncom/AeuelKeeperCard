package com.bbscncom.keepcard.matchedoutputbus;

import appeng.api.AEApi;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import com.bbscncom.keepcard.keeper.ItemKeeperUpgrade;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;
import thaumicenergistics.part.PartEssentiaExportBus;

import java.util.List;

public class ItemMatchedOutputBus extends Item implements IPartItem {

    public ItemMatchedOutputBus() {
        this.setCreativeTab(ItemKeeperUpgrade.tabs);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        AEApi.instance().registries().partModels().registerModels(PartEssentiaExportBus.MODELS);
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation("aeuelkeepercard:part/matchedoutputbus"));
    }

    @Nullable
    @Override
    public IPart createPartFromItemStack(ItemStack itemStack) {
        return new PartMatchedOutputBus(itemStack);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        return AEApi.instance().partHelper().placeBus(player.getHeldItem(hand), pos, side, player, hand, world);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        String format = I18n.format("item.aeuelkeepercard.matchedoutputbus.desc");
        for (String s : format.split("\\\\n")) {
            tooltip.add(s);
        }
    }
}
