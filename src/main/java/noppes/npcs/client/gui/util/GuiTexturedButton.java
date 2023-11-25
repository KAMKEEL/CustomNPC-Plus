package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.ImageDownloadAlt;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.renderer.ImageBufferDownloadAlt;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class GuiTexturedButton extends GuiNpcButton {
    ResourceLocation location;
    public int textureX;
    public int textureY;
    boolean field_146123_n;
    String label;

    public int color;
    public float alpha;
    public float scale;

    private int totalWidth, totalHeight;
    private ImageDownloadAlt imageDownloadAlt = null;
    private boolean isUrl = false;
    private boolean gotWidthHeight = false;

    public GuiTexturedButton(int id, String buttonText, int x, int y) {
        this(id, buttonText, x, y, 200, 20);
    }

    public GuiTexturedButton(int id, String buttonText, int x, int y, int width, int height) {
        this(id, buttonText, x, y, width, height, "");
    }

    public GuiTexturedButton(int buttonId, String buttonText, int x, int y, int width, int height, String texture) {
        this(buttonId, buttonText, x, y, width, height, texture, 0, 0);
    }

    public GuiTexturedButton(int buttonId, String buttonText, int x, int y, int width, int height, String texture, int textureX, int textureY) {
        super(buttonId, x, y, width, height, buttonText);
        this.textureX = textureX;
        this.textureY = textureY;
        this.label = StatCollector.translateToLocal(buttonText);
        this.scale = 1.0F;
        this.color = 0xFFFFFF;
        this.alpha = 1.0F;
        if (texture != null && !texture.isEmpty()) {
            this.location = new ResourceLocation(texture);
            if(texture.startsWith("https://")){
                TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
                ITextureObject object = new ImageDownloadAlt(null, texture, new ResourceLocation("customnpcs:textures/gui/invisible.png"), new ImageBufferDownloadAlt(true,false));
                texturemanager.loadTexture(this.location, object);
            } else {
                try {
                    this.getWidthHeight();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            this.location = null;
        }
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (!this.visible)
        {
            return;
        }
        this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width * this.scale && mouseY < this.yPosition + this.height * this.scale;

        if(imageDownloadAlt != null && isUrl && !gotWidthHeight){
            getURLWidthHeight();
        }

        if (this.location == null) {
            totalWidth = totalHeight = 256;
            textureX = textureY = 0;
        }

        GL11.glPushMatrix();
            if (this.location != null) {
                mc.getTextureManager().bindTexture(this.location);
                this.drawTexturedModalRect(0, 0, this.textureX, this.textureY, this.width, this.height);
                GL11.glTranslated(0.0D, 0.0D, 0.1D);
                this.drawCenteredString(mc.fontRenderer, this.label, (int) (this.xPosition + ((float)this.width)*scale / 2f),
                        (int) (this.yPosition+(((float)this.height)*scale- ClientProxy.Font.height()+4) / 2f), this.color);
            } else {
                FontRenderer fontrenderer = mc.fontRenderer;
                mc.getTextureManager().bindTexture(buttonTextures);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                int k = this.getHoverState(this.field_146123_n);
                GL11.glEnable(GL11.GL_BLEND);
                OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                this.drawTexturedModalRect(0, 0, 0, 46 + k * 20, this.width / 2, this.height);
                this.drawTexturedModalRect(this.width / 2, 0, 200 - this.width / 2, 46 + k * 20, this.width / 2, this.height);
                this.mouseDragged(mc, mouseX, mouseY);
                int l = 14737632;

                if (packedFGColour != 0)
                {
                    l = packedFGColour;
                }
                else if (!this.enabled)
                {
                    l = 10526880;
                }
                else if (this.field_146123_n)
                {
                    l = 16777120;
                }

                this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, l);
            }
        GL11.glPopMatrix();
    }

    public void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height)
    {
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glPushMatrix();
        float red = (color >> 16 & 255) / 255f;
        float green = (color >> 8  & 255) / 255f;
        float blue = (color & 255) / 255f;
        GL11.glColor4f(red,green,blue,this.alpha);

        float u1 = (float)textureX/(float)totalWidth;
        float u2 = u1 + (float)width/(float)totalWidth;
        float v1 = (float)textureY/(float)totalHeight;
        float v2 = v1 + (float)height/(float)totalHeight;

        if (this.location != null && this.enabled) {
            if (this.field_146123_n) {
                v1 = (float)(textureY + 2 * this.height)/(float)totalHeight;
            } else {
                v1 = (float)(textureY + this.height)/(float)totalHeight;
            }
            v2 = v1 + (float)height/(float)totalHeight;
        }

        GL11.glTranslatef(x + this.xPosition - u1 * totalWidth * this.scale, y + this.yPosition - v1 * totalHeight * this.scale, this.zLevel);

        GL11.glScalef(this.scale,this.scale,1.0F);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_F(1, 1, 1);
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertexWithUV(u2 * totalWidth, v2 * totalHeight, 0, u2, v2);
        tessellator.addVertexWithUV(u2 * totalWidth, v1 * totalHeight, 0, u2, v1);
        tessellator.addVertexWithUV(u1 * totalWidth, v1 * totalHeight, 0, u1, v1);
        tessellator.addVertexWithUV(u1 * totalWidth, v2 * totalHeight, 0, u1, v2);
        tessellator.draw();
        GL11.glPopMatrix();
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int i, int j)
    {
        boolean bo = this.enabled && this.visible && i >= this.xPosition && j >= this.yPosition
                && i < this.xPosition + this.width*scale && j < this.yPosition + this.height*scale;
        if(bo && display != null && display.length != 0){
            setDisplay((getValue()+1) % display.length);
        }
        return bo;
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
