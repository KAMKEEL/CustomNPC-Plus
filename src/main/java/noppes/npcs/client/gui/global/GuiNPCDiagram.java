package noppes.npcs.client.gui.global;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/**
 * Abstract GUI class for drawing diagrams that consist of icons (nodes) and connections (arrows).
 *
 * This class provides:
 *  - A standard circular layout of nodes.
 *  - Panning and zooming functionality.
 *  - Standard drawing of a node's "box": a background and border that can be customized.
 *  - Connection drawing (including arrow heads).
 *  - Highlighting logic: when hovering over an icon, its connections and related icons are highlighted.
 *  - Tooltip support for nodes and connections.
 *
 * IMPORTANT: Child classes must implement the data provider methods to supply the list of icons and connections,
 * and implement how an icon’s image is rendered (renderIcon).
 */
public abstract class GuiNPCDiagram extends Gui {

    // The area where the diagram is drawn.
    protected int x, y, width, height;
    // Panning offsets.
    protected float panX = 0, panY = 0;
    // Zoom factor.
    protected float zoom = 1.0f;
    // For mouse dragging.
    protected boolean dragging = false;
    protected int lastDragX, lastDragY;

    // The size of the icon image (e.g. 16 pixels square).
    protected int iconSize = 16;
    // Extra padding for the node background (box).
    protected int slotPadding = 4;
    // The overall size of the node's box (icon plus padding).
    protected int slotSize = iconSize + slotPadding;

    // Standard colors for the node background and border.
    protected int iconBackgroundColor = 0xFF999999;
    protected int iconBorderColor = 0xFF555555;
    // Thickness of the border (in pixels). We draw the border lines inside the box so nothing clips.
    protected int iconBorderThickness = 1;

    // Flags for drawing arrow heads and using color scaling on connections.
    protected boolean showArrowHeads = true;
    protected boolean useColorScaling = true;

