//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.client.gui.custom.components;

import java.util.Arrays;
import java.util.Iterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IClickListener;
import noppes.npcs.client.gui.custom.interfaces.IDataHolder;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.scripted.gui.ScriptGuiScroll;
import noppes.npcs.api.gui.ICustomGuiComponent;
import org.lwjgl.opengl.GL11;

public class CustomGuiScrollComponent extends GuiCustomScroll implements IDataHolder, IClickListener {
    GuiCustom parent;
    String[] hoverText;
    public boolean multiSelect;
    int color;
    float alpha;

    public CustomGuiScrollComponent(Minecraft mc, GuiScreen parent, int id, boolean multiSelect) {
        super(parent, id, multiSelect);
        this.mc = mc;
        this.fontRendererObj = mc.fontRenderer;
        this.multiSelect = multiSelect;
    }

    public void setParent(GuiCustom parent) {
        this.parent = parent;
    }

    public int getID() {
        return this.id;
    }

    public void onRender(Minecraft mc, int mouseX, int mouseY, int mouseWheel, float partialTicks) {
        GL11.glPushMatrix();
            float red = (color >> 16 & 255) / 255f;
            float green = (color >> 8  & 255) / 255f;
            float blue = (color & 255) / 255f;
            GL11.glColor4f(red,green,blue,this.alpha);

            GL11.glTranslatef(0.0F, 0.0F, (float)this.id);
            boolean hovered = mouseX >= this.guiLeft && mouseY >= this.guiTop && mouseX < this.guiLeft + this.xSize && mouseY < this.guiTop + this.ySize;
            super.drawScreen(mouseX, mouseY, partialTicks, mouseWheel);
            if (hovered && this.hoverText != null && this.hoverText.length > 0) {
                this.parent.hoverText = this.hoverText;
            }
        GL11.glPopMatrix();
    }

    public boolean mouseClicked(GuiCustom gui, int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        return this.isMouseOver(mouseX, mouseY);
    }

    public void fromComponent(ScriptGuiScroll component) {
        this.guiLeft = GuiCustom.guiLeft + component.getPosX();
        this.guiTop = GuiCustom.guiTop + component.getPosY();
        this.setSize(component.getWidth(), component.getHeight());
        this.setUnsortedList(Arrays.asList(component.getList()));
        if (component.getDefaultSelection() >= 0) {
            int defaultSelect = component.getDefaultSelection();
            if (defaultSelect < this.getList().size()) {
                this.selected = defaultSelect;
            }
        }

        if (component.hasHoverText()) {
            this.hoverText = component.getHoverText();
        }

        this.color = component.getColor();
        this.alpha = component.getAlpha();
    }

    public ICustomGuiComponent toComponent() {
        ScriptGuiScroll component = new ScriptGuiScroll(this.id, this.guiLeft - GuiCustom.guiLeft, this.guiTop - GuiCustom.guiTop, this.width, this.height, (String[])this.getList().toArray(new String[0]));
        component.setHoverText(this.hoverText);
        component.setColor(color);
        component.setAlpha(alpha);
        return component;
    }

    public NBTTagCompound toNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("id", this.id);
        if (!this.getSelectedList().isEmpty()) {
            NBTTagList tagList = new NBTTagList();
            Iterator var3 = this.getSelectedList().iterator();

            while(var3.hasNext()) {
                String s = (String)var3.next();
                tagList.appendTag(new NBTTagString(s));
            }

            nbt.setTag("selectedList", tagList);
        } else if (this.getSelected() != null && !this.getSelected().isEmpty()) {
            nbt.setString("selected", this.getSelected());
        } else {
            nbt.setString("selected", "Null");
        }

        return nbt;
    }
}
