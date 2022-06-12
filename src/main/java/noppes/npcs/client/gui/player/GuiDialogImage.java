package noppes.npcs.client.gui.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.ImageDownloadAlt;
import noppes.npcs.client.gui.customoverlay.OverlayCustom;
import noppes.npcs.client.renderer.ImageBufferDownloadAlt;
import noppes.npcs.controllers.data.DialogImage;
import org.lwjgl.opengl.GL11;

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
            TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
            ITextureObject object = new ImageDownloadAlt(null, texture, new ResourceLocation("customnpcs:textures/gui/invisible.png"), new ImageBufferDownloadAlt(true,false));
            texturemanager.loadTexture(this.location, object);
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

        GL11.glPushMatrix();
        float red = (color >> 16 & 255) / 255f;
        float green = (color >> 8  & 255) / 255f;
        float blue = (color & 255) / 255f;
        GL11.glColor4f(red,green,blue,this.alpha);

        GL11.glRotatef(this.rotation,0.0F,0.0F,1.0F);
        GL11.glScalef(this.scale, this.scale, this.scale);

        int p_73729_1_ = (int) (this.x/(this.scale));
        int p_73729_2_ = (int) (this.y/(this.scale));
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
    }
}
