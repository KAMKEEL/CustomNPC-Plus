package noppes.npcs.client.gui.util.animation.panel;

import noppes.npcs.client.gui.util.GuiUtil;
import noppes.npcs.client.gui.util.animation.AnimationGraphEditor;
import noppes.npcs.client.gui.util.animation.GridPointManager;
import noppes.npcs.client.utils.Color;
import noppes.npcs.constants.animation.EnumFrameType;
import noppes.npcs.util.ValueUtil;
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

    public void draw(int wheel) {
        GuiUtil.drawRectD(startX - 1, startY, endX, endY, 0xff1d1d1d);

        GL11.glPushMatrix();
        GL11.glTranslatef(startX, startY + 1, 0);

        for (int i = 0; i < list.size(); i++) {
            Element element = list.get(i);
            element.draw(i, wheel);
        }

        GL11.glPopMatrix();

    }

    public void keyTyped(char c, int i) {
        list.forEach((element) -> {
            element.box.type(c, i);
        });
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        list.forEach((element) -> {
            if (element.isMouseAbove(mouseX, mouseY) && button == 0)
                selectedElement = element;

            element.box.click(mouseX - startX, mouseY - startY, button);
        });
    }

    public boolean isMouseAbove(int mouseX, int mouseY) {
        return mouseX >= startX && mouseX < endX && mouseY >= startY && mouseY < endY;
    }

    public class Element {
        public EnumFrameType type;
        public float elementStartY;

        public PanelTextBox box = new PanelTextBox() {
            public void finishEdit(byte operation) {
                super.finishEdit(operation);
                graph.pointManager.setSelectedPoint(graph.pointManager.addPoint(type, graph.pointManager.playhead.worldX, -text.getDouble()));
            }
        }.setDoublesOnly(0, -9999, 9999);

        public Element(EnumFrameType key) {
            this.type = key;
        }

        public boolean isMouseAbove(int mouseX, int mouseY) {
            mouseX -= startX;
            mouseY -= startY;

            return mouseX >= 0 && mouseX < box.screenX - 3 && mouseY >= elementStartY && mouseY < elementStartY + elementHeight + elementSpacing;
        }

        public void draw(int index, int wheel) {
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
            boolean isEditing = box.isEditing();
            box.screenX = width / 1.25F * scale + 8; //remove scaling from maxStringWidth
            box.screenY = elementStartY - 7.5f;
            box.textScale = scale;
            boolean isAboveFrame = box.isMouseAbove(graph.grid.mouseX - startX, graph.grid.mouseY - startY);

            if (graph.pointManager.getPoint(type, graph.pointManager.playhead.worldX) != null)
                box.boxColor = isEditing ? new Color(0x83752a).multiply(0.65f) : new Color(isAboveFrame ? 0xd1b727 : 0x83752a);
            else
                box.boxColor = isEditing ? new Color(0x467d2a).multiply(0.65f) : new Color(isAboveFrame ? 0x5fc729 : 0x467d2a);

            box.draw(graph.grid.mouseX - startX, graph.grid.mouseY - startY, graph.mc.fontRenderer, wheel);
        }
    }
}
