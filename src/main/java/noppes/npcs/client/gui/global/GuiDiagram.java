package noppes.npcs.client.gui.global;

import java.awt.Point;
import java.util.*;

import noppes.npcs.client.gui.util.GuiNPCInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/**
 * Abstract GUI class for drawing diagrams that consist of icons (nodes) and connections (arrows).
 * <p>
 * This version adds:
 *  - A parent GuiNPCInterface (passed in the constructor) so that if the parent has a subgui, no
 *    mouse interactions are processed.
 *  - Caching for icons and connections. The abstract methods createIcons() and createConnections() are
 *    called only once (or when cache is invalidated).
 *  - DiagramIcon now has fields for enabled and pressable. Disabled icons are not rendered or used in
 *    connection validation.
 *  - Basic mouse-event callbacks for pressable icons: click, held and released.
 */
public abstract class GuiDiagram extends Gui {

    // ---------------
    // Diagram Area & Controls
    // ---------------
    protected int x, y, width, height;    // The drawing area.
    protected float panX = 0, panY = 0;     // Panning offsets.
    protected float zoom = 1.0f;            // Zoom factor.
    protected boolean dragging = false;     // Drag flag.
    protected int lastDragX, lastDragY;     // Last mouse positions.

    // ---------------
    // Node (Icon) Sizing
    // ---------------
    protected int iconSize = 16;            // Icon image size (square).
    protected int slotPadding = 4;          // Extra padding for the node's box.
    protected int slotSize = iconSize + slotPadding; // Overall size of the node box.

    // ---------------
    // Node Box Appearance
    // ---------------
    protected int iconBackgroundColor = 0xFF999999; // Background color.
    protected int iconBorderColor = 0xFF555555;       // Border color.
    protected int iconBorderThickness = 1;            // Border thickness in pixels.

    // ---------------
    // Connection Options
    // ---------------
    protected boolean showArrowHeads = true;  // Whether to draw arrow heads.
    protected boolean useColorScaling = true; // Whether to scale connection color based on a percentage.

    // ---------------
    // Layout Options
    // ---------------
    public enum DiagramLayout { CIRCULAR, SQUARE, TREE, GENERATED }
    protected DiagramLayout layout = DiagramLayout.CIRCULAR; // Default layout.

    /**
     * Allows setting the desired layout type.
     * This will invalidate the cached positions and cached icons/connections.
     */
    public void setLayout(DiagramLayout layout) {
        this.layout = layout;
        invalidateCache();
    }

    // ---------------
    // Caching
    // ---------------
    // Cache for positions (computed once for any given icon set & layout).
    protected Map<Integer, Point> cachedPositions = null;
    // Cache for icons and connections.
    protected List<DiagramIcon> iconsCache = null;
    protected List<DiagramConnection> connectionsCache = null;

    /**
     * Clears all caches: positions, icons, and connections.
     */
    public void invalidateCache() {
        cachedPositions = null;
        iconsCache = null;
        connectionsCache = null;
    }

    // ---------------
    // Parent Reference
    // ---------------
    protected GuiNPCInterface parent;

    // ---------------
    // Constructor
    // ---------------
    public GuiDiagram(GuiNPCInterface parent, int x, int y, int width, int height) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // ---------------
    // Data Providers (subclasses must implement the creation methods)
    // ---------------
    /**
     * Subclasses should override this method to create and return the list of DiagramIcons.
     * Note: the returned icons will be cached.
     */
    protected abstract List<DiagramIcon> createIcons();

    /**
     * Subclasses should override this method to create and return the list of DiagramConnections.
     * Note: the returned connections will be cached.
     */
    protected abstract List<DiagramConnection> createConnections();

    /**
     * Returns the cached icons.
     */
    protected final List<DiagramIcon> getIcons() {
        if (iconsCache == null) {
            iconsCache = createIcons();
        }
        return iconsCache;
    }

    /**
     * Returns the cached connections.
     */
    protected final List<DiagramConnection> getConnections() {
        if (connectionsCache == null) {
            connectionsCache = createConnections();
        }
        return connectionsCache;
    }

    /**
     * Subclasses must implement how an icon’s image is rendered.
     */
    protected abstract void renderIcon(DiagramIcon icon, int posX, int posY, IconRenderState state);

    /**
     * Return a tooltip for an icon.
     */
    protected List<String> getIconTooltip(DiagramIcon icon) { return null; }

