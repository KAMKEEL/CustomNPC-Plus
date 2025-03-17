package noppes.npcs.client.gui.util.animation.panel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiUtil;
import noppes.npcs.client.gui.util.animation.OverlayKeyPresetViewer;
import noppes.npcs.client.utils.Color;
import org.lwjgl.opengl.GL11;

public class PanelTextBox {

    public float screenX, screenY, textScreenX, textScale;
    public float boxScaleX = 0.85f, boxScaleY = 0.85f;
    public GuiNpcTextField text;
    public Color boxColor;

    private FontRenderer font;

    public PanelTextBox() {
        text = new GuiNpcTextField(0, null, 0, 0, 45, 10, "0");
        text.setEnableBackgroundDrawing(false);
    }

    public void draw(FontRenderer font) {
        if (this.font == null)
            this.font = font;
        ////////////////////////////////////////////
        ////////////////////////////////////////////
        //Box texture
        Minecraft.getMinecraft().getTextureManager().bindTexture(OverlayKeyPresetViewer.TEXTURE);
        GL11.glPushMatrix();
        float boxWidth = 32 * boxScaleX;
        GL11.glScalef(boxScaleX, boxScaleY, 0);

        //Box grey background
        new Color(0x3d3d3d, 1).glColor();
        GuiUtil.drawTexturedModalRect(screenX / boxScaleX, screenY / boxScaleY, 32, 20, 0, 492);

        //Box color
        if (boxColor != null)
            boxColor.glColor();
        GL11.glPushMatrix();
        float s = 0.945f, s2 = 0.875f;
        GL11.glScalef(s, s2, 0);
        GuiUtil.drawTexturedModalRect(screenX / boxScaleX / s + 1, screenY / boxScaleY / s2 + 2.25f, 32, 20, 0, 492);
        GL11.glPopMatrix();
        GL11.glPopMatrix();

        ////////////////////////////////////////////
        ////////////////////////////////////////////
        // Box text
        float textWidth = font.getStringWidth(text.getText());
        float textX = screenX + 1;
        float centeredTextX = (screenX) + boxWidth / 2 - textWidth / 2 * textScale;
        textScreenX = text.isFocused() || textWidth * textScale >= boxWidth ? textX : centeredTextX;

        GL11.glPushMatrix();
        GL11.glScalef(textScale, textScale, 1);
        GL11.glTranslatef(textScreenX / textScale, (screenY + 10.5f) / textScale, 0);
        text.drawTextBox();
        GL11.glPopMatrix();
    }

    public boolean isMouseAbove(int mouseX, int mouseY) {
        float screenY = (this.screenY + 9);

        return mouseX >= screenX && mouseX < screenX + 32 * boxScaleX && mouseY >= screenY && mouseY < screenY + 10 * boxScaleY;
    }

    public void setCursorPositionToMouse(int mouseX) {
        int relativeMX = (int) (mouseX - textScreenX);
        int pos = relativeMX > 0 ? 1 : -1;

        text.setCursorPosition(font.trimStringToWidth(text.getText(), (int) (relativeMX / textScale)).length() + text.lineScrollOffset + pos);
    }
}
