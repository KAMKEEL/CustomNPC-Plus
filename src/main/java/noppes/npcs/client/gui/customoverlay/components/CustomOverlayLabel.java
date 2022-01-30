package noppes.npcs.client.gui.customoverlay.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.OpenGlHelper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.customoverlay.OverlayCustom;
import noppes.npcs.client.gui.customoverlay.interfaces.IOverlayComponent;
import noppes.npcs.scripted.interfaces.ICustomOverlayComponent;
import noppes.npcs.scripted.overlay.ScriptOverlayLabel;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class CustomOverlayLabel extends Gui implements IOverlayComponent {
    OverlayCustom parent;
    int alignment;
    int id;

    int x, y, width, height;
    String fullLabel;
    int color;
    String[] hoverText;
    float scale;
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

    public CustomOverlayLabel(int id, String fullLabel, int x, int y, int width, int height, boolean shadow){
        this.id = id;
        this.fullLabel = fullLabel;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.labelShadowEnabled = shadow;
    }

    public void setParent(OverlayCustom parent) {
        this.parent = parent;
    }

    public void onRender(Minecraft mc, float partialTicks) {
        this.drawLabel();
    }

    public int getID() {
        return this.id;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public static CustomOverlayLabel fromComponent(ScriptOverlayLabel component) {
        CustomOverlayLabel lbl = new CustomOverlayLabel(component.getID(), component.getText(), GuiCustom.guiLeft + component.getPosX(), GuiCustom.guiTop + component.getPosY(), component.getWidth(), component.getHeight(), component.hasShadow());
        lbl.scale = 1.0F;
        lbl.color = component.getColor();
        lbl.field_146172_j = true;
        lbl.setScale(component.getScale());

        lbl.labelShadowEnabled = component.hasShadow();
        lbl.alignment = component.getAlignment();

        return lbl;
    }

    public ICustomOverlayComponent toComponent() {
        ScriptOverlayLabel component = new ScriptOverlayLabel(this.id, this.fullLabel, this.field_146162_g, this.field_146174_h, this.field_146167_a, this.field_146161_f, this.color);
        component.enableShadow(this.labelShadowEnabled);
        component.setAlignment(alignment);
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
                GL11.glTranslatef(this.alignment%3*((float)(OverlayCustom.scaledWidth)/2), (float) (Math.floor((float)(alignment/3))*((float)(OverlayCustom.scaledHeight)/2)),0.0F);//alignment%3 * width/2  Math.floor(alignment/3) * height/2

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
