//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.client.gui.custom.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.renderer.OpenGlHelper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.scripted.gui.ScriptGuiLabel;
import noppes.npcs.scripted.interfaces.ICustomGuiComponent;
import org.lwjgl.opengl.GL11;

public class CustomGuiLabel extends GuiLabel implements IGuiComponent {
    int x, y, width, height;
    GuiCustom parent;
    String fullLabel;
    int color;
    String[] hoverText;
    float scale;
    int id;
    int border = 2;
    boolean labelBgEnabled = true;

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
        CustomGuiLabel lbl = new CustomGuiLabel();
        lbl.scale = 1.0F;
        lbl.fullLabel = component.getText();
        lbl.color = component.getColor();
        lbl.field_146172_j = true;
        lbl.id = component.getID();
        lbl.x = GuiCustom.guiLeft + component.getPosX();
        lbl.y = GuiCustom.guiTop + component.getPosY();
        lbl.width = component.getWidth();
        lbl.height = component.getHeight();
        lbl.setScale(component.getScale());
        if (component.hasHoverText()) {
            lbl.hoverText = component.getHoverText();
        }

        return lbl;
    }

    public ICustomGuiComponent toComponent() {
        ScriptGuiLabel component = new ScriptGuiLabel(this.id, this.fullLabel, this.field_146162_g, this.field_146174_h, this.field_146167_a, this.field_146161_f, this.color);
        component.setHoverText(this.hoverText);
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
            this.drawString(Minecraft.getMinecraft().fontRenderer, fullLabel, this.x, this.y, this.color);
        }
    }

    protected void drawLabelBackground()
    {
        if (this.labelBgEnabled)
        {
            int k = this.width + this.border * 2;
            int l = this.height + this.border * 2;
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
