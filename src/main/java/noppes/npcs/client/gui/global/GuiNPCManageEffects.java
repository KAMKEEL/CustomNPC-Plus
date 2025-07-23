package noppes.npcs.client.gui.global;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.effects.EffectGetPacket;
import kamkeel.npcs.network.packets.request.effects.EffectRemovePacket;
import kamkeel.npcs.network.packets.request.effects.EffectSavePacket;
import kamkeel.npcs.network.packets.request.effects.EffectsGetPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiEffectGeneral;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.renderer.ImageData;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.controllers.CustomEffectController;
import noppes.npcs.controllers.data.CustomEffect;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import static noppes.npcs.client.gui.player.inventory.GuiCNPCInventory.specialIcons;

public class GuiNPCManageEffects extends GuiNPCInterface2 implements ICustomScrollListener, IScrollData, IGuiData, ISubGuiListener, GuiYesNoCallback {
    public GuiCustomScroll scrollEffects;
    public HashMap<String, Integer> data = new HashMap<>();
    public CustomEffect effect = new CustomEffect();
    public String selected = null;
    public String search = "";
    public String originalName = "";
    boolean setNormalSound = true;

    public GuiNPCManageEffects(EntityNPCInterface npc) {
        super(npc);

        PacketClient.sendClient(new EffectsGetPacket(-1));
    }

    public void initGui() {
        super.initGui();
        addButton(new GuiNpcButton(0, guiLeft + 368, guiTop + 8, 45, 20, "gui.add"));

        addButton(new GuiNpcButton(1, guiLeft + 368, guiTop + 32, 45, 20, "gui.remove"));
        getButton(1).enabled = effect != null && effect.id != -1;

        addButton(new GuiNpcButton(2, guiLeft + 368, guiTop + 56, 45, 20, "gui.copy"));
        getButton(2).enabled = effect != null && effect.id != -1;

        addButton(new GuiNpcButton(3, guiLeft + 368, guiTop + 80, 45, 20, "gui.edit"));
        getButton(3).enabled = effect != null && effect.id != -1;

        if (scrollEffects == null) {
            scrollEffects = new GuiCustomScroll(this, 0, 0);
            scrollEffects.setSize(143, 185);
        }
        scrollEffects.guiLeft = guiLeft + 220;
        scrollEffects.guiTop = guiTop + 4;
        addScroll(scrollEffects);
        scrollEffects.setList(getSearchList());

        addTextField(new GuiNpcTextField(55, this, fontRendererObj, guiLeft + 220, guiTop + 4 + 3 + 185, 143, 20, search));
        if (effect != null && effect.id != -1) {
            addLabel(new GuiNpcLabel(10, "ID", guiLeft + 368, guiTop + 4 + 3 + 185));
            addLabel(new GuiNpcLabel(11, effect.id + "", guiLeft + 368, guiTop + 4 + 3 + 195));
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        GuiNpcButton button = (GuiNpcButton) guibutton;
        if (button.id == 0) {
            save();
            String name = "New";
            while (data.containsKey(name))
                name += "_";
            CustomEffect effect = new CustomEffect(-1, name);
            PacketClient.sendClient(new EffectSavePacket(effect.writeToNBT(false), ""));
        } else if (button.id == 1) {
            if (data.containsKey(scrollEffects.getSelected())) {
                GuiYesNo guiyesno = new GuiYesNo(this, scrollEffects.getSelected(), StatCollector.translateToLocal("gui.delete"), 1);
                displayGuiScreen(guiyesno);
            }
        } else if (button.id == 2) {
            CustomEffect effect = this.effect.cloneEffect();
            while (data.containsKey(effect.name))
                effect.name += "_";
            PacketClient.sendClient(new EffectSavePacket(effect.writeToNBT(false), ""));
        }

        if (effect == null)
            return;

        if (button.id == 3) {
            if (data.containsKey(scrollEffects.getSelected()) && effect != null && effect.id >= 0) {
                setSubGui(new SubGuiEffectGeneral(this, effect));
            }
        }


    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        this.effect = new CustomEffect();
        effect.readFromNBT(compound);
        setSelected(effect.name);
        if (effect.id != -1) {
            CustomEffectController.getInstance().getCustomEffects().replace(effect.id, effect);
        }
        initGui();
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
        drawGradientRect(guiLeft + 5, guiTop + 4, guiLeft + 218, guiTop + 24, 0xC0101010, 0xC0101010);
        drawHorizontalLine(guiLeft + 5, guiLeft + 218, guiTop + 25, 0xFF000000 + CustomNpcResourceListener.DefaultTextColor);
        drawGradientRect(guiLeft + 5, guiTop + 27, guiLeft + 218, guiTop + ySize + 9, 0xA0101010, 0xA0101010);


        if (effect == null)
            return;
        if (effect.id == -1)
            return;

        String drawString = effect.getMenuName();
        int textWidth = getStringWidthWithoutColor(drawString);
        int centerX = guiLeft + 5 + ((218 - 10 - textWidth) / 2); // Adjusted centerX calculation
        fontRendererObj.drawString(drawString, centerX, guiTop + 10, CustomNpcResourceListener.DefaultTextColor, true);
        int y = guiTop + 33;
        int x = guiLeft + 12;

        int iconRenderSize = 48;

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

        x += iconRenderSize + 3;
        y += 2;

        String translated = StatCollector.translateToLocal("gui.name") + ": " + effect.name;
        fontRendererObj.drawString(translated, x, y, 0xFFFFFF, false);
        int transLength = getStringWidthWithoutColor(translated);
        fontRendererObj.drawString(" (ID: " + effect.id + ")", x + transLength, y, 0xB5B5B5, false);
        y += 12;
        fontRendererObj.drawString(StatCollector.translateToLocal("general.menuName") + ": " + effect.menuName, x, y, 0xFFFFFF, false);
        y += 12;
        fontRendererObj.drawString(StatCollector.translateToLocal("effect.runsEveryX") + ": " + effect.everyXTick + "t", x, y, 0xB5B5B5, false);
        y += 12;
        fontRendererObj.drawString(StatCollector.translateToLocal("effect.defaultLength") + ": " + effect.length + "s", x, y, 0xB5B5B5, false);

    }


    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);

