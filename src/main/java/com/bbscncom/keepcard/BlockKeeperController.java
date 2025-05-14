package com.bbscncom.keepcard;

import appeng.block.AEBaseTileBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockKeeperController extends AEBaseTileBlock {
    public static Class<TileKeeperController> tileclass= TileKeeperController.class;
    public BlockKeeperController() {
        super(Material.ROCK);
        this.setHardness(2.0F); // 相当于destroyTime(2)
        this.setSoundType(SoundType.METAL);
        this.setCreativeTab(ItemKeeperUpgrade.tabs);
    }


    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        TileKeeperController tileEntity = (TileKeeperController) worldIn.getTileEntity(pos);

    }


//    @Nullable
//    @Override
//    public TileEntity createTileEntity(World world, IBlockState state) {
//        return new TileKeeperController();
//    }

    @Override
    public boolean hasTileEntity(net.minecraft.block.state.IBlockState state) {
        return true;
    }
}
