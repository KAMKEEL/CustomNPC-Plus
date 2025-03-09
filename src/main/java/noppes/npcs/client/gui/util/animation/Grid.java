package noppes.npcs.client.gui.util.animation;

import net.minecraft.client.gui.Gui;
import noppes.npcs.client.gui.util.GuiUtil;
import noppes.npcs.constants.animation.EnumFrameType;
import noppes.npcs.util.ValueUtil;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

public class Grid {
    GuiGraphEditor parent;
    public int startX, startY; // Positioning
    public int endX, endY; // Positioning
    public int yAxisHeight = 12;

    public float zoomX = 1.0f, targetZoomX = 1f, zoomY = 1f, targetZoomY = 1f; //Zoom smoothness
    public float panX = 0, panY = 0, targetPanX, targetPanY; //  pan offsets
    public int startPanX, startPanY;
    public boolean smoothenPanX, smoothenPanY;
    public int subDivisionX = 1, subDivisionY = 1;

    public boolean isDragging, isResetting;
    public boolean centerAroundMouse = true;

    public GridPointManager manager = new GridPointManager(this);

    public Grid(GuiGraphEditor parent, int startX, int startY, int endX, int endY) {
        this.parent = parent;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;

        zoomX = targetZoomX = 20;
        subDivisionY = 2;
    }

