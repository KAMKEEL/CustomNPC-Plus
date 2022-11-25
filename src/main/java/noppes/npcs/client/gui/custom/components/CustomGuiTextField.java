//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.client.gui.custom.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IClickListener;
import noppes.npcs.client.gui.custom.interfaces.IDataHolder;
import noppes.npcs.client.gui.custom.interfaces.IKeyListener;
import noppes.npcs.scripted.gui.ScriptGuiTextField;
import noppes.npcs.api.gui.ICustomGuiComponent;
import org.lwjgl.opengl.GL11;

public class CustomGuiTextField extends GuiTextField implements IKeyListener, IDataHolder, IClickListener {
    GuiCustom parent;
    String[] hoverText;
    int id;

    int color;
    float alpha;

    public CustomGuiTextField(int id, int x, int y, int width, int height) {
        super(Minecraft.getMinecraft().fontRenderer, GuiCustom.guiLeft + x, GuiCustom.guiTop + y, width, height);
        this.id = id;
        this.setMaxStringLength(500);
    }

    public void keyTyped(char typedChar, int keyCode) {
        this.textboxKeyTyped(typedChar, keyCode);
    }

    public void onRender(Minecraft mc, int mouseX, int mouseY, int mouseWheel, float partialTicks) {
        boolean hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
        float red = (color >> 16 & 255) / 255f;
        float green = (color >> 8  & 255) / 255f;
        float blue = (color & 255) / 255f;
        GL11.glColor4f(red,green,blue,this.alpha);
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F,0.0F,(float)this.id);
        this.drawTextBox();
        GL11.glPopMatrix();
        GL11.glColor4f(1.0F,1.0F,1.0F,1.0F);
        if (hovered && this.hoverText != null && this.hoverText.length > 0) {
            this.parent.hoverText = this.hoverText;
        }
    }

    public void setParent(GuiCustom parent) {
        this.parent = parent;
    }

    public int getID() {
        return this.id;
    }

    public NBTTagCompound toNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("id", this.id);
        tag.setString("text", this.getText());
        return tag;
    }

    public ICustomGuiComponent toComponent() {
        ScriptGuiTextField component = new ScriptGuiTextField(this.id, this.xPosition - GuiCustom.guiLeft, this.yPosition - GuiCustom.guiTop, this.width, this.height);
        component.setText(this.getText());
        component.setHoverText(this.hoverText);
        component.setColor(color);
        component.setAlpha(alpha);
        return component;
    }

    public static CustomGuiTextField fromComponent(ScriptGuiTextField component) {
        CustomGuiTextField txt = new CustomGuiTextField(component.getID(), component.getPosX(), component.getPosY(), component.getWidth(), component.getHeight());
        if (component.hasHoverText()) {
            txt.hoverText = component.getHoverText();
        }

        if (component.getText() != null && !component.getText().isEmpty()) {
            txt.setText(component.getText());
        }

        txt.color = component.getColor();
        txt.alpha = component.getAlpha();

        return txt;
    }

    public boolean mouseClicked(GuiCustom gui, int mouseX, int mouseY, int mouseButton) {
        boolean flag = mouseX >= this.xPosition && mouseX < this.xPosition + this.width && mouseY >= this.yPosition && mouseY < this.yPosition + this.height;

        if (this.isFocused() && mouseButton == 0 && !flag){
            parent.onTextFieldUnfocused(this);
        }

        this.mouseClicked(mouseX, mouseY, mouseButton);

        return true;
    }
}
