//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.client.gui.custom.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.ImageDownloadAlt;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.client.renderer.ImageBufferDownloadAlt;
import noppes.npcs.scripted.gui.ScriptGuiTexturedRect;
import noppes.npcs.scripted.interfaces.ICustomGuiComponent;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class CustomGuiTexturedRect extends Gui implements IGuiComponent {
    GuiCustom parent;
    ResourceLocation texture;
    int id;
    int x;
    int y;
    int width;
    int height;
    int textureX;
    int textureY;
    float scale;
    String[] hoverText;

    int color;
    float alpha;
    float rotation;

    public CustomGuiTexturedRect(int id, String texture, int x, int y, int width, int height) {
        this(id, texture, x, y, width, height, 0, 0);
    }

    public CustomGuiTexturedRect(int id, String texture, int x, int y, int width, int height, int textureX, int textureY) {
        this.scale = 1.0F;
        this.id = id;
        this.texture = new ResourceLocation(texture);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.textureX = textureX;
        this.textureY = textureY;

        if(texture.startsWith("https://")){
            TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
            ITextureObject object = new ImageDownloadAlt(null, texture, SkinManager.field_152793_a, new ImageBufferDownloadAlt(false));
            texturemanager.loadTexture(this.texture, object);
        }
    }

    public void setParent(GuiCustom parent) {
        this.parent = parent;
    }

    public int getID() {
        return this.id;
    }

    public void onRender(Minecraft mc, int mouseX, int mouseY, int mouseWheel, float partialTicks) {
        boolean hovered = mouseX >= this.x + GuiCustom.guiLeft && mouseY >= this.y + GuiCustom.guiTop && mouseX < this.x + GuiCustom.guiLeft + this.width && mouseY < this.y + GuiCustom.guiTop + this.height;
        mc.getTextureManager().bindTexture(this.texture);

        GL11.glPushMatrix();
            float red = (color >> 16 & 255) / 255f;
            float green = (color >> 8  & 255) / 255f;
            float blue = (color & 255) / 255f;
            GL11.glColor4f(red,green,blue,this.alpha);

            GL11.glTranslatef(GuiCustom.guiLeft,GuiCustom.guiTop,0.0F);
            GL11.glRotatef(this.rotation,0.0F,0.0F,1.0F);
            GL11.glScalef(this.scale, this.scale, this.scale);

            int p_73729_1_ = (int) (this.x/this.scale);
            int p_73729_2_ = (int) (this.y/this.scale);
            int p_73729_3_ = this.textureX;
            int p_73729_4_ = this.textureY;
            int p_73729_5_ = (int)(this.width);
            int p_73729_6_ = (int)(this.height);

            float f = 0.00390625F;
            float f1 = 0.00390625F;
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            tessellator.setColorOpaque_F(1, 1, 1);
            tessellator.setColorRGBA_F(red, green, blue, alpha);
            tessellator.addVertexWithUV((double)(p_73729_1_ + 0), (double)(p_73729_2_ + p_73729_6_), (double)this.zLevel, (double)((float)(p_73729_3_ + 0) * f), (double)((float)(p_73729_4_ + p_73729_6_) * f1));
            tessellator.addVertexWithUV((double)(p_73729_1_ + p_73729_5_), (double)(p_73729_2_ + p_73729_6_), (double)this.zLevel, (double)((float)(p_73729_3_ + p_73729_5_) * f), (double)((float)(p_73729_4_ + p_73729_6_) * f1));
            tessellator.addVertexWithUV((double)(p_73729_1_ + p_73729_5_), (double)(p_73729_2_ + 0), (double)this.zLevel, (double)((float)(p_73729_3_ + p_73729_5_) * f), (double)((float)(p_73729_4_ + 0) * f1));
            tessellator.addVertexWithUV((double)(p_73729_1_ + 0), (double)(p_73729_2_ + 0), (double)this.zLevel, (double)((float)(p_73729_3_ + 0) * f), (double)((float)(p_73729_4_ + 0) * f1));
            tessellator.draw();
        GL11.glPopMatrix();

        if (hovered && this.hoverText != null && this.hoverText.length > 0) {
            this.parent.hoverText = this.hoverText;
        }
    }

    public ICustomGuiComponent toComponent() {
        ScriptGuiTexturedRect component = new ScriptGuiTexturedRect(this.id, this.texture.toString(), this.x, this.y, this.width, this.height, this.textureX, this.textureY);
        component.setHoverText(this.hoverText);
        component.setScale(this.scale);
        component.setColor(color);
        component.setAlpha(alpha);
        component.setRotation(rotation);
        return component;
    }

    public static CustomGuiTexturedRect fromComponent(ScriptGuiTexturedRect component) {
        CustomGuiTexturedRect rect;
        if (component.getTextureX() >= 0 && component.getTextureY() >= 0) {
            rect = new CustomGuiTexturedRect(component.getID(), component.getTexture(), component.getPosX(), component.getPosY(), component.getWidth(), component.getHeight(), component.getTextureX(), component.getTextureY());
        } else {
            rect = new CustomGuiTexturedRect(component.getID(), component.getTexture(), component.getPosX(), component.getPosY(), component.getWidth(), component.getHeight());
        }

        rect.scale = component.getScale();
        if (component.hasHoverText()) {
            rect.hoverText = component.getHoverText();
        }

        rect.color = component.getColor();
        rect.alpha = component.getAlpha();
        rect.rotation = component.getRotation();

        return rect;
    }
}
