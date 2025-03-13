package noppes.npcs.client.gui.custom.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IClickListener;
import noppes.npcs.client.renderer.ImageData;
import noppes.npcs.scripted.gui.ScriptGuiButton;
import org.lwjgl.opengl.GL11;

public class CustomGuiButton extends GuiButton implements IClickListener {
    GuiCustom parent;

    ImageData imageData = null;
    String texture;
    public int textureX;
    public int textureY;
    boolean field_146123_n;
    String label;
    String[] hoverText;

    int color;
    float alpha;
    float scale = 1.0F;

    public CustomGuiButton(int id, String buttonText, int x, int y) {
        super(id, x, y, buttonText);
    }

    public CustomGuiButton(int id, String buttonText, int x, int y, int width, int height) {
        super(id, x, y, width, height, buttonText);
    }

    public CustomGuiButton(int buttonId, String buttonText, int x, int y, int width, int height, String texture) {
        this(buttonId, buttonText, x, y, width, height, texture, 0, 0);
    }

    public CustomGuiButton(int buttonId, String buttonText, int x, int y, int width, int height, String texture, int textureX, int textureY) {
        this(buttonId, buttonText, x, y, width, height);
        this.texture = texture;
        this.textureX = textureX;
        this.textureY = textureY;
        this.label = buttonText;
        if (texture != null && !texture.isEmpty()) {
            this.imageData = ClientCacheHandler.getImageData(texture);
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
        btn.scale = component.getScale();
        btn.enabled = component.isEnabled();

        if (component.hasHoverText()) {
            btn.hoverText = component.getHoverText();
        }

        return btn;
    }

    public int getID() {
        return this.id;
    }

    public void onRender(Minecraft mc, int mouseX, int mouseY, int mouseWheel, float partialTicks) {
        this.field_146123_n = mouseX >= this.xPosition + GuiCustom.guiLeft && mouseY >= this.yPosition + GuiCustom.guiTop && mouseX < this.xPosition + GuiCustom.guiLeft + this.width * this.scale && mouseY < this.yPosition + GuiCustom.guiTop + this.height * this.scale;
        FontRenderer fontRenderer = mc.fontRenderer;

        float red = (color >> 16 & 255) / 255f;
        float green = (color >> 8 & 255) / 255f;
        float blue = (color & 255) / 255f;
        GL11.glColor4f(red, green, blue, this.alpha);

        if (this.imageData != null && this.imageData.imageLoaded()) {
            int totalWidth = this.imageData.getTotalWidth();
            int totalHeight = this.imageData.getTotalHeight();

            float u1 = (float) textureX / (float) totalWidth;
            float u2 = u1 + (float) width / (float) totalWidth;
            float v1 = (float) textureY / (float) totalHeight;
            float v2 = v1 + (float) height / (float) totalHeight;

            if (this.enabled) {
                if (this.field_146123_n) {
                    v1 = (float) (textureY + 2 * this.height) / (float) totalHeight;
                } else {
                    v1 = (float) (textureY + this.height) / (float) totalHeight;
                }
                v2 = v1 + (float) height / (float) totalHeight;
            }

            GL11.glPushMatrix();
            GL11.glTranslatef(GuiCustom.guiLeft + this.xPosition - u1 * totalWidth * this.scale, GuiCustom.guiTop + this.yPosition - v1 * totalHeight * this.scale, (float) this.id);
            GL11.glScalef(this.scale, this.scale, 1.0F);

            this.imageData.bindTexture();
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
            GL11.glTranslated(0.0D, 0.0D, 0.1D);
            this.drawCenteredString(fontRenderer, this.label, this.width / 2, (this.height - 8) / 2, this.color);
            GL11.glPopMatrix();
        } else {
            GL11.glPushMatrix();
            GL11.glTranslatef(GuiCustom.guiLeft + this.xPosition, GuiCustom.guiTop + this.yPosition, (float) this.id);
            GL11.glScalef(this.scale, this.scale, 1.0F);

            mc.getTextureManager().bindTexture(buttonTextures);
            int i = this.getHoverState(this.field_146123_n);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            this.drawTexturedModalRect(0, 0, 0, 46 + i * 20, this.width / 2, this.height);
            this.drawTexturedModalRect(this.width / 2, 0, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
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
            this.drawCenteredString(fontRenderer, this.displayString, this.width / 2, (this.height - 8) / 2, j);
            GL11.glPopMatrix();
        }

        if (this.field_146123_n && this.hoverText != null && this.hoverText.length > 0) {
            this.parent.hoverText = this.hoverText;
        }
    }

    public ICustomGuiComponent toComponent() {
        ScriptGuiButton component = new ScriptGuiButton(this.id, this.label, this.xPosition, this.yPosition, this.width, this.height, this.texture, this.textureX, this.textureY);
        component.setHoverText(this.hoverText);
        component.setColor(this.color);
        component.setAlpha(this.alpha);
        return component;
    }

    public boolean mouseClicked(GuiCustom gui, int mouseX, int mouseY, int mouseButton) {
        if (this.field_146123_n && this.enabled) {
            gui.buttonClick(this);
            return true;
        } else {
            return false;
        }
    }
}
