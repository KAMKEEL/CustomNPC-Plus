package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.DataStats;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.data.CustomTintData;
import noppes.npcs.controllers.data.DialogImage;

public class SubGuiNpcTint extends SubGuiInterface implements ISubGuiListener, ITextfieldListener {
    private CustomTintData tintData;
    private int lastColorClicked = -1;

    public SubGuiNpcTint(CustomTintData tintData) {
        this.tintData = tintData;
        setBackground("menubg.png");
        xSize = 256;
        ySize = 216;
        closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();
        int y = 30;
        addLabel(new GuiNpcLabel(0, "tint.enablecustom", guiLeft + 5, guiTop + y + 5));
        addButton(new GuiNpcButtonYesNo(0, guiLeft + 122, guiTop + y, 56, 20, tintData.isEnableCustomTint()));
        y+=22;
        if (tintData.isEnableCustomTint()) {
            addLabel(new GuiNpcLabel(1, "tint.enablenpc", guiLeft + 5, guiTop + y + 5));
            addButton(new GuiNpcButtonYesNo(1, guiLeft + 122, guiTop + y, 56, 20, tintData.isEnableNpcTint()));
            y+=22;
            if(tintData.isEnableNpcTint()) {
                String color = Integer.toHexString(tintData.getColorNpcTint());
                while (color.length() < 6) color = 0 + color;
                addLabel(new GuiNpcLabel(2, "tint.tint", guiLeft + 4, guiTop + y + 5));
                addButton(new GuiNpcButton(2, guiLeft + 122, guiTop + y, 60, 20, color));
                getButton(2).setTextColor(tintData.getColorNpcTint());
                y+=22;
                addLabel(new GuiNpcLabel(3,"tint.alpha", guiLeft + 5, guiTop + y + 5));
                addTextField(new GuiNpcTextField(3,this, fontRendererObj, guiLeft + 122, guiTop + y, 60, 20, tintData.getColorNpcTintAlpha() + ""));
                getTextField(3).integersOnly = true;
                getTextField(3).setMinMaxDefault(1, 100, 40);
                y+=22;
            }
            addLabel(new GuiNpcLabel(4, "tint.enablehurt", guiLeft + 5, guiTop + y + 5));
            addButton(new GuiNpcButtonYesNo(4, guiLeft + 122, guiTop + y, 56, 20, tintData.isEnableHurtTint()));
            y+=22;
            if (tintData.isEnableHurtTint()) {
                String color2 = Integer.toHexString(tintData.getColorHurtTint());
                while (color2.length() < 6) color2 = 0 + color2;
                addLabel(new GuiNpcLabel(5, "tint.hurt", guiLeft + 4, guiTop + y + 5));
                addButton(new GuiNpcButton(5, guiLeft + 122, guiTop + y, 60, 20, color2));
                getButton(5).setTextColor(tintData.getColorHurtTint());
            }
        }
        addButton(new GuiNpcButton(66, guiLeft + 82, guiTop + 190, 98, 20, "gui.done"));
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        int id = guibutton.id;
        GuiNpcButton button = (GuiNpcButton) guibutton;
        if (id == 0) {
            tintData.setEnableCustomTint(((GuiNpcButtonYesNo) button).getBoolean());
            initGui();
        }
        if (id == 1) {
            tintData.setEnableNpcTint(((GuiNpcButtonYesNo) button).getBoolean());
            initGui();
        }
        if (id == 4) {
            tintData.setEnableHurtTint(((GuiNpcButtonYesNo) button).getBoolean());
            initGui();
        }
        if (button.id == 2) {
            setSubGui(new SubGuiColorSelector(tintData.getColorNpcTint()));
            lastColorClicked = 0;
        }
        if (button.id == 5) {
            setSubGui(new SubGuiColorSelector(tintData.getColorHurtTint()));
            lastColorClicked = 1;
        }
        if (id == 66) {
            close();
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (lastColorClicked == 0) {
            tintData.setColorNpcTint(((SubGuiColorSelector) subgui).color);
        } else if (lastColorClicked == 1) {
            tintData.setColorHurtTint(((SubGuiColorSelector) subgui).color);
        }
        initGui();
        save();
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if(textfield.id==3){
            tintData.setColorNpcTintAlpha(textfield.getInteger());
        }
    }
}
