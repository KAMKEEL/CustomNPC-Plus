package noppes.npcs.client.gui.util.animation;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.gui.util.GuiUtil;
import noppes.npcs.client.utils.Color;
import noppes.npcs.constants.animation.EnumFrameType;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class GridPointManager {
    public Grid grid;
    public HashMap<EnumFrameType, HashMap<Double, Point>> typePoints = new HashMap<>();
    public List<EnumFrameType> highlightedTypes = new ArrayList<>();
    public List<Point> highlightedPoints = new ArrayList<>();
    public Point selectedPoint;
    public Playhead playhead = new Playhead(0);

    public boolean isFreeTransforming;
    public double ftGrabX, ftGrabY; //where point is grabbed on free transforming
    public GridPointManager(Grid grid) {
        this.grid = grid;
    }

    public Point addPoint(EnumFrameType type, double x, double y) {
        return addPoint(type, new Point(type, x, y));
    }

    public Point addPoint(EnumFrameType type, Point point) {
        HashMap<Double, Point> points = pointsOf(type);
        if (points == null) {
            points = new HashMap<>();
            typePoints.put(type, points);
        }

        points.put(point.worldX, point);
        return point;
    }

    public void deletePoint(EnumFrameType type, double x) {
        HashMap<Double, Point> points = pointsOf(type);
        if (points == null)
            return;

        points.remove(x);
    }
    public Point getPoint(EnumFrameType type, double x) {
        HashMap<Double, Point> points = pointsOf(type);
        if (points == null)
            return null;

        return points.get(x);
    }

    public HashMap<Double, Point> pointsOf(EnumFrameType type) {
        return typePoints.get(type);
    }

    public void forEachActive(BiConsumer<EnumFrameType, Point> consumer) {
        for (Map.Entry<EnumFrameType, HashMap<Double, Point>> entry : typePoints.entrySet()) {
            EnumFrameType type = entry.getKey();

            if (!highlightedTypes.contains(type))
                continue;

            for (Map.Entry<Double, Point> points : entry.getValue().entrySet()) {
                Point point = points.getValue();
                consumer.accept(type, point);
            }
        }
    }

    public void setSelectedPoint(Point point) {
        if (selectedPoint != null)
            selectedPoint.highlighted = false;

        this.selectedPoint = point;

        if (point != null)
            point.highlighted = true;
    }

    public void draw(int mouseX, int mouseY, float partialTicks) {
        if (isFreeTransforming && selectedPoint != null) {
            if (grid.xDown())
                selectedPoint.worldX = (int) Math.round(grid.worldX(GuiUtil.preciseMouseX() - grid.startX));
            else if (grid.yDown())
                selectedPoint.worldY = grid.worldY(GuiUtil.preciseMouseY() - grid.startY);
            else {
                selectedPoint.worldX = (int) Math.round(grid.worldX(GuiUtil.preciseMouseX() - grid.startX));
                selectedPoint.worldY = grid.worldY(GuiUtil.preciseMouseY() - grid.startY);
            }
        }

        playhead.draw(mouseX, mouseY, partialTicks);

        forEachActive((type, point) -> {
            point.draw(mouseX, mouseY, partialTicks);
        });

        if (Cursors.currentCursor != null)
            Cursors.currentCursor.draw(mouseX, mouseY);

    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (selectedPoint != null && !isFreeTransforming) {
            if (grid.parent.isWithin(mouseX, mouseY) && !selectedPoint.isMouseAbove(mouseX, mouseY))
                setSelectedPoint(null);
        }

        if (isFreeTransforming) {
            Cursors.setCursor(null);
            isFreeTransforming = false;
            if (button == 1 && selectedPoint != null) {
                selectedPoint.set(ftGrabX, ftGrabY);
                ftGrabX = ftGrabY = 0;
            }
        }

        playhead.mouseClicked(mouseX, mouseY, button);

        forEachActive((type, point) -> {
            point.mouseClicked(mouseX, mouseY, button);
        });
    }

    public void keyTyped(char c, int key) {
        if (key == Keyboard.KEY_G && selectedPoint != null) {
            isFreeTransforming = !isFreeTransforming;
            ftGrabX = selectedPoint.worldX;
            ftGrabY = selectedPoint.worldY;
            Cursors.setCursor(isFreeTransforming ? Cursors.MOVE : null);
        }

        EnumFrameType type = EnumFrameType.ROTATION_X;
        if (key == Keyboard.KEY_Z) {
            highlightedTypes.add(type);

            HashMap<Double, Point> points = pointsOf(type);
            Point point = points != null ? points.get((double) playhead.worldX) : null; //check if it exists
            if (point == null)
                point = addPoint(type, playhead.worldX, 0); // worldX(mouseX - startX), worldY(mouseY - startY)

            setSelectedPoint(point);
        }

        if (key == Keyboard.KEY_DELETE && selectedPoint != null){
            deletePoint(type,selectedPoint.worldX);
        }


    }

    private static ResourceLocation TEXTURE = new ResourceLocation("customnpcs:textures/gui/animation.png");

    public class Point {
        public EnumFrameType type;
        public double worldX, worldY;
        public boolean highlighted;

        public Point(EnumFrameType type, double worldX, double worldY) {
            this.type = type;
            this.worldX = worldX;
            this.worldY = worldY;
        }

        public void draw(int mouseX, int mouseY, float partialTicks) {

            grid.parent.getFontRenderer().drawString("above: " + isMouseAbove(mouseX, mouseY), mouseX, mouseY, 0xffffffff);

            float scale = 0.1f;
            int textureWidth = 32, textureHeight = 32;
            float offsetX = (textureHeight / 2) * scale - 0.5f, offsetY = (textureHeight / 2) * scale - 0.5f;
            double screenX = screenX() / scale, screenY = screenY() / scale;

            grid.parent.mc.getTextureManager().bindTexture(TEXTURE);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            new Color(highlighted ? 0xffbe33 : 0xbfbfbf, 1).glColor();

            GL11.glPushMatrix();
            GL11.glTranslatef(grid.startX - offsetX, grid.startY - offsetY, 0);
            GL11.glScalef(scale, scale, 1);
            GuiUtil.drawTexturedModalRect(screenX, screenY, textureWidth, textureHeight, 256 - textureWidth, 256 - textureHeight);

            GL11.glPopMatrix();
            GL11.glDisable(GL11.GL_BLEND);
        }

        public boolean isMouseAbove(int mouseX, int mouseY) {
            int rMX = mouseX - grid.startX, rMY = mouseY - grid.startY; //relative to grid
            float scale = 0.1f;
            float textureWidth = (32), textureHeight = (32);
            float offsetX = (textureWidth / 2) * scale - 0.5f, offsetY = (textureHeight / 2) * scale - 0.5f;

            int screenX = (int) (screenX() - offsetX);
            int screenY = (int) ((screenY()) - offsetY);

            //            grid.parent.getFontRenderer().drawString("mouseX: " + mouseX, mouseX, mouseY + 10, 0xffffffff);
            //            grid.parent.getFontRenderer().drawString(String.format("Point(%s, %s)", screenX, screenY), mouseX, mouseY + 20, 0xffffffff);
            //            grid.parent.getFontRenderer().drawString(String.format("Mouse(%s, %s)", rMX, rMY), mouseX, mouseY + 30, 0xffffffff);
            //            grid.parent.getFontRenderer().drawString(String.format("Offset(%s, %s)", offsetX / scale, offsetX), mouseX, mouseY + 40, 0xffffffff);

            return rMX >= screenX && rMX < screenX + textureWidth * scale && rMY >= screenY && rMY < screenY + textureHeight * scale;
        }

        public double screenX() {
            return grid.screenX(worldX);
        }

        public double screenY() {
            return grid.screenY(worldY);
        }

        public void mouseClicked(int mouseX, int mouseY, int button) {
            if (button == 0 && isMouseAbove(mouseX, mouseY))
                setSelectedPoint(this);
        }

        public void set(double x, double y) {
            this.worldX = x;
            this.worldY = y;
        }
    }

    public class Playhead {
        public int worldX;
        public boolean isDragging;

        public Playhead(int currentX) {
            this.worldX = currentX;
        }

        public void draw(int mouseX, int mouseY, float partialTicks) {
            // Dragging
            if (isDragging && !Mouse.isButtonDown(0))
                isDragging = false;

            if (isDragging)
                worldX = (int) Math.round(grid.worldX(GuiUtil.preciseMouseX() - grid.startX));

            //Expand clip boundary
            grid.parent.setClip(grid.startX, grid.startY - grid.yAxisHeight, grid.parent.clipWidth, grid.parent.clipHeight + grid.yAxisHeight);

            grid.parent.getFontRenderer().drawString(String.format("Mouse(%s, %s)", mouseX, mouseY), mouseX, mouseY + 10, 0xffffffff);
            grid.parent.getFontRenderer().drawString(String.format("m(%s, %s)", GuiUtil.preciseMouseX(), GuiUtil.preciseMouseY()), mouseX, mouseY + 20, 0xffffffff);

            ////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////
            // Head lines
            double screenX = screenX();

            GL11.glPushMatrix();
            GL11.glTranslatef(grid.startX, 0, 0);
            GL11.glDepthMask(false);
            GuiUtil.drawVerticalLine(screenX - 1, grid.startY, grid.endY, 0xFF212121);
            GuiUtil.drawVerticalLine(screenX, grid.startY - 5, grid.endY, 0xFF4772b3);
            GuiUtil.drawVerticalLine(screenX + 1, grid.startY, grid.endY, 0xFF212121);
            GL11.glPopMatrix();

            ////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////
            // Head texture
            FontRenderer fontRenderer = grid.parent.getFontRenderer();
            float textureScaleX = 0.5f, textureScaleY = 0.5f;

            // Texture width dynamically scales with string width
            int defaultWidth = 6, width = fontRenderer.getStringWidth(worldX + "");
            if (width / defaultWidth > 1)
                textureScaleX += width / defaultWidth * 0.125;

            int textureWidth = 25, textureHeight = 17;
            float offset = (textureWidth / 2) * textureScaleX;
            float screenY = grid.startY - grid.yAxisHeight;

            grid.parent.mc.getTextureManager().bindTexture(TEXTURE);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            new Color(0x426dae, 1).glColor();

            GL11.glPushMatrix();
            GL11.glTranslatef(grid.startX, 0, 0);

            GL11.glPushMatrix();
            GL11.glTranslatef(-offset, 0, 0);
            GL11.glScalef(textureScaleX, textureScaleY, 1);

            GuiUtil.drawTexturedModalRect(screenX / textureScaleX, (screenY + 2) / textureScaleY, textureWidth, textureHeight, 228, 199);


            GL11.glPopMatrix();
            GL11.glDisable(GL11.GL_BLEND);

            ////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////
            // Head value
            float vScale = 0.75f;
            double valueScreenX = (screenX + 1) / vScale;
            double valueScreenY = (screenY + 4) / vScale;
            GL11.glScalef(vScale, vScale, 1);
            grid.parent.drawString(String.valueOf(worldX), (int) valueScreenX, (int) valueScreenY, 0xFFccd7e9);
            GL11.glPopMatrix();

            ////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////
            //Reset clip
            grid.parent.setClip(grid.startX, grid.startY, grid.parent.clipWidth, grid.parent.clipHeight);
        }

        public boolean isMouseAbove(int mouseX, int mouseY) {
            int rMX = mouseX - grid.startX, rMY = mouseY - grid.startY; //relative to grid
            float scale = 0.5f;
            float textureWidth = (25 * scale), textureHeight = (17 * scale);
            float offsetX = (textureWidth / 2) * scale;

            int screenX = (int) Math.round(screenX() - (offsetX / scale));
            int screenY = -grid.yAxisHeight + 2;

            return rMX >= screenX && rMX <= screenX + textureWidth && rMY >= screenY && rMY < screenY + textureHeight;
        }

        public void mouseClicked(int mouseX, int mouseY, int button) {


            if (button == 0 && mouseX >= grid.startX && mouseX <= grid.endX && mouseY >= grid.startY - grid.yAxisHeight && mouseY <= grid.startY) {
                worldX = (int) Math.round(grid.worldX(GuiUtil.preciseMouseX() - grid.startX));
                isDragging = true;
            }
        }

        public double screenX() {
            return grid.screenX(worldX);
        }
    }
}
