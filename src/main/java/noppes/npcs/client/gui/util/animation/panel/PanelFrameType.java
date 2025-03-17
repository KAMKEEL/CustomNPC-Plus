package noppes.npcs.client.gui.util.animation.panel;

import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiUtil;
import noppes.npcs.client.gui.util.animation.AnimationGraphEditor;
import noppes.npcs.client.gui.util.animation.GridPointManager;
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
            GridPointManager.Point onHead = graph.pointManager.getPoint(e.type, playheadX);
            if (onHead != null)
                value = ValueUtil.format(onHead.worldY != 0 ? -onHead.worldY : 0);
            else {
                GridPointManager.Point beforeHead = graph.pointManager.getPrevious(e.type, playheadX);
                GridPointManager.Point afterHead = graph.pointManager.getNext(e.type, playheadX);
                if (beforeHead != null && afterHead != null) {
                    double easedValue = graph.pointManager.getValueBetweenPointsAt(beforeHead, afterHead, playheadX);
                    value = ValueUtil.format(easedValue);
                }
            }

            e.box.text.setText(value);
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
        GuiUtil.drawRectD(startX - 1, startY, endX, endY, 0xff1d1d1d);

        GL11.glPushMatrix();
        GL11.glTranslatef(startX, startY + 1, 0);

        for (int i = 0; i < list.size(); i++) {
            Element element = list.get(i);
            element.draw(i);
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

            if (!element.box.isMouseAbove(mouseX - startX, mouseY - startY)) {
                if (element.isEditing)
                    element.cancelEdit();
            } else
                element.boxClicked(button);
        });
    }

    public boolean isMouseAbove(int mouseX, int mouseY) {
        return mouseX >= startX && mouseX < endX && mouseY >= startY && mouseY < endY;
    }

    public class Element {
        public EnumFrameType type;

        public float elementStartY;
        public boolean isEditing;
        public PanelTextBox box = new PanelTextBox();

        public Element(EnumFrameType key) {
            this.type = key;
            box.text = new GuiNpcTextField(0, graph.parent, 0, 0, 45, 10, "0");
            box.text.setEnableBackgroundDrawing(false);
            box.text.setDoublesOnly();
            box.text.setMinMaxDefaultDouble(0, 100000, 0);
        }

        public boolean isMouseAbove(int mouseX, int mouseY) {
            mouseX -= startX;
            mouseY -= startY;

            return mouseX >= 0 && mouseX < box.screenX - 3 && mouseY >= elementStartY && mouseY < elementStartY + elementHeight + elementSpacing;
        }

        public void draw(int index) {
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
            //Box
            if (Mouse.isButtonDown(0) && isEditing && box.text.isFocused())
                box.setCursorPositionToMouse(graph.parent.mouseX - startX);

            box.screenX = width / 1.25F * scale + 8; //remove scaling from maxStringWidth
            box.screenY = elementStartY - 7.5f;
            box.textScale = scale;

            if (graph.pointManager.getPoint(type, graph.pointManager.playhead.worldX) != null)
                box.boxColor = isEditing ? new Color(0x83752a, 1).multiply(0.65f) : new Color(0x83752a, 1);
            else
                box.boxColor = isEditing ? new Color(0x467d2a, 1).multiply(0.65f) : new Color(0x467d2a, 1);

            box.draw(graph.mc.fontRenderer);
        }

        public void boxClicked(int button) {
            if (isEditing) {
            } else if (button == 0) {
                isEditing = true;
                box.text.setFocused(true);
                box.text.setCursorPositionEnd();
            }
        }

        public void keyTyped(char c, int typedKey) {
            if (typedKey == 28)
                cancelEdit();
            else {
              //  String newText = box.text.getText() + c;

              //  if (ValueUtil.isValidNumber(newText)) {
                    box.text.textboxKeyTyped(c, typedKey);
             //   }
                //                String prev = box.text.getText();
                //                box.text.textboxKeyTyped(c, typedKey);
                //                String newText = box.text.getText();
                //                if (typedKey != 14 && !newText.equals(prev)) { //backspace
                //                    try {
                //                        Double.parseDouble(box.text.getText());
                //                    } catch (NumberFormatException var6) {
                //                        box.text.setText(prev);
                //                    }
                //                }
            }
        }

        public void cancelEdit() {
            String s = box.text.getText();
            box.text.setText(s.isEmpty() ? "0" : ValueUtil.format(box.text.getDouble()));
            graph.pointManager.addPoint(type, graph.pointManager.playhead.worldX, box.text.getDouble());
            box.text.setFocused(false);
            isEditing = false;
        }

    }
}
