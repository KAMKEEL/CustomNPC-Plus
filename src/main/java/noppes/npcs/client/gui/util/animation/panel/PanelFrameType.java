package noppes.npcs.client.gui.util.animation.panel;

import net.minecraft.client.Minecraft;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiUtil;
import noppes.npcs.client.gui.util.animation.AnimationGraphEditor;
import noppes.npcs.client.gui.util.animation.GridPointManager;
import noppes.npcs.client.gui.util.animation.OverlayKeyPresetViewer;
import noppes.npcs.client.utils.Color;
import noppes.npcs.constants.animation.EnumFrameType;
import noppes.npcs.util.ValueUtil;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PanelFrameType {
    public AnimationGraphEditor graph;

    public int startX, startY, endX, endY, width, height;
    public float scale = 0.6f;
    public int elementHeight = 11, elementSpacing = 2;

    public LinkedList<Element> list = new LinkedList<>();
    public Element selectedElement;
    public List<EnumFrameType> highlightedTypes = new ArrayList<>();


    public PanelFrameType(AnimationGraphEditor graph) {
        this.graph = graph;

        for (EnumFrameType type : EnumFrameType.values())
            list.add(new Element(type));

        selectedElement = list.get(0);
    }

    public void updateTypeValues(double playheadX) {
        list.forEach(e -> {
            String value = "0";
            GridPointManager.Point p = graph.pointManager.getPoint(e.type, playheadX);
            if (p != null)
                value = ValueUtil.format(p.worldY);

            e.text.setText(value);
        });
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
        GL11.glPushMatrix();
        GL11.glTranslatef(startX, startY, 0);
        GuiUtil.drawRectD(0, 0, width, 6 * (elementHeight + elementSpacing)-2, 0x77000000);

        for (int i = 0; i < list.size(); i++) {
            Element element = list.get(i);
            element.draw(i);

            // if (element.isMouseAboveBox(graph.grid.mouseX, graph.grid.mouseY))
            //   element.setCursorPositionToMouse();
        }

        GL11.glPopMatrix();

    }

    public void keyTyped(char c, int i) {
        list.forEach((element) -> {
            if (element.isEditing)
                element.keyTyped(c, i);
        });
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        list.forEach((element) -> {
            if (element.isMouseAbove(mouseX, mouseY) && button == 0)
                selectedElement = element;

            if (!element.isMouseAboveBox(mouseX, mouseY)) {
                if (element.isEditing)
                    element.cancelEdit();
            } else
                element.boxClicked(button);
        });
    }

    public boolean isMouseAbove(int mouseX, int mouseY) {
        return mouseX >= startX && mouseX < endX && mouseY >= startY && mouseY < endY;
    }

    public int getWidth() {
        return (int) (width / scale);
    }

    public class Element {
        public EnumFrameType type;

        public float elementStartY, boxScreenX, boxScreenY, textScreenX;
        public boolean isEditing, isMouseDragging;
        public GuiNpcTextField text;

        public Element(EnumFrameType key) {
            this.type = key;
            text = new GuiNpcTextField(0, graph.parent, 0, 0, 45, 10, "0");
            text.setEnableBackgroundDrawing(false);
            text.setDoublesOnly();
            text.setMinMaxDefaultDouble(0, 1000000000, 0);
        }

        public boolean isMouseAbove(int mouseX, int mouseY) {
            mouseX -= startX;
            mouseY -= startY;

            return mouseX >= 0 && mouseX < boxScreenX - 3 && mouseY >= elementStartY && mouseY < elementStartY + elementHeight + elementSpacing;
        }

        public void draw(int index) {
            if (Mouse.isButtonDown(0))
                setCursorPositionToMouse();

            ////////////////////////////////////////////
            ////////////////////////////////////////////
            //Type names
            elementStartY = index * (elementHeight + elementSpacing) / 1;
            GL11.glPushMatrix();
            GL11.glTranslatef(0, elementStartY, 0);
            GuiUtil.drawRectD(0, 0, width - 1, elementHeight, 0xaa000000); //type black bg

            boolean selected = selectedElement == this;
            type.getColor().multiply(selected ? 1 : 0.5f).glColor();
            GuiUtil.drawRectD(0, 0, 3, elementHeight); //type color

            GL11.glScalef(scale, scale, 1);
            GL11.glTranslatef(0, elementHeight / 2, 0);
            String name = String.format("%s", type.toString());
            int color = selected ? 0xd79520 : isMouseAbove(graph.grid.mouseX, graph.grid.mouseY) ? 0xffffff : 0x9a9a9a;

            graph.getFontRenderer().drawString(name, 7, 0, color);

            GL11.glPopMatrix();

            ////////////////////////////////////////////
            ////////////////////////////////////////////
            //Box texture
            Minecraft.getMinecraft().getTextureManager().bindTexture(OverlayKeyPresetViewer.TEXTURE);
            GL11.glPushMatrix();
            float boxScaleX = 0.85f;
            float boxWidth = 32 * boxScaleX;
            boxScreenX = width / 1.25F * scale + 8; //remove scaling from maxStringWidth
            boxScreenY = elementStartY - 7.5f;
            GL11.glScalef(boxScaleX, boxScaleX, 0);

            //Box grey background
            new Color(0x3d3d3d, 1).glColor();
            GuiUtil.drawTexturedModalRect(boxScreenX / boxScaleX, boxScreenY / boxScaleX, 32, 20, 0, 492);

            //Box color
            if (graph.pointManager.getPoint(type, graph.pointManager.playhead.worldX) != null)
                new Color(0x83752a, 1).glColor();
            else
                new Color(0x467d2a, 1).glColor();
            GL11.glPushMatrix();
            float s = 0.945f, s2 = 0.875f;
            GL11.glScalef(s, s2, 0);
            GuiUtil.drawTexturedModalRect(boxScreenX / boxScaleX / s + 1, boxScreenY / boxScaleX / s2 + 2.25f, 32, 20, 0, 492);
            GL11.glPopMatrix();
            GL11.glPopMatrix();

            ////////////////////////////////////////////
            ////////////////////////////////////////////
            // Box text
            float textScale = scale;
            float textWidth = graph.getFontRenderer().getStringWidth(text.getText());
            float textX = boxScreenX + 1;
            float centeredTextX = (boxScreenX) + boxWidth / 2 - textWidth / 2 * textScale;
            textScreenX = text.isFocused() || textWidth * textScale >= boxWidth ? textX : centeredTextX;

            GL11.glPushMatrix();
            GL11.glScalef(textScale, textScale, 1);
            GL11.glTranslatef(textScreenX / textScale, (boxScreenY + 10.5f) / textScale, 0);
            text.drawTextBox();
            GL11.glPopMatrix();
        }

        public void setCursorPositionToMouse() {
            if (!text.isFocused() || !isEditing)
                return;

            int mouseX = graph.parent.mouseX - startX;
            int relativeMX = (int) (mouseX - textScreenX);

            int pos = relativeMX > 0 ? 1 : -1;
            text.setCursorPosition(graph.getFontRenderer().trimStringToWidth(text.getText(), (int) (relativeMX / scale)).length() + text.lineScrollOffset + pos);
        }

        public void boxClicked(int button) {
            if (isEditing) {
            } else if (button == 0) {
                isEditing = true;
                text.setFocused(true);
                text.setCursorPositionEnd();
            }
        }

        public void keyTyped(char c, int typedKey) {
            if (typedKey == 28)
                cancelEdit();
            else {
                String prev = text.getText();
                text.textboxKeyTyped(c, typedKey);
                String newText = text.getText();
                if (typedKey != 14 && !newText.equals(prev)) { //backspace
                    try {
                        Double.parseDouble(text.getText());
                    } catch (NumberFormatException var6) {
                        text.setText(prev);
                    }
                }
            }
        }

        public void cancelEdit() {
            String s = text.getText();
            text.setText(s.isEmpty() ? "0" : ValueUtil.format(text.getDouble()));
            graph.pointManager.addPoint(type, graph.pointManager.playhead.worldX, text.getDouble());
            text.setFocused(false);
            isEditing = false;
        }

        public boolean isMouseAboveBox(int mouseX, int mouseY) {
            mouseX -= startX;
            mouseY -= startY;

            float boxScaleX = 0.85f;
            float screenY = boxScreenY + 8f;

            return mouseX >= boxScreenX && mouseX < boxScreenX + 32 * boxScaleX && mouseY >= screenY && mouseY < screenY + 11 * boxScaleX;
        }
    }
}
