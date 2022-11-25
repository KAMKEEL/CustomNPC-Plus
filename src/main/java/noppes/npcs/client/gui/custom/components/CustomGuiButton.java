//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.client.gui.custom.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.ImageDownloadAlt;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IClickListener;
import noppes.npcs.client.renderer.ImageBufferDownloadAlt;
import noppes.npcs.scripted.gui.ScriptGuiButton;
import noppes.npcs.api.gui.ICustomGuiComponent;
import org.lwjgl.opengl.GL11;

public class CustomGuiButton extends GuiButton implements IClickListener {
    GuiCustom parent;
    ResourceLocation texture;
    public int textureX;
    public int textureY;
    boolean field_146123_n;
    String label;
    String[] hoverText;

    int color;
    float alpha;

    public CustomGuiButton(int id, String buttonText, int x, int y) {
        super(id, GuiCustom.guiLeft + x, GuiCustom.guiTop + y, buttonText);
    }

    public CustomGuiButton(int id, String buttonText, int x, int y, int width, int height) {
        super(id, GuiCustom.guiLeft + x, GuiCustom.guiTop + y, width, height, buttonText);
    }

    public CustomGuiButton(int buttonId, String buttonText, int x, int y, int width, int height, String texture) {
        this(buttonId, buttonText, x, y, width, height, texture, 0, 0);
    }

    public CustomGuiButton(int buttonId, String buttonText, int x, int y, int width, int height, String texture, int textureX, int textureY) {
        this(buttonId, buttonText, x, y, width, height);
        this.textureX = textureX;
        this.textureY = textureY;
        this.label = buttonText;
        this.texture = new ResourceLocation(texture);

        if(texture.startsWith("https://")){
            TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
            ITextureObject object = new ImageDownloadAlt(null, texture, new ResourceLocation("customnpcs:textures/gui/invisible.png"), new ImageBufferDownloadAlt(true,false));
            texturemanager.loadTexture(this.texture, object);
        }
    }

    public void setParent(GuiCustom parent) {
        this.parent = parent;
    }

    public static CustomGuiButton fromComponent(ScriptGuiButton component) {
        CustomGuiButton btn;
        if (component.hasTexture()) {
            if (component.getTextureX() >= 0 && component.getTextureY() >= 0) {
                btn = new CustomGuiButton(component.getID(), component.getLabel(), component.getPosX(), component.getPosY(), component.getWidth(), component.getHeight(), component.getTexture(), component.getTextureX(), component.getTextureY());
            } else {
                btn = new CustomGuiButton(component.getID(), component.getLabel(), component.getPosX(), component.getPosY(), component.getWidth(), component.getHeight(), component.getTexture());
            }
        } else if (component.getWidth() >= 0 && component.getHeight() >= 0) {
            btn = new CustomGuiButton(component.getID(), component.getLabel(), component.getPosX(), component.getPosY(), component.getWidth(), component.getHeight());
        } else {
            btn = new CustomGuiButton(component.getID(), component.getLabel(), component.getPosX(), component.getPosY());
        }

        btn.color = component.getColor();
        btn.alpha = component.getAlpha();

        if (component.hasHoverText()) {
            btn.hoverText = component.getHoverText();
        }

        return btn;
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
            FontRenderer fontRenderer = mc.fontRenderer;
            int i;
            if (this.texture == null) {
                mc.getTextureManager().bindTexture(buttonTextures);
                this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
                i = this.getHoverState(this.field_146123_n);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                //GL11.func_187428_a(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
                //GL11.func_187401_a(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
                this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + i * 20, this.width / 2, this.height);
                this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
                this.mouseDragged(mc, mouseX, mouseY);
                int j = 14737632;
                if (this.packedFGColour != 0) {
                    j = this.packedFGColour;
                } else if (!this.enabled) {
                    j = 10526880;
                } else if (this.field_146123_n) {
                    j = 16777120;
                }

                GL11.glTranslated(0.0D, 0.0D, 0.1D);
                this.drawCenteredString(fontRenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);
            } else {
                mc.getTextureManager().bindTexture(this.texture);
                this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
                i = this.hoverState(this.field_146123_n);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                //GL11.func_187428_a(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
                //GL11.func_187401_a(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
                this.drawTexturedModalRect(this.xPosition, this.yPosition, this.textureX, this.textureY + i * this.height, this.width, this.height);
                this.drawCenteredString(fontRenderer, this.label, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, this.color);
            }
        GL11.glPopMatrix();

        if (this.field_146123_n && this.hoverText != null && this.hoverText.length > 0) {
            this.parent.hoverText = this.hoverText;
        }
    }

    public ICustomGuiComponent toComponent() {
        ScriptGuiButton component = new ScriptGuiButton(this.id, this.label, this.xPosition, this.yPosition, this.width, this.height, this.texture.toString(), this.textureX, this.textureY);
        component.setHoverText(this.hoverText);
        component.setColor(this.color);
        component.setAlpha(this.alpha);
        return component;
    }

    protected int hoverState(boolean mouseOver) {
        int i = 0;
        if (mouseOver) {
            i = 1;
        }

        return i;
    }

    public boolean mouseClicked(GuiCustom gui, int mouseX, int mouseY, int mouseButton) {
        if (mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height) {
            //Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.func_147673_a(SoundEvents.field_187909_gi, 1.0F));
            gui.buttonClick(this);
            return true;
        } else {
            return false;
        }
    }
}
