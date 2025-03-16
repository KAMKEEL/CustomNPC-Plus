package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.DataStats;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPotionType;

public class SubGuiNpcMeleeProperties extends SubGuiInterface implements ITextfieldListener {
    private DataStats stats;
    private String[] potionNames = new String[]{"gui.none", "tile.fire.name", "potion.poison", "potion.hunger", "potion.weakness", "potion.moveSlowdown", "potion.confusion", "potion.blindness", "potion.wither"};

    public SubGuiNpcMeleeProperties(DataStats stats) {
        this.stats = stats;
        setBackground("menubg.png");
        xSize = 256;
        ySize = 216;
        closeOnEsc = true;
    }

    public void initGui() {
        super.initGui();
        addLabel(new GuiNpcLabel(1, "stats.meleestrength", guiLeft + 5, guiTop + 15));
        addTextField(new GuiNpcTextField(1, this, fontRendererObj, guiLeft + 85, guiTop + 10, 160, 18, String.format("%.0f", stats.getAttackStrength()) + ""));
        getTextField(1).floatsOnly = true;
        getTextField(1).setMinMaxDefaultFloat(0, Float.MAX_VALUE, 5);
        addLabel(new GuiNpcLabel(2, "stats.meleerange", guiLeft + 5, guiTop + 45));
        addTextField(new GuiNpcTextField(2, this, fontRendererObj, guiLeft + 85, guiTop + 40, 40, 18, stats.attackRange + ""));
        getTextField(2).integersOnly = true;
        getTextField(2).setMinMaxDefault(1, Integer.MAX_VALUE, 2);
        addLabel(new GuiNpcLabel(3, "stats.meleespeed", guiLeft + 5, guiTop + 75));
        addTextField(new GuiNpcTextField(3, this, fontRendererObj, guiLeft + 85, guiTop + 70, 40, 18, stats.attackSpeed + ""));
        getTextField(3).integersOnly = true;
        getTextField(3).setMinMaxDefault(1, Integer.MAX_VALUE, 20);

        addLabel(new GuiNpcLabel(4, "enchantment.knockback", guiLeft + 5, guiTop + 105));
        addTextField(new GuiNpcTextField(4, this, fontRendererObj, guiLeft + 85, guiTop + 100, 40, 18, stats.knockback + ""));
        getTextField(4).integersOnly = true;
        getTextField(4).setMinMaxDefault(0, Integer.MAX_VALUE, 0);
        addLabel(new GuiNpcLabel(5, "stats.meleeeffect", guiLeft + 5, guiTop + 135));
        addButton(new GuiButtonBiDirectional(5, guiLeft + 85, guiTop + 130, 100, 20, potionNames, stats.potionType.ordinal()));
        if (stats.potionType != EnumPotionType.None) {
            addLabel(new GuiNpcLabel(6, "gui.time", guiLeft + 5, guiTop + 165));
            addTextField(new GuiNpcTextField(6, this, fontRendererObj, guiLeft + 85, guiTop + 160, 40, 18, stats.potionDuration + ""));
            getTextField(6).integersOnly = true;
            getTextField(6).setMinMaxDefault(1, Integer.MAX_VALUE, 5);
            if (stats.potionType != EnumPotionType.Fire) {
                addLabel(new GuiNpcLabel(7, "stats.amplify", guiLeft + 5, guiTop + 195));
                addButton(new GuiButtonBiDirectional(7, guiLeft + 85, guiTop + 190, 52, 20, new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}, stats.potionAmp));
            }
        }
        addButton(new GuiNpcButton(66, guiLeft + 190, guiTop + 190, 60, 20, "gui.done"));
    }

    public void unFocused(GuiNpcTextField textfield) {
        if (textfield.id == 1) {
            stats.setAttackStrength(Float.parseFloat(textfield.getText()));
        } else if (textfield.id == 2) {
            stats.attackRange = textfield.getInteger();
        } else if (textfield.id == 3) {
            stats.attackSpeed = textfield.getInteger();
        } else if (textfield.id == 4) {
            stats.knockback = textfield.getInteger();
        } else if (textfield.id == 6) {
            stats.potionDuration = textfield.getInteger();
        }
    }

    protected void actionPerformed(GuiButton guibutton) {
        GuiNpcButton button = (GuiNpcButton) guibutton;
        if (button.id == 5) {
            stats.potionType = EnumPotionType.values()[button.getValue()];
            initGui();
        }
        if (button.id == 7) {
            stats.potionAmp = button.getValue();
        }
        if (button.id == 66) {
            close();
        }
    }

}
