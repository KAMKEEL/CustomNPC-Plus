package noppes.npcs.client.gui.util.animation.panel;

import net.minecraft.client.Minecraft;
import noppes.npcs.client.gui.util.GuiUtil;
import noppes.npcs.client.gui.util.animation.AnimationGraphEditor;
import noppes.npcs.client.gui.util.animation.OverlayKeyPresetViewer;
import noppes.npcs.client.utils.Color;
import org.lwjgl.opengl.GL11;

public class PanelActiveFrame {
    public AnimationGraphEditor graph;

    public int startX, startY, endX, endY, width, height;
    public float scale = 0.6f;
    public int elementHeight = 11, elementSpacing = 2;

    public PanelTextBox frame = new PanelTextBox(), value = new PanelTextBox();

    public PanelActiveFrame(AnimationGraphEditor graph) {
        this.graph = graph;
    }

    public void initGui(int startX, int startY, int endX, int endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.width = endX - startX;
        this.height = endY - startY;
    }

    public void draw() {
        int mouseX = graph.parent.mouseX, mouseY = graph.parent.mouseY;

        GL11.glPushMatrix();
        GL11.glTranslatef(startX, startY + 1, 0);
        //   GuiUtil.drawRectD(0, 0, width - 1, height, 0xff3d3d3d);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        new Color(0X2D2D2D,0.75F).glColor();
        Minecraft.getMinecraft().getTextureManager().bindTexture(OverlayKeyPresetViewer.TEXTURE);
        GuiUtil.drawTexturedModalRect(0, -38, 88, 77, 44, 435);
        GL11.glDisable(GL11.GL_BLEND);


        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, 1);

        GL11.glPushMatrix();
        GL11.glRotatef(-90,0,0,1);
        graph.getFontRenderer().drawString("Active frame", -62, -14, 0xdddddd);
        GuiUtil.drawHorizontalLine(-6,-62,-2,0xffdddddd);
        GL11.glPopMatrix();


       // GL11.glTranslatef(0, 17, 0);
        graph.getFontRenderer().drawString("Easing", 9, 3, 0xb1b1b1);
        GL11.glTranslatef(0, 17, 0);
        graph.getFontRenderer().drawString("-Type", 9, 0, 0xb1b1b1);
        GL11.glTranslatef(0, 18, 0);
        graph.getFontRenderer().drawString("Frame", 9, 0, 0xb1b1b1);

        GL11.glTranslatef(0, 15, 0);
        graph.getFontRenderer().drawString("Value", 9, 0, 0xb1b1b1);
        GL11.glPopMatrix();

        frame.screenX = 29; //remove scaling from maxStringWidth
        frame.screenY = 11;
        frame.boxScaleX = 1.2f;
        frame.textScale = scale;

        boolean isAboveFrame = frame.isMouseAbove(mouseX - startX, mouseY - startY);
        frame.boxColor = new Color(isAboveFrame ? 0x797979 : 0x545454);
        frame.draw(graph.mc.fontRenderer);

        value.screenX = 29; //remove scaling from maxStringWidth
        value.screenY = 20;
        value.boxScaleX = 1.2f;
        value.textScale = scale;
        boolean isAboveValue = value.isMouseAbove(mouseX - startX, mouseY - startY);
        value.boxColor = new Color(isAboveValue ? 0x797979 : 0x545454);
        value.draw(graph.mc.fontRenderer);

        GL11.glPopMatrix();
    }

    public void keyTyped(char c, int i) {

    }

    public void mouseClicked(int mouseX, int mouseY, int button) {

    }

    public boolean isMouseAbove(int mouseX, int mouseY) {
        return mouseX >= startX && mouseX < endX && mouseY >= startY && mouseY < endY;
    }
}
