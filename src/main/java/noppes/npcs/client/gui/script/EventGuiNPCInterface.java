//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.client.gui.script;

import com.google.common.collect.Lists;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Post;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre;
import net.minecraftforge.common.MinecraftForge;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public abstract class EventGuiNPCInterface extends GuiScreen {
    public EntityPlayerSP player;
    public boolean drawDefaultBackground;
    public EntityNPCInterface npc;
    private Map<Integer, GuiNpcButton> buttons;
    private Map<Integer, GuiMenuTopButton> topbuttons;
    private Map<Integer, GuiMenuSideButton> sidebuttons;
    private Map<Integer, GuiNpcTextField> textfields;
    private Map<Integer, GuiNpcLabel> labels;
    private Map<Integer, GuiCustomScroll> scrolls;
    private Map<Integer, GuiNpcSlider> sliders;
    private Map<Integer, GuiScreen> extra;
    private List<IGui> components;
    public String title;
    public ResourceLocation background;
    public boolean closeOnEsc;
    public int guiLeft;
    public int guiTop;
    public int xSize;
    public int ySize;
    private SubGuiInterface subgui;
    public int mouseX;
    public int mouseY;
    public float bgScale;
    private GuiButton selectedButton;

    public EventGuiNPCInterface(EntityNPCInterface npc) {
        this.drawDefaultBackground = true;
        this.buttons = new ConcurrentHashMap();
        this.topbuttons = new ConcurrentHashMap();
        this.sidebuttons = new ConcurrentHashMap();
        this.textfields = new ConcurrentHashMap();
        this.labels = new ConcurrentHashMap();
        this.scrolls = new ConcurrentHashMap();
        this.sliders = new ConcurrentHashMap();
        this.extra = new ConcurrentHashMap();
        this.components = new ArrayList();
        this.background = null;
        this.closeOnEsc = false;
        this.bgScale = 1.0F;
        this.player = Minecraft.getMinecraft().thePlayer;
        this.npc = npc;
        this.title = "";
        this.xSize = 200;
        this.ySize = 222;
        this.drawDefaultBackground = false;
        this.mc = Minecraft.getMinecraft();
        this.itemRender = RenderItem.getInstance();
        this.fontRendererObj = this.mc.fontRenderer;
    }

    public EventGuiNPCInterface() {
        this((EntityNPCInterface)null);
    }

    public void setBackground(String texture) {
        this.background = new ResourceLocation("customnpcs", "textures/gui/" + texture);
    }

    public ResourceLocation getResource(String texture) {
        return new ResourceLocation("customnpcs", "textures/gui/" + texture);
    }

    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        super.setWorldAndResolution(mc, width, height);
        this.initPacket();
    }

    public void initPacket() {
    }

    public void initGui() {
        super.initGui();
        GuiNpcTextField.unfocus();
        if (this.subgui != null) {
            this.subgui.setWorldAndResolution(this.mc, this.width, this.height);
            this.subgui.initGui();
        }

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
        this.buttonList = Lists.newArrayList();
        this.buttons = new ConcurrentHashMap();
        this.topbuttons = new ConcurrentHashMap();
        this.sidebuttons = new ConcurrentHashMap();
        this.textfields = new ConcurrentHashMap();
        this.labels = new ConcurrentHashMap();
        this.scrolls = new ConcurrentHashMap();
        this.sliders = new ConcurrentHashMap();
        this.extra = new ConcurrentHashMap();
        this.components = new ArrayList();
    }

    public void updateScreen() {
        if (this.subgui != null) {
            this.subgui.updateScreen();
        } else {
            Iterator var1 = (new ArrayList(this.textfields.values())).iterator();

            while(var1.hasNext()) {
                GuiNpcTextField tf = (GuiNpcTextField)var1.next();
                if (tf.enabled) {
                    tf.updateCursorCounter();
                }
            }

            var1 = (new ArrayList(this.components)).iterator();

            while(var1.hasNext()) {
                IGui comp = (IGui)var1.next();
                comp.updateScreen();
            }

            super.updateScreen();
        }

    }

    public void addExtra(GuiHoverText gui) {
        gui.setWorldAndResolution(this.mc, 350, 250);
        this.extra.put(gui.id, gui);
    }

    public void mouseClicked(int i, int j, int k) {
        if (this.subgui != null) {
            this.subgui.mouseClicked(i, j, k);
        } else {
            Iterator var4 = (new ArrayList(this.textfields.values())).iterator();

            while(var4.hasNext()) {
                GuiNpcTextField tf = (GuiNpcTextField)var4.next();
                if (tf.enabled) {
                    tf.mouseClicked(i, j, k);
                }
            }

            var4 = (new ArrayList(this.components)).iterator();

            while(var4.hasNext()) {
                IGui comp = (IGui)var4.next();
                if (comp instanceof IMouseListener) {
                    ((IMouseListener)comp).mouseClicked(i, j, k);
                }
            }

            this.mouseEvent(i, j, k);
            if (k == 0) {
                var4 = (new ArrayList(this.scrolls.values())).iterator();

                while(var4.hasNext()) {
                    GuiCustomScroll scroll = (GuiCustomScroll)var4.next();
                    scroll.mouseClicked(i, j, k);
                }

                var4 = this.buttonList.iterator();

                while(var4.hasNext()) {
                    GuiButton guibutton = (GuiButton)var4.next();
                    if (guibutton.mousePressed(this.mc, this.mouseX, this.mouseY)) {
                        Pre event = new Pre(this, guibutton, this.buttonList);
                        if (!MinecraftForge.EVENT_BUS.post(event)) {
                            this.selectedButton = guibutton;
                            guibutton.func_146113_a(this.mc.getSoundHandler());
                            this.actionPerformed(guibutton);
                            if (this.equals(this.mc.currentScreen)) {
                                MinecraftForge.EVENT_BUS.post(new Post(this, guibutton, this.buttonList));
                            }
                        }
                        break;
                    }
                }
            }
        }

    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (this.selectedButton != null && state == 0) {
            this.selectedButton.mouseReleased(mouseX, mouseY);
            this.selectedButton = null;
        }

    }

    public void mouseEvent(int i, int j, int k) {
    }

    protected void actionPerformed(GuiButton guibutton) {
        if (this.subgui != null) {
            this.subgui.buttonEvent(guibutton);
        } else {
            this.buttonEvent(guibutton);
        }

    }
    public void buttonEvent(GuiButton guibutton){};

    public void keyTyped(char c, int i) {
        if (this.subgui != null) {
            this.subgui.keyTyped(c, i);
        } else {
            boolean active = false;
            Iterator var4 = this.components.iterator();

            IGui comp;
            while(var4.hasNext()) {
                comp = (IGui)var4.next();
                if (comp.isActive()) {
                    active = true;
                    break;
                }
            }

            active = active || GuiNpcTextField.isFieldActive();
            if (!this.closeOnEsc || i != 1 && (active || !this.isInventoryKey(i))) {
                var4 = (new ArrayList(this.textfields.values())).iterator();

                while(var4.hasNext()) {
                    GuiNpcTextField tf = (GuiNpcTextField)var4.next();
                    tf.textboxKeyTyped(c, i);
                }

                var4 = (new ArrayList(this.components)).iterator();

                while(var4.hasNext()) {
                    comp = (IGui)var4.next();
                    if (comp instanceof IKeyListener) {
                        ((IKeyListener)comp).keyTyped(c, i);
                    }
                }

            } else {
                this.close();
            }
        }
    }

    public void onGuiClosed() {
        GuiNpcTextField.unfocus();
    }

    public void close() {
        this.displayGuiScreen((GuiScreen)null);
        this.mc.setIngameFocus();
        this.save();
    }

    public void addButton(GuiNpcButton button) {
        this.buttons.put(button.id, button);
        this.buttonList.add(button);
    }

    public void addTopButton(GuiMenuTopButton button) {
        this.topbuttons.put(button.id, button);
        this.buttonList.add(button);
    }

    public void addSideButton(GuiMenuSideButton button) {
        this.sidebuttons.put(button.id, button);
        this.buttonList.add(button);
    }

    public GuiNpcButton getButton(int i) {
        return (GuiNpcButton)this.buttons.get(i);
    }

    public GuiMenuSideButton getSideButton(int i) {
        return (GuiMenuSideButton)this.sidebuttons.get(i);
    }

    public GuiMenuTopButton getTopButton(int i) {
        return (GuiMenuTopButton)this.topbuttons.get(i);
    }

    public void addTextField(GuiNpcTextField tf) {
        this.textfields.put(tf.id, tf);
    }

    public GuiNpcTextField getTextField(int i) {
        return (GuiNpcTextField)this.textfields.get(i);
    }

    public void add(IGui gui) {
        this.components.add(gui);
    }

    public IGui get(int id) {
        Iterator var2 = this.components.iterator();

        IGui comp;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            comp = (IGui)var2.next();
        } while(comp.getID() != id);

        return comp;
    }

    public void addLabel(GuiNpcLabel label) {
        this.labels.put(label.id, label);
    }

    public GuiNpcLabel getLabel(int i) {
        return (GuiNpcLabel)this.labels.get(i);
    }

    public void addSlider(GuiNpcSlider slider) {
        this.sliders.put(slider.id, slider);
        this.buttonList.add(slider);
    }

    public GuiNpcSlider getSlider(int i) {
        return (GuiNpcSlider)this.sliders.get(i);
    }

    public void addScroll(GuiCustomScroll scroll) {
        scroll.setWorldAndResolution(this.mc, 350, 250);
        this.scrolls.put(scroll.id, scroll);
    }

    public GuiCustomScroll getScroll(int id) {
        return (GuiCustomScroll)this.scrolls.get(id);
    }

    public abstract void save();

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        int x = mouseX;
        int y = mouseY;
        if (this.subgui != null) {
            y = 0;
            x = 0;
        }

        if (this.drawDefaultBackground && this.subgui == null) {
            this.drawDefaultBackground();
        }

        if (this.background != null && this.mc.renderEngine != null) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glPushMatrix();
            GL11.glTranslatef((float) this.guiLeft, (float) this.guiTop, 0.0F);
            GL11.glScalef(this.bgScale, this.bgScale, this.bgScale);
            this.mc.renderEngine.bindTexture(this.background);
            if (this.xSize > 256) {
                this.drawTexturedModalRect(0, 0, 0, 0, 250, this.ySize);
                this.drawTexturedModalRect(250, 0, 256 - (this.xSize - 250), 0, this.xSize - 250, this.ySize);
            } else {
                this.drawTexturedModalRect(0, 0, 0, 0, this.xSize, this.ySize);
            }

            GL11.glPopMatrix();
        }

        this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 8, 16777215);
        Iterator var6 = (new ArrayList(this.labels.values())).iterator();

        while(var6.hasNext()) {
            GuiNpcLabel label = (GuiNpcLabel)var6.next();
            label.drawLabel(this, this.fontRendererObj);
        }

        var6 = (new ArrayList(this.textfields.values())).iterator();

        while(var6.hasNext()) {
            GuiNpcTextField tf = (GuiNpcTextField)var6.next();
            tf.drawTextBox(x, y);
        }

        var6 = (new ArrayList(this.components)).iterator();

        while(var6.hasNext()) {
            IGui comp = (IGui)var6.next();
            comp.drawScreen(x, y);
        }

        var6 = (new ArrayList(this.scrolls.values())).iterator();

        while(var6.hasNext()) {
            GuiCustomScroll scroll = (GuiCustomScroll)var6.next();
            scroll.drawScreen(x, y, partialTicks, !this.hasSubGui() && scroll.isMouseOver(x, y) ? Mouse.getDWheel() : 0);
        }

        var6 = (new ArrayList(this.extra.values())).iterator();

        while(var6.hasNext()) {
            GuiScreen gui = (GuiScreen)var6.next();
            gui.drawScreen(x, y, partialTicks);
        }

        super.drawScreen(x, y, partialTicks);
        if (this.subgui != null) {
            this.subgui.drawScreen(mouseX, mouseY, partialTicks);
        }

    }

    public FontRenderer getFontRenderer() {
        return this.fontRendererObj;
    }

    public void elementClicked() {
        if (this.subgui != null) {
            this.subgui.elementClicked();
        }

    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    public void doubleClicked() {
    }

    public boolean isInventoryKey(int i) {
        return i == this.mc.gameSettings.keyBindInventory.getKeyCode();
    }

    public void drawDefaultBackground() {
        super.drawDefaultBackground();
    }

    public void displayGuiScreen(GuiScreen gui) {
        this.mc.displayGuiScreen(gui);
    }

    public void setSubGui(SubGuiInterface gui) {
        this.subgui = gui;
        this.subgui.npc = this.npc;
        this.subgui.setWorldAndResolution(this.mc, this.width, this.height);
        this.subgui.parent = this;
        this.initGui();
    }

    public void closeSubGui(SubGuiInterface gui) {
        this.subgui = null;
    }

    public boolean hasSubGui() {
        return this.subgui != null;
    }

    public SubGuiInterface getSubGui() {
        return this.hasSubGui() && this.subgui.hasSubGui() ? this.subgui.getSubGui() : this.subgui;
    }

    public void drawNpc(int x, int y) {
        this.drawNpc(this.npc, x, y, 1.0F, 0);
    }

    public void drawNpc(EntityLivingBase entity, int x, int y, float zoomed, int rotation) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glColorMaterial(0,1);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(this.guiLeft + x), (float)(this.guiTop + y), 50.0F);
        float scale = 1.0F;
        if ((double)entity.height > 2.4D) {
            scale = 2.0F / entity.height;
        }

        GL11.glScalef(-30.0F * scale * zoomed, 30.0F * scale * zoomed, 30.0F * scale * zoomed);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        RenderHelper.enableStandardItemLighting();
        float f2 = entity.renderYawOffset;
        float f3 = entity.rotationYaw;
        float f4 = entity.rotationPitch;
        float f7 = entity.rotationYawHead;
        float f5 = (float)(this.guiLeft + x) - (float)this.mouseX;
        float f6 = (float)(this.guiTop + y) - 50.0F * scale * zoomed - (float)this.mouseY;

        GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-((float) Math.atan((double) (f6 / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
        entity.renderYawOffset = (float)rotation;
        entity.rotationYaw = (float)Math.atan((double)(f5 / 80.0F)) * 40.0F + (float)rotation;
        entity.rotationPitch = -((float)Math.atan((double)(f6 / 40.0F))) * 20.0F;
        entity.rotationYawHead	 = entity.rotationYaw;
        RenderItem.getInstance().zLevel = 180.0F;
        RenderItem.getInstance().doRender(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        entity.prevRenderYawOffset = entity.renderYawOffset = f2;
        entity.prevRotationYaw = entity.rotationYaw = f3;
        entity.prevRotationPitch = entity.rotationPitch = f4;
        entity.prevRotationYawHead = entity.rotationYawHead = f7;

        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
    }

    public void openLink(String link) {
        try {
            Class oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop").invoke((Object)null);
            oclass.getMethod("browse", URI.class).invoke(object, new URI(link));
        } catch (Throwable var4) {
        }

    }
}