    public GuiNPCDiagram(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // -------------
    // Data Providers
    // -------------
    /**
     * Must return the list of icons (nodes) to be drawn.
     */
    protected abstract List<DiagramIcon> getIcons();

    /**
     * Must return the list of connections (arrows) between nodes.
     */
    protected abstract List<DiagramConnection> getConnections();

    /**
     * Subclass must implement rendering of the icon image itself (without drawing the background box).
     *
     * @param icon        The icon data.
     * @param posX        The center X position where the icon is drawn.
     * @param posY        The center Y position.
     * @param highlighted Whether this node is currently highlighted.
     */
    protected abstract void renderIcon(DiagramIcon icon, int posX, int posY, boolean highlighted);

    /**
     * Optionally supply tooltip text for an icon.
     */
    protected List<String> getIconTooltip(DiagramIcon icon) {
        return null;
    }

    /**
     * Default tooltip for a connection is based on its hoverText.
     * <p>
     * Also, if a reciprocal connection exists (i.e. A→B and B→A) then the tooltip will include two lines.
     * For example:
     * <pre>
     *   A --> Hover Text
     *   B --> Hover Text
     * </pre>
     * To display the names of the endpoints, getIconName() is used.
     */
    protected List<String> getConnectionTooltip(DiagramConnection conn) {
        List<String> tooltip = new ArrayList<>();
        // Get the display names for the endpoints.
        DiagramIcon iconFrom = getIconById(conn.idFrom);
        DiagramIcon iconTo = getIconById(conn.idTo);
        String nameFrom = iconFrom != null ? getIconName(iconFrom) : "Unknown";
        String nameTo = iconTo != null ? getIconName(iconTo) : "Unknown";
        // Add tooltip for the current connection.
        tooltip.add(nameFrom + " > " + nameTo + ":");
        tooltip.add(conn.hoverText);
        // Check if there is a reciprocal connection (B→A).
        DiagramConnection reverse = getConnectionByIds(conn.idTo, conn.idFrom);
        if (reverse != null) {
            tooltip.add(nameTo + " > " + nameFrom + ":");
            tooltip.add(reverse.hoverText);
        }
        return tooltip;
    }

    /**
     * Returns a display name for the given icon.
     * Child classes may override this method to provide a more meaningful name.
     * By default, it returns "Icon <id>".
     */
    protected String getIconName(DiagramIcon icon) {
        return "Icon " + icon.id;
    }

    // -------------
    // Layout Calculation
    // -------------
    /**
     * Arranges icons in a circle centered in the diagram area.
     * Returns a mapping from an icon’s id to its (x,y) center position.
     */
    protected Map<Integer, Point> calculatePositions() {
        List<DiagramIcon> icons = getIcons();
        Map<Integer, Point> positions = new HashMap<>();
        int count = icons.size();
        // Calculate the center of the diagram area.
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        // Determine the radius of the circle, leaving room for icons.
        int radius = Math.min(width, height) / 2 - iconSize - 10;
        for (int i = 0; i < count; i++) {
            // Angle for this icon in radians.
            double angle = 2 * Math.PI * i / count;
            int posX = centerX + (int) (radius * Math.cos(angle));
            int posY = centerY + (int) (radius * Math.sin(angle));
            positions.put(icons.get(i).id, new Point(posX, posY));
        }
        return positions;
    }

    // -------------
    // Standard Icon Box (Background and Border)
    // -------------
    /**
     * Draws the background box (with optional highlighting) and border for an icon.
     * The box is drawn at (slotX, slotY) with the given slotSize.
     *
     * @param slotX       The top-left x-coordinate of the box.
     * @param slotY       The top-left y-coordinate of the box.
     * @param slotSize    The width/height of the box.
     * @param highlighted Whether the box should appear highlighted.
     */
    protected void drawIconBox(int slotX, int slotY, int slotSize, boolean highlighted) {
        // If highlighted, mix the background color with white a little.
        int bg = highlighted ? mixColors(iconBackgroundColor, 0xFFFFFFFF, 0.2f) : iconBackgroundColor;
        // Draw the box background.
        drawRect(slotX, slotY, slotX + slotSize, slotY + slotSize, bg);
        // Draw the border lines inside the box.
        for (int i = 0; i < iconBorderThickness; i++) {
            // Draw horizontal top and bottom borders.
            drawHorizontalLine(slotX + i, slotX + slotSize - i, slotY + i, iconBorderColor);
            drawHorizontalLine(slotX + i, slotX + slotSize - i, slotY + slotSize - 1 - i, iconBorderColor);
            // Draw vertical left and right borders.
            drawVerticalLine(slotX + i, slotY + i, slotY + slotSize - i, iconBorderColor);
            drawVerticalLine(slotX + slotSize - 1 - i, slotY + i, slotY + slotSize - i, iconBorderColor);
        }
    }

    /**
     * Helper to mix two ARGB colors by a given ratio.
     * If ratio is 0, returns color1; if 1, returns color2.
     */
    protected int mixColors(int color1, int color2, float ratio) {
        int a1 = (color1 >> 24) & 0xFF, r1 = (color1 >> 16) & 0xFF, g1 = (color1 >> 8) & 0xFF, b1 = color1 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF, r2 = (color2 >> 16) & 0xFF, g2 = (color2 >> 8) & 0xFF, b2 = color2 & 0xFF;
        int a = (int) (a1 + (a2 - a1) * ratio);
        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    // -------------
    // Main Draw Method
    // -------------
    /**
     * Draws the entire diagram: nodes (with their standard boxes) and connections (with arrow heads).
     * Also performs hit testing to determine which icon or connection is being hovered,
     * so that appropriate highlighting and tooltips can be shown.
     *
     * @param mouseX The current mouse x-coordinate.
     * @param mouseY The current mouse y-coordinate.
     */
    public void drawDiagram(int mouseX, int mouseY) {
        // Handle zoom changes from the scroll wheel.
        handleMouseScroll(Mouse.getDWheel());

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int factor = sr.getScaleFactor();

        // Calculate positions for all icons.
        Map<Integer, Point> positions = calculatePositions();
        // Determine the center of the diagram.
        int centerX = x + width / 2, centerY = y + height / 2;
        // Transform mouse coordinates to account for zoom (for hit testing).
        int tMouseX = centerX + (int) ((mouseX - centerX) / zoom);
        int tMouseY = centerY + (int) ((mouseY - centerY) / zoom);

        // -------------
        // Hit Testing & Selection
        // -------------
        Integer hoveredIconId = null;
        int hoveredConnFrom = -1, hoveredConnTo = -1;
        // This set will contain IDs of icons that should be drawn highlighted.
        HashSet<Integer> selectedIconIds = new HashSet<>();

        // Check if the mouse is over any icon.
        for (DiagramIcon icon : getIcons()) {
            Point pos = positions.get(icon.id);
            if (pos == null) continue;
            // Calculate the top-left corner of the icon box.
            int slotX = pos.x + (int) panX - slotSize / 2;
            int slotY = pos.y + (int) panY - slotSize / 2;
            // If the transformed mouse is within the box...
            if (tMouseX >= slotX && tMouseX < slotX + slotSize &&
                tMouseY >= slotY && tMouseY < slotY + slotSize) {
                hoveredIconId = icon.id;
                selectedIconIds.add(icon.id);
                break;
            }
        }
        // If an icon is hovered, also mark all icons connected to it.
        if (hoveredIconId != null) {
            for (DiagramConnection conn : getConnections()) {
                if (conn.idFrom == hoveredIconId || conn.idTo == hoveredIconId) {
                    selectedIconIds.add(conn.idFrom);
                    selectedIconIds.add(conn.idTo);
                }
            }
        } else {
            // If no icon is hovered, check if the mouse is near any connection line.
            final double threshold = 5.0;
            outer:
            for (DiagramConnection conn : getConnections()) {
                Point pFrom = positions.get(conn.idFrom);
                Point pTo = positions.get(conn.idTo);
                if (pFrom == null || pTo == null) continue;
                double dist = pointLineDistance(tMouseX, tMouseY,
                    pFrom.x + panX, pFrom.y + panY, pTo.x + panX, pTo.y + panY);
                if (dist < threshold) {
                    hoveredConnFrom = conn.idFrom;
                    hoveredConnTo = conn.idTo;
                    selectedIconIds.add(conn.idFrom);
                    selectedIconIds.add(conn.idTo);
                    break outer;
                }
            }
        }

        // -------------
        // Clipping & Background
        // -------------
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        // Set the scissor rectangle to the diagram area.
        GL11.glScissor(x * factor, (sr.getScaledHeight() - (y + height)) * factor, width * factor, height * factor);
        // Draw the overall diagram background.
        drawRect(x, y, x + width, y + height, 0xFF333333);

        // -------------
        // Zoom Transformation
        // -------------
        GL11.glPushMatrix();
        GL11.glTranslatef(centerX, centerY, 0);
        GL11.glScalef(zoom, zoom, 1.0f);
        GL11.glTranslatef(-centerX, -centerY, 0);

        // -------------
        // Draw Connections (Arrows)
        // -------------
        for (DiagramConnection conn : getConnections()) {
            Point pFrom = positions.get(conn.idFrom);
            Point pTo = positions.get(conn.idTo);
            if (pFrom == null || pTo == null) continue;
            int ax = pFrom.x + (int) panX;
            int ay = pFrom.y + (int) panY;
            int bx = pTo.x + (int) panX;
            int by = pTo.y + (int) panY;
            // Dim the connection if both endpoints are not selected.
            boolean dim = !selectedIconIds.isEmpty() &&
                !(selectedIconIds.contains(conn.idFrom) && selectedIconIds.contains(conn.idTo));
            drawConnectionLine(ax, ay, bx, by, conn, dim);
        }

        // -------------
        // Draw Icons (Nodes)
        // -------------
        for (DiagramIcon icon : getIcons()) {
            Point pos = positions.get(icon.id);
            if (pos == null) continue;
            int slotX = pos.x + (int) panX - slotSize / 2;
            int slotY = pos.y + (int) panY - slotSize / 2;
            // If this icon's id is in the selected set, consider it highlighted.
            boolean highlighted = !selectedIconIds.isEmpty() && selectedIconIds.contains(icon.id);
            // First draw the standardized icon box (background and border).
            drawIconBox(slotX, slotY, slotSize, highlighted);
            // Then let the subclass render the icon image.
            renderIcon(icon, pos.x + (int) panX, pos.y + (int) panY, highlighted);
        }
        GL11.glPopMatrix();

        // -------------
        // Draw Arrow Heads (if enabled)
        // -------------
        if (showArrowHeads) {
            GL11.glPushMatrix();
            GL11.glTranslatef(centerX, centerY, 0);
            GL11.glScalef(zoom, zoom, 1.0f);
            GL11.glTranslatef(-centerX, -centerY, 0);
            for (DiagramConnection conn : getConnections()) {
                Point pFrom = positions.get(conn.idFrom);
                Point pTo = positions.get(conn.idTo);
                if (pFrom == null || pTo == null) continue;
                int ax = pFrom.x + (int) panX;
                int ay = pFrom.y + (int) panY;
                int bx = pTo.x + (int) panX;
                int by = pTo.y + (int) panY;
                boolean dim = !selectedIconIds.isEmpty() &&
                    !(selectedIconIds.contains(conn.idFrom) && selectedIconIds.contains(conn.idTo));
                drawArrowHead(ax, ay, bx, by, conn, dim);
            }
            GL11.glPopMatrix();
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // -------------
        // Draw Tooltips
        // -------------
        if (hoveredIconId != null) {
            DiagramIcon icon = getIconById(hoveredIconId);
            List<String> tooltip = getIconTooltip(icon);
            if (tooltip != null && !tooltip.isEmpty()) {
                drawHoveringText(tooltip, mouseX, mouseY, mc.fontRenderer);
            }
        } else if (hoveredConnFrom != -1 && hoveredConnTo != -1) {
            DiagramConnection conn = getConnectionByIds(hoveredConnFrom, hoveredConnTo);
            List<String> tooltip = getConnectionTooltip(conn);
            if (tooltip != null && !tooltip.isEmpty()) {
                drawHoveringText(tooltip, mouseX, mouseY, mc.fontRenderer);
            }
        }
    }

    // -------------
    // Helper Methods for Hit Testing & Drawing
    // -------------
    /**
     * Returns the DiagramIcon with the given id, or null if not found.
     */
    protected DiagramIcon getIconById(int id) {
        for (DiagramIcon icon : getIcons()) {
            if (icon.id == id)
                return icon;
        }
        return null;
    }

    /**
     * Returns the DiagramConnection connecting idFrom to idTo, or null if not found.
     */
    protected DiagramConnection getConnectionByIds(int idFrom, int idTo) {
        for (DiagramConnection conn : getConnections()) {
            if (conn.idFrom == idFrom && conn.idTo == idTo)
                return conn;
        }
        return null;
    }

    /**
     * Draws a connection line between two points.
     *
     * @param x1   Source x-coordinate.
     * @param y1   Source y-coordinate.
     * @param x2   Target x-coordinate.
     * @param y2   Target y-coordinate.
     * @param conn The connection data.
     * @param dim  Whether to draw a dimmed (less bright) version.
     */
    protected void drawConnectionLine(int x1, int y1, int x2, int y2, DiagramConnection conn, boolean dim) {
        int color = getConnectionColor(conn);
        if (!useColorScaling) {
            color = 0xFFFFFFFF;
        }
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(2.0F);
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        if (dim) {
            r *= 0.4f;
            g *= 0.4f;
            b *= 0.4f;
        }
        GL11.glColor4f(r, g, b, 1f);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x2, y2);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopAttrib();
    }

    /**
     * Draws an arrow head at the target end of a connection.
     *
     * @param x1   Source x-coordinate.
     * @param y1   Source y-coordinate.
     * @param x2   Target x-coordinate.
     * @param y2   Target y-coordinate.
     * @param conn The connection data.
     * @param dim  Whether to draw a dimmed version.
     */
    protected void drawArrowHead(int x1, int y1, int x2, int y2, DiagramConnection conn, boolean dim) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len == 0) return;
        // Compute the unit vector from source to target.
        float ux = dx / len, uy = dy / len;
        // Offset from target center by half the slot size to get the edge of the icon box.
        float defenderEdgeX = x2 - ux * (slotSize / 2f);
        float defenderEdgeY = y2 - uy * (slotSize / 2f);

        int arrowSize = 6; // Adjustable arrow size.
        double angle = Math.atan2(uy, ux);
        // Calculate the left and right vertices of the arrow head triangle.
        float leftX = (float) (defenderEdgeX - arrowSize * Math.cos(angle - Math.PI / 6));
        float leftY = (float) (defenderEdgeY - arrowSize * Math.sin(angle - Math.PI / 6));
        float rightX = (float) (defenderEdgeX - arrowSize * Math.cos(angle + Math.PI / 6));
        float rightY = (float) (defenderEdgeY - arrowSize * Math.sin(angle + Math.PI / 6));

        int color = getConnectionColor(conn);
        if (!useColorScaling) {
            color = 0xFFFFFFFF;
        }
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        if (dim) {
            r *= 0.4f;
            g *= 0.4f;
            b *= 0.4f;
        }
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

    /**
     * Computes a color for a connection based on its percentage value.
     * Uses different thresholds to return a base color, and interpolates for higher percentages.
     */
    protected int getConnectionColor(DiagramConnection conn) {
        float percent = conn.percent;
        if (percent <= 0.10f) return 0xFF80FF80;      // light green
        else if (percent <= 0.40f) return 0xFFCCFF66; // green-yellow
        else if (percent <= 0.50f) return 0xFFFFA500; // orange
        else {
            float ratio = (percent - 0.50f) / 0.50f;
            ratio = Math.min(1f, Math.max(0f, ratio));
            int r1 = 0xFF, g1 = 0xA5, b1 = 0x00;
            int r2 = 0xFF, g2 = 0x00, b2 = 0x00;
            int r = r1 + (int) ((r2 - r1) * ratio);
            int g = g1 + (int) ((g2 - g1) * ratio);
            int b = b1 + (int) ((b2 - b1) * ratio);
            return (0xFF << 24) | (r << 16) | (g << 8) | b;
        }
    }

    /**
     * Utility method: returns the shortest distance from a point (px,py) to the line segment defined by (x1,y1)-(x2,y2).
     */
    private double pointLineDistance(double px, double py, double x1, double y1, double x2, double y2) {
        double A = px - x1, B = py - y1, C = x2 - x1, D = y2 - y1;
        double dot = A * C + B * D;
        double lenSq = C * C + D * D;
        double param = (lenSq != 0) ? (dot / lenSq) : -1;
        double xx, yy;
        if (param < 0) {
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            xx = x2;
            yy = y2;
        } else {
            xx = x1 + param * C;
            yy = y1 + param * D;
        }
        double dx = px - xx, dy = py - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // -------------
    // Mouse Handling
    // -------------
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && isWithin(mouseX, mouseY)) {
            dragging = true;
            lastDragX = mouseX;
            lastDragY = mouseY;
            return true;
        }
        return false;
    }

