package noppes.npcs.client.gui.util.animation;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.gui.util.GuiUtil;
import noppes.npcs.client.gui.util.animation.keys.AnimationKeyPresets;
import noppes.npcs.client.gui.util.animation.keys.KeyPreset;
import noppes.npcs.client.utils.Color;
import noppes.npcs.constants.animation.EnumFrameType;
import noppes.npcs.util.Ease;
import noppes.npcs.util.ValueUtil;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.function.BiConsumer;

public class GridPointManager {
    public Grid grid;
    public AnimationKeyPresets keys;
    public HashMap<EnumFrameType, TreeMap<Double, Point>> typePoints = new HashMap<>();
    public List<EnumFrameType> highlightedTypes = new ArrayList<>();
    public List<Point> highlightedPoints = new ArrayList<>();
    public Point selectedPoint;
    public Playhead playhead = new Playhead(0);

    public boolean isFreeTransforming;
    public double ftGrabX, ftGrabY; //where point is grabbed on free transforming
    public GridPointManager(Grid grid) {
        this.grid = grid;
        keys = grid.parent.keys;
        keys();
    }

    public void keys() {
        EnumFrameType type = EnumFrameType.ROTATION_X;

        keys.SELECT_POINT.setTask((pressType) -> {
            if (pressType == KeyPreset.PRESS) {
                forEachActive((pointType, point) -> {
                    if (point.isMouseAbove(grid.mouseX, grid.mouseY))
                        setSelectedPoint(point);
                });
            }
        });

        keys.ADD_POINT.setTask((pressType) -> {
            if (pressType == KeyPreset.PRESS) {
                if (!highlightedTypes.contains(type))
                    highlightedTypes.add(type);

                TreeMap<Double, Point> points = pointsOf(type);
                Point point = points != null ? points.get((double) playhead.worldX) : null; //check if it exists
                if (point == null)
                    point = addPoint(type, playhead.worldX, 0); // worldX(mouseX - startX), worldY(mouseY - startY)

                setSelectedPoint(point);
            }
        });

        keys.DELETE_POINT.setTask((pressType) -> {
            if ((pressType == KeyPreset.PRESS) && selectedPoint != null && !grid.isDragging) {
                deletePoint(type, selectedPoint.worldX);
            }
        });

        keys.FREE_TRANSFORM.setTask((pressType) -> {
            if (pressType == KeyPreset.PRESS && selectedPoint != null) {
                if (isFreeTransforming) {
                    selectedPoint.set(ftGrabX, ftGrabY);
                    ftGrabX = ftGrabY = 0;
                    Cursors.reset();
                } else {
                    ftGrabX = selectedPoint.worldX;
                    ftGrabY = selectedPoint.worldY;
                    Cursors.setCursor(Cursors.MOVE);
                }

                isFreeTransforming = !isFreeTransforming;
            }
        });

    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0 && selectedPoint != null && !isFreeTransforming) {
            if (grid.parent.isWithin(mouseX, mouseY) && !selectedPoint.isMouseAbove(mouseX, mouseY))
                setSelectedPoint(null);
        }

        if (button == 0 && isFreeTransforming) {
            selectedPoint.updateKey();
            Cursors.reset();
            isFreeTransforming = false;
        }