    protected List<String> getConnectionTooltip(DiagramConnection conn) {
        List<String> tooltip = new ArrayList<>();
        DiagramIcon iconFrom = getIconById(conn.idFrom);
        DiagramIcon iconTo = getIconById(conn.idTo);
        String nameFrom = iconFrom != null ? getIconName(iconFrom) : "Unknown";
        String nameTo = iconTo != null ? getIconName(iconTo) : "Unknown";
        tooltip.add(nameFrom + " > " + nameTo + ":");
        tooltip.add(conn.hoverText);
        DiagramConnection reverse = getConnectionByIds(conn.idTo, conn.idFrom);
        if (reverse != null) {
            tooltip.add(nameTo + " > " + nameFrom + ":");
            tooltip.add(reverse.hoverText);
        }
        return tooltip;
    }

    protected String getIconName(DiagramIcon icon) { return "Icon " + icon.id; }

    // ---------------
    // Layout Calculation – choose algorithm based on layout type.
    // ---------------
    /**
     * Returns a mapping from each icon's id to its (x,y) center position.
     * Uses caching so that positions are computed only once (unless invalidated).
     */
    protected Map<Integer, Point> calculatePositions() {
        if (cachedPositions != null) {
            return cachedPositions;
        }
        switch (layout) {
            case CIRCULAR:
                cachedPositions = calculateCircularPositions();
                break;
            case SQUARE:
                cachedPositions = calculateSquarePositions();
                break;
            case TREE:
                cachedPositions = calculateTreePositions();
                break;
            case GENERATED:
                cachedPositions = calculateGeneratedPositions();
                break;
            default:
                cachedPositions = calculateCircularPositions();
                break;
        }
        return cachedPositions;
    }

