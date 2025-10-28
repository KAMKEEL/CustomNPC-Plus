package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.data.BossBarData;
import noppes.npcs.DataDisplay;

public class SubGuiNpcBossBar extends SubGuiInterface implements ISubGuiListener, ITextfieldListener {
    private final BossBarData bossBarData;
    private final DataDisplay display;
    private int lastColorClicked = -1;
    public int textureSelectionType = -1; // 0 = boss bar texture, 1 = background texture

    public SubGuiNpcBossBar(BossBarData bossBarData, DataDisplay display) {
        this.bossBarData = bossBarData;
        this.display = display;
        setBackground("menubg.png");
        xSize = 256;
        ySize = 216; // 恢复原来的高度
        closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();
        int y = 4; // 从顶部开始

        // Show Boss Bar Mode (always visible)
        addLabel(new GuiNpcLabel(1, "display.bossbar", guiLeft + 5, guiTop + y + 5));
        addButton(new GuiNpcButton(1, guiLeft + 122, guiTop + y, 110, 20, new String[]{"display.hide", "display.show", "display.showAttacking"}, display.showBossBar));
        y += 22;

        // Custom Boss Bar Enable/Disable
        addLabel(new GuiNpcLabel(0, "bossbar.enablecustom", guiLeft + 5, guiTop + y + 5));
        addButton(new GuiNpcButtonYesNo(0, guiLeft + 122, guiTop + y, 56, 20, bossBarData.isBossBarEnabled()));
        y += 22;

        if (bossBarData.isBossBarEnabled()) {
            // Boss Bar Texture
            addLabel(new GuiNpcLabel(2, "bossbar.texture", guiLeft + 5, guiTop + y + 5));
            addTextField(new GuiNpcTextField(2, this, fontRendererObj, guiLeft + 122, guiTop + y, 125, 20, bossBarData.getBossBarTexture()));
            y += 22;

            // Boss Bar Background Texture
            addLabel(new GuiNpcLabel(3, "bossbar.backgroundtexture", guiLeft + 5, guiTop + y + 5));
            addTextField(new GuiNpcTextField(3, this, fontRendererObj, guiLeft + 122, guiTop + y, 125, 20, bossBarData.getBossBarBackgroundTexture()));
            y += 22;

            // Boss Bar Color
            String color = Integer.toHexString(bossBarData.getBossBarColor());
            while (color.length() < 6) color = "0" + color;
            addLabel(new GuiNpcLabel(4, "bossbar.color", guiLeft + 5, guiTop + y + 5));
            addButton(new GuiNpcButton(4, guiLeft + 122, guiTop + y, 60, 20, color));
            getButton(4).setTextColor(bossBarData.getBossBarColor());
            y += 22;

            // Boss Bar Background Color
            String bgColor = Integer.toHexString(bossBarData.getBossBarBackgroundColor());
            while (bgColor.length() < 6) bgColor = "0" + bgColor;
            addLabel(new GuiNpcLabel(5, "bossbar.backgroundcolor", guiLeft + 5, guiTop + y + 5));
            addButton(new GuiNpcButton(5, guiLeft + 122, guiTop + y, 60, 20, bgColor));
            getButton(5).setTextColor(bossBarData.getBossBarBackgroundColor());
            y += 22;

            // Boss Bar Scale
            addLabel(new GuiNpcLabel(6, "bossbar.scale", guiLeft + 5, guiTop + y + 5));
            addTextField(new GuiNpcTextField(6, this, fontRendererObj, guiLeft + 122, guiTop + y, 60, 20, String.valueOf(bossBarData.getBossBarScale())));
            getTextField(6).floatsOnly = true;
            getTextField(6).setMinMaxDefaultFloat(0.1f, 5.0f, 1.0f);
            y += 22;

            // Boss Bar Offset X
            addLabel(new GuiNpcLabel(7, "bossbar.offsetx", guiLeft + 5, guiTop + y + 5));
            addTextField(new GuiNpcTextField(7, this, fontRendererObj, guiLeft + 122, guiTop + y, 60, 20, String.valueOf(bossBarData.getBossBarOffsetX())));
            getTextField(7).integersOnly = true;
            getTextField(7).setMinMaxDefault(-1000, 1000, 0);
            y += 22;

            // Boss Bar Offset Y
            addLabel(new GuiNpcLabel(8, "bossbar.offsety", guiLeft + 5, guiTop + y + 5));
            addTextField(new GuiNpcTextField(8, this, fontRendererObj, guiLeft + 122, guiTop + y, 60, 20, String.valueOf(bossBarData.getBossBarOffsetY())));
            getTextField(8).integersOnly = true;
            getTextField(8).setMinMaxDefault(-1000, 1000, 0);
        }

        addButton(new GuiNpcButton(66, guiLeft + 210, guiTop + 190, 40, 20, "gui.done"));
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        int id = guibutton.id;
        GuiNpcButton button = (GuiNpcButton) guibutton;

        if (id == 0) {
            bossBarData.setBossBarEnabled(((GuiNpcButtonYesNo) button).getBoolean());
            save(); // 保存更改
            initGui();
        }
        if (id == 1) {
            display.showBossBar = (byte) button.getValue();
        }
        if (id == 4) {
            setSubGui(new SubGuiColorSelector(bossBarData.getBossBarColor()));
            lastColorClicked = 0;
        }
        if (id == 5) {
            setSubGui(new SubGuiColorSelector(bossBarData.getBossBarBackgroundColor()));
            lastColorClicked = 1;
        }
        if (id == 66) {
            close();
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiColorSelector) {
            if (lastColorClicked == 0) {
                bossBarData.setBossBarColor(((SubGuiColorSelector) subgui).color);
            } else if (lastColorClicked == 1) {
                bossBarData.setBossBarBackgroundColor(((SubGuiColorSelector) subgui).color);
            }
        }
        // GuiBossBarTextureSelection handles its own data updates
        initGui();
        save();
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if (textfield.id == 2) {
            bossBarData.setBossBarTexture(textfield.getText());
        } else if (textfield.id == 3) {
            bossBarData.setBossBarBackgroundTexture(textfield.getText());
        } else if (textfield.id == 6) {
            bossBarData.setBossBarScale(textfield.getFloat());
        } else if (textfield.id == 7) {
            bossBarData.setBossBarOffsetX(textfield.getInteger());
        } else if (textfield.id == 8) {
            bossBarData.setBossBarOffsetY(textfield.getInteger());
        }
    }

    @Override
    public void save() {

    }
}
