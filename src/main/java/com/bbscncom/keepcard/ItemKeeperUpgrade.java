package com.bbscncom.keepcard;

import appeng.api.definitions.IBlockDefinition;
import appeng.api.definitions.IItemDefinition;
import appeng.block.AEBaseBlock;
import appeng.block.AEBaseItemBlock;
import appeng.items.AEBaseItem;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class ItemKeeperUpgrade extends AEBaseItem implements IUpgradeModuleExtend {
    public static int typeId = 999;
    public static CreativeTabs tabs = new CreativeTabs(Main.MOD_ID) {
        public ItemStack createIcon() {
            return new ItemStack(item);
        }
    };

    public ItemKeeperUpgrade() {
        this.setCreativeTab(tabs);
    }

    public static List<IItemDefinition> allow = new ArrayList<>();
    public static Item item = null;
    public static Block block = null;
    public static ItemBlock itemBlock = null;

    public static void reg() {
    }

    public static void onFMLLoadComplete(FMLPreInitializationEvent event) {
        item = new ItemKeeperUpgrade();
        register("keeper", item, null);
        block = new BlockKeeperController();
        itemBlock = register("keepercontroller", block, BlockKeeperController.tileclass);
        registerItemModels();

        GameRegistry.registerTileEntity(TileKeeperController.class, new ResourceLocation(Main.MOD_ID, "keepertile"));
    }


    @Override
    public Integer getType(ItemStack itemStack) {
        return typeId;
    }

    @SideOnly(Side.CLIENT)
    public static void registerItemModels() {
        ModelLoader.setCustomModelResourceLocation(
                item,    // 你的物品对象
                0,                     // metadata
                new ModelResourceLocation(Main.MOD_ID + ":keeper", "inventory")
        );
        ModelLoader.setCustomModelResourceLocation(
                itemBlock,    // 你的物品对象
                0,                     // metadata
                new ModelResourceLocation(Main.MOD_ID + ":keepercontroller", "inventory")
        );
    }

    private static <T> T register(String name, Object obj, Class tile) {
        if (obj instanceof Block block) {
            if (Loader.isModLoaded("appliedenergistics2")) {
                itemBlock = (ItemBlock) (block instanceof AEBaseBlock ? new AEBaseItemBlock(block) : new ItemBlock(block));
            }

            itemBlock.setRegistryName(Main.MOD_ID, name);
            itemBlock.setTranslationKey(Main.MOD_ID + "." + name);
            ForgeRegistries.ITEMS.register(itemBlock);

            block.setRegistryName(Main.MOD_ID, name);
            block.setTranslationKey(Main.MOD_ID + "." + name);
            ForgeRegistries.BLOCKS.register(block);

            IBlockDefinition build = new BlockDefinitionBuilder(block, itemBlock, tile).build();
            return (T) itemBlock;

        } else if (obj instanceof Item) {
            Item item = (Item) obj;
            item.setRegistryName(Main.MOD_ID, name);
            item.setTranslationKey(Main.MOD_ID + "." + name);
            ForgeRegistries.ITEMS.register(item);
            return (T) item;
        }
        throw new IllegalArgumentException("不支持的对象类型: " + obj.getClass().getName());
    }

    public static long time = 0;

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (worldIn.isRemote) {
            Minecraft.getMinecraft().displayGuiScreen(new KeeperGuiEditScreen(playerIn.getHeldItem(handIn)));
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    public static void setNums(ItemStack stack, Integer keepNum,Integer perCraft){
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        perCraft = Math.abs(perCraft);
        perCraft = Math.min(100000, perCraft);
        keepNum = Math.abs(keepNum);
        keepNum = Math.min(100000, keepNum);
        tagCompound.setInteger("keepnum", keepNum);
        tagCompound.setInteger("perCraft", perCraft);
        stack.setTagCompound(tagCompound);
    }
    public static int[] getNums(ItemStack stack){
        NBTTagCompound tag = stack.getTagCompound();
        int[] ints = new int[2];
        ints[0]=10;
        ints[1]=1;
        if (tag != null) {
            ints[0]=Math.max(1,tag.getInteger("keepnum"));
            ints[1]=Math.max(1,tag.getInteger("perCraft"));
        }
        return ints;
    }

    @Override
    protected void addCheckedInformation(ItemStack stack, World world, List<String> lines, ITooltipFlag advancedTooltips) {
        int[] nums = ItemKeeperUpgrade.getNums(stack);
        int keepNum = nums[0];
        int perCraft = nums[1];

        lines.add(I18n.format(Main.MOD_ID + ".keeper.keepernum.name") + (keepNum == 0 ? "-" : keepNum));
        lines.add(I18n.format(Main.MOD_ID + ".keeper.percraft.name")  + (perCraft == 0 ? "-" : perCraft));

        // 添加使用说明
        lines.add(TextFormatting.GRAY + I18n.format(Main.MOD_ID + ".keeper.info"));

        super.addCheckedInformation(stack, world, lines, advancedTooltips);
    }
}
