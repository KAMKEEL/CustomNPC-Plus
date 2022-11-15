package noppes.npcs.client.gui.customoverlay.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.ImageDownloadAlt;
import noppes.npcs.client.gui.customoverlay.OverlayCustom;
import noppes.npcs.client.gui.customoverlay.interfaces.IOverlayComponent;
import noppes.npcs.client.renderer.ImageBufferDownloadAlt;
import noppes.npcs.api.overlay.ICustomOverlayComponent;
import noppes.npcs.scripted.overlay.ScriptOverlayTexturedRect;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class CustomOverlayTexturedRect extends Gui implements IOverlayComponent {
    OverlayCustom parent;
    int alignment;
    int id;

    ResourceLocation location;
    int x;
    int y;
    int width;
    int height;
    int textureX;
    int textureY;
    float scale;

    int color;
    float alpha;
    float rotation;

    private int totalWidth, totalHeight;
    private ImageDownloadAlt imageDownloadAlt = null;
    private boolean isUrl = false;
    private boolean gotWidthHeight = false;

    public CustomOverlayTexturedRect(int id, String texture, int x, int y, int width, int height) {
        this(id, texture, x, y, width, height, 0, 0);
    }

    public CustomOverlayTexturedRect(int id, String directory, int x, int y, int width, int height, int textureX, int textureY) {
        this.scale = 1.0F;
        this.alignment = 0;
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

    public void setParent(OverlayCustom parent) {
        this.parent = parent;
    }

    public int getID() {
        return this.id;
    }

    public void onRender(Minecraft mc, float partialTicks) {
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

            GL11.glTranslatef(this.alignment%3*((float)(OverlayCustom.scaledWidth)/2), (float) (Math.floor((float)(alignment/3))*((float)(OverlayCustom.scaledHeight)/2)),0.0F);
            GL11.glTranslatef( this.x - u1 * totalWidth, this.y - v1 * totalHeight,(float)this.id);

            GL11.glScalef(this.scale, this.scale, this.scale);

            GL11.glRotatef(this.rotation,0.0F,0.0F,1.0F);

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
    }

    public ICustomOverlayComponent toComponent() {
        ScriptOverlayTexturedRect component = new ScriptOverlayTexturedRect(this.id, this.location.toString(), this.x, this.y, this.width, this.height, this.textureX, this.textureY);
        component.setScale(scale);
        component.setAlignment(alignment);
        component.setAlpha(alpha);
        component.setColor(color);
        component.setRotation(rotation);
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
