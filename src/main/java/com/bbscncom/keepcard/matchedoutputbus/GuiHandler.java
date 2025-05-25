package com.bbscncom.keepcard.matchedoutputbus;

import appeng.api.parts.IPart;
import appeng.api.util.AEPartLocation;
import appeng.tile.networking.TileCableBus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import org.jetbrains.annotations.Nullable;

public class GuiHandler implements IGuiHandler {

    public static int getGuiIDFromOrdinal(int ordinal) {
        return ordinal >> 4;
    }

    public static AEPartLocation getSideFromOrdinal(int ordinal) {
        return AEPartLocation.fromOrdinal(ordinal & 7);
    }
    @Nullable
    @Override
    public Object getServerGuiElement(int IDWithPart, EntityPlayer player, World world, int x, int y, int z) {
        int guiIDFromOrdinal = getGuiIDFromOrdinal(IDWithPart);
        AEPartLocation sideFromOrdinal = getSideFromOrdinal(IDWithPart);
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        if(guiIDFromOrdinal==1){
            if(tileEntity instanceof TileCableBus bus){
                IPart part = bus.getPart(sideFromOrdinal);
                if(part instanceof PartMatchedOutputBus matchedOutputBus){
                    return new ContainerMatchedOutputBus(player.inventory,matchedOutputBus);
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int IDWithPart, EntityPlayer player, World world, int x, int y, int z) {
        int guiIDFromOrdinal = getGuiIDFromOrdinal(IDWithPart);
        AEPartLocation sideFromOrdinal = getSideFromOrdinal(IDWithPart);
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        if(guiIDFromOrdinal==1){
            if(tileEntity instanceof TileCableBus bus){
                IPart part = bus.getPart(sideFromOrdinal);
                if(part instanceof PartMatchedOutputBus matchedOutputBus){
                    return new GuiMatchedOutputBus(player.inventory,matchedOutputBus);
                }
            }
        }
        return null;
    }
}
