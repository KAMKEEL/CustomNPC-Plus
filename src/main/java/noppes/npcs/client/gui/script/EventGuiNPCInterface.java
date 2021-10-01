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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Post;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre;
import net.minecraftforge.common.MinecraftForge;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.input.Mouse;

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
        this.player = Minecraft.func_71410_x().field_71439_g;
        this.npc = npc;
        this.title = "";
        this.xSize = 200;
        this.ySize = 222;
        this.drawDefaultBackground = false;
        this.field_146297_k = Minecraft.func_71410_x();
        this.field_146296_j = this.field_146297_k.func_175599_af();
        this.field_146289_q = this.field_146297_k.field_71466_p;
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

    public void func_146280_a(Minecraft mc, int width, int height) {
        super.func_146280_a(mc, width, height);
        this.initPacket();
    }

    public void initPacket() {
    }

    public void func_73866_w_() {
        super.func_73866_w_();
        GuiNpcTextField.unfocus();
        if (this.subgui != null) {
            this.subgui.func_146280_a(this.field_146297_k, this.field_146294_l, this.field_146295_m);
            this.subgui.func_73866_w_();
        }

        this.guiLeft = (this.field_146294_l - this.xSize) / 2;
        this.guiTop = (this.field_146295_m - this.ySize) / 2;
        this.field_146292_n = Lists.newArrayList();
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

    public void func_73876_c() {
        if (this.subgui != null) {
            this.subgui.func_73876_c();
        } else {
            Iterator var1 = (new ArrayList(this.textfields.values())).iterator();

            while(var1.hasNext()) {
                GuiNpcTextField tf = (GuiNpcTextField)var1.next();
                if (tf.enabled) {
                    tf.func_146178_a();
                }
            }

            var1 = (new ArrayList(this.components)).iterator();

            while(var1.hasNext()) {
                IGui comp = (IGui)var1.next();
                comp.updateScreen();
            }

            super.func_73876_c();
        }

    }

    public void addExtra(GuiHoverText gui) {
        gui.func_146280_a(this.field_146297_k, 350, 250);
        this.extra.put(gui.id, gui);
    }

    public void func_73864_a(int i, int j, int k) {
        if (this.subgui != null) {
            this.subgui.func_73864_a(i, j, k);
        } else {
            Iterator var4 = (new ArrayList(this.textfields.values())).iterator();

            while(var4.hasNext()) {
                GuiNpcTextField tf = (GuiNpcTextField)var4.next();
                if (tf.enabled) {
                    tf.func_146192_a(i, j, k);
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
                    scroll.func_73864_a(i, j, k);
                }

                var4 = this.field_146292_n.iterator();

                while(var4.hasNext()) {
                    GuiButton guibutton = (GuiButton)var4.next();
                    if (guibutton.func_146116_c(this.field_146297_k, this.mouseX, this.mouseY)) {
                        Pre event = new Pre(this, guibutton, this.field_146292_n);
                        if (!MinecraftForge.EVENT_BUS.post(event)) {
                            guibutton = event.getButton();
                            this.selectedButton = guibutton;
                            guibutton.func_146113_a(this.field_146297_k.func_147118_V());
                            this.func_146284_a(guibutton);
                            if (this.equals(this.field_146297_k.field_71462_r)) {
                                MinecraftForge.EVENT_BUS.post(new Post(this, event.getButton(), this.field_146292_n));
                            }
                        }
                        break;
                    }
                }
            }
        }

    }

    public void func_146286_b(int mouseX, int mouseY, int state) {
        if (this.selectedButton != null && state == 0) {
            this.selectedButton.func_146118_a(mouseX, mouseY);
            this.selectedButton = null;
        }

    }

    public void mouseEvent(int i, int j, int k) {
    }

    protected void func_146284_a(GuiButton guibutton) {
        if (this.subgui != null) {
            this.subgui.buttonEvent(guibutton);
        } else {
            this.buttonEvent(guibutton);
        }

    }

    public void buttonEvent(GuiButton guibutton) {
    }

    public void func_73869_a(char c, int i) {
        if (this.subgui != null) {
            this.subgui.func_73869_a(c, i);
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

            active = active || GuiNpcTextField.isActive();
            if (!this.closeOnEsc || i != 1 && (active || !this.isInventoryKey(i))) {
                var4 = (new ArrayList(this.textfields.values())).iterator();

                while(var4.hasNext()) {
                    GuiNpcTextField tf = (GuiNpcTextField)var4.next();
                    tf.func_146201_a(c, i);
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

    public void func_146281_b() {
        GuiNpcTextField.unfocus();
    }

    public void close() {
        this.displayGuiScreen((GuiScreen)null);
        this.field_146297_k.func_71381_h();
        this.save();
    }

    public void addButton(GuiNpcButton button) {
        this.buttons.put(button.field_146127_k, button);
        this.field_146292_n.add(button);
    }

    public void addTopButton(GuiMenuTopButton button) {
        this.topbuttons.put(button.field_146127_k, button);
        this.field_146292_n.add(button);
    }

    public void addSideButton(GuiMenuSideButton button) {
        this.sidebuttons.put(button.field_146127_k, button);
        this.field_146292_n.add(button);
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
        this.textfields.put(tf.field_175208_g, tf);
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
        this.sliders.put(slider.field_146127_k, slider);
        this.field_146292_n.add(slider);
    }

    public GuiNpcSlider getSlider(int i) {
        return (GuiNpcSlider)this.sliders.get(i);
    }

    public void addScroll(GuiCustomScroll scroll) {
        scroll.func_146280_a(this.field_146297_k, 350, 250);
        this.scrolls.put(scroll.id, scroll);
    }

    public GuiCustomScroll getScroll(int id) {
        return (GuiCustomScroll)this.scrolls.get(id);
    }

    public abstract void save();

    public void func_73863_a(int mouseX, int mouseY, float partialTicks) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        int x = mouseX;
        int y = mouseY;
        if (this.subgui != null) {
            y = 0;
            x = 0;
        }

        if (this.drawDefaultBackground && this.subgui == null) {
            this.func_146276_q_();
        }

        if (this.background != null && this.field_146297_k.field_71446_o != null) {
            GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.func_179094_E();
            GlStateManager.func_179109_b((float)this.guiLeft, (float)this.guiTop, 0.0F);
            GlStateManager.func_179152_a(this.bgScale, this.bgScale, this.bgScale);
            this.field_146297_k.field_71446_o.func_110577_a(this.background);
            if (this.xSize > 256) {
                this.func_73729_b(0, 0, 0, 0, 250, this.ySize);
                this.func_73729_b(250, 0, 256 - (this.xSize - 250), 0, this.xSize - 250, this.ySize);
            } else {
                this.func_73729_b(0, 0, 0, 0, this.xSize, this.ySize);
            }

            GlStateManager.func_179121_F();
        }

        this.func_73732_a(this.field_146289_q, this.title, this.field_146294_l / 2, 8, 16777215);
        Iterator var6 = (new ArrayList(this.labels.values())).iterator();

        while(var6.hasNext()) {
            GuiNpcLabel label = (GuiNpcLabel)var6.next();
            label.drawLabel(this, this.field_146289_q);
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
            gui.func_73863_a(x, y, partialTicks);
        }

        super.func_73863_a(x, y, partialTicks);
        if (this.subgui != null) {
            this.subgui.func_73863_a(mouseX, mouseY, partialTicks);
        }

    }

    public FontRenderer getFontRenderer() {
        return this.field_146289_q;
    }

    public void elementClicked() {
        if (this.subgui != null) {
            this.subgui.elementClicked();
        }

    }

    public boolean func_73868_f() {
        return false;
    }

    public void doubleClicked() {
    }

    public boolean isInventoryKey(int i) {
        return i == this.field_146297_k.field_71474_y.field_151445_Q.func_151463_i();
    }

    public void func_146276_q_() {
        super.func_146276_q_();
    }

    public void displayGuiScreen(GuiScreen gui) {
        this.field_146297_k.func_147108_a(gui);
    }

    public void setSubGui(SubGuiInterface gui) {
        this.subgui = gui;
        this.subgui.npc = this.npc;
        this.subgui.func_146280_a(this.field_146297_k, this.field_146294_l, this.field_146295_m);
        this.subgui.parent = this;
        this.func_73866_w_();
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
        EntityNPCInterface npc = null;
        if (entity instanceof EntityNPCInterface) {
            npc = (EntityNPCInterface)entity;
        }

        GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.func_179142_g();
        GlStateManager.func_179094_E();
        GlStateManager.func_179109_b((float)(this.guiLeft + x), (float)(this.guiTop + y), 50.0F);
        float scale = 1.0F;
        if ((double)entity.field_70131_O > 2.4D) {
            scale = 2.0F / entity.field_70131_O;
        }

        GlStateManager.func_179152_a(-30.0F * scale * zoomed, 30.0F * scale * zoomed, 30.0F * scale * zoomed);
        GlStateManager.func_179114_b(180.0F, 0.0F, 0.0F, 1.0F);
        RenderHelper.func_74519_b();
        float f2 = entity.field_70761_aq;
        float f3 = entity.field_70177_z;
        float f4 = entity.field_70125_A;
        float f7 = entity.field_70759_as;
        float f5 = (float)(this.guiLeft + x) - (float)this.mouseX;
        float f6 = (float)(this.guiTop + y) - 50.0F * scale * zoomed - (float)this.mouseY;
        int orientation = 0;
        if (npc != null) {
            orientation = npc.ais.orientation;
            npc.ais.orientation = rotation;
        }

        GlStateManager.func_179114_b(135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.func_179114_b(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.func_179114_b(-((float)Math.atan((double)(f6 / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
        entity.field_70761_aq = (float)rotation;
        entity.field_70177_z = (float)Math.atan((double)(f5 / 80.0F)) * 40.0F + (float)rotation;
        entity.field_70125_A = -((float)Math.atan((double)(f6 / 40.0F))) * 20.0F;
        entity.field_70759_as = entity.field_70177_z;
        this.field_146297_k.func_175598_ae().field_78735_i = 180.0F;
        this.field_146297_k.func_175598_ae().func_188391_a(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        entity.field_70760_ar = entity.field_70761_aq = f2;
        entity.field_70126_B = entity.field_70177_z = f3;
        entity.field_70127_C = entity.field_70125_A = f4;
        entity.field_70758_at = entity.field_70759_as = f7;
        if (npc != null) {
            npc.ais.orientation = orientation;
        }

        GlStateManager.func_179121_F();
        RenderHelper.func_74518_a();
        GlStateManager.func_179101_C();
        GlStateManager.func_179138_g(OpenGlHelper.field_77476_b);
        GlStateManager.func_179090_x();
        GlStateManager.func_179138_g(OpenGlHelper.field_77478_a);
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