    public void draw(int mouseX, int mouseY, float partialTicks, int wheel) {
        //////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////
        // Panning
        if (isDragging && !Mouse.isButtonDown(0))
            isDragging = false;

        if (parent.isWithin(mouseX, mouseY) && !isResetting) {
            if (isDragging) {
                if (xDown())
                    panX -= (mouseX - startPanX) / zoomX;
                else if (yDown())
                    panY -= (mouseY - startPanY) / zoomY;
                else {
                    panX -= (mouseX - startPanX) / zoomX;
                    panY -= (mouseY - startPanY) / zoomY * subDivisionY;
                }
                startPanX = mouseX;
                startPanY = mouseY;
            }
        }

        //////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////
        // Reset key
        if (Keyboard.isKeyDown(Keyboard.KEY_R) && !isResetting) {
            if (xDown()) {
                targetPanX = 0;
                targetZoomX = 20;
                smoothenPanX = true;
            } else if (yDown()) {
                targetPanY = -((parent.clipHeight) * subDivisionY / 2);
                targetZoomY = 1.0f;
                smoothenPanY = true;
            } else {
                targetPanY = -((parent.clipHeight) * subDivisionY / 2);
                targetPanX = 0;
                targetZoomX = 20;
                targetZoomY = 1.0f;
                smoothenPanX = smoothenPanY = true;
            }
            isResetting = true;
            centerAroundMouse = false;
        } else if (isResetting && zoomX == targetZoomX && zoomY == targetZoomY && (smoothenPanX ? panX == targetPanX : smoothenPanY ? panY == targetPanY : true)) {
            isResetting = smoothenPanX = smoothenPanY = false;
            centerAroundMouse = true;
        }

        //////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////
        // Zoom logic
        if (wheel != 0) {
            float xRate = zoomX < 15 ? 1.1f : 1.25f;
            float yRate = zoomY < 15 ? 1.1f : 1.25f;

            if (xDown())
                targetZoomX = ValueUtil.clamp(wheel > 0 ? targetZoomX * xRate : targetZoomX / xRate, 0.025f, 100);
            else if (yDown())
                targetZoomY = ValueUtil.clamp(wheel > 0 ? targetZoomY * yRate : targetZoomY / yRate, 0.025f, 100);
            else {
                targetZoomX = ValueUtil.clamp(wheel > 0 ? targetZoomX * xRate : targetZoomX / xRate, 0.025f, 100);
                targetZoomY = ValueUtil.clamp(wheel > 0 ? targetZoomY * yRate : targetZoomY / yRate, 0.025f, 100);
            }
        }

        if (zoomX != targetZoomX || zoomY != targetZoomY)
            zoom(mouseX, mouseY, wheel);

        //////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////
        // Smoothen panning while resetting
        if (isResetting) {
            float rate = 0.1f;
            if (smoothenPanX && panX != targetPanX) {
                panX = ValueUtil.lerp(panX, targetPanX, rate);
                if (Math.abs(panX - targetPanX) < 0.1)
                    panX = targetPanX;
            }

            if (smoothenPanY && panY != targetPanY) {
                panY = ValueUtil.lerp(panY, targetPanY, rate);
                if (Math.abs(panY - targetPanY) < 0.1)
                    panY = targetPanY;
            }
        }

        //////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////
        Gui.drawRect(startX, startY - yAxisHeight, endX, startY, 0xFF161616); //background
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glPushMatrix();
        float scale = 3f;
        float offset = -400;
        //   GL11.glTranslatef(offset, offset,0);
        //   GL11.glScalef(scale, scale, 1);
        GL11.glDepthMask(false);
        drawLines(mouseX, mouseY);
        GL11.glDepthMask(true);
        manager.draw(mouseX, mouseY, partialTicks);
        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public void zoom(int mouseX, int mouseY, int wheel) {
        ///////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////
        //Zooms in on mouse point
        double beforeWorldX = worldX(mouseX - startX);
        double beforeWorldY = worldY(mouseY - startY);

        float rate = isResetting ? 0.1f : 0.05f;
        zoomX = ValueUtil.lerp(zoomX, targetZoomX, rate);
        zoomY = ValueUtil.lerp(zoomY, targetZoomY, rate);

        // Snap to exact target value
        if (Math.abs(zoomX - targetZoomX) < 0.001)
            zoomX = targetZoomX;
        if (Math.abs(zoomY - targetZoomY) < 0.001)
            zoomY = targetZoomY;

        double afterWorldX = worldX(mouseX - startX);
        double afterWorldY = worldY(mouseY - startY);

        if (centerAroundMouse) {
            if (xDown())
                panX += beforeWorldX - afterWorldX;
            else if (yDown())
                panY += beforeWorldY - afterWorldY;
            else {
                panX += beforeWorldX - afterWorldX;
                panY += beforeWorldY - afterWorldY;
            }
        }
    }

    public void drawLines(int mouseX, int mouseY) {
        int darkGray = 0xFF1a1a1a; // Major grid
        int lightGray = 0xFF2a2a2a; // Minor grid

        ////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////
        // Vertical
        double stepX = getStepX();
        double firstGridX = Math.floor((panX) / stepX) * stepX;
        for (double x = firstGridX, i = 0; x <= worldX(endX); x += stepX / 5, i++) {
            boolean isMajor = i % 5 == 0;
            double screenX = screenX(x);

            glPushMatrix();
            glTranslatef(startX, 0, 0);
            GuiUtil.drawVerticalLine(screenX, startY, endY, isMajor ? darkGray : lightGray);
            glPopMatrix();
        }

        for (double x = firstGridX; x <= worldX(endX); x += stepX)
            drawXValue((int) screenX(x), (int) x);

        ////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////
        // Horizontal
        double stepY = getStepY();
        double firstGridY = Math.floor((panY) / stepY) * stepY;
        for (double y = firstGridY, i = 0; y <= worldY(endY); y += stepY / 5, i++) {
            boolean isMajor = i % 5 == 0;
            double screenY = screenY(y);

            glPushMatrix();
            glTranslatef(0, startY, 0);
            GuiUtil.drawHorizontalLine(screenY, startX, endX, isMajor ? darkGray : lightGray);
            glPopMatrix();
        }

        for (double y = firstGridY; y <= worldY(endY); y += stepY)
            drawYValue((int) screenY(y), (int) -y);

        ////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////
        boolean debug = false;
        if (debug) {
            int my = mouseY;
            parent.getFontRenderer().drawString("panX: " + panX, mouseX + 5, my, 0xffffffff);
            parent.getFontRenderer().drawString("panY: " + panY, mouseX + 5, my += 10, 0xffffffff);
            parent.getFontRenderer().drawString("zoomX: " + zoomX, mouseX + 5, my += 10, 0xffffffff);
            parent.getFontRenderer().drawString("zoomY: " + zoomY, mouseX + 5, my += 10, 0xffffffff);
            parent.getFontRenderer().drawString("tZoomY: " + targetZoomY, mouseX + 5, my += 10, 0xffffffff);
            // parent.getFontRenderer().drawString("Raw Step: " + rawStep, mouseX + 5, my += 10, 0xffffffff);
            //  parent.getFontRenderer().drawString("Magnitude: " + magnitude, mouseX + 5, my += 10, 0xffffffff);
            //   parent.getFontRenderer().drawString("raw/mag: " + rawStep / magnitude, mouseX + 5, my += 10, 0xffffffff);
            parent.getFontRenderer().drawString("stepSize: " + stepX, mouseX + 5, my += 10, 0xffffffff);
        }
    }

    public double getStepX() {
        double stepSize = 0;
        if (zoomX < 0.1) // 0-1000
            stepSize = 1000;
        else if (zoomX < 0.2) // 0-500
            stepSize = 500;
        else if (zoomX < 0.5) // 0-200
            stepSize = 200;
        else if (zoomX <= 1) // 0-100
            stepSize = 100;
        else if (zoomX < 1.5) //0-50
            stepSize = 50;
        else if (zoomX < 3) //0-20
            stepSize = 20;
        else if (zoomX < 4.5) //0-10
            stepSize = 10;
        else if (zoomX < 10) //0-5
            stepSize = 5;
        else if (zoomX < 18) //0-2  For Default reset
            stepSize = 2;
        else if (zoomX <= 100) //0-1
            stepSize = 1;
        return stepSize;
    }

    public double getStepY() {
        double stepSize = 0;
        if (zoomY < 0.1) // 0-1000
            stepSize = 1000;
        else if (zoomY < 0.2) // 0-500
            stepSize = 500;
        else if (zoomY < 0.5) // 0-200
            stepSize = 200;
        else if (zoomY < 0.9) // 0-100  For Default reset
            stepSize = 100;
        else if (zoomY < 1.5) //0-50
            stepSize = 50;
        else if (zoomY < 3) //0-20
            stepSize = 20;
        else if (zoomY < 4.5) //0-10
            stepSize = 10;
        else if (zoomY < 10) //0-5
            stepSize = 5;
        else if (zoomY < 20) //0-2
            stepSize = 2;
        else if (zoomY <= 100) //0-1
            stepSize = 1;
        return stepSize;
    }

    public void drawXValue(int screenX, int value) {
        //Expand boundaries of scissor clip
        parent.setClip(startX, startY - yAxisHeight, parent.clipWidth, parent.clipHeight + yAxisHeight);

        GL11.glPushMatrix();
        float scale = 0.75f;
        int adjustedX = (int) ((screenX + 1) / scale); // Offset to the left of grid line
        int adjustedY = (int) ((startY - yAxisHeight + 4) / scale); // Adjust Y position

        GL11.glTranslatef(startX, 0, 1f);
        GL11.glScalef(scale, scale, 1f);
        parent.drawString(String.valueOf(value), adjustedX, adjustedY, 0xFFA0A0A0);
        GL11.glPopMatrix();

        //Reset clip
        parent.setClip(startX, startY, parent.clipWidth, parent.clipHeight);
    }

    public void drawYValue(int screenY, int value) {

        GL11.glPushMatrix();
        float scale = 0.5f;
        int adjustedX = (int) ((startX + 10) / scale); // Offset to the left of grid line
        int adjustedY = (int) ((screenY - 1) / scale); // Adjust Y position

        GL11.glTranslatef(0, startY, 1f);
        GL11.glScalef(scale, scale, 1f);
        parent.drawString(String.valueOf(value), adjustedX, adjustedY, 0xFFFFFF);
        GL11.glPopMatrix();
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        manager.mouseClicked(mouseX, mouseY, button);
        if (button == 0 && parent.isWithin(mouseX,mouseY)) { // Left-click to start dragging
            isDragging = true;
            startPanX = mouseX;
            startPanY = mouseY;
        }

    }
    public void keyTyped(char c, int i) {
       manager.keyTyped(c,i);
    }

    public boolean xDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_X);
    }

    public boolean yDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_Y);
    }

    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    // Double
    public double worldX(double screenX) {
        return screenX * subDivisionX / zoomX + panX;
    }

    public double worldY(double screenY) {
        return screenY * subDivisionY / zoomY + panY;
    }

    public double screenX(double worldX) {
        return (worldX - panX) * zoomX / subDivisionX;
    }

    public double screenY(double worldY) {
        return (worldY - panY) * zoomY / subDivisionY;
    }

    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    // Float
    public double worldX(float screenX) {
        return screenX * subDivisionX / zoomX + panX;
    }

    public double worldY(float screenY) {
        return screenY * subDivisionY / zoomY + panY;
    }

    public double screenX(float worldX) {
        return (worldX - panX) * zoomX / subDivisionX;
    }

    public double screenY(float worldY) {
        return (worldY - panY) * zoomY / subDivisionY;
    }

    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    // Int
    public double worldX(int screenX) {
        return screenX * subDivisionX / zoomX + panX;
    }

    public double worldY(int screenY) {
        return screenY * subDivisionY / zoomY + panY;
    }

    public double screenX(int worldX) {
        return (worldX - panX) * zoomX / subDivisionX;
    }

    public double screenY(int worldY) {
        return (worldY - panY) * zoomY / subDivisionY;
    }
}
