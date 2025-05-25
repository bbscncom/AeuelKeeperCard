package com.bbscncom.keepcard.keeper;

import appeng.api.definitions.IBlockDefinition;
import appeng.block.AEBaseTileBlock;
import appeng.core.features.ActivityState;
import appeng.core.features.BlockDefinition;
import appeng.core.features.BlockStackSrc;
import appeng.core.features.TileDefinition;
import appeng.tile.AEBaseTile;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class BlockDefinitionBuilder {
    private final Block block;
    private final ItemBlock item;

    private final Class tile;

    public BlockDefinitionBuilder(Block block, ItemBlock item, Class tile) {
        this.block = block;
        this.item = item;
        this.tile = tile;
    }


    public <T extends IBlockDefinition> T build() {
        Object definition;
        if (block instanceof AEBaseTileBlock) {
            ((AEBaseTileBlock) block).setTileEntity(tile);

            AEBaseTile.registerTileItem(((AEBaseTileBlock) block).getTileEntityClass(), new BlockStackSrc(block, 0, ActivityState.Enabled));

            definition = new TileDefinition(this.block.getRegistryName().getPath(), (AEBaseTileBlock) block, item);
        } else {
            definition = new BlockDefinition(this.block.getRegistryName().getPath(), block, item);
        }

        return (T) definition;
    }

}
