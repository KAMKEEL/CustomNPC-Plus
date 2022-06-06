package noppes.npcs.client.gui.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.ImageDownloadAlt;
import noppes.npcs.client.renderer.ImageBufferDownloadAlt;
import noppes.npcs.scripted.gui.ScriptGuiTexturedRect;
import noppes.npcs.scripted.interfaces.gui.ICustomGuiComponent;
import org.lwjgl.opengl.GL11;

public class GuiDialogImage extends Gui {
    GuiDialogInteract parent;
    String texture;
    ResourceLocation location;
    int id;
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

    public GuiDialogImage(int id, String texture, int x, int y, int width, int height) {
        this(id, texture, x, y, width, height, 0, 0);
    }

    public GuiDialogImage(int id, String texture, int x, int y, int width, int height, int textureX, int textureY) {
        this.color = 0xFFFFFF;
        this.alpha = 1.0F;
        this.scale = 1.0F;
        this.id = id;
        this.texture = texture;
        this.location = new ResourceLocation(texture);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.textureX = textureX;
        this.textureY = textureY;

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

    public ICustomGuiComponent toComponent() {
        ScriptGuiTexturedRect component = new ScriptGuiTexturedRect(this.id, this.location.toString(), this.x, this.y, this.width, this.height, this.textureX, this.textureY);
        component.setScale(this.scale);
        component.setColor(color);
        component.setAlpha(alpha);
        component.setRotation(rotation);
        return component;
    }

    public static GuiDialogImage fromComponent(ScriptGuiTexturedRect component) {
        GuiDialogImage rect;
        if (component.getTextureX() >= 0 && component.getTextureY() >= 0) {
            rect = new GuiDialogImage(component.getID(), component.getTexture(), component.getPosX(), component.getPosY(), component.getWidth(), component.getHeight(), component.getTextureX(), component.getTextureY());
        } else {
            rect = new GuiDialogImage(component.getID(), component.getTexture(), component.getPosX(), component.getPosY(), component.getWidth(), component.getHeight());
        }

        rect.scale = component.getScale();
        rect.color = component.getColor();
        rect.alpha = component.getAlpha();
        rect.rotation = component.getRotation();

        return rect;
    }
}
