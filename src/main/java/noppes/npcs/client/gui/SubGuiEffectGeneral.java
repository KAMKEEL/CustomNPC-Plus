package noppes.npcs.client.gui;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.effects.EffectSavePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.gui.global.GuiNPCManageEffects;
import noppes.npcs.client.gui.script.GuiScriptEffect;
import noppes.npcs.client.gui.util.GuiMenuTopButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiScrollWindow;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.renderer.ImageData;
import noppes.npcs.controllers.data.CustomEffect;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static noppes.npcs.client.gui.player.inventory.GuiCNPCInventory.specialIcons;

public class SubGuiEffectGeneral extends SubGuiInterface implements ITextfieldListener {
    private final GuiNPCManageEffects parent;
    public CustomEffect effect;
    private final String originalName;

    private final List<GuiMenuTopButton> topButtons = new ArrayList<>();

    public SubGuiEffectGeneral(GuiNPCManageEffects parent, CustomEffect effect) {
        this.effect = effect;
        this.parent = parent;
        this.originalName = effect.name;
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
        int x = guiLeft + 4 + 4;
        addTextField(new GuiNpcTextField(1, this, this.fontRendererObj, x + 36, y, 280, 20, effect.name));
        getTextField(1).setMaxStringLength(40);
        addLabel(new GuiNpcLabel(1, "gui.name", x, y + 5));

        addLabel(new GuiNpcLabel(-1, "ID", x + 320, y + 1));
        addLabel(new GuiNpcLabel(-2, effect.id + "", x + 320, y + 11));

        y += 23;

        addTextField(new GuiNpcTextField(2, this, x + 70, y, 246, 20, effect.menuName.replaceAll("§", "&")));
        getTextField(2).setMaxStringLength(40);
        addLabel(new GuiNpcLabel(2, "general.menuName", x, y + 5));

        y += 23;

        addTextField(setIntegerOnly(
            new GuiNpcTextField(3, this, x + 70, y, 83, 20, "" + effect.everyXTick),
            10,
            1200,
            effect.everyXTick
        ));
        getTextField(3).setMaxStringLength(6);
        getTextField(3).integersOnly = true;
        addLabel(new GuiNpcLabel(3, "effect.editor.runsEveryX", x, y + 5));

        int oldX = x;
        int xEnd = guiLeft + xSize - 10;
        x = getTextField(3).xPosition + getTextField(3).width;
        addButton(new GuiNpcButtonYesNo(10, (x + xEnd - 83) / 2, y + 23, 83, 20, effect.lossOnDeath));
        GuiNpcLabel label = new GuiNpcLabel(10, "effect.editor.lossOnDeath", x, y + 5);
        label.x = (x + xEnd - fontRendererObj.getStringWidth(label.label)) / 2;
        addLabel(label);
        x = oldX;
        y += 23;

        addTextField(setIntegerOnly(
            new GuiNpcTextField(4, this, x + 70, y, 83, 20, "" + effect.length),
            -100,
            86400,
            effect.length
        ));

        addLabel(new GuiNpcLabel(4, "effect.editor.defaultLength", x, y + 5));

        x = oldX - 4;
        y += 23;
        GuiScrollWindow scrollWindow = new GuiScrollWindow(this, x + 5, y, xSize - 20, ySize - 10 - (y - guiTop), 0) {
            @Override
            public void drawComponents(int mouseX, int mouseY, float partialTicks) {
                super.drawComponents(mouseX, mouseY, partialTicks);

                int iconRenderSize = 96;

                int x = 10, y = (clipHeight - 96) / 2;

                TextureManager textureManager = mc.getTextureManager();
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                ImageData data = ClientCacheHandler.getImageData(effect.icon);
                if (data.imageLoaded()) {
                    data.bindTexture();
                    int iconX = effect.iconX;
                    int iconY = effect.iconY;
                    int iconWidth = effect.getWidth();
                    int iconHeight = effect.getHeight();
                    int width = data.getTotalWidth();
                    int height = data.getTotalWidth();


                    func_152125_a(x, y, iconX, iconY, iconWidth, iconHeight, iconRenderSize, iconRenderSize, width, height);

                } else {
                    textureManager.bindTexture(new ResourceLocation("customnpcs", "textures/marks/question.png"));
                    func_152125_a(x, y, 0, 0, 1, 1, iconRenderSize, iconRenderSize, 1, 1);
                }
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                textureManager.bindTexture(specialIcons);
                func_152125_a(x, y, 0, 224, 16, 16, iconRenderSize, iconRenderSize, 256, 256);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }
        };
        addScrollableGui(0, scrollWindow);

        y = (scrollWindow.clipHeight - 96) / 2 + 5;
        x = 116;
        scrollWindow.addLabel(new GuiNpcLabel(5, "display.texture", x, y, 0xFFFFFF));
        y += 12;
        scrollWindow.addTextField(new GuiNpcTextField(5, this, x, y, scrollWindow.clipWidth - x - 10, 20, effect.icon));
        scrollWindow.getTextField(5).setMaxStringLength(100);

        y += 25;

        scrollWindow.addLabel(new GuiNpcLabel(6, "effect.editor.xPos", x, y + 6, 0xFFFFFF));
        scrollWindow.addTextField(setIntegerOnly(
            new GuiNpcTextField(6, this, x + 43, y, 60, 20, "" + effect.iconX),
            0,
            10240,
            effect.iconX
        ));
        scrollWindow.addTextField(setIntegerOnly(
            new GuiNpcTextField(7, this, scrollWindow.clipWidth - 60 - 10, y, 60, 20, "" + effect.iconY),
            0,
            10240,
            effect.iconY
        ));
        scrollWindow.addLabel(new GuiNpcLabel(7, "effect.editor.yPos", scrollWindow.getTextField(7).xPosition - 43, y + 6, 0xFFFFFF));
        y += 23;

        scrollWindow.addLabel(new GuiNpcLabel(8, "effect.editor.width", x, y + 6, 0xFFFFFF));
        scrollWindow.addTextField(setIntegerOnly(
            new GuiNpcTextField(8, this, x + 43, y, 60, 20, "" + effect.width),
            0,
            10240,
            effect.width
        ));
        scrollWindow.addTextField(setIntegerOnly(
            new GuiNpcTextField(9, this, scrollWindow.clipWidth - 60 - 10, y, 60, 20, "" + effect.height),
            0,
            10240,
            effect.height
        ));
        scrollWindow.addLabel(new GuiNpcLabel(9, "effect.editor.height", scrollWindow.getTextField(9).xPosition - 43, y + 6, 0xFFFFFF));

    }

