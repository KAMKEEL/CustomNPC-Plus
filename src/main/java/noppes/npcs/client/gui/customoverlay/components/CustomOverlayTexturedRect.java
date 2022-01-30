package noppes.npcs.client.gui.customoverlay.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.gui.customoverlay.OverlayCustom;
import noppes.npcs.client.gui.customoverlay.interfaces.IOverlayComponent;
import noppes.npcs.scripted.interfaces.ICustomOverlayComponent;
import noppes.npcs.scripted.overlay.ScriptOverlayTexturedRect;
import org.lwjgl.opengl.GL11;

public class CustomOverlayTexturedRect extends Gui implements IOverlayComponent {
    OverlayCustom parent;
    int alignment;
    int id;

    ResourceLocation texture;
    int x;
    int y;
    int width;
    int height;
    int textureX;
    int textureY;
    float scale;

    int color;
    float alpha;

    public CustomOverlayTexturedRect(int id, String texture, int x, int y, int width, int height) {
        this(id, texture, x, y, width, height, 0, 0);
    }

    public CustomOverlayTexturedRect(int id, String texture, int x, int y, int width, int height, int textureX, int textureY) {
        this.scale = 1.0F;
        this.alignment = 0;
        this.id = id;
        this.texture = new ResourceLocation(texture);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.textureX = textureX;
        this.textureY = textureY;
    }

    public void setParent(OverlayCustom parent) {
        this.parent = parent;
    }

    public int getID() {
        return this.id;
    }

    public void onRender(Minecraft mc, float partialTicks) {
        mc.getTextureManager().bindTexture(this.texture);

        GL11.glPushMatrix();
            float red = (color >> 16 & 255) / 255f;
            float green = (color >> 8  & 255) / 255f;
            float blue = (color & 255) / 255f;
            GL11.glColor4f(red,green,blue,this.alpha);

            GL11.glTranslatef(this.alignment%3*((float)(OverlayCustom.scaledWidth)/2), (float) (Math.floor((float)(alignment/3))*((float)(OverlayCustom.scaledHeight)/2)),0.0F);//alignment%3 * width/2  Math.floor(alignment/3) * height/2

            GL11.glScalef(this.scale, this.scale, this.scale);
            this.drawTexturedModalRect((int) (this.x/this.scale), (int) (this.y/this.scale),  this.textureX, this.textureY, (int)(this.width), (int)(this.height));
        GL11.glPopMatrix();
    }

    public ICustomOverlayComponent toComponent() {
        ScriptOverlayTexturedRect component = new ScriptOverlayTexturedRect(this.id, this.texture.toString(), this.x, this.y, this.width, this.height, this.textureX, this.textureY);
        component.setScale(scale);
        component.setAlignment(alignment);
        component.setAlpha(alpha);
        component.setColor(color);
        return component;
    }

    public static CustomOverlayTexturedRect fromComponent(ScriptOverlayTexturedRect component) {
        CustomOverlayTexturedRect rect;
        if (component.getTextureX() >= 0 && component.getTextureY() >= 0) {
            rect = new CustomOverlayTexturedRect(component.getID(), component.getTexture(), component.getPosX(), component.getPosY(), component.getWidth(), component.getHeight(), component.getTextureX(), component.getTextureY());
        } else {
            rect = new CustomOverlayTexturedRect(component.getID(), component.getTexture(), component.getPosX(), component.getPosY(), component.getWidth(), component.getHeight());
        }

        rect.scale = component.getScale();
        rect.alignment = component.getAlignment();
        rect.alpha = component.getAlpha();
        rect.color = component.getColor();

        return rect;
    }
}
