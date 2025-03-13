package noppes.npcs.client.gui.util.animation.panel;

import noppes.npcs.client.gui.util.GuiUtil;
import noppes.npcs.client.gui.util.animation.AnimationGraphEditor;
import noppes.npcs.constants.animation.EnumFrameType;
import org.lwjgl.opengl.GL11;

import java.util.LinkedList;

public class PanelFrameType {
    public AnimationGraphEditor graph;

    public int startX, startY, endX, endY, width, height;
    public float scale = 0.66f;
    public int elementHeight = 5, elementSpacing = 7;

    public LinkedList<Element> list = new LinkedList<>();

    public PanelFrameType(AnimationGraphEditor graph) {
        this.graph = graph;

        for (EnumFrameType type : EnumFrameType.values())
            list.add(new Element(type));
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
        //  GuiUtil.drawRectD(startX,startY,endX,endY, 0xffffffff);
        GL11.glPushMatrix();
        GL11.glTranslatef((startX + 2), startY, 0);


        for (int i = 0; i < list.size(); i++) {
            Element element = list.get(i);
            element.draw(i);
        }

        GL11.glPopMatrix();
    }

    public void keyTyped(char c, int i) {
        list.forEach((element) -> {
        });
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        list.forEach((element) -> {
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

        public Element(EnumFrameType key) {
            this.type = key;
        }

        public void draw(int index) {


            //  GuiUtil.drawRectD(100,100,endX,endY, type.color);
            float offsetY = index * (elementHeight + elementSpacing + 2) / scale;
            GL11.glPushMatrix();
            GL11.glScalef(scale, scale, 1);
            GL11.glTranslatef(0, offsetY, 0);
            GuiUtil.drawRectD(0, 0, width / scale, (elementHeight + elementSpacing) / scale, 0x0);
            GuiUtil.drawRectD(0, 0, 3, (elementHeight + elementSpacing) / scale, type.color);

            GL11.glTranslatef(0, ((elementHeight + elementSpacing) / 2), 0);
            String name = String.format("%s", type.toString());
            graph.getFontRenderer().drawString(name, 6, 0, 0xffffff);

            GL11.glPopMatrix();
        }
    }
}