    public void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        if (dragging) {
            int dx = mouseX - lastDragX;
            int dy = mouseY - lastDragY;
            // Adjust panning speed relative to zoom.
            panX += dx / zoom * 0.7f;
            panY += dy / zoom * 0.7f;
            lastDragX = mouseX;
            lastDragY = mouseY;
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        dragging = false;
    }

    protected boolean isWithin(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    /**
     * Handles mouse wheel scrolling for zooming in/out.
     */
    public void handleMouseScroll(int scrollDelta) {
        zoom += scrollDelta * 0.0009f;
        if (zoom < 0.5f) zoom = 0.5f;
        if (zoom > 2.0f) zoom = 2.0f;
    }

    /**
     * Override this method if you want to render tooltips differently.
     * By default, it does nothing.
     */
    protected void drawHoveringText(List<String> text, int mouseX, int mouseY, FontRenderer fontRenderer) {
        // Default implementation is empty.
    }

    // -------------
    // Flag Setters
    // -------------
    public void setShowArrowHeads(boolean showArrowHeads) {
        this.showArrowHeads = showArrowHeads;
    }

    public void setUseColorScaling(boolean useColorScaling) {
        this.useColorScaling = useColorScaling;
    }

    // -------------
    // Inner Classes for Data Wrapping
    // -------------
    /**
     * A simple wrapper class for a node/icon.
     */
    public static class DiagramIcon {
        public int id;
        public DiagramIcon(int id) {
            this.id = id;
        }
    }

    /**
     * A simple wrapper class for a connection (arrow) between nodes.
     * Contains the source node id (idFrom), target node id (idTo), a percentage value (for color scaling),
     * and a hover text.
     */
    public static class DiagramConnection {
        public int idFrom, idTo;
        public float percent;
        public String hoverText;
        public DiagramConnection(int idFrom, int idTo, float percent, String hoverText) {
            this.idFrom = idFrom;
            this.idTo = idTo;
            this.percent = percent;
            this.hoverText = hoverText;
        }
    }
}
