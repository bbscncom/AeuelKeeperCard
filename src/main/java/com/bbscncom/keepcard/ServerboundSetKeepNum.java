package com.bbscncom.keepcard;

import com.bbscncom.keepercard.aeuelkeepercard.Tags;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class ServerboundSetKeepNum implements IMessage {
    private EnumHand hand; //用来读取tileentity
    private int keepNum; //需要保存的数据
    private int perCraft; //需要保存的数据

    public ServerboundSetKeepNum() {
    }

    public ServerboundSetKeepNum(EnumHand hand, int keepNum, int perCraft) {
        this.hand = hand;
        this.keepNum = keepNum;
        this.perCraft=perCraft;
    }



    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        this.hand = packetBuffer.readEnumValue(EnumHand.class);
        this.keepNum = buf.readInt();
        this.perCraft = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeEnumValue(hand);
        buf.writeInt(keepNum);
        buf.writeInt(perCraft);
    }

    public static class Handler implements IMessageHandler<ServerboundSetKeepNum, IMessage> {
        @Override
        public IMessage onMessage(ServerboundSetKeepNum message, MessageContext ctx) {
            // 确保在主线程处理
            MinecraftServer server = ctx.getServerHandler().player.getServer();
            server.addScheduledTask(() -> {
                EntityPlayerMP player = ctx.getServerHandler().player;
                ItemStack heldItem = player.getHeldItem(message.hand);
                Item item = heldItem.getItem();
                if (item instanceof ItemKeeperUpgrade) {
                    ItemKeeperUpgrade.setNums(heldItem,message.keepNum,message.perCraft);
                }
            });
            return null;
        }
    }

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MOD_ID);

    //注册数据包
    public static void registerPackets() {
        int id = 0;
        INSTANCE.registerMessage(Handler.class, ServerboundSetKeepNum.class, id++, Side.SERVER);
    }
}