        if (getTextField(55) != null) {
            if (getTextField(55).isFocused()) {
                if (search.equals(getTextField(55).getText()))
                    return;
                search = getTextField(55).getText().toLowerCase();
                scrollEffects.resetScroll();
                scrollEffects.setList(getSearchList());
            }
        }
    }

    private List<String> getSearchList() {
        if (search.isEmpty()) {
            return new ArrayList<String>(this.data.keySet());
        }
        List<String> list = new ArrayList<String>();
        for (String name : this.data.keySet()) {
            if (name.toLowerCase().contains(search))
                list.add(name);
        }
        return list;
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        String name = scrollEffects.getSelected();
        this.data = data;
        scrollEffects.setList(getSearchList());

        if (name != null)
            scrollEffects.setSelected(name);
    }

    @Override
    public void setSelected(String selected) {
        this.selected = selected;
        scrollEffects.setSelected(selected);
        originalName = scrollEffects.getSelected();
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 0) {
            save();
            effect = null;
            selected = scrollEffects.getSelected();
            originalName = scrollEffects.getSelected();
            if (selected != null && !selected.isEmpty()) {
                PacketClient.sendClient(new EffectGetPacket(data.get(selected)));
            }
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
//        ICustomScrollListener.super.customScrollDoubleClicked(selection, scroll);
    }

    @Override
    public void save() {
        if (this.selected != null && this.data.containsKey(this.selected) && this.effect != null) {
            PacketClient.sendClient(new EffectSavePacket(effect.writeToNBT(false), originalName));
        }
    }


    @Override
    public void subGuiClosed(SubGuiInterface subgui) {

    }

    @Override
    public void confirmClicked(boolean result, int id) {
        NoppesUtil.openGUI(player, this);
        if (!result)
            return;
        if (id == 1) {
            if (data.containsKey(scrollEffects.getSelected())) {
                PacketClient.sendClient(new EffectRemovePacket(data.get(scrollEffects.getSelected())));
                scrollEffects.clear();
                effect = new CustomEffect();
                initGui();
            }
        }
    }

    public int getStringWidthWithoutColor(String text) {
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '§') {
                if (i < text.length() - 1) {
                    i += 1;
                }
            } else {
                // If not a color code, calculate the width
                width += fontRendererObj.getCharWidth(c);
            }
        }
        return width;
    }
}
