package com.bbscncom.keepcard;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.definitions.IDefinitions;
import appeng.core.Api;
import com.bbscncom.keepcard.mixins.UpgradesAccessor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixins;
import zone.rong.mixinbooter.ILateMixinLoader;
import zone.rong.mixinbooter.MixinLoader;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mod(modid = Main.MOD_ID, name = Main.NAME, version = Main.VERSION ,dependencies="required-after:appliedenergistics2;required-after:mixinbooter;")
@Mod.EventBusSubscriber(modid = Main.MOD_ID)
//@MixinLoader
public class Main
implements ILateMixinLoader
{
    public static final String MOD_ID = "aeuelkeepercard";
    public static final Logger LOGGER = LogManager.getLogger(Main.MOD_ID);
    public static final String NAME = "Aeuel KeeperCard";
    public static final String VERSION = "1.0";
    @Mod.Instance(Main.MOD_ID)
    public static Main instance;

    public Main() {
//        Mixins.addConfiguration("mixins." + MOD_ID + ".json");
        LOGGER.info("hello");
    }


    @Mod.EventHandler
    public void registerItems(FMLPreInitializationEvent event) {
        ItemKeeperUpgrade.onFMLLoadComplete(event);
    }

    @Mod.EventHandler
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        IDefinitions definitions = AEApi.instance().definitions();

        Upgrades targetUpgrade = Upgrades.CAPACITY;
        Map<ItemStack, Integer> supportedMap = ((UpgradesAccessor) (Object) targetUpgrade).getSupportedMax();
        supportedMap.put(new ItemStack(ItemKeeperUpgrade.item), 1);

        ServerboundSetKeepNum.registerPackets();

        ItemKeeperUpgrade.allow.add(Api.INSTANCE.definitions().parts().iface());
        ItemKeeperUpgrade.allow.add(Api.INSTANCE.definitions().blocks().iface());
    }

    public List<String> getMixinConfigs() {
        return Collections.singletonList("mixins." + MOD_ID + ".json");
    }
}
