package noppes.npcs.client.gui.item;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.linked.LinkedItemSavePacket;
import kamkeel.npcs.util.ColorUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.global.GuiNPCManageLinked;
import noppes.npcs.client.gui.script.GuiScriptLinkedItem;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiMenuTopButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiScrollWindow;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.renderer.ImageData;
import noppes.npcs.controllers.data.LinkedItem;
import org.lwjgl.opengl.GL11;

public class SubGuiLinkedItem extends SubGuiInterface implements ITextfieldListener, GuiYesNoCallback, ISubGuiListener {

    public LinkedItem linkedItem;
    private final String originalName;

    private int tab = -1;
    private int colorPicked = 0;

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

        // Top buttons
        GuiMenuTopButton close = new GuiMenuTopButton(-5, guiLeft + xSize - 22, guiTop - 10, "X");
        GuiMenuTopButton general = new GuiMenuTopButton(-1, guiLeft + 4, guiTop - 10, "menu.general");
        GuiMenuTopButton advanced = new GuiMenuTopButton(-2, general.xPosition + general.getWidth(), guiTop - 10, "menu.advanced");
        GuiMenuTopButton scripts = new GuiMenuTopButton(-3, advanced.xPosition + advanced.getWidth(), guiTop - 10, "script.scripts");

        // Set active tab
        general.active = (tab == -1 || tab == 0);
        advanced.active = (tab == -2);
        scripts.active = (tab == -3);
        close.active = (tab == -5);

        addTopButton(close);
        addTopButton(advanced);
        addTopButton(general);
        addTopButton(scripts);

        guiTop += 7;