        playhead.mouseClicked(mouseX, mouseY, button);
        forEachActive((type, point) -> {
            point.mouseClicked(mouseX, mouseY, button);
        });
    }

    public Point addPoint(EnumFrameType type, double x, double y) {
        return addPoint(type, new Point(type, x, y));
    }

    public Point addPoint(EnumFrameType type, Point point) {
        TreeMap<Double, Point> points = pointsOf(type);
        if (points == null) {
            points = new TreeMap<>();
            typePoints.put(type, points);
        }

        points.put(point.worldX, point);
        return point;
    }

    public void deletePoint(EnumFrameType type, double x) {
        TreeMap<Double, Point> points = pointsOf(type);
        if (points == null)
            return;

        Point point = points.remove(x);
        if (point == selectedPoint)
            setSelectedPoint(null);
    }
    public Point getPoint(EnumFrameType type, double x) {
        TreeMap<Double, Point> points = pointsOf(type);
        if (points == null)
            return null;

        return points.get(x);
    }

    public TreeMap<Double, Point> pointsOf(EnumFrameType type) {
        return typePoints.get(type);
    }

    public void forEachActive(BiConsumer<EnumFrameType, Point> consumer) {
        for (Map.Entry<EnumFrameType, TreeMap<Double, Point>> entry : typePoints.entrySet()) {
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

    public Point getNext(Point point) {
        TreeMap<Double, Point> points = pointsOf(point.type);
        if (points == null)
            return null;

        Map.Entry<Double, Point> entry = points.higherEntry(point.worldX);
        if (entry != null)
            return entry.getValue();

        return null;
    }

    public void drawEasedCurve(Point from, Point to) {
        if (from == null || to == null)
            return;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glLineWidth(2F); // Increase line thickness

        GL11.glPushMatrix();
        GL11.glTranslatef(grid.startX, grid.startY, 0);
        GL11.glColor4f(1, 1, 1, 1);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(GL11.GL_LINE_STRIP);


        for (float t = 0; t <= 1; t += 0.005) {
            float easedT = Ease.OUTEXPO.apply(t);
            double xt = ValueUtil.lerp(from.screenX() + 0.5, to.screenX() + 0.5, easedT);
            double yt = ValueUtil.lerp(from.screenY() + 0.5, to.screenY() + 0.5, easedT);
            tessellator.addVertex(xt, yt, 0);
        }

        tessellator.draw();
        GL11.glPopMatrix();

        GL11.glLineWidth(1F); // Increase line thickness
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public void draw(int mouseX, int mouseY, float partialTicks) {


        ////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////
        // Free transform logic
        if (isFreeTransforming && selectedPoint != null) {
                if (grid.xDown())
                    selectedPoint.setX(Math.round(grid.worldX(GuiUtil.preciseMouseX() - grid.startX)));
                else if (grid.yDown())
                    selectedPoint.setY(grid.worldY(GuiUtil.preciseMouseY() - grid.startY));
                else
                    selectedPoint.set(Math.round(grid.worldX(GuiUtil.preciseMouseX() - grid.startX)), grid.worldY(GuiUtil.preciseMouseY() - grid.startY));
        }

        ////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////
        playhead.draw(mouseX, mouseY, partialTicks);

        forEachActive((type, point) -> {
            point.draw(mouseX, mouseY, partialTicks);
        });

        if (Cursors.currentCursor != null)
            Cursors.currentCursor.draw(mouseX, mouseY);

        ////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////
        // Pans grid while free transforming
        // MUST BE DONE AT THE VERY END -> CHANGES CURSOR POSITION WITHOUT
        // CHANGING GuiScreen.drawScreen(mouseX,mouseY) VALUES
        if (isFreeTransforming) {
            if (mouseX < grid.startX) {
                int deltaX = mouseX - grid.startX;
                grid.panX += deltaX / grid.zoomX;
                GuiUtil.setMouseX(grid.startX - 1);
            }
            if (mouseX > grid.endX) {
                int deltaX = mouseX - grid.endX;
                grid.panX += (deltaX / grid.zoomX);
                GuiUtil.setMouseX(grid.endX + 1);
            }
            if (mouseY < grid.startY) {
                int deltaY = mouseY - grid.startY;
                grid.panY += deltaY / grid.zoomY;
                GuiUtil.setMouseY(grid.startY);
            }
            if (mouseY > grid.endY) {
                int deltaY = mouseY - grid.endY;
                grid.panY += deltaY / grid.zoomY;
                GuiUtil.setMouseY(grid.endY + 2);
            }
        }

        ////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////
    }

    public void keyTyped(char c, int key) {
    }

    private static ResourceLocation TEXTURE = new ResourceLocation("customnpcs:textures/gui/animation.png");

    public class Point {
        public EnumFrameType type;
        public double worldX, worldY;
        public boolean highlighted;

        public double previousX;

        public Point(EnumFrameType type, double worldX, double worldY) {
            this.type = type;
            this.worldX = previousX = worldX;
            this.worldY = worldY;
        }

        public void setX(double x) {
            worldX = (int) x;
        }

        public void setY(double y) {
            worldY = y;
        }

        public void set(double x, double y) {
            setX(x);
            setY(y);
        }

        public void updateKey() {
            TreeMap<Double, Point> points = pointsOf(type);
            if (points == null)
                return;

            points.remove(previousX);
            points.put(worldX, this);
            previousX = worldX;
        }

        public void draw(int mouseX, int mouseY, float partialTicks) {

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
            drawEasedCurve(this, getNext(this));

        }

        public boolean isMouseAbove(int mouseX, int mouseY) {
            int rMX = mouseX - grid.startX, rMY = mouseY - grid.startY; //relative to grid
            float scale = 0.1f;
            float textureWidth = (32), textureHeight = (32);
            float offsetX = (textureWidth / 2) * scale - 0.5f, offsetY = (textureHeight / 2) * scale - 0.5f;

            int screenX = (int) (screenX() - offsetX);
            int screenY = (int) ((screenY()) - offsetY);

            return rMX >= screenX && rMX < screenX + textureWidth * scale && rMY >= screenY && rMY < screenY + textureHeight * scale;
        }

        public double screenX() {

            return grid.screenX(worldX);
        }

        public double screenY() {
            return grid.screenY(worldY);
        }

        public void mouseClicked(int mouseX, int mouseY, int button) {
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

            ////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////
            // Head lines
            double screenX = screenX();
            float height = grid.endY - grid.startY;

            GL11.glPushMatrix();
            GL11.glTranslated(grid.startX + screenX, grid.startY, 0);
            GL11.glDepthMask(false);
            GuiUtil.drawVerticalLine(-1, 0, height, 0xFF212121);
            GuiUtil.drawVerticalLine(0, -5, height, 0xFF4772b3);
            GuiUtil.drawVerticalLine(1, 0, height, 0xFF212121);
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