    private GuiNpcTextField setIntegerOnly(GuiNpcTextField field, int min, int max, int def) {
        field.integersOnly = true;
        field.setMinMaxDefault(min, max, def);
        field.setMaxStringLength(6);
        return field;
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        int id = guibutton.id;

        if (id == -5) {
            close();
            return;
        }
        if (id == -2) {
            PacketClient.sendClient(new EffectSavePacket(effect.writeToNBT(false), originalName));
            GuiScriptEffect scriptGUI = new GuiScriptEffect(parent, effect);
            scriptGUI.setWorldAndResolution(mc, width, height);
            scriptGUI.initGui();
            mc.currentScreen = scriptGUI;
        }
        if (id == 10) {
            GuiNpcButtonYesNo button = (GuiNpcButtonYesNo) guibutton;
            effect.lossOnDeath = button.getBoolean();
        }
    }

    @Override
    public void unFocused(GuiNpcTextField guiNpcTextField) {
        int id = guiNpcTextField.id;

        switch (id) {
            case 1:
                effect.setName(guiNpcTextField.getText());
                break;
            case 2:
                effect.setMenuName(guiNpcTextField.getText());
                break;
            case 3:
                effect.setEveryXTick(guiNpcTextField.getInteger());
                guiNpcTextField.setText(effect.everyXTick + "");
                break;
            case 4:
                int length = guiNpcTextField.getInteger();
                if (length < 0)
                    length = -100;
                guiNpcTextField.setText(length + "");
                effect.length = length;
                break;
            case 5:
                effect.icon = guiNpcTextField.getText();
                break;
            case 6:
                effect.iconX = guiNpcTextField.getInteger();
                break;
            case 7:
                effect.iconY = guiNpcTextField.getInteger();
                break;
            case 8:
                effect.width = guiNpcTextField.getInteger();
                break;
            case 9:
                effect.height = guiNpcTextField.getInteger();
                break;
        }
    }

    @Override
    public void close() {
        super.close();
        PacketClient.sendClient(new EffectSavePacket(effect.writeToNBT(false), originalName));
    }
}