    protected Map<Integer, Point> calculateCircularPositions() {
        List<DiagramIcon> icons = getIcons();
        Map<Integer, Point> positions = new HashMap<>();
        int count = icons.size();
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        int radius = Math.min(width, height) / 2 - iconSize - 10;
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            int posX = centerX + (int)(radius * Math.cos(angle));
            int posY = centerY + (int)(radius * Math.sin(angle));
            positions.put(icons.get(i).id, new Point(posX, posY));
        }
        return positions;
    }

    protected Map<Integer, Point> calculateSquarePositions() {
        List<DiagramIcon> icons = getIcons();
        Map<Integer, Point> positions = new HashMap<>();
        int count = icons.size();
        int n = (int)Math.ceil(Math.sqrt(count));
        int gridWidth = width - 20;
        int gridHeight = height - 20;
        int cellWidth = gridWidth / n;
        int cellHeight = gridHeight / n;
        int startX = x + (width - gridWidth) / 2;
        int startY = y + (height - gridHeight) / 2;
        for (int i = 0; i < count; i++) {
            int row = i / n;
            int col = i % n;
            int posX = startX + col * cellWidth + cellWidth / 2;
            int posY = startY + row * cellHeight + cellHeight / 2;
            positions.put(icons.get(i).id, new Point(posX, posY));
        }
        return positions;
    }

    protected Map<Integer, Point> calculateTreePositions() {
        List<DiagramIcon> icons = getIcons();
        Map<Integer, Point> positions = new HashMap<>();
        int count = icons.size();
        int levels = (int)Math.ceil(Math.log(count + 1) / Math.log(2));
        int levelHeight = height / (levels + 1);
        int index = 0;
        for (int level = 0; level < levels && index < count; level++) {
            int nodesThisLevel = (int)Math.min(Math.pow(2, level), count - index);
            int levelWidth = width - 20;
            int cellWidth = levelWidth / nodesThisLevel;
            for (int i = 0; i < nodesThisLevel && index < count; i++, index++) {
                int posX = x + 10 + i * cellWidth + cellWidth / 2;
                int posY = y + (level + 1) * levelHeight;
                positions.put(icons.get(index).id, new Point(posX, posY));
            }
        }
        return positions;
    }

    /**
     * Generated layout: uses a force-directed algorithm to position nodes.
     * Computed only once using a fixed seed for determinism.
     */
    protected Map<Integer, Point> calculateGeneratedPositions() {
        List<DiagramIcon> icons = getIcons();
        List<DiagramConnection> connections = getConnections();

        // Build a complete undirected graph from icons & connections.
        Map<Integer, List<Integer>> graph = buildGraph(icons, connections);

        // Partition the graph into clusters (connected components).
        List<Set<Integer>> clusters = getConnectedComponents(graph);

        // Arrange clusters into a global layout. We’ll place each cluster
        // in its own bounding box on a grid.
        int clusterCount = clusters.size();
        int gridCols = (int) Math.ceil(Math.sqrt(clusterCount));
        int gridRows = (int) Math.ceil((double) clusterCount / gridCols);
        int clusterMargin = 20;
        int compWidth = (width - (gridCols + 1) * clusterMargin) / gridCols;
        int compHeight = (height - (gridRows + 1) * clusterMargin) / gridRows;

        Map<Integer, Point> globalPositions = new HashMap<>();
        int clusterIndex = 0;
        for (Set<Integer> cluster : clusters) {
            int col = clusterIndex % gridCols;
            int row = clusterIndex / gridCols;
            int compX = x + clusterMargin + col * (compWidth + clusterMargin);
            int compY = y + clusterMargin + row * (compHeight + clusterMargin);

            // Look for a pattern in the cluster.
            Map<Integer, Point> compPositions;
            if (isCycle(cluster, graph)) {
                // Layout nodes evenly on a circle.
                compPositions = layoutCycle(cluster, compX, compY, compWidth, compHeight);
            } else if (isTree(cluster, graph)) {
                // Layout nodes in a tree, minimizing edge crossings.
                compPositions = layoutTree(cluster, compX, compY, compWidth, compHeight, graph);
            } else if (isSquare(cluster)) {
                // If the number of nodes is a perfect square (or near enough)
                // you might try a grid layout.
                compPositions = layoutSquare(cluster, compX, compY, compWidth, compHeight);
            } else {
                // Otherwise, use a force-directed layout optimized for minimal overlap.
                compPositions = layoutForceDirected(cluster, compX, compY, compWidth, compHeight, connections);
            }
            globalPositions.putAll(compPositions);
            clusterIndex++;
        }

        return globalPositions;
    }

    // Build an undirected graph from icons and connections.
    private Map<Integer, List<Integer>> buildGraph(List<DiagramIcon> icons, List<DiagramConnection> connections) {
        Map<Integer, List<Integer>> graph = new HashMap<>();
        for (DiagramIcon icon : icons) {
            graph.put(icon.id, new ArrayList<>());
        }
        for (DiagramConnection conn : connections) {
            if (graph.containsKey(conn.idFrom) && graph.containsKey(conn.idTo)) {
                graph.get(conn.idFrom).add(conn.idTo);
                graph.get(conn.idTo).add(conn.idFrom);
            }
        }
        return graph;
    }

    // Extract connected components using DFS.
    private List<Set<Integer>> getConnectedComponents(Map<Integer, List<Integer>> graph) {
        Set<Integer> visited = new HashSet<>();
        List<Set<Integer>> components = new ArrayList<>();
        for (Integer node : graph.keySet()) {
            if (!visited.contains(node)) {
                Set<Integer> comp = new HashSet<>();
                dfsComponent(node, graph, visited, comp);
                components.add(comp);
            }
        }
        return components;
    }

    private void dfsComponent(Integer current, Map<Integer, List<Integer>> graph,
                              Set<Integer> visited, Set<Integer> comp) {
        visited.add(current);
        comp.add(current);
        for (Integer neighbor : graph.get(current)) {
            if (!visited.contains(neighbor)) {
                dfsComponent(neighbor, graph, visited, comp);
            }
        }
    }

    // Pattern detection helpers:

    // A cluster is considered a cycle if each node has exactly 2 neighbors (within the cluster)
    // and there are at least 3 nodes.
    private boolean isCycle(Set<Integer> cluster, Map<Integer, List<Integer>> graph) {
        if (cluster.size() < 3) return false;
        for (Integer node : cluster) {
            int degree = 0;
            for (Integer neighbor : graph.get(node)) {
                if (cluster.contains(neighbor)) degree++;
            }
            if (degree != 2) return false;
        }
        return true;
    }

    // A cluster is considered a tree if the number of edges equals nodes-1.
    private boolean isTree(Set<Integer> cluster, Map<Integer, List<Integer>> graph) {
        int edgeCount = 0;
        for (Integer node : cluster) {
            for (Integer neighbor : graph.get(node)) {
                if (cluster.contains(neighbor)) edgeCount++;
            }
        }
        edgeCount /= 2; // each edge counted twice.
        return edgeCount == cluster.size() - 1;
    }

    // A simple heuristic: if the cluster size is a perfect square or nearly so, treat it as a square.
    private boolean isSquare(Set<Integer> cluster) {
        int n = cluster.size();
        int sqrt = (int) Math.round(Math.sqrt(n));
        return sqrt * sqrt == n;
    }

