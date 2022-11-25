//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.client.gui.custom.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.ImageDownloadAlt;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.client.renderer.ImageBufferDownloadAlt;
import noppes.npcs.scripted.gui.ScriptGuiTexturedRect;
import noppes.npcs.api.gui.ICustomGuiComponent;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class CustomGuiTexturedRect extends Gui implements IGuiComponent {
    GuiCustom parent;
    ResourceLocation location;
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

    private int totalWidth, totalHeight;
    private ImageDownloadAlt imageDownloadAlt = null;
    private boolean isUrl = false;
    private boolean gotWidthHeight = false;

    public CustomGuiTexturedRect(int id, String texture, int x, int y, int width, int height) {
        this(id, texture, x, y, width, height, 0, 0);
    }

    public CustomGuiTexturedRect(int id, String directory, int x, int y, int width, int height, int textureX, int textureY) {
        this.scale = 1.0F;
        this.id = id;
        this.location = new ResourceLocation(directory);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.textureX = textureX;
        this.textureY = textureY;

        if(directory.startsWith("https://")){
            this.isUrl = true;
            TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
            this.imageDownloadAlt = new ImageDownloadAlt(null, directory, new ResourceLocation("customnpcs:textures/gui/invisible.png"), new ImageBufferDownloadAlt(true,false));
            texturemanager.loadTexture(this.location, this.imageDownloadAlt);
        } else {
            try {
                this.getWidthHeight();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        mc.getTextureManager().bindTexture(this.location);

        float u1 = (float)textureX/(float)totalWidth;
        float u2 = u1 + (float)width/(float)totalWidth;
        float v1 = (float)textureY/(float)totalHeight;
        float v2 = v1 + (float)height/(float)totalHeight;

        if(imageDownloadAlt != null && isUrl && !gotWidthHeight){
            getURLWidthHeight();
        }

        GL11.glPushMatrix();
            float red = (color >> 16 & 255) / 255f;
            float green = (color >> 8  & 255) / 255f;
            float blue = (color & 255) / 255f;
            GL11.glColor4f(red,green,blue,this.alpha);

            GL11.glTranslatef(GuiCustom.guiLeft + this.x - u1 * totalWidth, GuiCustom.guiTop + this.y - v1 * totalHeight,(float)this.id);
            GL11.glRotatef(this.rotation,0.0F,0.0F,1.0F);
            GL11.glScalef(this.scale, this.scale, this.scale);

            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            tessellator.setColorOpaque_F(1, 1, 1);
            tessellator.setColorRGBA_F(red, green, blue, alpha);
            tessellator.addVertexWithUV( u2 * totalWidth,  v2 * totalHeight, 0, u2, v2);
            tessellator.addVertexWithUV( u2 * totalWidth, v1 * totalHeight, 0, u2, v1);
            tessellator.addVertexWithUV( u1 * totalWidth,  v1 * totalHeight, 0, u1, v1);
            tessellator.addVertexWithUV( u1 * totalWidth,  v2 * totalHeight, 0, u1, v2);
            tessellator.draw();
        GL11.glPopMatrix();

        if (hovered && this.hoverText != null && this.hoverText.length > 0) {
            this.parent.hoverText = this.hoverText;
        }
    }

    public ICustomGuiComponent toComponent() {
        ScriptGuiTexturedRect component = new ScriptGuiTexturedRect(this.id, this.location.toString(), this.x, this.y, this.width, this.height, this.textureX, this.textureY);
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

    public void getWidthHeight() throws IOException {
        InputStream inputstream = null;

        try {
            IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(this.location);
            inputstream = iresource.getInputStream();
            BufferedImage bufferedimage = ImageIO.read(inputstream);
            gotWidthHeight = true;
            this.totalWidth = bufferedimage.getWidth();
            this.totalHeight = bufferedimage.getHeight();
            correctWidthHeight();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputstream != null) {
                inputstream.close();
            }
        }
    }

    public void getURLWidthHeight(){
        if(imageDownloadAlt.getBufferedImage() != null) {
            gotWidthHeight = true;
            this.totalWidth = imageDownloadAlt.getBufferedImage().getWidth();
            this.totalHeight = imageDownloadAlt.getBufferedImage().getHeight();
            correctWidthHeight();
        }
    }

    public void correctWidthHeight(){
        totalWidth = Math.max(totalWidth, 1);
        totalHeight = Math.max(totalHeight, 1);
        this.width = width < 0 ? totalWidth : width;
        this.height = height < 0 ? totalHeight : height;
    }
}