        if (general.active) {
            addGeneralComponents();
        } else if (advanced.active) {
            addAdvancedComponents();
        }
    }

    // ===================== GENERAL TAB COMPONENTS =====================
    private void addGeneralComponents() {
        int x = guiLeft + 10;
        int y = guiTop + 8;
        int spacing = 25;

        // NAME + ID label
        addLabel(new GuiNpcLabel(1, "gui.name", x, y + 5, CustomNpcResourceListener.DefaultTextColor));
        addTextField(new GuiNpcTextField(1, this, x + 60, y, 180, 20, linkedItem.name));
        addLabel(new GuiNpcLabel(100, "gui.id", x + 250, y + 1, CustomNpcResourceListener.DefaultTextColor));
        addLabel(new GuiNpcLabel(101, linkedItem.id + "", x + 250, y + 11, CustomNpcResourceListener.DefaultTextColor));
        y += spacing;

        // VERSION + Bump Button
        addLabel(new GuiNpcLabel(2, "display.version", x, y + 5, CustomNpcResourceListener.DefaultTextColor));
        GuiNpcButton versionButton = new GuiNpcButton(20, x + 100, y, 80, 20, "gui.update");
        versionButton.setHoverText("display.versionInfo");
        addButton(versionButton);
        addLabel(new GuiNpcLabel(200, "display.version", x + 190, y + 1, CustomNpcResourceListener.DefaultTextColor));
        addLabel(new GuiNpcLabel(201, linkedItem.version + "", x + 190, y + 11, CustomNpcResourceListener.DefaultTextColor));

        y += spacing;

        // MAX STACKSIZE field (integers only, 1 to 64)
        addLabel(new GuiNpcLabel(4, "display.maxStack", x, y + 5, CustomNpcResourceListener.DefaultTextColor));
        GuiNpcTextField stackField = new GuiNpcTextField(3, this, x + 110, y, 50, 20, "" + linkedItem.stackSize);
        stackField.setIntegersOnly().setMinMaxDefault(1, 64, 64);
        addTextField(stackField);

        y += spacing;

        // Enchantability field (0 to 30)
        addLabel(new GuiNpcLabel(9, "display.enchantability", x, y + 5, CustomNpcResourceListener.DefaultTextColor));
        GuiNpcTextField enchantField = new GuiNpcTextField(10, this, x + 110, y, 50, 20, "" + linkedItem.enchantability);
        enchantField.setIntegersOnly().setMinMaxDefault(0, 30, 0);
        addTextField(enchantField);
        y += spacing;

        // Dig Speed field (0 to 20)
        addLabel(new GuiNpcLabel(11, "Dig & Attack Speed", x, y + 5, CustomNpcResourceListener.DefaultTextColor));
        GuiNpcTextField digSpeedField = new GuiNpcTextField(12, this, x + 110, y, 50, 20, "" + linkedItem.digSpeed);
        digSpeedField.setIntegersOnly().setMinMaxDefault(0, 20, 1);
        addTextField(digSpeedField);
        GuiNpcTextField attackSpeedField = new GuiNpcTextField(14, this, x +165, y, 50, 20, "" +linkedItem.attackSpeed);
        attackSpeedField.setIntegersOnly().setMinMaxDefault(0,999999999,20);
        addTextField(attackSpeedField);

        y += spacing;

        // TEXTURE field
        addLabel(new GuiNpcLabel(3, "display.texture", x, y + 5, CustomNpcResourceListener.DefaultTextColor));
        addTextField(new GuiNpcTextField(2, this, x + 60, y, 200, 20, linkedItem.display.texture));

        y += spacing;

        // Item Use Action: BiDirectional button (None, Block, Eat, Drink)
        addLabel(new GuiNpcLabel(5, "display.useAction", x, y + 5, CustomNpcResourceListener.DefaultTextColor));
        String[] useActions = {"use_action.none", "use_action.block", "use_action.eat", "use_action.drink", "use_action.bow"};
        int useActionIndex = 0;
        switch (linkedItem.itemUseAction) {
            case 0:
                useActionIndex = 0;
                break;
            case 1:
                useActionIndex = 1;
                break;
            case 2:
                useActionIndex = 4;
                break;
            case 3:
                useActionIndex = 2;
                break;
            case 4:
                useActionIndex = 3;
                break;
            default:
                useActionIndex = 0;
                break;
        }
        GuiButtonBiDirectional useActionButton = new GuiButtonBiDirectional(6, x + 120, y, 100, 20, useActions, useActionIndex);
        addButton(useActionButton);
        y += spacing;

        // Armor Type: BiDirectional button (None, All, Head, Chestplate, Leggings, Boots)
        addLabel(new GuiNpcLabel(7, "display.armor", x, y + 5, CustomNpcResourceListener.DefaultTextColor));
        String[] armorOptions = {"armor_type.none", "armor_type.all", "armor_type.head", "armor_type.chestplate", "armor_type.leggings", "armor_type.boots"};
        int armorIndex;
        if (linkedItem.armorType == -2) {
            armorIndex = 0;
        } else if (linkedItem.armorType == -1) {
            armorIndex = 1;
        } else {
            armorIndex = linkedItem.armorType + 2; // 0->2, 1->3, etc.
        }
        GuiButtonBiDirectional armorButton = new GuiButtonBiDirectional(8, x + 120, y, 100, 20, armorOptions, armorIndex);
        addButton(armorButton);
        y += spacing;
    }

    // ===================== ADVANCED TAB COMPONENTS =====================
    private void addAdvancedComponents() {
        int x = 5;
        int y = 5;
        int clipWidth = xSize - 20;
        int clipHeight = ySize - 20;
        GuiScrollWindow scrollWindow = new GuiScrollWindow(this, guiLeft + 10, guiTop + 10, clipWidth, clipHeight, 0);
        addScrollableGui(0, scrollWindow);

        int localY = 5;
        // Item Color: label and two buttons
        scrollWindow.addLabel(new GuiNpcLabel(20, "gui.color", x, localY + 5, 0xFFFFFF));
        String colorHex = Integer.toHexString(linkedItem.display.itemColor);
        while (colorHex.length() < 6)
            colorHex = "0" + colorHex;
        GuiNpcButton colorPickerButton = new GuiNpcButton(24, x + 80, localY, 80, 20, colorHex);
        colorPickerButton.packedFGColour = linkedItem.display.itemColor;
        scrollWindow.addButton(colorPickerButton);
        GuiNpcButton clearColorButton = new GuiNpcButton(25, x + 170, localY, 60, 20, "gui.clear");
        scrollWindow.addButton(clearColorButton);
        localY += 30;

        scrollWindow.addLabel(new GuiNpcLabel(27, "display.isTool", x, localY + 5, 0xFFFFFF));
        GuiNpcButtonYesNo toolButton = new GuiNpcButtonYesNo(27, x + 120, localY, linkedItem.isTool);
        toolButton.setHoverText("display.isToolInfo");
        scrollWindow.addButton(toolButton);
        localY += 25;

        scrollWindow.addLabel(new GuiNpcLabel(28, "display.isNormalItem", x, localY + 5, 0xFFFFFF));
        GuiNpcButtonYesNo normalButton = new GuiNpcButtonYesNo(28, x + 120, localY, linkedItem.isNormalItem);
        normalButton.setHoverText("display.isNormalItemInfo");
        scrollWindow.addButton(normalButton);
        localY += 25;

        // Scale: X, Y, Z
        scrollWindow.addLabel(new GuiNpcLabel(31, "model.scale", x, localY + 5, 0xFFFFFF));
        scrollWindow.addLabel(new GuiNpcLabel(32, "X", x + 80, localY + 5, 0xFFFFFF));
        GuiNpcTextField scaleXField = new GuiNpcTextField(31, this, x + 90, localY, 40, 20, linkedItem.display.scaleX + "");
        scaleXField.setFloatsOnly().setMinMaxDefaultFloat(0, 30, 1);
        scrollWindow.addTextField(scaleXField);

        scrollWindow.addLabel(new GuiNpcLabel(33, "Y", x + 140, localY + 5, 0xFFFFFF));
        GuiNpcTextField scaleYField = new GuiNpcTextField(32, this, x + 150, localY, 40, 20, linkedItem.display.scaleY + "");
        scaleYField.setFloatsOnly().setMinMaxDefaultFloat(0, 30, 1);
        scrollWindow.addTextField(scaleYField);

        scrollWindow.addLabel(new GuiNpcLabel(34, "Z", x + 200, localY + 5, 0xFFFFFF));
        GuiNpcTextField scaleZField = new GuiNpcTextField(33, this, x + 210, localY, 40, 20, linkedItem.display.scaleZ + "");
        scaleZField.setFloatsOnly().setMinMaxDefaultFloat(0, 30, 1);
        scrollWindow.addTextField(scaleZField);
        localY += 30;

        // Rotation: X, Y, Z
        scrollWindow.addLabel(new GuiNpcLabel(41, "model.rotate", x, localY + 5, 0xFFFFFF));
        scrollWindow.addLabel(new GuiNpcLabel(42, "X", x + 80, localY + 5, 0xFFFFFF));
        GuiNpcTextField rotXField = new GuiNpcTextField(41, this, x + 90, localY, 40, 20, linkedItem.display.rotationX + "");
        rotXField.setFloatsOnly().setMinMaxDefaultFloat(-360, 360, 0);
        scrollWindow.addTextField(rotXField);

        scrollWindow.addLabel(new GuiNpcLabel(43, "Y", x + 140, localY + 5, 0xFFFFFF));
        GuiNpcTextField rotYField = new GuiNpcTextField(42, this, x + 150, localY, 40, 20, linkedItem.display.rotationY + "");
        rotYField.setFloatsOnly().setMinMaxDefaultFloat(-360, 360, 0);
        scrollWindow.addTextField(rotYField);

        scrollWindow.addLabel(new GuiNpcLabel(44, "Z", x + 200, localY + 5, 0xFFFFFF));
        GuiNpcTextField rotZField = new GuiNpcTextField(43, this, x + 210, localY, 40, 20, linkedItem.display.rotationZ + "");
        rotZField.setFloatsOnly().setMinMaxDefaultFloat(-360, 360, 0);
        scrollWindow.addTextField(rotZField);
        localY += 30;

        // Rotation Rate: X, Y, Z
        scrollWindow.addLabel(new GuiNpcLabel(51, "model.rotationRate", x, localY + 5, 0xFFFFFF));
        scrollWindow.addLabel(new GuiNpcLabel(52, "X", x + 80, localY + 5, 0xFFFFFF));
        GuiNpcTextField rotXRateField = new GuiNpcTextField(51, this, x + 90, localY, 40, 20, linkedItem.display.rotationXRate + "");
        rotXRateField.setFloatsOnly().setMinMaxDefaultFloat(0, 100, 0);
        scrollWindow.addTextField(rotXRateField);

        scrollWindow.addLabel(new GuiNpcLabel(53, "Y", x + 140, localY + 5, 0xFFFFFF));
        GuiNpcTextField rotYRateField = new GuiNpcTextField(52, this, x + 150, localY, 40, 20, linkedItem.display.rotationYRate + "");
        rotYRateField.setFloatsOnly().setMinMaxDefaultFloat(0, 100, 0);
        scrollWindow.addTextField(rotYRateField);

        scrollWindow.addLabel(new GuiNpcLabel(54, "Z", x + 200, localY + 5, 0xFFFFFF));
        GuiNpcTextField rotZRateField = new GuiNpcTextField(53, this, x + 210, localY, 40, 20, linkedItem.display.rotationZRate + "");
        rotZRateField.setFloatsOnly().setMinMaxDefaultFloat(0, 100, 0);
        scrollWindow.addTextField(rotZRateField);
        localY += 30;

        // Translate: X, Y, Z
        scrollWindow.addLabel(new GuiNpcLabel(61, "model.translate", x, localY + 5, 0xFFFFFF));
        scrollWindow.addLabel(new GuiNpcLabel(62, "X", x + 80, localY + 5, 0xFFFFFF));
        GuiNpcTextField transXField = new GuiNpcTextField(61, this, x + 90, localY, 40, 20, linkedItem.display.translateX + "");
        transXField.setFloatsOnly().setMinMaxDefaultFloat(-10, 10, 0);
        scrollWindow.addTextField(transXField);

        scrollWindow.addLabel(new GuiNpcLabel(63, "Y", x + 140, localY + 5, 0xFFFFFF));
        GuiNpcTextField transYField = new GuiNpcTextField(62, this, x + 150, localY, 40, 20, linkedItem.display.translateY + "");
        transYField.setFloatsOnly().setMinMaxDefaultFloat(-10, 10, 0);
        scrollWindow.addTextField(transYField);

        scrollWindow.addLabel(new GuiNpcLabel(64, "Z", x + 200, localY + 5, 0xFFFFFF));
        GuiNpcTextField transZField = new GuiNpcTextField(63, this, x + 210, localY, 40, 20, linkedItem.display.translateZ + "");
        transZField.setFloatsOnly().setMinMaxDefaultFloat(-10, 10, 0);
        scrollWindow.addTextField(transZField);
        localY += 30;

        // Durability Show toggle (Yes/No button)
        scrollWindow.addLabel(new GuiNpcLabel(70, "display.durabilityShow", x, localY + 5, 0xFFFFFF));
        GuiNpcButtonYesNo durabilityButton = new GuiNpcButtonYesNo(26, x + 120, localY, linkedItem.display.durabilityShow);
        scrollWindow.addButton(durabilityButton);

        scrollWindow.scrollY = 0;
        scrollWindow.maxScrollY = Math.max(localY - scrollWindow.clipHeight, 0);
    }

    // ===================== BUTTON & TEXTFIELD HANDLING =====================
    @Override
    protected void actionPerformed(GuiButton guibutton) {
        int id = guibutton.id;
        if (id == -5) {
            close();
            return;
        } else if (id == -1 && tab != -1) {
            tab = -1;
            initGui();
            return;
        } else if (id == -2 && tab != -2) {
            tab = -2;
            initGui();
            return;
        } else if (id == -3) {
            PacketClient.sendClient(new LinkedItemSavePacket(linkedItem.writeToNBT(false), originalName));
            GuiScriptLinkedItem scriptGUI = new GuiScriptLinkedItem((GuiNPCManageLinked) this.parent, linkedItem);
            scriptGUI.setWorldAndResolution(mc, width, height);
            scriptGUI.initGui();
            mc.currentScreen = scriptGUI;
            return;
        } else if (id == 20) {
            // Open confirmation for version bump.
            GuiYesNo guiyesno = new GuiYesNo(this, "Confirm", "Bump version and update all linked items?", 20);
            displayGuiScreen(guiyesno);
            return;
        } else if (id == 24) {
            setSubGui(new SubGuiColorSelector(linkedItem.display.itemColor));
            colorPicked = 1;
            return;
        } else if (id == 25) {
            // Clear color: set to 0xFFFFFF.
            linkedItem.display.itemColor = 0xFFFFFF;
            GuiScrollWindow scrollWindow = getScrollableGui(0);
            if (scrollWindow != null) {
                GuiNpcButton npcButton = scrollWindow.getButton(24);
                if (npcButton != null) {
                    npcButton.displayString = "ffffff";
                    npcButton.packedFGColour = 0xFFFFFF;
                }
            }
        } else if (id == 6) {
            // Item Use Action button.
            int index = ((GuiButtonBiDirectional) guibutton).getValue();
            switch (index) {
                case 0:
                    linkedItem.itemUseAction = 0;
                    break;
                case 1:
                    linkedItem.itemUseAction = 1;
                    break;
                case 2:
                    linkedItem.itemUseAction = 3;
                    break;
                case 3:
                    linkedItem.itemUseAction = 4;
                    break;
                case 4:
                    linkedItem.itemUseAction = 2;
                    break;
            }
        } else if (id == 8) {
            // Armor Type button.
            int index = ((GuiButtonBiDirectional) guibutton).getValue();
            switch (index) {
                case 0:
                    linkedItem.armorType = -2;
                    break;
                case 1:
                    linkedItem.armorType = -1;
                    break;
                case 2:
                    linkedItem.armorType = 0;
                    break;
                case 3:
                    linkedItem.armorType = 1;
                    break;
                case 4:
                    linkedItem.armorType = 2;
                    break;
                case 5:
                    linkedItem.armorType = 3;
                    break;
            }
        } else if (id == 27 && guibutton instanceof GuiNpcButtonYesNo) {
            // IsTool toggle
            linkedItem.isTool = ((GuiNpcButtonYesNo) guibutton).getBoolean();
        } else if (id == 28 && guibutton instanceof GuiNpcButtonYesNo) {
            // IsNormalItem toggle
            linkedItem.isNormalItem = ((GuiNpcButtonYesNo) guibutton).getBoolean();
        } else if (id == 26 && guibutton instanceof GuiNpcButtonYesNo) {
            linkedItem.display.durabilityShow = ((GuiNpcButtonYesNo) guibutton).getBoolean();
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
        } else if (id == 3) {
            // Max StackSize
            linkedItem.stackSize = textField.getInteger();
        } else if (id == 10) {
            // Enchantability
            linkedItem.enchantability = textField.getInteger();
        } else if (id == 12) {
            // Dig Speed
            linkedItem.digSpeed = textField.getInteger();
        } else if (id == 14) {
            // attack Speed
            linkedItem.attackSpeed = textField.getInteger();
        } else if (id == 31) {
            linkedItem.display.scaleX = textField.getFloat();
        } else if (id == 32) {
            linkedItem.display.scaleY = textField.getFloat();
        } else if (id == 33) {
            linkedItem.display.scaleZ = textField.getFloat();
        } else if (id == 41) {
            linkedItem.display.rotationX = textField.getFloat();
        } else if (id == 42) {
            linkedItem.display.rotationY = textField.getFloat();
        } else if (id == 43) {
            linkedItem.display.rotationZ = textField.getFloat();
        } else if (id == 51) {
            linkedItem.display.rotationXRate = textField.getFloat();
        } else if (id == 52) {
            linkedItem.display.rotationYRate = textField.getFloat();
        } else if (id == 53) {
            linkedItem.display.rotationZRate = textField.getFloat();
        } else if (id == 61) {
            linkedItem.display.translateX = textField.getFloat();
        } else if (id == 62) {
            linkedItem.display.translateY = textField.getFloat();
        } else if (id == 63) {
            linkedItem.display.translateZ = textField.getFloat();
        }
    }

    @Override
    public void confirmClicked(boolean flag, int id) {
        if (id == 20 && flag) {
            linkedItem.version++;
            initGui();
        }
        mc.currentScreen = this.parent;
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        super.drawScreen(i, j, f);
    }

    @Override
    public void drawBackground() {
        super.drawBackground();
        renderScreen();
    }

    private void renderScreen() {
        if (tab != -1)
            return;

        int y = guiTop + 36;
        int x = guiLeft + 250;

        int iconRenderSize = 86;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        TextureManager textureManager = mc.getTextureManager();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        ImageData data = ClientCacheHandler.getImageData(linkedItem.display.texture);
        if (data.imageLoaded()) {
            data.bindTexture();
            int iconX = 0;
            int iconY = 0;
            int iconWidth = data.getTotalWidth();
            int iconHeight = data.getTotalWidth();
            int width = data.getTotalWidth();
            int height = data.getTotalWidth();

            float[] colors = ColorUtil.hexToRGB(linkedItem.display.itemColor);
            GL11.glColor3f(colors[0], colors[1], colors[2]);
            func_152125_a(x, y, iconX, iconY, iconWidth, iconHeight, iconRenderSize, iconRenderSize, width, height);
        } else {
            textureManager.bindTexture(new ResourceLocation("customnpcs", "textures/marks/question.png"));
            func_152125_a(x, y, 0, 0, 1, 1, iconRenderSize, iconRenderSize, 1, 1);
        }
        GL11.glPopAttrib();
    }

    @Override
    public void close() {
        super.close();
        PacketClient.sendClient(new LinkedItemSavePacket(linkedItem.writeToNBT(false), originalName));
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiColorSelector) {
            if (colorPicked == 1)
                linkedItem.display.itemColor = ((SubGuiColorSelector) subgui).color;
            if (colorPicked == 2)
                linkedItem.display.durabilityColor = ((SubGuiColorSelector) subgui).color;

            initGui();
        }
    }
}
