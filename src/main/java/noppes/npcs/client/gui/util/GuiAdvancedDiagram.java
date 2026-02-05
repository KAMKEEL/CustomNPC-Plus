package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import noppes.npcs.client.ClientProxy;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Abstract pannable/zoomable canvas for rendering rectangular nodes with text
 * and curved connections. Text is rendered in screen-space for crisp display at any zoom.
 */
public abstract class GuiAdvancedDiagram extends Gui {

    // ===== Inner Data Structures =====

    public static class AdvancedNode {
        public int id;
        public String title;
        public String subtitle;
        public List<NodeSlot> slots;
        public int headerColor;
        public int x, y;
        public int width, height;
        public boolean selected;

        public AdvancedNode(int id, String title) {
            this.id = id;
            this.title = title;
            this.slots = new ArrayList<>();
            this.headerColor = 0xFF4488CC;
        }
    }

    public static class NodeSlot {
        public int index;
        public String label;
        public int color;
        public int targetNodeId;
        public int type; // 0=linked, 1=quit, 2=command, 3=disabled

        public NodeSlot(int index, String label, int color, int targetNodeId, int type) {
            this.index = index;
            this.label = label;
            this.color = color;
            this.targetNodeId = targetNodeId;
            this.type = type;
        }
    }

    public static class AdvancedConnection {
        public int sourceNodeId;
        public int sourceSlot;
        public int targetNodeId;
        public int color;

        public AdvancedConnection(int sourceNodeId, int sourceSlot, int targetNodeId, int color) {
            this.sourceNodeId = sourceNodeId;
            this.sourceSlot = sourceSlot;
            this.targetNodeId = targetNodeId;
            this.color = color;
        }
    }

    /** Buffered text entry for screen-space rendering */
    private static class PendingText {
        float worldX, worldY;
        String text;
        int color;
        boolean centered;
        int maxWidth;

        PendingText(float wx, float wy, String text, int color, boolean centered, int maxWidth) {
            this.worldX = wx;
            this.worldY = wy;
            this.text = text;
            this.color = color;
            this.centered = centered;
            this.maxWidth = maxWidth;
        }
    }

    // ===== Layout Constants =====
    protected int nodeMinWidth = 100;
    protected int nodeMaxWidth = 160;
    protected int nodeHeaderHeight = 14;
    protected int slotHeight = 11;
    protected int nodePadding = 4;
    protected int nodeSpacingX = 50;
    protected int nodeSpacingY = 35;
    protected int connectionPointRadius = 3;

    // ===== Diagram Bounds =====
    protected int x, y, diagramWidth, diagramHeight;

    // ===== Pan/Zoom =====
    protected float panX = 0, panY = 0;
    protected float zoom = 1.0f;
    protected static final float MIN_ZOOM = 0.3f;
    protected static final float MAX_ZOOM = 3.0f;

    // ===== Dragging =====
    protected boolean dragging = false;
    protected int lastDragX, lastDragY;

    // ===== Node Cache =====
    protected Map<Integer, AdvancedNode> nodes;
    protected List<AdvancedConnection> connections;

    // ===== Selection/Hover =====
    protected int selectedNodeId = -1;
    protected int hoveredNodeId = -1;

    // ===== Double-click =====
    private long lastClickTime = 0;
    private int lastClickedNodeId = -1;
    private static final long DOUBLE_CLICK_MS = 400;

    // ===== Text Buffer =====
    private final List<PendingText> pendingTexts = new ArrayList<>();

    // ===== Cached transform values =====
    private int cCenterX, cCenterY;

    // ===== Parent =====
    protected GuiNPCInterface parent;

