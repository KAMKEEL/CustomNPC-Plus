package noppes.npcs.client.gui.global;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.controllers.MagicController;
import noppes.npcs.controllers.data.Magic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiMagicMap extends Gui {

    private final GuiScreen parent;
    private final int x, y, width, height;
    private final int iconSize = 16;
    private final int slotPadding = 4; // extra pixels for the slot background
    private final int slotSize = iconSize + slotPadding; // e.g., 20
    private float panX = 0, panY = 0;
    private int lastDragX, lastDragY;
    private boolean dragging = false;

    private float zoom = 1.0f; // Zoom factor

    // Build our own list from the controller's map.
    private List<Magic> magics;
    private Map<Integer, Point> positions = new HashMap<>();

    public GuiMagicMap(GuiScreen parent, int x, int y, int width, int height) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        magics = new ArrayList<Magic>();
        for (Magic magic : MagicController.getInstance().magics.values()) {
            magics.add(magic);
        }
        calculatePositions();
    }

    // Call this from your scroll wheel event:
    public void handleMouseScroll(int scrollDelta) {
        zoom += scrollDelta * 0.0009f;
        if (zoom < 0.5f) zoom = 0.5f;
        if (zoom > 2.0f) zoom = 2.0f;
    }

    // Lay out icons evenly in a circle.
    private void calculatePositions() {
        int count = magics.size();
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        int radius = Math.min(width, height) / 2 - iconSize - 10;
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            int posX = (int) (centerX + radius * Math.cos(angle));
            int posY = (int) (centerY + radius * Math.sin(angle));
            positions.put(magics.get(i).id, new Point(posX, posY));
        }
    }

    // Helper: distance from point (px,py) to line segment (x1,y1)-(x2,y2)
    private double pointLineDistance(double px, double py, double x1, double y1, double x2, double y2) {
        double A = px - x1, B = py - y1, C = x2 - x1, D = y2 - y1;
        double dot = A * C + B * D;
        double lenSq = C * C + D * D;
        double param = (lenSq != 0) ? (dot / lenSq) : -1;
        double xx, yy;
        if (param < 0) { xx = x1; yy = y1; }
        else if (param > 1) { xx = x2; yy = y2; }
        else { xx = x1 + param * C; yy = y1 + param * D; }
        double dx = px - xx, dy = py - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Returns an ARGB color for an arrow based on the weakness percentage.
    private int getArrowColor(float percent) {
        if (percent <= 0.10f) return 0xFF80FF80; // light green
        else if (percent <= 0.40f) return 0xFFCCFF66; // green-yellow
        else if (percent <= 0.50f) return 0xFFFFA500; // orange
        else {
            float ratio = (percent - 0.50f) / 0.50f;
            ratio = Math.min(1.0f, Math.max(0.0f, ratio));
            int r1 = 0xFF, g1 = 0xA5, b1 = 0x00;
            int r2 = 0xFF, g2 = 0x00, b2 = 0x00;
            int r = r1 + (int)((r2 - r1) * ratio);
            int g = g1 + (int)((g2 - g1) * ratio);
            int b = b1 + (int)((b2 - b1) * ratio);
            return (0xFF << 24) | (r << 16) | (g << 8) | b;
        }
    }

    // Main draw method.
    public void drawMap(int mouseX, int mouseY) {
        // Call this once per draw; adjust if needed.
        handleMouseScroll(Mouse.getDWheel());

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int factor = sr.getScaleFactor();

        // Recalculate positions.
        calculatePositions();

        // Compute transformed mouse coordinates for hit testing.
        int centerX = x + width / 2, centerY = y + height / 2;
        int tMouseX = centerX + (int)((mouseX - centerX) / zoom);
        int tMouseY = centerY + (int)((mouseY - centerY) / zoom);

        // --- Determine selection state ---
        Integer hoveredIconID = null;
        int selectedArrowAttacker = -1, selectedArrowDefender = -1;
        HashSet<Integer> selectedIconIDs = new HashSet<>();
        // Check icons first.
        for (Magic magic : magics) {
            Point pos = positions.get(magic.id);
            if (pos == null) continue;
            int slotX = pos.x + (int)panX - slotSize / 2;
            int slotY = pos.y + (int)panY - slotSize / 2;
            if (tMouseX >= slotX && tMouseX < slotX + slotSize &&
                tMouseY >= slotY && tMouseY < slotY + slotSize) {
                hoveredIconID = magic.id;
                break;
            }
        }
        if (hoveredIconID != null) {
            selectedIconIDs.add(hoveredIconID);
            Magic hoveredMagic = null;
            for (Magic magic : magics) {
                if (magic.id == hoveredIconID) {
                    hoveredMagic = magic;
                    break;
                }
            }
            if (hoveredMagic != null) {
                // Add connected icons.
                for (Magic magic : magics) {
                    if (magic.weaknesses.containsKey(hoveredIconID)) {
                        selectedIconIDs.add(magic.id);
                    }
                }
                for (Integer otherId : hoveredMagic.weaknesses.keySet()) {
                    selectedIconIDs.add(otherId);
                }
            }
        } else {
            // Otherwise, check for arrow hover.
            final double threshold = 5.0;
            outer: for (Magic defMagic : magics) {
                Point defPos = positions.get(defMagic.id);
                if (defPos == null) continue;
                for (Integer attackerId : defMagic.weaknesses.keySet()) {
                    Point attPos = positions.get(attackerId);
                    if (attPos == null) continue;
                    double x1 = attPos.x + panX, y1 = attPos.y + panY;
                    double x2 = defPos.x + panX, y2 = defPos.y + panY;
                    double dist = pointLineDistance(tMouseX, tMouseY, x1, y1, x2, y2);
                    if (dist < threshold) {
                        selectedArrowAttacker = attackerId;
                        selectedArrowDefender = defMagic.id;
                        selectedIconIDs.add(attackerId);
                        selectedIconIDs.add(defMagic.id);
                        break outer;
                    }
                }
            }
        }

        // --- Set scissor rectangle (in original coords) ---
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * factor, (sr.getScaledHeight() - (y + height)) * factor, width * factor, height * factor);

        // --- Draw dark background BEFORE zoom ---
        parent.drawRect(x, y, x + width, y + height, 0xFF333333);

        // --- Begin zoom transformation ---
        GL11.glPushMatrix();
        GL11.glTranslatef(centerX, centerY, 0);
        GL11.glScalef(zoom, zoom, 1.0f);
        GL11.glTranslatef(-centerX, -centerY, 0);

        // --- Draw arrow lines first ---
        for (Magic defMagic : magics) {
            Point defPos = positions.get(defMagic.id);
            if (defPos == null) continue;
            for (Integer attackerId : defMagic.weaknesses.keySet()) {
                Point attPos = positions.get(attackerId);
                if (attPos == null) continue;
                float percent = defMagic.weaknesses.get(attackerId);
                int ax = attPos.x + (int)panX;
                int ay = attPos.y + (int)panY;
                int dx = defPos.x + (int)panX;
                int dy = defPos.y + (int)panY;
                boolean dim = false;
                if (!selectedIconIDs.isEmpty() && !(selectedIconIDs.contains(attackerId) && selectedIconIDs.contains(defMagic.id))) {
                    dim = true;
                }
                drawArrowLine(ax, ay, dx, dy, percent, dim);
            }
        }

        // --- Draw icons (with larger slot backgrounds) ---
        RenderHelper.enableGUIStandardItemLighting();
        RenderItem renderItem = new RenderItem();
        FontRenderer fontRenderer = mc.fontRenderer;
        TextureManager textureManager = mc.getTextureManager();
        for (Magic magic : magics) {
            Point pos = positions.get(magic.id);
            if (pos == null) continue;
            int slotX = pos.x + (int)panX - slotSize / 2;
            int slotY = pos.y + (int)panY - slotSize / 2;
            boolean iconDim = (!selectedIconIDs.isEmpty() && !selectedIconIDs.contains(magic.id));
            if (iconDim) {
                GL11.glColor4f(0.4f, 0.4f, 0.4f, 1f);
            }
            parent.drawRect(slotX, slotY, slotX + slotSize, slotY + slotSize, 0xFF999999);
            drawHorizontalLine(slotX, slotX + slotSize, slotY, 0xFF555555);
            drawHorizontalLine(slotX, slotX + slotSize, slotY + slotSize - 1, 0xFF555555);
            drawVerticalLine(slotX, slotY, slotY + slotSize, 0xFF555555);
            drawVerticalLine(slotX + slotSize - 1, slotY, slotY + slotSize, 0xFF555555);
            int iconX = pos.x + (int)panX - iconSize / 2;
            int iconY = pos.y + (int)panY - iconSize / 2;
            if (magic.iconItem != null) {
                renderItem.renderItemAndEffectIntoGUI(fontRenderer, textureManager, magic.iconItem, iconX, iconY);
            } else if (magic.iconTexture != null && !magic.iconTexture.isEmpty()) {
                textureManager.bindTexture(new ResourceLocation(magic.iconTexture));
                parent.drawTexturedModalRect(iconX, iconY, 0, 0, iconSize, iconSize);
            } else {
                ItemStack sword = new ItemStack(Items.iron_sword);
                renderItem.renderItemAndEffectIntoGUI(fontRenderer, textureManager, sword, iconX, iconY);
            }
            if (iconDim) {
                GL11.glColor4f(1f, 1f, 1f, 1f);
            }
        }
        RenderHelper.disableStandardItemLighting();
        GL11.glPopMatrix();

        // --- Draw arrow heads (on top) using the same zoom transformation ---
        GL11.glPushMatrix();
        GL11.glTranslatef(centerX, centerY, 0);
        GL11.glScalef(zoom, zoom, 1.0f);
        GL11.glTranslatef(-centerX, -centerY, 0);
        for (Magic defMagic : magics) {
            Point defPos = positions.get(defMagic.id);
            if (defPos == null) continue;
            for (Integer attackerId : defMagic.weaknesses.keySet()) {
                Point attPos = positions.get(attackerId);
                if (attPos == null) continue;
                float percent = defMagic.weaknesses.get(attackerId);
                int ax = attPos.x + (int)panX;
                int ay = attPos.y + (int)panY;
                int dx = defPos.x + (int)panX;
                int dy = defPos.y + (int)panY;
                boolean dim = false;
                if (!selectedIconIDs.isEmpty() && !(selectedIconIDs.contains(attackerId) && selectedIconIDs.contains(defMagic.id))) {
                    dim = true;
                }
                drawArrowHead(ax, ay, dx, dy, percent, dim);
            }
        }
        GL11.glPopMatrix();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // --- Draw tooltips outside of the zoom transformation ---
        if (hoveredIconID != null) {
            for (Magic magic : magics) {
                if (magic.id == hoveredIconID) {
                    List<String> tooltip = new ArrayList<>();
                    tooltip.add(magic.getName());
                    if (parent instanceof GuiNPCInterface2) {
                        ((GuiNPCInterface2) parent).renderHoveringText(tooltip, mouseX, mouseY, fontRenderer);
                    }
                    return;
                }
            }
        } else {
            final double threshold = 5.0;
            for (Magic defMagic : magics) {
                Point defPos = positions.get(defMagic.id);
                if (defPos == null) continue;
                for (Integer attackerId : defMagic.weaknesses.keySet()) {
                    Point attPos = positions.get(attackerId);
                    if (attPos == null) continue;
                    double x1 = attPos.x + panX, y1 = attPos.y + panY;
                    double x2 = defPos.x + panX, y2 = defPos.y + panY;
                    double dist = pointLineDistance(tMouseX, tMouseY, x1, y1, x2, y2);
                    if (dist < threshold) {
                        float percent = defMagic.weaknesses.get(attackerId);
                        List<String> tooltip = new ArrayList<>();
                        tooltip.add("+" + (int)(percent * 100) + "%");
                        if (parent instanceof GuiNPCInterface2) {
                            ((GuiNPCInterface2) parent).renderHoveringText(tooltip, mouseX, mouseY, fontRenderer);
                        }
                        return;
                    }
                }
            }
        }
    }

    // Draw only the arrow line (without arrow head).
    private void drawArrowLine(int x1, int y1, int x2, int y2, float percent, boolean dim) {
        int arrowColor = getArrowColor(percent);
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(2.0F);
        float r = ((arrowColor >> 16) & 0xFF) / 255.0F;
        float g = ((arrowColor >> 8) & 0xFF) / 255.0F;
        float b = (arrowColor & 0xFF) / 255.0F;
        if (dim) { r *= 0.4f; g *= 0.4f; b *= 0.4f; }
        GL11.glColor4f(r, g, b, 1f);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x2, y2);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopAttrib();
    }

    // Draw the arrow head so that its tip connects at the defender icon edge.
    private void drawArrowHead(int x1, int y1, int x2, int y2, float percent, boolean dim) {
        // x1,y1 and x2,y2 are the centers (after pan) of the attacker and defender icons.
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float)Math.sqrt(dx * dx + dy * dy);
        if(len == 0) return;
        // Compute unit vector from attacker to defender.
        float ux = dx / len, uy = dy / len;
        // Offset defender center by half the slot size to get the edge.
        float defenderEdgeX = x2 - ux * (slotSize / 2f);
        float defenderEdgeY = y2 - uy * (slotSize / 2f);

        // For testing, try a larger arrow size.
        int arrowSize = 5;  // adjust as needed

        // Compute the angle from attacker to defender.
        double angle = Math.atan2(uy, ux);
        // Compute left and right points of the arrow head.
        float leftX = (float)(defenderEdgeX - arrowSize * Math.cos(angle - Math.PI / 6));
        float leftY = (float)(defenderEdgeY - arrowSize * Math.sin(angle - Math.PI / 6));
        float rightX = (float)(defenderEdgeX - arrowSize * Math.cos(angle + Math.PI / 6));
        float rightY = (float)(defenderEdgeY - arrowSize * Math.sin(angle + Math.PI / 6));

        int arrowColor = getArrowColor(percent);
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        float r = ((arrowColor >> 16) & 0xFF) / 255.0F;
        float g = ((arrowColor >> 8) & 0xFF) / 255.0F;
        float b = (arrowColor & 0xFF) / 255.0F;
        if(dim) { r *= 0.4f; g *= 0.4f; b *= 0.4f; }
        GL11.glColor4f(r, g, b, 1f);

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2f(defenderEdgeX, defenderEdgeY);
        GL11.glVertex2f(leftX, leftY);
        GL11.glVertex2f(rightX, rightY);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_CULL_FACE);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopAttrib();
    }

    // --- Mouse handling methods ---
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && isWithin(mouseX, mouseY)) {
            dragging = true;
            lastDragX = mouseX;
            lastDragY = mouseY;
            return true;
        }
        return false;
    }

    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (dragging) {
            int dx = mouseX - lastDragX;
            int dy = mouseY - lastDragY;
            // Adjust panning speed by zoom so dragging "feels" the same.
            panX += dx / zoom * 0.7f;
            panY += dy / zoom * 0.7f;
            lastDragX = mouseX;
            lastDragY = mouseY;
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        dragging = false;
    }

    private boolean isWithin(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
