//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.client.gui.custom.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.renderer.OpenGlHelper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.scripted.gui.ScriptGuiLabel;
import noppes.npcs.scripted.interfaces.ICustomGuiComponent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class CustomGuiLabel extends Gui implements IGuiComponent {
    int x, y, width, height;
    GuiCustom parent;
    String fullLabel;
    int color;
    String[] hoverText;
    float scale;
    int id;
    int border = 2;
    boolean labelBgEnabled = true;
    boolean labelShadowEnabled = false;

    protected int field_146167_a;
    protected int field_146161_f;
    public int field_146162_g;
    public int field_146174_h;
    private ArrayList field_146173_k;
    private boolean field_146170_l;
    public boolean field_146172_j;
    private boolean field_146171_m;
    private int field_146168_n;
    private int field_146169_o;
    private int field_146166_p;
    private int field_146165_q;
    private FontRenderer field_146164_r;
    private int field_146163_s;
    private static final String __OBFID = "CL_00000671";

    public CustomGuiLabel(int id, String fullLabel, int x, int y, int width, int height, boolean shadow){
        this.id = id;
        this.fullLabel = fullLabel;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.labelShadowEnabled = shadow;
    }

    public void setParent(GuiCustom parent) {
        this.parent = parent;
    }

    public void onRender(Minecraft mc, int mouseX, int mouseY, int mouseWheel, float partialTicks) {
        boolean hovered = mouseX >= this.field_146162_g && mouseY >= this.field_146174_h && mouseX < this.field_146162_g + this.field_146167_a && mouseY < this.field_146174_h + this.field_146161_f;
        this.drawLabel();
        if (hovered && this.hoverText != null && this.hoverText.length > 0) {
            this.parent.hoverText = this.hoverText;
        }
    }

    public int getID() {
        return this.id;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public static CustomGuiLabel fromComponent(ScriptGuiLabel component) {
        CustomGuiLabel lbl = new CustomGuiLabel(component.getID(), component.getText(), GuiCustom.guiLeft + component.getPosX(), GuiCustom.guiTop + component.getPosY(), component.getWidth(), component.getHeight(), component.hasShadow());
        lbl.scale = 1.0F;
        lbl.color = component.getColor();
        lbl.field_146172_j = true;
        lbl.setScale(component.getScale());
        if (component.hasHoverText()) {
            lbl.hoverText = component.getHoverText();
        }
        lbl.labelShadowEnabled = component.hasShadow();

        return lbl;
    }

    public ICustomGuiComponent toComponent() {
        ScriptGuiLabel component = new ScriptGuiLabel(this.id, this.fullLabel, this.field_146162_g, this.field_146174_h, this.field_146167_a, this.field_146161_f, this.color);
        component.setHoverText(this.hoverText);
        component.enableShadow(this.labelShadowEnabled);
        return component;
    }

    public void drawLabel()
    {
        if (this.field_146172_j)
        {
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            this.drawLabelBackground();
            GL11.glPushMatrix();
                GL11.glTranslatef(this.x,this.y,0.0F);
                GL11.glScalef(this.scale, this.scale, this.scale);
                Minecraft.getMinecraft().fontRenderer.drawString(fullLabel, 0, 0, this.color, this.labelShadowEnabled);
            GL11.glPopMatrix();
        }
    }

    protected void drawLabelBackground()
    {
        if (this.labelBgEnabled)
        {
            int k = (int)(this.width*this.scale) + this.border * 2;
            int l = (int)(this.height*this.scale) + this.border * 2;
            int i1 = this.x - this.border;
            int j1 = this.y - this.border;
            drawRect(i1, j1, i1 + k, j1 + l, this.color);//backColor
            this.drawHorizontalLine(i1, i1 + k, j1, this.color);//ulColor
            this.drawHorizontalLine(i1, i1 + k, j1 + l, this.color);//brColor
            this.drawVerticalLine(i1, j1, j1 + l, this.color);//ulColor
            this.drawVerticalLine(i1 + k, j1, j1 + l, this.color);//brColor
        }
    }
}
