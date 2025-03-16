package noppes.npcs.client.gui.custom.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.client.renderer.ImageData;
import noppes.npcs.scripted.gui.ScriptGuiTexturedRect;
import org.lwjgl.opengl.GL11;

public class CustomGuiTexturedRect extends Gui implements IGuiComponent {
    GuiCustom parent;

    ImageData imageData = null;
    public String texture;
    public int id;
    public int x;
    public int y;
    public int width;
    public int height;
    public int textureX;
    public int textureY;
    public float scale;
    String[] hoverText;

    public int color;
    public float alpha;
    public float rotation;

    public CustomGuiTexturedRect(int id, String texture, int x, int y, int width, int height) {
        this(id, texture, x, y, width, height, 0, 0);
    }

    public CustomGuiTexturedRect(int id, String directory, int x, int y, int width, int height, int textureX, int textureY) {
        this.scale = 1.0F;
        this.id = id;
        this.texture = directory;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.textureX = textureX;
        this.textureY = textureY;
        if (texture != null && !texture.isEmpty()) {
            this.imageData = ClientCacheHandler.getImageData(texture);
        }
    }

    public void setParent(GuiCustom parent) {
        this.parent = parent;
    }

    public int getID() {
        return this.id;
    }

    public void onRender(Minecraft mc, int mouseX, int mouseY, int mouseWheel, float partialTicks) {
        boolean hovered = mouseX >= this.x + GuiCustom.guiLeft && mouseY >= this.y + GuiCustom.guiTop && mouseX < this.x + GuiCustom.guiLeft + this.width * scale && mouseY < this.y + GuiCustom.guiTop + this.height * scale;

        if (this.imageData != null && this.imageData.imageLoaded()) {
            int totalWidth = this.imageData.getTotalWidth();
            int totalHeight = this.imageData.getTotalHeight();

            float u1 = (float) textureX / (float) totalWidth;
            float u2 = u1 + (float) width / (float) totalWidth;
            float v1 = (float) textureY / (float) totalHeight;
            float v2 = v1 + (float) height / (float) totalHeight;

            GL11.glPushMatrix();
            float red = (color >> 16 & 255) / 255f;
            float green = (color >> 8 & 255) / 255f;
            float blue = (color & 255) / 255f;
            GL11.glColor4f(red, green, blue, this.alpha);

            GL11.glTranslatef(GuiCustom.guiLeft + this.x, GuiCustom.guiTop + this.y, (float) this.id);
            GL11.glRotatef(this.rotation, 0.0F, 0.0F, 1.0F);
            GL11.glScalef(this.scale, this.scale, this.scale);

            this.imageData.bindTexture();
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            tessellator.setColorOpaque_F(1, 1, 1);
            tessellator.setColorRGBA_F(red, green, blue, alpha);
            tessellator.addVertexWithUV(0, height, 0, u1, v2);
            tessellator.addVertexWithUV(width, height, 0, u2, v2);
            tessellator.addVertexWithUV(width, 0, 0, u2, v1);
            tessellator.addVertexWithUV(0, 0, 0, u1, v1);
            tessellator.draw();
            GL11.glPopMatrix();
        }

        if (hovered && this.hoverText != null && this.hoverText.length > 0) {
            this.parent.hoverText = this.hoverText;
        }
    }

    public ICustomGuiComponent toComponent() {
        ScriptGuiTexturedRect component = new ScriptGuiTexturedRect(this.id, this.texture, this.x, this.y, this.width, this.height, this.textureX, this.textureY);
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
