package com.bbscncom.keepcard.keeper;

import com.bbscncom.keepcard.Main;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class KeeperGuiEditScreen extends GuiScreen {
    private final ItemStack tile;
    private GuiTextField inputFieldKeeper;
    private GuiTextField inputFieldPerCraft;
    private GuiButton doneButton;

    public KeeperGuiEditScreen(ItemStack tile) {
        this.tile = tile;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.inputFieldKeeper = new GuiTextField(0, this.fontRenderer, this.width / 2 - 100, this.height / 2 - 60, 200, 20);
        this.inputFieldKeeper.setMaxStringLength(50);
        this.inputFieldKeeper.setFocused(true);

        this.inputFieldPerCraft = new GuiTextField(0, this.fontRenderer, this.width / 2 - 100, this.height / 2 - 30, 200, 20);
        this.inputFieldPerCraft.setMaxStringLength(50);

        int[] nums = ItemKeeperUpgrade.getNums(tile);
        int keepNum=nums[0];
        this.inputFieldKeeper.setText(keepNum == 0 ? "" : String.valueOf(keepNum));
        int perCraft=nums[1];
        this.inputFieldPerCraft.setText(perCraft == 0 ? "" : String.valueOf(perCraft));


        this.doneButton = new GuiButton(1, this.width / 2 - 100, this.height / 2, I18n.format("gui.done"));
        this.buttonList.add(doneButton);
    }

    public static int stringToInt(String s){
        try{
            return Integer.parseInt(s.substring(0, Math.min(s.length(),9)));
        }catch(Exception e){
            return 10;
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        int keepNum = inputFieldKeeper.getText().isEmpty()?0:stringToInt(inputFieldKeeper.getText());
        int perCraft = inputFieldPerCraft.getText().isEmpty()?0:stringToInt(inputFieldPerCraft.getText());
        ServerboundSetKeepNum serverboundSetKeepNum = new ServerboundSetKeepNum(EnumHand.MAIN_HAND, keepNum,perCraft);
        ServerboundSetKeepNum.INSTANCE.sendToServer(serverboundSetKeepNum);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) {
            // 处理输入内容
            this.mc.displayGuiScreen(null); // 关闭GUI
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (Character.isDigit(typedChar) || isControlKey(keyCode)) {
            Boolean b = this.inputFieldKeeper.textboxKeyTyped(typedChar, keyCode);
            b = this.inputFieldPerCraft.textboxKeyTyped(typedChar, keyCode);

            try{
                if(this.inputFieldKeeper.isFocused()){
                    int i = stringToInt(inputFieldKeeper.getText());
                    if(i>ItemKeeperUpgrade.MAX_NUM){
                        this.inputFieldKeeper.setText(String.valueOf(ItemKeeperUpgrade.MAX_NUM));
                    }else{
                    }
                }
                if(this.inputFieldPerCraft.isFocused()){
                    int i = stringToInt(inputFieldPerCraft.getText());
                    if(i>ItemKeeperUpgrade.MAX_NUM){
                        this.inputFieldPerCraft.setText(String.valueOf(ItemKeeperUpgrade.MAX_NUM));
                    }else{
                    }
                }

            }catch(Exception e){

            }

        } else {
        }
        super.keyTyped(typedChar, keyCode);
    }

    private boolean isControlKey(int keyCode) {
        return keyCode == Keyboard.KEY_BACK
                || keyCode == Keyboard.KEY_DELETE
                || keyCode == Keyboard.KEY_LEFT
                || keyCode == Keyboard.KEY_RIGHT
                || keyCode == Keyboard.KEY_HOME
                || keyCode == Keyboard.KEY_END;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.inputFieldKeeper.mouseClicked(mouseX, mouseY, mouseButton);
        this.inputFieldPerCraft.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.inputFieldKeeper.updateCursorCounter();
        this.inputFieldPerCraft.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        String labelKeeper = I18n.format(Main.MOD_ID + ".keeper.keepernum.name");
        int labelKeeperWidth = this.fontRenderer.getStringWidth(labelKeeper);
        this.fontRenderer.drawString(labelKeeper, this.inputFieldKeeper.x - labelKeeperWidth - 5, this.inputFieldKeeper.y + 6, 0xFFFFFF);

        String labelPerCraft = I18n.format(Main.MOD_ID + ".keeper.percraft.name");
        int labelPerCraftWidth = this.fontRenderer.getStringWidth(labelPerCraft);
        this.fontRenderer.drawString(labelPerCraft, this.inputFieldPerCraft.x - labelPerCraftWidth - 5, this.inputFieldPerCraft.y + 6, 0xFFFFFF);

        this.inputFieldKeeper.drawTextBox();
        this.inputFieldPerCraft.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}

