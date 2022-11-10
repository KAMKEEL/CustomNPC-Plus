package noppes.npcs.client.gui.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.ImageDownloadAlt;
import noppes.npcs.client.gui.customoverlay.OverlayCustom;
import noppes.npcs.client.renderer.ImageBufferDownloadAlt;
import noppes.npcs.controllers.data.DialogImage;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class GuiDialogImage extends Gui {
    GuiDialogInteract parent;
    ResourceLocation location;

    public int id;
    public String texture;
    public int x;
    public int y;
    public int width;
    public int height;
    public int textureX;
    public int textureY;
    public float scale;

    public int color;
    public int selectedColor;
    public float alpha;
    public float rotation;

    public int imageType = 0; //0 - Default, 1 - Text, 2 - Option
    public int alignment = 0;

    private int totalWidth, totalHeight;
    private ImageDownloadAlt imageDownloadAlt = null;
    private boolean isUrl = false;
    private boolean gotWidthHeight = false;

    public GuiDialogImage(DialogImage dialogImage) {
        this.location = new ResourceLocation(dialogImage.texture);

        this.id = dialogImage.id;
        this.texture = dialogImage.texture;
        this.x = dialogImage.x;
        this.y = dialogImage.y;
        this.width = dialogImage.width;
        this.height = dialogImage.height;
        this.textureX = dialogImage.textureX;
        this.textureY = dialogImage.textureY;
        this.scale = dialogImage.scale;

        this.color = dialogImage.color;
        this.selectedColor = dialogImage.selectedColor;
        this.rotation = dialogImage.rotation;
        this.alpha = dialogImage.alpha;

        this.imageType = dialogImage.imageType;
        this.alignment = dialogImage.alignment;

        if(texture.startsWith("https://")){
            this.isUrl = true;
            TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
            this.imageDownloadAlt = new ImageDownloadAlt(null, texture, new ResourceLocation("customnpcs:textures/gui/invisible.png"), new ImageBufferDownloadAlt(true,false));
            texturemanager.loadTexture(this.location, this.imageDownloadAlt);
        } else {
            try {
                this.getWidthHeight();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setParent(GuiDialogInteract parent) {
        this.parent = parent;
    }

    public int getID() {
        return this.id;
    }

    public void onRender(Minecraft mc) {
        try {
            mc.getTextureManager().bindTexture(this.location);
        } catch (Exception ignored) { return; }

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
