package com.bbscncom.keepcard;

import appeng.api.definitions.IBlockDefinition;
import appeng.api.definitions.IItemDefinition;
import appeng.block.AEBaseTileBlock;
import appeng.bootstrap.IBootstrapComponent;
import appeng.bootstrap.definitions.TileEntityDefinition;
import appeng.core.features.ActivityState;
import appeng.core.features.BlockDefinition;
import appeng.core.features.BlockStackSrc;
import appeng.core.features.TileDefinition;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;
import co.neeve.nae2.common.features.IFeature;
import co.neeve.nae2.common.integration.jei.NAEJEIPlugin;
import co.neeve.nae2.common.registration.registry.rendering.NAEBlockRendering;
import co.neeve.nae2.common.registration.registry.rendering.NAEItemRendering;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

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
