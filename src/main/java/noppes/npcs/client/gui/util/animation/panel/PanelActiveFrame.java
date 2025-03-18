package noppes.npcs.client.gui.util.animation.panel;

import net.minecraft.client.Minecraft;
import noppes.npcs.client.gui.util.GuiUtil;
import noppes.npcs.client.gui.util.animation.AnimationGraphEditor;
import noppes.npcs.client.gui.util.animation.GridPointManager;
import noppes.npcs.client.gui.util.animation.OverlayKeyPresetViewer;
import noppes.npcs.client.utils.Color;
import noppes.npcs.util.ValueUtil;
import org.lwjgl.opengl.GL11;

public class PanelActiveFrame {
    public AnimationGraphEditor graph;

    public int startX, startY, endX, endY, width, height;
    public float scale = 0.6f;

    public PanelTextBox frame = new PanelTextBox() {
        public void finishEdit(byte operation) {
            super.finishEdit(operation);
            GridPointManager.Point p = graph.pointManager.selectedPoint;
            if (p != null) {
                p.setX(text.getDouble());
                p.updateKey();
            }
        }
    }.setDoublesOnly(0, 0, 9999);

    public PanelTextBox value = new PanelTextBox() {
        public void finishEdit(byte operation) {
            super.finishEdit(operation);
            GridPointManager.Point p = graph.pointManager.selectedPoint;
            if (p != null)
                p.setY(-text.getDouble());
        }
    }.setDoublesOnly(0, -9999, 9999);

    public PanelActiveFrame(AnimationGraphEditor graph) {
        this.graph = graph;
        frame.text.width = value.text.width = 70;
    }

    public void initGui(int startX, int startY, int endX, int endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.width = endX - startX;
        this.height = endY - startY;
    }

    public void keyTyped(char c, int i) {
        frame.type(c, i);
        value.type(c, i);
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        frame.click(mouseX - startX, mouseY - startY, button);
        value.click(mouseX - startX, mouseY - startY, button);
    }

    public void draw(int wheel) {
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

        GridPointManager.Point p = graph.pointManager.selectedPoint;
        if (p != null) {
            if (!frame.isEditing())
                frame.text.setText(ValueUtil.format(p.worldX));
            if (!value.isEditing())
                value.text.setText(ValueUtil.format(-p.worldY));
        }

        frame.screenX = 29; //remove scaling from maxStringWidth
        frame.screenY = 11;
        frame.boxScaleX = 1.2f;
        frame.textScale = scale;

        boolean isAboveFrame = frame.isMouseAbove(mouseX - startX, mouseY - startY);
        frame.boxColor = new Color(frame.isEditing() ? 0x111111 : isAboveFrame ? 0x797979 : 0x545454);
        frame.draw(mouseX - startX,mouseY - startY, graph.mc.fontRenderer, wheel);

        value.screenX = 29; //remove scaling from maxStringWidth
        value.screenY = 20;
        value.boxScaleX = 1.2f;
        value.textScale = scale;
        boolean isAboveValue = value.isMouseAbove(mouseX - startX, mouseY - startY);
        value.boxColor = new Color(value.isEditing() ? 0x111111 : isAboveValue ? 0x797979 : 0x545454);
        value.draw(mouseX - startX, mouseY - startY, graph.mc.fontRenderer, wheel);

        GL11.glPopMatrix();
    }

    public boolean isMouseAbove(int mouseX, int mouseY) {
        return mouseX >= startX && mouseX < endX && mouseY >= startY && mouseY < endY;
    }
}
