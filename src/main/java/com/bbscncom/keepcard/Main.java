package com.bbscncom.keepcard;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.definitions.IDefinitions;
import appeng.core.Api;
import appeng.core.features.ItemDefinition;
import com.bbscncom.keepcard.keeper.ItemKeeperUpgrade;
import com.bbscncom.keepcard.keeper.ServerboundSetKeepNum;
import com.bbscncom.keepcard.matchedoutputbus.GuiHandler;
import com.bbscncom.keepcard.mixins.keeper.UpgradesAccessor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mod(modid = Main.MOD_ID, name = Main.NAME, version = Main.VERSION, dependencies = "required-after:appliedenergistics2;required-after:mixinbooter;")
@Mod.EventBusSubscriber(modid = Main.MOD_ID)
//@MixinLoader
public class Main
        implements ILateMixinLoader {
    public static final String MOD_ID = "aeuelkeepercard";
    public static final Logger LOGGER = LogManager.getLogger(Main.MOD_ID);
    public static final String NAME = "Aeuel KeeperCard";
    public static final String VERSION = "1.0";

    @GameRegistry.ObjectHolder("ae2fc:dual_interface")
    public static Item fluidCraftingDualInterfaceItem;
    @Mod.Instance(Main.MOD_ID)
    public static Main instance;

    public Main() {
//        Mixins.addConfiguration("mixins." + MOD_ID + ".json");
        LOGGER.info("hello");
    }


    @Mod.EventHandler
    public void registerItems(FMLPreInitializationEvent event) {
        ItemKeeperUpgrade.onFMLLoadComplete(event);
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
    }

    @Mod.EventHandler
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        //keeper
        IDefinitions definitions = AEApi.instance().definitions();

        Upgrades targetUpgrade = Upgrades.CAPACITY;
        Map<ItemStack, Integer> supportedMap = ((UpgradesAccessor) (Object) targetUpgrade).getSupportedMax();
        supportedMap.put(new ItemStack(ItemKeeperUpgrade.item), 1);

        ServerboundSetKeepNum.registerPackets();

        ItemKeeperUpgrade.allow.add(Api.INSTANCE.definitions().parts().iface());
        ItemKeeperUpgrade.allow.add(Api.INSTANCE.definitions().blocks().iface());
        ItemKeeperUpgrade.allow.add(Api.INSTANCE.definitions().parts().fluidIface());
        ItemKeeperUpgrade.allow.add(Api.INSTANCE.definitions().blocks().fluidIface());
        if (Loader.isModLoaded("ae2fc")) {
            if (fluidCraftingDualInterfaceItem != null)
                ItemKeeperUpgrade.allow.add(new ItemDefinition("dualinterface", fluidCraftingDualInterfaceItem));
        }
        ItemKeeperUpgrade.allow.add(new ItemDefinition("matchedoutputbus", ItemKeeperUpgrade.outputbusItem));
        //outputbus
    }

    public List<String> getMixinConfigs() {
        return Collections.singletonList("mixins." + MOD_ID + ".json");
    }
}