    public GuiAdvancedDiagram(GuiNPCInterface parent, int x, int y, int width, int height) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.diagramWidth = width;
        this.diagramHeight = height;
    }

    // ===== Abstract Methods =====
    protected abstract List<AdvancedNode> createNodes();
    protected abstract List<AdvancedConnection> createConnections();
    protected abstract void onNodeClick(AdvancedNode node);
    protected abstract void onNodeDoubleClick(AdvancedNode node);

    // ===== Cache Management =====
    public void invalidateCache() {
        nodes = null;
        connections = null;
    }

    protected Map<Integer, AdvancedNode> getNodes() {
        if (nodes == null) {
            List<AdvancedNode> nodeList = createNodes();
            nodes = new HashMap<>();
            for (AdvancedNode node : nodeList) {
                nodes.put(node.id, node);
            }
            connections = createConnections();
            computeNodeSizes();
            layoutNodes();
        }
        return nodes;
    }

    protected List<AdvancedConnection> getConnections() {
        getNodes();
        return connections;
    }

    // ===== Font Helpers (using ClientProxy.Font) =====
    protected int fontWidth(String text) {
        return ClientProxy.Font.width(text);
    }

    protected int fontHeight() {
        return ClientProxy.Font.height();
    }

    // Font draw helper
    protected void fontDraw(String text, int x, int y, int color) {
        ClientProxy.Font.drawString(text, x, y, color);
    }

    // Trim text to fit within maxWidth, adding ".." if needed
    protected String fontTrimToWidth(String text, int maxWidth) {
        if (ClientProxy.Font.width(text) <= maxWidth) return text;
        String ellipsis = "..";
        int ellipsisWidth = ClientProxy.Font.width(ellipsis);
        String trimmed = text;
        while (trimmed.length() > 1 && ClientProxy.Font.width(trimmed) + ellipsisWidth > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed + ellipsis;
    }

    // ===== World -> Screen coordinate transform =====
    private float toScreenX(float wx) {
        return (wx - cCenterX) * zoom + cCenterX + panX;
    }

    private float toScreenY(float wy) {
        return (wy - cCenterY) * zoom + cCenterY + panY;
    }

    // ===== Buffered text: add text at world coordinates, rendered later in screen-space =====
    private void bufferText(float worldX, float worldY, String text, int color, boolean centered, int maxWidth) {
        pendingTexts.add(new PendingText(worldX, worldY, text, color, centered, maxWidth));
    }

    // ===== Node Size Computation =====
    protected void computeNodeSizes() {
        slotHeight = fontHeight() + 2;
        nodeHeaderHeight = fontHeight() + 4;

        for (AdvancedNode node : nodes.values()) {
            int titleW = fontWidth(node.title) + nodePadding * 2;
            int w = Math.max(nodeMinWidth, titleW);
            if (node.subtitle != null) {
                int subW = fontWidth(node.subtitle) + nodePadding * 2;
                w = Math.max(w, subW);
            }
            for (NodeSlot slot : node.slots) {
                int slotW = fontWidth(slot.label) + nodePadding * 2 + connectionPointRadius * 2 + 4;
                w = Math.max(w, slotW);
            }
            node.width = Math.min(w, nodeMaxWidth);

            int h = nodeHeaderHeight;
            if (node.subtitle != null) h += slotHeight;
            h += node.slots.size() * slotHeight;
            h += nodePadding;
            node.height = h;
        }
    }

    // ===== Layered Tree Layout (Sugiyama-style) =====
    protected void layoutNodes() {
        if (nodes.isEmpty()) return;

        Set<Integer> hasIncoming = new HashSet<>();
        Map<Integer, List<Integer>> outgoing = new HashMap<>();
        for (AdvancedNode n : nodes.values()) {
            outgoing.put(n.id, new ArrayList<Integer>());
        }
        for (AdvancedConnection conn : connections) {
            if (nodes.containsKey(conn.sourceNodeId) && nodes.containsKey(conn.targetNodeId)) {
                hasIncoming.add(conn.targetNodeId);
                outgoing.get(conn.sourceNodeId).add(conn.targetNodeId);
            }
        }

        List<Integer> roots = new ArrayList<>();
        for (AdvancedNode n : nodes.values()) {
            if (!hasIncoming.contains(n.id)) {
                roots.add(n.id);
            }
        }
        if (roots.isEmpty()) {
            int minId = Integer.MAX_VALUE;
            for (int id : nodes.keySet()) {
                if (id < minId) minId = id;
            }
            roots.add(minId);
        }

        Map<Integer, Integer> layerMap = new HashMap<>();
        Queue<Integer> queue = new LinkedList<>();
        for (int root : roots) {
            if (!layerMap.containsKey(root)) {
                layerMap.put(root, 0);
                queue.add(root);
            }
        }
        while (!queue.isEmpty()) {
            int curr = queue.poll();
            int currLayer = layerMap.get(curr);
            for (int target : outgoing.get(curr)) {
                if (!layerMap.containsKey(target)) {
                    layerMap.put(target, currLayer + 1);
                    queue.add(target);
                }
            }
        }

        List<Integer> orphanIds = new ArrayList<>();
        for (AdvancedNode n : nodes.values()) {
            if (!layerMap.containsKey(n.id)) {
                orphanIds.add(n.id);
            }
        }
        Collections.sort(orphanIds);

        int maxLayer = 0;
        for (int l : layerMap.values()) {
            if (l > maxLayer) maxLayer = l;
        }

        Map<Integer, List<Integer>> layers = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : layerMap.entrySet()) {
            int layer = entry.getValue();
            if (!layers.containsKey(layer)) {
                layers.put(layer, new ArrayList<Integer>());
            }
            layers.get(layer).add(entry.getKey());
        }

        for (List<Integer> layer : layers.values()) {
            Collections.sort(layer);
        }

        // Barycenter ordering
        for (int pass = 0; pass < 4; pass++) {
            for (int l = 1; l <= maxLayer; l++) {
                List<Integer> layer = layers.get(l);
                if (layer == null) continue;
                final Map<Integer, Float> bary = new HashMap<>();
                for (int nodeId : layer) {
                    float sum = 0;
                    int count = 0;
                    List<Integer> prevLayer = layers.get(l - 1);
                    if (prevLayer != null) {
                        for (AdvancedConnection conn : connections) {
                            if (conn.targetNodeId == nodeId && prevLayer.contains(conn.sourceNodeId)) {
                                sum += prevLayer.indexOf(conn.sourceNodeId);
                                count++;
                            }
                        }
                    }
                    bary.put(nodeId, count > 0 ? sum / count : Float.MAX_VALUE);
                }
                Collections.sort(layer, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer a, Integer b) {
                        return Float.compare(bary.get(a), bary.get(b));
                    }
                });
            }
        }

        int startX = x + 30;
        int startY = y + 30;

        for (int l = 0; l <= maxLayer; l++) {
            List<Integer> layer = layers.get(l);
            if (layer == null) continue;

            int maxW = 0;
            for (int nodeId : layer) {
                AdvancedNode n = nodes.get(nodeId);
                if (n.width > maxW) maxW = n.width;
            }

            int yOffset = startY;
            for (int nodeId : layer) {
                AdvancedNode n = nodes.get(nodeId);
                n.x = startX;
                n.y = yOffset;
                yOffset += n.height + nodeSpacingY;
            }
            startX += maxW + nodeSpacingX;
        }

        // Orphan grid
        if (!orphanIds.isEmpty()) {
            int cols = (int) Math.ceil(Math.sqrt(orphanIds.size()));
            int gridX = startX + nodeSpacingX;
            int gridY = startY;
            int col = 0;
            int rowMaxH = 0;

            for (int orphanId : orphanIds) {
                AdvancedNode n = nodes.get(orphanId);
                n.x = gridX;
                n.y = gridY;

                if (n.height > rowMaxH) rowMaxH = n.height;
                col++;

                if (col >= cols) {
                    col = 0;
                    gridX = startX + nodeSpacingX;
                    gridY += rowMaxH + nodeSpacingY;
                    rowMaxH = 0;
                } else {
                    gridX += n.width + nodeSpacingX;
                }
            }
        }
    }

    // ===== Drawing =====
    public void drawDiagram(int mouseX, int mouseY) {
        // Note: We removed the hasSubGui() check here. The diagram should continue
        // drawing even when a SubGui is open - the SubGui renders on top via z-order.
        // Previously this caused the entire diagram to disappear when opening dialogs.

        if (isWithin(mouseX, mouseY)) {
            handleMouseScroll(Mouse.getDWheel());
        }

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int factor = sr.getScaleFactor();

        Map<Integer, AdvancedNode> nodeMap = getNodes();
        List<AdvancedConnection> conns = getConnections();

        // Cache transform center
        cCenterX = x + diagramWidth / 2;
        cCenterY = y + diagramHeight / 2;

        // Update hover using world-space mouse
        float emx = ((float) mouseX - (cCenterX + panX)) / zoom + cCenterX;
        float emy = ((float) mouseY - (cCenterY + panY)) / zoom + cCenterY;
        hoveredNodeId = -1;
        if (isWithin(mouseX, mouseY)) {
            for (AdvancedNode node : nodeMap.values()) {
                if (emx >= node.x && emx <= node.x + node.width &&
                    emy >= node.y && emy <= node.y + node.height) {
                    hoveredNodeId = node.id;
                    break;
                }
            }
        }

        // Scissor clip
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * factor, (sr.getScaledHeight() - (y + diagramHeight)) * factor,
            diagramWidth * factor, diagramHeight * factor);

        // Background
        drawRect(x, y, x + diagramWidth, y + diagramHeight, 0xFF1A1A2E);

        // ===== PASS 1: Geometry in zoom-transform =====
        GL11.glPushMatrix();
        GL11.glTranslatef(cCenterX + panX, cCenterY + panY, 0);
        GL11.glScalef(zoom, zoom, 1.0f);
        GL11.glTranslatef(-cCenterX, -cCenterY, 0);

        // Highlight set
        Set<AdvancedConnection> highlightedConns = new HashSet<>();
        if (hoveredNodeId != -1) {
            for (AdvancedConnection conn : conns) {
                if (conn.sourceNodeId == hoveredNodeId || conn.targetNodeId == hoveredNodeId) {
                    highlightedConns.add(conn);
                }
            }
        }

        // Draw connections
        for (AdvancedConnection conn : conns) {
            AdvancedNode source = nodeMap.get(conn.sourceNodeId);
            AdvancedNode target = nodeMap.get(conn.targetNodeId);
            if (source == null || target == null) continue;
            boolean highlight = highlightedConns.contains(conn);
            drawConnection(source, conn.sourceSlot, target, conn.color, highlight);
        }

        // Draw node geometry (rectangles, borders, circles) and buffer text
        pendingTexts.clear();
        for (AdvancedNode node : nodeMap.values()) {
            drawNodeGeometry(node);
        }

        GL11.glPopMatrix();

        // ===== PASS 2: Text rendered at base size with GL scaling =====
        // Using a single font size with GL scaling ensures text stays proportionally
        // positioned within boxes at all zoom levels
        if (zoom >= 0.35f) {
            for (PendingText pt : pendingTexts) {
                float sx = toScreenX(pt.worldX);
                float sy = toScreenY(pt.worldY);

                // Cull text outside visible area
                if (sx > x + diagramWidth + 50 || sx < x - 200) continue;
                if (sy > y + diagramHeight + 20 || sy < y - 20) continue;

                GL11.glPushMatrix();
                GL11.glTranslatef(sx, sy, 0);
                GL11.glScalef(zoom, zoom, 1.0f);

                if (pt.centered) {
                    String trimmed = fontTrimToWidth(pt.text, pt.maxWidth);
                    int tw = fontWidth(trimmed);
                    fontDraw(trimmed, -tw / 2, 0, pt.color);
                } else {
                    String trimmed = fontTrimToWidth(pt.text, pt.maxWidth);
                    fontDraw(trimmed, 0, 0, pt.color);
                }

                GL11.glPopMatrix();
            }
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    // ===== Node Geometry (rectangles, borders, circles only - no text) =====
    protected void drawNodeGeometry(AdvancedNode node) {
        int nx = node.x;
        int ny = node.y;
        int nw = node.width;
        int nh = node.height;

        // Node background
        drawRect(nx, ny, nx + nw, ny + nh, 0xE0202020);

        // Header bar
        drawRect(nx, ny, nx + nw, ny + nodeHeaderHeight, node.headerColor);

        // Border
        int borderColor;
        if (node.id == selectedNodeId) {
            borderColor = 0xFFFFFFFF;
        } else if (node.id == hoveredNodeId) {
            borderColor = 0xFFCCCCCC;
        } else {
            borderColor = 0xFF444444;
        }
        drawHorizontalLine(nx, nx + nw - 1, ny, borderColor);
        drawHorizontalLine(nx, nx + nw - 1, ny + nh - 1, borderColor);
        drawVerticalLine(nx, ny, ny + nh - 1, borderColor);
        drawVerticalLine(nx + nw - 1, ny, ny + nh - 1, borderColor);

        // Buffer title text (centered in header)
        float titleCenterX = nx + nw / 2.0f;
        bufferText(titleCenterX, ny + 2, node.title, 0xFFFFFFFF, true, nw - nodePadding * 2);

        int yPos = ny + nodeHeaderHeight;

        // Buffer subtitle
        if (node.subtitle != null) {
            bufferText(nx + nodePadding, yPos + 1, node.subtitle, 0xFF999999, false, nw - nodePadding * 2);
            yPos += slotHeight;
        }

        // Buffer slots + draw circles
        for (NodeSlot slot : node.slots) {
            bufferText(nx + nodePadding, yPos + 1, slot.label, slot.color, false,
                nw - nodePadding * 2 - connectionPointRadius * 2 - 6);

            if (slot.targetNodeId >= 0) {
                int cpx = nx + nw - connectionPointRadius - 2;
                int cpy = yPos + slotHeight / 2;
                drawCircle(cpx, cpy, connectionPointRadius, slot.color);
            }

            yPos += slotHeight;
        }
    }

    // ===== Connection Rendering (Cubic Bezier) =====
    protected void drawConnection(AdvancedNode source, int slotIndex, AdvancedNode target, int color, boolean highlight) {
        int sx = source.x + source.width;
        int sy = source.y + nodeHeaderHeight + (source.subtitle != null ? slotHeight : 0)
            + slotIndex * slotHeight + slotHeight / 2;

        int tx = target.x;
        int ty = target.y + target.height / 2;

        int dx = Math.abs(tx - sx);
        int offset = Math.max(40, dx / 3);
        int cx1 = sx + offset;
        int cy1 = sy;
        int cx2 = tx - offset;
        int cy2 = ty;

        float r, g, b, a;
        if (highlight) {
            r = 1.0f; g = 1.0f; b = 0.0f; a = 1.0f;
        } else {
            r = ((color >> 16) & 0xFF) / 255f;
            g = ((color >> 8) & 0xFF) / 255f;
            b = (color & 0xFF) / 255f;
            a = 0.85f;
        }

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_HINT_BIT);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(highlight ? 3.0f : 2.0f);
        GL11.glColor4f(r, g, b, a);

        int segments = 60;
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i = 0; i <= segments; i++) {
            double t = (double) i / segments;
            double mt = 1.0 - t;
            double bx = mt * mt * mt * sx + 3 * mt * mt * t * cx1 + 3 * mt * t * t * cx2 + t * t * t * tx;
            double by = mt * mt * mt * sy + 3 * mt * mt * t * cy1 + 3 * mt * t * t * cy2 + t * t * t * ty;
            GL11.glVertex2d(bx, by);
        }
        GL11.glEnd();

        // Arrow head
        double tangentX = 3 * (tx - cx2);
        double tangentY = 3 * (ty - cy2);
        double len = Math.sqrt(tangentX * tangentX + tangentY * tangentY);
        if (len > 0) {
            tangentX /= len;
            tangentY /= len;
        }

        float arrowSize = 6;
        float ax1 = tx - arrowSize * (float)(tangentX - tangentY * 0.5);
        float ay1 = ty - arrowSize * (float)(tangentY + tangentX * 0.5);
        float ax2 = tx - arrowSize * (float)(tangentX + tangentY * 0.5);
        float ay2 = ty - arrowSize * (float)(tangentY - tangentX * 0.5);

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2f(tx, ty);
        GL11.glVertex2f(ax1, ay1);
        GL11.glVertex2f(ax2, ay2);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_CULL_FACE);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopAttrib();
    }

    // ===== Drawing Helpers =====

    protected void drawCircle(int cx, int cy, int radius, int color) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(r, g, b, 1.0f);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2i(cx, cy);
        int segs = 12;
        for (int i = 0; i <= segs; i++) {
            double angle = 2.0 * Math.PI * i / segs;
            GL11.glVertex2d(cx + radius * Math.cos(angle), cy + radius * Math.sin(angle));
        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopAttrib();
    }

    protected String trimToWidth(String text, int maxWidth) {
        if (fontWidth(text) <= maxWidth) return text;
        String trimmed = text;
        while (trimmed.length() > 1 && fontWidth(trimmed + "..") > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed + "..";
    }

    // ===== Mouse Handling =====

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (parent != null && parent.hasSubGui()) return false;
        if (!isWithin(mouseX, mouseY)) return false;

        if (mouseButton == 0) {
            float emx = ((float) mouseX - (cCenterX + panX)) / zoom + cCenterX;
            float emy = ((float) mouseY - (cCenterY + panY)) / zoom + cCenterY;

            for (AdvancedNode node : getNodes().values()) {
                if (emx >= node.x && emx <= node.x + node.width &&
                    emy >= node.y && emy <= node.y + node.height) {

                    long now = System.currentTimeMillis();
                    if (node.id == lastClickedNodeId && (now - lastClickTime) < DOUBLE_CLICK_MS) {
                        onNodeDoubleClick(node);
                        lastClickedNodeId = -1;
                        lastClickTime = 0;
                    } else {
                        selectedNodeId = node.id;
                        onNodeClick(node);
                        lastClickedNodeId = node.id;
                        lastClickTime = now;
                    }
                    return true;
                }
            }

            dragging = true;
            lastDragX = mouseX;
            lastDragY = mouseY;
            return true;
        }
        return false;
    }

    public void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        if (parent != null && parent.hasSubGui()) return;
        if (dragging) {
            int dx = mouseX - lastDragX;
            int dy = mouseY - lastDragY;
            panX += dx;
            panY += dy;
            lastDragX = mouseX;
            lastDragY = mouseY;
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        dragging = false;
    }

    public void handleMouseScroll(int scrollDelta) {
        if (scrollDelta == 0) return;
        zoom += scrollDelta * 0.0009f;
        if (zoom < MIN_ZOOM) zoom = MIN_ZOOM;
        if (zoom > MAX_ZOOM) zoom = MAX_ZOOM;
    }

    public boolean isWithin(int mouseX, int mouseY) {
        if (parent != null && parent.hasSubGui()) return false;
        return mouseX >= x && mouseX < x + diagramWidth && mouseY >= y && mouseY < y + diagramHeight;
    }

    // ===== Public API =====

    public void setSelectedNode(int id) {
        this.selectedNodeId = id;
    }

    public int getSelectedNodeId() {
        return selectedNodeId;
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.diagramWidth = width;
        this.diagramHeight = height;
    }

    // ===== Pan/Zoom Accessors =====
    public float getPanX() { return panX; }
    public float getPanY() { return panY; }
    public float getZoom() { return zoom; }

    public void setPanX(float px) { this.panX = px; }
    public void setPanY(float py) { this.panY = py; }
    public void setZoom(float z) { this.zoom = z; }
}