// Layout algorithms:

    // Circular layout for cycles.
    private Map<Integer, Point> layoutCycle(Set<Integer> cluster, int compX, int compY, int compWidth, int compHeight) {
        Map<Integer, Point> positions = new HashMap<>();
        int centerX = compX + compWidth / 2;
        int centerY = compY + compHeight / 2;
        int radius = Math.min(compWidth, compHeight) / 3;
        int i = 0;
        for (Integer id : cluster) {
            double angle = 2 * Math.PI * i / cluster.size();
            int posX = centerX + (int)(radius * Math.cos(angle));
            int posY = centerY + (int)(radius * Math.sin(angle));
            positions.put(id, new Point(posX, posY));
            i++;
        }
        return positions;
    }

    // A simple top-down tree layout using BFS.
    private Map<Integer, Point> layoutTree(Set<Integer> cluster, int compX, int compY, int compWidth, int compHeight,
                                           Map<Integer, List<Integer>> fullGraph) {
        Map<Integer, Point> positions = new HashMap<>();

        // Build subgraph for the cluster.
        Map<Integer, List<Integer>> subgraph = new HashMap<>();
        for (Integer id : cluster) {
            List<Integer> neighbors = new ArrayList<>();
            for (Integer neighbor : fullGraph.get(id)) {
                if (cluster.contains(neighbor))
                    neighbors.add(neighbor);
            }
            subgraph.put(id, neighbors);
        }

        // Choose a root (for example, the node with the smallest id).
        Integer root = cluster.iterator().next();
        for (Integer id : cluster) {
            if (id < root)
                root = id;
        }

        // BFS to determine levels.
        Map<Integer, Integer> levelMap = new HashMap<>();
        Map<Integer, List<Integer>> levels = new HashMap<>();
        Queue<Integer> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();
        queue.add(root);
        visited.add(root);
        levelMap.put(root, 0);

        while (!queue.isEmpty()) {
            Integer curr = queue.poll();
            int lvl = levelMap.get(curr);
            levels.computeIfAbsent(lvl, k -> new ArrayList<>()).add(curr);
            for (Integer neighbor : subgraph.get(curr)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    levelMap.put(neighbor, lvl + 1);
                    queue.add(neighbor);
                }
            }
        }

        // Calculate vertical spacing.
        int maxLevel = levels.keySet().stream().max(Integer::compare).orElse(0);
        double levelHeight = compHeight / (maxLevel + 1.0);

        // For each level, assign horizontal positions evenly.
        for (Map.Entry<Integer, List<Integer>> entry : levels.entrySet()) {
            int lvl = entry.getKey();
            List<Integer> nodesAtLevel = entry.getValue();
            double horizontalSpacing = compWidth / (nodesAtLevel.size() + 1.0);
            for (int i = 0; i < nodesAtLevel.size(); i++) {
                int id = nodesAtLevel.get(i);
                int posX = compX + (int)((i + 1) * horizontalSpacing);
                int posY = compY + (int)(lvl * levelHeight + levelHeight / 2);
                positions.put(id, new Point(posX, posY));
            }
        }

        return positions;
    }

    // Grid layout for square-like clusters.
    private Map<Integer, Point> layoutSquare(Set<Integer> cluster, int compX, int compY, int compWidth, int compHeight) {
        Map<Integer, Point> positions = new HashMap<>();
        int n = (int) Math.round(Math.sqrt(cluster.size()));
        int gridCellWidth = compWidth / n;
        int gridCellHeight = compHeight / n;
        int index = 0;
        List<Integer> nodes = new ArrayList<>(cluster);
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                if (index >= nodes.size()) break;
                int posX = compX + col * gridCellWidth + gridCellWidth / 2;
                int posY = compY + row * gridCellHeight + gridCellHeight / 2;
                positions.put(nodes.get(index), new Point(posX, posY));
                index++;
            }
        }
        return positions;
    }

    // Force-directed layout for generic clusters.
    private Map<Integer, Point> layoutForceDirected(Set<Integer> cluster, int compX, int compY, int compWidth, int compHeight,
                                                    List<DiagramConnection> connections) {
        Map<Integer, Point> positions = new HashMap<>();
        Random rand = new Random(100);
        for (Integer id : cluster) {
            int posX = compX + rand.nextInt(compWidth);
            int posY = compY + rand.nextInt(compHeight);
            positions.put(id, new Point(posX, posY));
        }

        double area = compWidth * compHeight;
        double k = Math.sqrt(area / cluster.size());
        int iterations = 100;
        double temperature = compWidth / 10.0;
        double cooling = temperature / (iterations + 1);
        double minDistance = iconSize + slotPadding;

        // Consider only connections within the cluster.
        List<DiagramConnection> compConnections = new ArrayList<>();
        for (DiagramConnection conn : connections) {
            if (cluster.contains(conn.idFrom) && cluster.contains(conn.idTo)) {
                compConnections.add(conn);
            }
        }

        for (int iter = 0; iter < iterations; iter++) {
            Map<Integer, double[]> disp = new HashMap<>();
            for (Integer id : cluster) {
                disp.put(id, new double[] {0.0, 0.0});
            }

            // Repulsive forces.
            List<Integer> nodes = new ArrayList<>(cluster);
            for (int i = 0; i < nodes.size(); i++) {
                Integer v = nodes.get(i);
                Point posV = positions.get(v);
                for (int j = i + 1; j < nodes.size(); j++) {
                    Integer u = nodes.get(j);
                    Point posU = positions.get(u);
                    double dx = posV.x - posU.x;
                    double dy = posV.y - posU.y;
                    double distance = Math.sqrt(dx * dx + dy * dy);
                    if (distance < minDistance) distance = minDistance;
                    double repForce = (k * k) / distance;
                    double[] dispV = disp.get(v);
                    double[] dispU = disp.get(u);
                    dispV[0] += (dx / distance) * repForce;
                    dispV[1] += (dy / distance) * repForce;
                    dispU[0] -= (dx / distance) * repForce;
                    dispU[1] -= (dy / distance) * repForce;
                }
            }

            // Attractive forces.
            for (DiagramConnection conn : compConnections) {
                Point posV = positions.get(conn.idFrom);
                Point posU = positions.get(conn.idTo);
                double dx = posV.x - posU.x;
                double dy = posV.y - posU.y;
                double distance = Math.sqrt(dx * dx + dy * dy);
                if (distance < 0.01) distance = 0.01;
                double attrForce = (distance * distance) / k;
                double[] dispV = disp.get(conn.idFrom);
                double[] dispU = disp.get(conn.idTo);
                dispV[0] -= (dx / distance) * attrForce;
                dispV[1] -= (dy / distance) * attrForce;
                dispU[0] += (dx / distance) * attrForce;
                dispU[1] += (dy / distance) * attrForce;
            }

            // Update positions.
            for (Integer id : cluster) {
                double[] d = disp.get(id);
                double dispLength = Math.sqrt(d[0] * d[0] + d[1] * d[1]);
                if (dispLength < 0.01) dispLength = 0.01;
                int newX = positions.get(id).x + (int)((d[0] / dispLength) * Math.min(dispLength, temperature));
                int newY = positions.get(id).y + (int)((d[1] / dispLength) * Math.min(dispLength, temperature));
                newX = Math.max(compX, Math.min(compX + compWidth, newX));
                newY = Math.max(compY, Math.min(compY + compHeight, newY));
                positions.get(id).x = newX;
                positions.get(id).y = newY;
            }

            temperature -= cooling;
        }

        return positions;
    }


    // ---------------
    // Standard Icon Box Drawing (Background + Border)
    // ---------------
    protected void drawIconBox(int slotX, int slotY, int slotSize, boolean highlighted) {
        int bg = highlighted ? mixColors(iconBackgroundColor, 0xFFFFFFFF, 0.2f) : iconBackgroundColor;
        drawRect(slotX, slotY, slotX + slotSize, slotY + slotSize, bg);
        for (int i = 0; i < iconBorderThickness; i++) {
            drawHorizontalLine(slotX + i, slotX + slotSize - i, slotY + i, iconBorderColor);
            drawHorizontalLine(slotX + i, slotX + slotSize - i, slotY + slotSize - 1 - i, iconBorderColor);
            drawVerticalLine(slotX + i, slotY + i, slotY + slotSize - i, iconBorderColor);
            drawVerticalLine(slotX + slotSize - 1 - i, slotY + i, slotY + slotSize - i, iconBorderColor);
        }
    }

    protected int mixColors(int color1, int color2, float ratio) {
        int a1 = (color1 >> 24) & 0xFF, r1 = (color1 >> 16) & 0xFF, g1 = (color1 >> 8) & 0xFF, b1 = color1 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF, r2 = (color2 >> 16) & 0xFF, g2 = (color2 >> 8) & 0xFF, b2 = color2 & 0xFF;
        int a = (int)(a1 + (a2 - a1) * ratio);
        int r = (int)(r1 + (r2 - r1) * ratio);
        int g = (int)(g1 + (g2 - g1) * ratio);
        int b = (int)(b1 + (b2 - b1) * ratio);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    // ---------------
    // Main Draw Method
    // ---------------
    public void drawDiagram(int mouseX, int mouseY, boolean subGui) {
        // Only allow mouse interaction if parent's not showing a subgui.
        boolean allowInput = (parent == null || !parent.hasSubGui());
        // Update zoom based on mouse scroll if allowed.
        if (allowInput && isWithin(mouseX, mouseY) && !subGui) {
            handleMouseScroll(Mouse.getDWheel());
        }

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int factor = sr.getScaleFactor();

        Map<Integer, Point> positions = calculatePositions();
        int centerX = x + width / 2;
        int centerY = y + height / 2;

        int effectiveMouseX = (int)(((float)mouseX - (centerX + panX)) / zoom + centerX);
        int effectiveMouseY = (int)(((float)mouseY - (centerY + panY)) / zoom + centerY);

        Integer hoveredIconId = null;
        int hoveredConnFrom = -1, hoveredConnTo = -1;
        Set<Integer> selectedIconIds = new HashSet<>();

        // Hit testing for icons (only consider enabled icons)
        for (DiagramIcon icon : getIcons()) {
            if (!icon.enabled) continue;
            Point pos = positions.get(icon.id);
            if (pos == null) continue;
            int slotX = pos.x - slotSize / 2;
            int slotY = pos.y - slotSize / 2;
            if (effectiveMouseX >= slotX && effectiveMouseX < slotX + slotSize &&
                effectiveMouseY >= slotY && effectiveMouseY < slotY + slotSize) {
                hoveredIconId = icon.id;
                selectedIconIds.add(icon.id);
                break;
            }
        }
        if (hoveredIconId != null) {
            for (DiagramConnection conn : getConnections()) {
                // Validate connection: both endpoints must exist and be enabled.
                DiagramIcon iconFrom = getIconById(conn.idFrom);
                DiagramIcon iconTo = getIconById(conn.idTo);
                if (iconFrom == null || iconTo == null || !iconFrom.enabled || !iconTo.enabled)
                    continue;
                if (conn.idFrom == hoveredIconId || conn.idTo == hoveredIconId) {
                    selectedIconIds.add(conn.idFrom);
                    selectedIconIds.add(conn.idTo);
                }
            }
        } else {
            final double threshold = 5.0;
            outer:
            for (DiagramConnection conn : getConnections()) {
                DiagramIcon iconFrom = getIconById(conn.idFrom);
                DiagramIcon iconTo = getIconById(conn.idTo);
                if (iconFrom == null || iconTo == null || !iconFrom.enabled || !iconTo.enabled)
                    continue;
                Point pFrom = positions.get(conn.idFrom);
                Point pTo = positions.get(conn.idTo);
                if (pFrom == null || pTo == null) continue;
                double dist = pointLineDistance(effectiveMouseX, effectiveMouseY, pFrom.x, pFrom.y, pTo.x, pTo.y);
                if (dist < threshold) {
                    hoveredConnFrom = conn.idFrom;
                    hoveredConnTo = conn.idTo;
                    selectedIconIds.add(conn.idFrom);
                    selectedIconIds.add(conn.idTo);
                    break outer;
                }
            }
        }

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * factor, (sr.getScaledHeight() - (y + height)) * factor, width * factor, height * factor);
        drawRect(x, y, x + width, y + height, 0xFF333333);

        GL11.glPushMatrix();
        GL11.glTranslatef(centerX + panX, centerY + panY, 0);
        GL11.glScalef(zoom, zoom, 1.0f);
        GL11.glTranslatef(-centerX, -centerY, 0);

        // Draw connections (only for endpoints that are enabled).
        for (DiagramConnection conn : getConnections()) {
            DiagramIcon iconFrom = getIconById(conn.idFrom);
            DiagramIcon iconTo = getIconById(conn.idTo);
            if (iconFrom == null || iconTo == null || !iconFrom.enabled || !iconTo.enabled)
                continue;
            Point pFrom = positions.get(conn.idFrom);
            Point pTo = positions.get(conn.idTo);
            if (pFrom == null || pTo == null) continue;
            int ax = pFrom.x;
            int ay = pFrom.y;
            int bx = pTo.x;
            int by = pTo.y;
            boolean dim = !selectedIconIds.isEmpty() &&
                !(selectedIconIds.contains(conn.idFrom) && selectedIconIds.contains(conn.idTo));
            drawConnectionLine(ax, ay, bx, by, conn, dim);
        }

        // Draw icons.
        for (DiagramIcon icon : getIcons()) {
            if (!icon.enabled) continue;
            Point pos = positions.get(icon.id);
            if (pos == null) continue;
            int slotX = pos.x - slotSize / 2;
            int slotY = pos.y - slotSize / 2;
            boolean highlighted = selectedIconIds.isEmpty() || selectedIconIds.contains(icon.id);
            drawIconBox(slotX, slotY, slotSize, highlighted);
            IconRenderState state = highlighted ? IconRenderState.HIGHLIGHTED : IconRenderState.NOT_HIGHLIGHTED;
            renderIcon(icon, pos.x, pos.y, state);
        }
        GL11.glPopMatrix();

        // Draw arrow heads.
        if (showArrowHeads) {
            GL11.glPushMatrix();
            GL11.glTranslatef(centerX + panX, centerY + panY, 0);
            GL11.glScalef(zoom, zoom, 1.0f);
            GL11.glTranslatef(-centerX, -centerY, 0);
            for (DiagramConnection conn : getConnections()) {
                DiagramIcon iconFrom = getIconById(conn.idFrom);
                DiagramIcon iconTo = getIconById(conn.idTo);
                if (iconFrom == null || iconTo == null || !iconFrom.enabled || !iconTo.enabled)
                    continue;
                Point pFrom = positions.get(conn.idFrom);
                Point pTo = positions.get(conn.idTo);
                if (pFrom == null || pTo == null) continue;
                int ax = pFrom.x;
                int ay = pFrom.y;
                int bx = pTo.x;
                int by = pTo.y;
                boolean dim = !selectedIconIds.isEmpty() &&
                    !(selectedIconIds.contains(conn.idFrom) && selectedIconIds.contains(conn.idTo));
                drawArrowHead(ax, ay, bx, by, conn, dim);
            }
            GL11.glPopMatrix();
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Tooltips.
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

    protected DiagramIcon getIconById(int id) {
        for (DiagramIcon icon : getIcons()) {
            if (icon.id == id)
                return icon;
        }
        return null;
    }

    protected DiagramConnection getConnectionByIds(int idFrom, int idTo) {
        for (DiagramConnection conn : getConnections()) {
            if (conn.idFrom == idFrom && conn.idTo == idTo)
                return conn;
        }
        return null;
    }

    protected void drawConnectionLine(int x1, int y1, int x2, int y2, DiagramConnection conn, boolean dim) {
        int color = getConnectionColor(conn);
        if (!useColorScaling) { color = 0xFFFFFFFF; }
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(2.0F);
        float r = ((color >> 16)&0xFF)/255f;
        float g = ((color >> 8)&0xFF)/255f;
        float b = (color&0xFF)/255f;
        if (dim) { r*=0.4f; g*=0.4f; b*=0.4f; }
        GL11.glColor4f(r, g, b, 1f);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x2, y2);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopAttrib();
    }

    protected void drawArrowHead(int x1, int y1, int x2, int y2, DiagramConnection conn, boolean dim) {
        float dx = x2 - x1, dy = y2 - y1;
        float len = (float)Math.sqrt(dx*dx+dy*dy);
        if (len==0)return;
        float ux = dx/len, uy = dy/len;
        float defenderEdgeX = x2 - ux*(slotSize/2f);
        float defenderEdgeY = y2 - uy*(slotSize/2f);
        int arrowSize = 6;
        double angle = Math.atan2(uy,ux);
        float leftX = (float)(defenderEdgeX - arrowSize * Math.cos(angle - Math.PI/6));
        float leftY = (float)(defenderEdgeY - arrowSize * Math.sin(angle - Math.PI/6));
        float rightX = (float)(defenderEdgeX - arrowSize * Math.cos(angle + Math.PI/6));
        float rightY = (float)(defenderEdgeY - arrowSize * Math.sin(angle + Math.PI/6));
        int color = getConnectionColor(conn);
        if (!useColorScaling) { color = 0xFFFFFFFF; }
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        float r = ((color >> 16)&0xFF)/255f;
        float g = ((color >> 8)&0xFF)/255f;
        float b = (color&0xFF)/255f;
        if(dim){ r*=0.4f; g*=0.4f; b*=0.4f; }
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

    protected int getConnectionColor(DiagramConnection conn) {
        float percent = conn.percent;
        if (percent<=0.10f)return 0xFF80FF80;
        else if (percent<=0.40f)return 0xFFCCFF66;
        else if (percent<=0.50f)return 0xFFFFA500;
        else {
            float ratio = (percent-0.50f)/0.50f;
            ratio = Math.min(1f, Math.max(0f, ratio));
            int r1 = 0xFF, g1 = 0xA5, b1 = 0x00;
            int r2 = 0xFF, g2 = 0x00, b2 = 0x00;
            int r = r1 + (int)((r2-r1)*ratio);
            int g = g1 + (int)((g2-g1)*ratio);
            int b = b1 + (int)((b2-b1)*ratio);
            return (0xFF << 24) | (r << 16) | (g << 8) | b;
        }
    }

    private double pointLineDistance(double px, double py, double x1, double y1, double x2, double y2) {
        double A = px - x1, B = py - y1, C = x2 - x1, D = y2 - y1;
        double dot = A * C + B * D;
        double lenSq = C * C + D * D;
        double param = (lenSq!=0)?(dot/lenSq):-1;
        double xx, yy;
        if(param<0){ xx = x1; yy = y1; }
        else if(param>1){ xx = x2; yy = y2; }
        else { xx = x1+param*C; yy = y1+param*D; }
        double dx = px-xx, dy = py-yy;
        return Math.sqrt(dx*dx+dy*dy);
    }

    // ---------------
    // Mouse Handling
    // ---------------
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        // Do not process if parent's subgui is active.
        if (parent != null && parent.hasSubGui()) return false;
        // If left mouse button:
        if (mouseButton == 0) {
            // First, try to dispatch icon events for pressable icons.
            for (DiagramIcon icon : getIcons()) {
                if (!icon.enabled || !icon.pressable)
                    continue;
                Point pos = calculatePositions().get(icon.id);
                if (pos == null) continue;
                int centerX = x + width / 2;
                int centerY = y + height / 2;
                int effectiveX = (int)(((float)mouseX - (centerX + panX)) / zoom + centerX);
                int effectiveY = (int)(((float)mouseY - (centerY + panY)) / zoom + centerY);
                int slotX = pos.x - slotSize / 2;
                int slotY = pos.y - slotSize / 2;
                if (effectiveX >= slotX && effectiveX < slotX + slotSize &&
                    effectiveY >= slotY && effectiveY < slotY + slotSize) {
                    onIconClick(icon);
                    // Mark as pressed for held/release events.
                    currentlyPressedIcon = icon;
                    return true;
                }
            }
            // Otherwise, start dragging for panning.
            if (isWithin(mouseX, mouseY)) {
                dragging = true;
                lastDragX = mouseX;
                lastDragY = mouseY;
                return true;
            }
        }
        return false;
    }

    protected DiagramIcon currentlyPressedIcon = null;

    public void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        if (parent != null && parent.hasSubGui()) return;
        if (dragging) {
            int dx = mouseX - lastDragX;
            int dy = mouseY - lastDragY;
            panX += dx / zoom * 0.7f;
            panY += dy / zoom * 0.7f;
            lastDragX = mouseX;
            lastDragY = mouseY;
        }
        // If an icon is pressed, dispatch held event.
        if (currentlyPressedIcon != null) {
            onIconHeld(currentlyPressedIcon);
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (parent != null && parent.hasSubGui()) return;
        if (currentlyPressedIcon != null) {
            onIconRelease(currentlyPressedIcon);
            currentlyPressedIcon = null;
        }
        dragging = false;
    }

    public boolean isWithin(int mouseX, int mouseY) {
        if(parent.hasSubGui())
            return false;
        return mouseX >= x && mouseX <= x+width && mouseY >= y && mouseY <= y+height;
    }

    /**
     * Handles zoom in/out based on mouse wheel scrolling.
     */
    public void handleMouseScroll(int scrollDelta) {
        zoom += scrollDelta * 0.0009f;
        if(zoom < 0.5f) zoom = 0.5f;
        if(zoom > 2.0f) zoom = 2.0f;
    }

    /**
     * Override this method if you wish to render tooltips differently.
     */
    protected void drawHoveringText(List<String> text, int mouseX, int mouseY, FontRenderer fontRenderer) {
        // Default implementation is empty.
    }

    // ---------------
    // Flag Setters
    // ---------------
    public void setShowArrowHeads(boolean showArrowHeads) {
        this.showArrowHeads = showArrowHeads;
    }

    public void setUseColorScaling(boolean useColorScaling) {
        this.useColorScaling = useColorScaling;
    }

    public enum IconRenderState {
        DEFAULT,
        HIGHLIGHTED,
        NOT_HIGHLIGHTED;
    }

    // ---------------
    // Inner Classes for Data Wrapping
    // ---------------
    public static class DiagramIcon {
        public int id;
        public boolean enabled = true;    // If false, the icon is not rendered.
        public boolean pressable = false; // If true, it will receive mouse events.
        public DiagramIcon(int id) {
            this.id = id;
        }
    }

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

    // ---------------
    // Icon Event Callbacks (override these in subclasses as needed)
    // ---------------
    protected void onIconClick(DiagramIcon icon) {
        // Called when a pressable icon is clicked.
        System.out.println("Icon " + icon.id + " clicked.");
    }

    protected void onIconHeld(DiagramIcon icon) {
        // Called repeatedly if an icon remains pressed.
        // (You can add timing logic to fire this only after a threshold.)
        // For now, we simply print once.
        System.out.println("Icon " + icon.id + " held.");
    }

    protected void onIconRelease(DiagramIcon icon) {
        // Called when a pressable icon is released.
        System.out.println("Icon " + icon.id + " released.");
    }
}
