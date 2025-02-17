package noppes.npcs.client.gui.item;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.linked.LinkedItemSavePacket;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.global.GuiNPCManageLinked;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.data.LinkedItem;

public class SubGuiLinkedItem extends SubGuiInterface implements ITextfieldListener {
    private final GuiNPCManageLinked parent;
    public LinkedItem linkedItem;
    private final String originalName;

    public SubGuiLinkedItem(GuiNPCManageLinked parent, LinkedItem linkedItem) {
        this.linkedItem = linkedItem;
        this.parent = parent;
        this.originalName = linkedItem.name;
        this.closeOnEsc = true;

        setBackground("menubg.png");
        xSize = 360;
        ySize = 216;
    }

    @Override
    public void initGui() {
        super.initGui();
        GuiMenuTopButton close = new GuiMenuTopButton(-5, guiLeft + xSize - 22, guiTop - 10, "X");

        GuiMenuTopButton general = new GuiMenuTopButton(-1, guiLeft + 4, guiTop - 10, "menu.general");
        GuiMenuTopButton scripts = new GuiMenuTopButton(-2, general.xPosition + general.getWidth(), guiTop - 10, "script.scripts");

        close.active = false;
        general.active = true;
        scripts.active = false;

        addTopButton(close);
        addTopButton(general);
        addTopButton(scripts);

        guiTop += 7;
        int y = guiTop + 7;
        int x = guiLeft + 4;
        y += 5;

        GuiScrollWindow scrollWindow = new GuiScrollWindow(this, x + 5, y, xSize - 20, ySize - 10 - (y - guiTop), 0);
        addScrollableGui(0, scrollWindow);

        y = 5;
        x = 5;
        scrollWindow.addLabel(new GuiNpcLabel(1, "display.name", x, y + 5, 0xFFFFFF));
        scrollWindow.addTextField(new GuiNpcTextField(1, this, x + 80, y, scrollWindow.clipWidth - 20 - 80, 20, linkedItem.name));
        scrollWindow.getTextField(1).setMaxStringLength(100);

        y += 25;

        scrollWindow.addLabel(new GuiNpcLabel(2, "display.texture", x, y + 5, 0xFFFFFF));
        scrollWindow.addTextField(new GuiNpcTextField(2, this, x + 80, y, scrollWindow.clipWidth - 20 - 80, 20, linkedItem.display.texture));
        scrollWindow.getTextField(2).setMaxStringLength(300);

        y += 30;

        String color = Integer.toHexString(linkedItem.display.itemColor);
        while(color.length() < 6)
            color = "0" + color;

        scrollWindow.addLabel(new GuiNpcLabel(9, "display.color", x, y + 5,  0xFFFFFF));
        x += 35;
        scrollWindow.addButton(new GuiNpcButton(9, x + 80, y, 80, 20, color + ""));
        scrollWindow.getButton(9).packedFGColour = linkedItem.display.itemColor;

        x = 5;

        y += 30;
        scrollWindow.addLabel(new GuiNpcLabel(10, "display.isTool", x, y + 5, 0xFFFFFF));
        x += 35;
        scrollWindow.addButton(new GuiNpcButtonYesNo(10, x + 80, y, linkedItem.isTool));

        x = 5;
        y += 25;

        scrollWindow.addLabel(new GuiNpcLabel(11, "display.isNormalItem", x, y + 5, 0xFFFFFF));
        x += 35;
        scrollWindow.addButton(new GuiNpcButtonYesNo(11, x + 80, y, linkedItem.isNormalItem));

        x = 5;

        y += 30;
        scrollWindow.addLabel(new GuiNpcLabel(3, "display.scale", x, y + 5, 0xFFFFFF));

        x += 30;
        scrollWindow.addLabel(new GuiNpcLabel(31, "X", x + 80, y + 5, 0xFFFFFF));
        scrollWindow.addTextField(new GuiNpcTextField(31, this, x + 90, y, 40, 20, linkedItem.display.scaleX + ""));
        scrollWindow.getTextField(31).setFloatsOnly();
        scrollWindow.getTextField(31).setMinMaxDefaultFloat(0, 30, 1);

        x += 70;
        scrollWindow.addLabel(new GuiNpcLabel(32, "Y", x + 80, y + 5, 0xFFFFFF));
        scrollWindow.addTextField(new GuiNpcTextField(32, this, x + 90, y, 40, 20, linkedItem.display.scaleY + ""));
        scrollWindow.getTextField(32).setFloatsOnly();
        scrollWindow.getTextField(32).setMinMaxDefaultFloat(0, 30, 1);

        x += 70;
        scrollWindow.addLabel(new GuiNpcLabel(33, "Z", x + 80, y + 5, 0xFFFFFF));
        scrollWindow.addTextField(new GuiNpcTextField(33, this, x + 90, y, 40, 20, linkedItem.display.scaleZ + ""));
        scrollWindow.getTextField(33).setFloatsOnly();
        scrollWindow.getTextField(33).setMinMaxDefaultFloat(0, 30, 1);

        x = 5;
        y += 25;
        scrollWindow.addLabel(new GuiNpcLabel(4, "display.rotation", x, y + 5, 0xFFFFFF));

        x += 30;
        scrollWindow.addLabel(new GuiNpcLabel(41, "X", x + 80, y + 5, 0xFFFFFF));
        scrollWindow.addTextField(new GuiNpcTextField(41, this, x + 90, y, 40, 20, linkedItem.display.rotationX + ""));
        scrollWindow.getTextField(41).setFloatsOnly();
        scrollWindow.getTextField(41).setMinMaxDefaultFloat(-360, 360, 0);

        x += 70;
        scrollWindow.addLabel(new GuiNpcLabel(42, "Y", x + 80, y + 5, 0xFFFFFF));
        scrollWindow.addTextField(new GuiNpcTextField(42, this, x + 90, y, 40, 20, linkedItem.display.rotationY + ""));
        scrollWindow.getTextField(42).setFloatsOnly();
        scrollWindow.getTextField(42).setMinMaxDefaultFloat(-360, 360, 0);

        x += 70;
        scrollWindow.addLabel(new GuiNpcLabel(43, "Z", x + 80, y + 5, 0xFFFFFF));
        scrollWindow.addTextField(new GuiNpcTextField(43, this, x + 90, y, 40, 20, linkedItem.display.rotationZ + ""));
        scrollWindow.getTextField(43).setFloatsOnly();
        scrollWindow.getTextField(43).setMinMaxDefaultFloat(-360, 360, 0);

        x = 5;
        y += 25;
        scrollWindow.addLabel(new GuiNpcLabel(5, "display.rotationRate", x, y + 5, 0xFFFFFF));

        x += 30;
        scrollWindow.addLabel(new GuiNpcLabel(51, "X", x + 80, y + 5, 0xFFFFFF));
        scrollWindow.addTextField(new GuiNpcTextField(51, this, x + 90, y, 40, 20, linkedItem.display.rotationXRate + ""));
        scrollWindow.getTextField(51).setFloatsOnly();
        scrollWindow.getTextField(51).setMinMaxDefaultFloat(0, 100, 0);

        x += 70;
        scrollWindow.addLabel(new GuiNpcLabel(52, "Y", x + 80, y + 5, 0xFFFFFF));
        scrollWindow.addTextField(new GuiNpcTextField(52, this, x + 90, y, 40, 20, linkedItem.display.rotationYRate + ""));
        scrollWindow.getTextField(52).setFloatsOnly();
        scrollWindow.getTextField(52).setMinMaxDefaultFloat(0, 100, 0);

        x += 70;
        scrollWindow.addLabel(new GuiNpcLabel(53, "Z", x + 80, y + 5, 0xFFFFFF));
        scrollWindow.addTextField(new GuiNpcTextField(53, this, x + 90, y, 40, 20, linkedItem.display.rotationZRate + ""));
        scrollWindow.getTextField(53).setFloatsOnly();
        scrollWindow.getTextField(53).setMinMaxDefaultFloat(0, 100, 0);

        x = 5;
        y += 25;
        scrollWindow.addLabel(new GuiNpcLabel(6, "display.translate", x, y + 5, 0xFFFFFF));

        x += 30;
        scrollWindow.addLabel(new GuiNpcLabel(61, "X", x + 80, y + 5, 0xFFFFFF));
        scrollWindow.addTextField(new GuiNpcTextField(61, this, x + 90, y, 40, 20, linkedItem.display.translateX + ""));
        scrollWindow.getTextField(61).setFloatsOnly();
        scrollWindow.getTextField(61).setMinMaxDefaultFloat(-10, 10, 0);

        x += 70;
        scrollWindow.addLabel(new GuiNpcLabel(62, "Y", x + 80, y + 5, 0xFFFFFF));
        scrollWindow.addTextField(new GuiNpcTextField(62, this, x + 90, y, 40, 20, linkedItem.display.translateY + ""));
        scrollWindow.getTextField(62).setFloatsOnly();
        scrollWindow.getTextField(62).setMinMaxDefaultFloat(-10, 10, 0);

        x += 70;
        scrollWindow.addLabel(new GuiNpcLabel(63, "Z", x + 80, y + 5, 0xFFFFFF));
        scrollWindow.addTextField(new GuiNpcTextField(63, this, x + 90, y, 40, 20, linkedItem.display.translateZ + ""));
        scrollWindow.getTextField(63).setFloatsOnly();
        scrollWindow.getTextField(63).setMinMaxDefaultFloat(-10, 10, 0);

        y += 30;

        scrollWindow.scrollY = 0;
        scrollWindow.maxScrollY = y - scrollWindow.clipHeight;
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        int id = guibutton.id;
        if (id == -5) {
            close();
            return;
        } else if (id == 9) {
            // OPEN COLOR PICKER
        } else if (id == 10) {
            // IsTool
            if (guibutton instanceof GuiNpcButtonYesNo) {
                linkedItem.isTool = ((GuiNpcButtonYesNo) guibutton).getBoolean();
            }
        } else if (id == 11) {
            // isNormalItem
            if (guibutton instanceof GuiNpcButtonYesNo) {
                linkedItem.isNormalItem = ((GuiNpcButtonYesNo) guibutton).getBoolean();
            }
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textField) {
        int id = textField.id;
        if (id == 1) {
            // Name
            linkedItem.name = textField.getText();
        } else if (id == 2) {
            // Texture
            linkedItem.display.texture = textField.getText();
        } else if (id == 31) {
            // Scale X
            linkedItem.display.scaleX = textField.getFloat();
        } else if (id == 32) {
            // Scale Y
            linkedItem.display.scaleY = textField.getFloat();
        } else if (id == 33) {
            // Scale Z
            linkedItem.display.scaleZ = textField.getFloat();
        } else if (id == 41) {
            // Rotation X
            linkedItem.display.rotationX = textField.getFloat();
        } else if (id == 42) {
            // Rotation Y
            linkedItem.display.rotationY = textField.getFloat();
        } else if (id == 43) {
            // Rotation Z
            linkedItem.display.rotationZ = textField.getFloat();
        } else if (id == 51) {
            // Rotation X Rate
            linkedItem.display.rotationXRate = textField.getFloat();
        } else if (id == 52) {
            // Rotation Y Rate
            linkedItem.display.rotationYRate = textField.getFloat();
        } else if (id == 53) {
            // Rotation Z Rate
            linkedItem.display.rotationZRate = textField.getFloat();
        } else if (id == 61) {
            // Translate X
            linkedItem.display.translateX = textField.getFloat();
        } else if (id == 62) {
            // Translate Y
            linkedItem.display.translateY = textField.getFloat();
        } else if (id == 63) {
            // Translate Z
            linkedItem.display.translateZ = textField.getFloat();
        }
    }

    @Override
    public void close() {
        super.close();
        PacketClient.sendClient(new LinkedItemSavePacket(linkedItem.writeToNBT(false), originalName));
    }

    public enum LinkedItemType {
        TOOL,
        NORMAL
    }

    public enum LinkedItemUseAction {
        NONE,
        BLOCK,
        BOW,
        EAT,
        DRINK
    }

    public enum LinkedItemArmorType {
        NONE(-2, "none"),
        ALL(-1, "all"),
        HELM(0, "helm"),
        CHESTPLATE(1, "chestplate"),
        LEGGINGS(2, "leggings"),
        BOOTS(3, "boots");

        public int value;
        public String name;

        LinkedItemArmorType(int type, String display){
            this.value = type;
            this.name = display;
        }
    }
}
