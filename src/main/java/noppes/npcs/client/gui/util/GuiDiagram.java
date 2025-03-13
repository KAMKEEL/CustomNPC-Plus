package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.entity.RenderItem;
import noppes.npcs.constants.EnumDiagramLayout;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.Queue;
import java.util.*;

/**
 * Abstract GUI class for drawing diagrams with icons (nodes) and connections (arrows).
 *
 * Features include:
 * <ul>
 *   <li>Parent reference to disable mouse interaction when a subGUI is active.</li>
 *   <li>Caching for icons and connections (subclasses must implement createIcons() and createConnections()).</li>
 *   <li>Support for different layout types (circular, square, tree, generated, and manual versions)
 *       plus a CHART layout.</li>
 *   <li>Drawing of connections as either straight or curved arrows (with hit‐detection improved for curves).</li>
 *   <li>Chart layout: a grid with header icons and inner cells that show connection colors.
 *       When hovering over a header or cell, only the corresponding row/column (or cell and its two icons) are highlighted
 *       while all non‑relevant cells are dimmed. Also, cells with no connection are drawn with a darker gray.
 *   </li>
 *   <li>Zooming and panning controls apply to both chart and non‑chart layouts.</li>
 *   <li>Callbacks for icon and connection interactions including click, held, release and hover events.</li>
 * </ul>
 */
public abstract class GuiDiagram extends Gui {

    protected RenderItem renderItem = new RenderItem();

    // --- Diagram Area & Controls ---
    protected int x, y, width, height;         // Drawing area for the diagram
    protected float panX = 0, panY = 0;          // Panning offsets
    protected float zoom = 1.0f;                 // Zoom factor
    protected boolean dragging = false;          // Flag indicating active panning
    protected int lastDragX, lastDragY;          // Last mouse positions used for panning

    // --- Node (Icon) Sizing ---
    protected int iconSize = 16;                 // Icon image size (square)
    protected int slotPadding = 4;               // Padding around icons
    protected int slotSize = iconSize + slotPadding; // Total size of the icon box

    // --- Icon Box Appearance ---
    protected int iconBackgroundColor = 0xFF999999; // Background color for icon boxes
    protected int iconBorderColor = 0xFF555555;       // Border color for icon boxes
    protected int iconBorderThickness = 1;            // Border thickness in pixels

    // --- Connection Options ---
    protected boolean showArrowHeads = true;     // Whether to draw arrow heads
    protected boolean useColorScaling = true;    // Scale connection color based on a percentage

    // --- Layout Options ---
    protected EnumDiagramLayout layout = EnumDiagramLayout.CIRCULAR; // Default layout

    /**
     * Sets the layout type and invalidates caches.
     */
    public void setLayout(EnumDiagramLayout layout) {
        this.layout = layout;
        invalidateCache();
    }

    // --- Arrow Drawing Options ---
    protected boolean curvedArrows = true;   // If false, draw straight connection lines
    protected int curveAngle = 15;             // Curvature angle in degrees

    public void setCurvedArrows(boolean curved) {
        this.curvedArrows = curved;
    }
    public void setCurveAngle(int angle) {
        this.curveAngle = angle;
    }

    // --- Caching for icons and connections ---
    protected Map<Integer, Point> cachedPositions = null;      // Cached positions for nodes (non-CHART)
    protected List<DiagramIcon> iconsCache = null;             // Cached list of icons
    protected List<DiagramConnection> connectionsCache = null; // Cached list of connections

    /**
     * Clears cached layout information.
     */
    public void invalidateCache() {
        cachedPositions = null;
        iconsCache = null;
        connectionsCache = null;
    }

    // --- Parent reference ---
    protected GuiNPCInterface parent;

    // --- Constructor ---
    public GuiDiagram(GuiNPCInterface parent, int x, int y, int width, int height) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // --- Data Providers (Subclasses must implement these) ---
    protected abstract List<DiagramIcon> createIcons();
    protected abstract List<DiagramConnection> createConnections();

    protected final List<DiagramIcon> getIcons() {
        if (iconsCache == null) {
            iconsCache = createIcons();
        }
        return iconsCache;
    }
    protected final List<DiagramConnection> getConnections() {
        if (connectionsCache == null) {
            connectionsCache = createConnections();
        }
        return connectionsCache;
    }

    /**
     * Subclasses must implement how an icon is rendered.
     */
    protected abstract void renderIcon(DiagramIcon icon, int posX, int posY, IconRenderState state);

    /**
     * Returns the tooltip text lines for an icon.
     */
    protected List<String> getIconTooltip(DiagramIcon icon) { return null; }

    /**
     * Returns the tooltip text lines for a connection.
     */
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

    // --- Layout Calculation ---
    /**
     * For non‑CHART layouts, returns the mapping from node ID to (x,y) position.
     * CHART layout is drawn directly.
     */
    protected Map<Integer, Point> calculatePositions() {
        if (cachedPositions != null) return cachedPositions;
        if (layout == EnumDiagramLayout.CHART) {
            return new HashMap<>();
        }
        switch (layout) {
            case CIRCULAR:         cachedPositions = calculateCircularPositions(); break;
            case SQUARE:           cachedPositions = calculateSquarePositions(); break;
            case TREE:             cachedPositions = calculateTreePositions(); break;
            case GENERATED:        cachedPositions = calculateGeneratedPositions(); break;
            case CIRCULAR_MANUAL:  cachedPositions = calculateCircularManualPositions(); break;
            case SQUARE_MANUAL:    cachedPositions = calculateSquareManualPositions(); break;
            case TREE_MANUAL:      cachedPositions = calculateTreeManualPositions(); break;
            default:               cachedPositions = calculateCircularPositions(); break;
        }
        return cachedPositions;
    }

    // --- Standard Layout Methods ---

    protected Map<Integer, Point> calculateCircularPositions() {
        List<DiagramIcon> icons = getIcons();
        Map<Integer, Point> positions = new HashMap<>();
        int count = icons.size();
        if (count == 0) {  // avoid division by zero
            return positions;
        }
        int centerX = x + width / 2, centerY = y + height / 2;
        int radius = Math.min(width, height) / 2 - (int) (0.8 * slotSize);
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            int posX = centerX + (int) (radius * Math.cos(angle));
            int posY = centerY + (int) (radius * Math.sin(angle));
            positions.put(icons.get(i).id, new Point(posX, posY));
        }
        return positions;
    }

    protected Map<Integer, Point> calculateSquarePositions() {
        List<DiagramIcon> icons = getIcons();
        Map<Integer, Point> positions = new HashMap<>();
        int count = icons.size();
        if (count == 0) {  // early exit if no icons
            return positions;
        }
        int n = (int) Math.ceil(Math.sqrt(count));
        int gridWidth = width - 20, gridHeight = height - 20;
        int cellWidth = gridWidth / n, cellHeight = gridHeight / n;
        int startX = x + (width - gridWidth) / 2, startY = y + (height - gridHeight) / 2;
        for (int i = 0; i < count; i++) {
            int row = i / n, col = i % n;
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
        if(count == 0) { // avoid arithmetic errors when no icons exist
            return positions;
        }
        int levels = (int)Math.ceil(Math.log(count + 1) / Math.log(2));
        int levelHeight = height / (levels + 1);
        int index = 0;
        for (int level = 0; level < levels && index < count; level++) {
            int nodesThisLevel = (int)Math.min(Math.pow(2, level), count - index);
            int levelWidth = width - 20;
            int cellWidth = levelWidth / nodesThisLevel;
            for (int i = 0; i < nodesThisLevel && index < count; i++, index++) {
                int posX = x + 10 + i * cellWidth + cellWidth/2;
                int posY = y + (level + 1) * levelHeight;
                positions.put(icons.get(index).id, new Point(posX, posY));
            }
        }
        return positions;
    }


    protected Map<Integer, Point> calculateGeneratedPositions() {
        List<DiagramIcon> icons = getIcons();
        List<DiagramConnection> connections = getConnections();
        Map<Integer, List<Integer>> graph = buildGraph(icons, connections);
        List<Set<Integer>> clusters = getConnectedComponents(graph);
        int clusterCount = clusters.size();
        int gridCols = (int)Math.ceil(Math.sqrt(clusterCount));
        int gridRows = (int)Math.ceil((double)clusterCount / gridCols);
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
            Map<Integer, Point> compPositions;
            if (isCycle(cluster, graph))
                compPositions = layoutCycle(cluster, compX, compY, compWidth, compHeight);
            else if (isTree(cluster, graph))
                compPositions = layoutTree(cluster, compX, compY, compWidth, compHeight, graph);
            else if (isSquare(cluster))
                compPositions = layoutSquare(cluster, compX, compY, compWidth, compHeight);
            else
                compPositions = layoutForceDirected(cluster, compX, compY, compWidth, compHeight, connections);
            globalPositions.putAll(compPositions);
            clusterIndex++;
        }
        return globalPositions;
    }

    // --- Helpers for Generated Layout ---
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
    private void dfsComponent(Integer current, Map<Integer, List<Integer>> graph, Set<Integer> visited, Set<Integer> comp) {
        visited.add(current);
        comp.add(current);
        for (Integer neighbor : graph.get(current)) {
            if (!visited.contains(neighbor))
                dfsComponent(neighbor, graph, visited, comp);
        }
    }
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
    private boolean isTree(Set<Integer> cluster, Map<Integer, List<Integer>> graph) {
        int edgeCount = 0;
        for (Integer node : cluster) {
            for (Integer neighbor : graph.get(node)) {
                if (cluster.contains(neighbor)) edgeCount++;
            }
        }
        edgeCount /= 2;
        return edgeCount == cluster.size() - 1;
    }
    private boolean isSquare(Set<Integer> cluster) {
        int n = cluster.size();
        int sqrt = (int)Math.round(Math.sqrt(n));
        return sqrt * sqrt == n;
    }

    // --- Layout Algorithms for Clusters ---
    private Map<Integer, Point> layoutCycle(Set<Integer> cluster, int compX, int compY, int compWidth, int compHeight) {
        Map<Integer, Point> positions = new HashMap<>();
        if (cluster.isEmpty()) {  // guard against an empty cluster
            return positions;
        }
        int centerX = compX + compWidth / 2, centerY = compY + compHeight / 2;
        int radius = Math.min(compWidth, compHeight) / 3;
        int i = 0;
        for (Integer id : cluster) {
            double angle = 2 * Math.PI * i / cluster.size();
            int posX = centerX + (int) (radius * Math.cos(angle));
            int posY = centerY + (int) (radius * Math.sin(angle));
            positions.put(id, new Point(posX, posY));
            i++;
        }
        return positions;
    }


    private Map<Integer, Point> layoutTree(Set<Integer> cluster, int compX, int compY, int compWidth, int compHeight,
                                           Map<Integer, List<Integer>> fullGraph) {
        Map<Integer, Point> positions = new HashMap<>();
        if (cluster.isEmpty()) { // guard against empty clusters
            return positions;
        }

        Map<Integer, List<Integer>> subgraph = new HashMap<>();
        for (Integer id : cluster) {
            List<Integer> neighbors = new ArrayList<>();
            for (Integer neighbor : fullGraph.get(id)) {
                if (cluster.contains(neighbor))
                    neighbors.add(neighbor);
            }
            subgraph.put(id, neighbors);
        }
        Integer root = cluster.iterator().next();
        for (Integer id : cluster) {
            if (id < root) root = id;
        }
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
        int maxLevel = levels.keySet().stream().max(Integer::compare).orElse(0);
        double levelHeight = compHeight / (maxLevel + 1.0);
        for (Map.Entry<Integer, List<Integer>> entry : levels.entrySet()) {
            int lvl = entry.getKey();
            List<Integer> nodesAtLevel = entry.getValue();
            nodesAtLevel.sort(Comparator.comparingInt(a -> getIconById(a).priority));
            double spacing = compWidth / (nodesAtLevel.size() + 1.0);
            for (int i = 0; i < nodesAtLevel.size(); i++) {
                int id = nodesAtLevel.get(i);
                int posX = compX + (int)((i + 1) * spacing);
                int posY = compY + (int)(lvl * levelHeight + levelHeight / 2);
                positions.put(id, new Point(posX, posY));
            }
        }
        return positions;
    }

    private Map<Integer, Point> layoutSquare(Set<Integer> cluster, int compX, int compY, int compWidth, int compHeight) {
        Map<Integer, Point> positions = new HashMap<>();
        if (cluster.isEmpty()) { // guard against empty clusters
            return positions;
        }

        int n = (int)Math.round(Math.sqrt(cluster.size()));
        int gridCellWidth = compWidth / n, gridCellHeight = compHeight / n;
        int index = 0;
        List<Integer> nodes = new ArrayList<>(cluster);
        nodes.sort(Comparator.comparingInt(a -> getIconById(a).priority));
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

    private Map<Integer, Point> layoutForceDirected(Set<Integer> cluster, int compX, int compY, int compWidth, int compHeight,
                                                    List<DiagramConnection> connections) {
        Map<Integer, Point> positions = new HashMap<>();
        if (cluster.isEmpty()) {  // Early exit to avoid division by zero
            return positions;
        }
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
        List<DiagramConnection> compConnections = new ArrayList<>();
        for (DiagramConnection conn : connections) {
            if (cluster.contains(conn.idFrom) && cluster.contains(conn.idTo))
                compConnections.add(conn);
        }
        for (int iter = 0; iter < iterations; iter++) {
            Map<Integer, double[]> disp = new HashMap<>();
            for (Integer id : cluster) {
                disp.put(id, new double[]{0.0, 0.0});
            }
            List<Integer> nodes = new ArrayList<>(cluster);
            for (int i = 0; i < nodes.size(); i++) {
                Integer v = nodes.get(i);
                Point posV = positions.get(v);
                for (int j = i + 1; j < nodes.size(); j++) {
                    Integer u = nodes.get(j);
                    Point posU = positions.get(u);
                    double dx = posV.x - posU.x, dy = posV.y - posU.y;
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
            for (DiagramConnection conn : compConnections) {
                Point posV = positions.get(conn.idFrom);
                Point posU = positions.get(conn.idTo);
                double dx = posV.x - posU.x, dy = posV.y - posU.y;
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


    // --- Manual Layouts ---
    // Manual layouts use the icon index (e.g. ring number, row, level) and the priority for ordering.
    private Map<Integer, Point> calculateCircularManualPositions() {
        List<DiagramIcon> icons = getIcons();
        Map<Integer, Point> positions = new HashMap<>();
        if (icons.isEmpty()) {
            return positions;
        }
        Map<Integer, List<DiagramIcon>> groups = new TreeMap<>();
        for (DiagramIcon icon : icons) {
            groups.computeIfAbsent(icon.index, k -> new ArrayList<>()).add(icon);
        }
        int centerX = x + width / 2, centerY = y + height / 2;
        int maxRing = groups.isEmpty() ? 0 : Collections.max(groups.keySet());
        int maxRadius = Math.min(width, height) / 2 - iconSize - 10;
        double ringSpacing = (maxRing + 1) > 0 ? (double) maxRadius / (maxRing + 1) : 0;
        for (Map.Entry<Integer, List<DiagramIcon>> entry : groups.entrySet()) {
            int ring = entry.getKey();
            List<DiagramIcon> groupIcons = entry.getValue();
            if (groupIcons.isEmpty())
                continue;
            groupIcons.sort(Comparator.comparingInt(icon -> icon.priority));
            double radius = ringSpacing * (ring + 1);
            int count = groupIcons.size();
            double startAngle = (ring == 0) ? -Math.PI / 2 : -3 * Math.PI / 4;
            for (int i = 0; i < count; i++) {
                double angle = startAngle + 2 * Math.PI * i / count;
                int posX = centerX + (int) (radius * Math.cos(angle));
                int posY = centerY + (int) (radius * Math.sin(angle));
                positions.put(groupIcons.get(i).id, new Point(posX, posY));
            }
        }
        return positions;
    }


    private Map<Integer, Point> calculateSquareManualPositions() {
        List<DiagramIcon> icons = getIcons();
        Map<Integer, Point> positions = new HashMap<>();
        if (icons.isEmpty()) {
            return positions;
        }
        Map<Integer, List<DiagramIcon>> groups = new TreeMap<>();
        for (DiagramIcon icon : icons) {
            groups.computeIfAbsent(icon.index, k -> new ArrayList<>()).add(icon);
        }
        int centerX = x + width / 2, centerY = y + height / 2;
        int maxRing = groups.isEmpty() ? 0 : Collections.max(groups.keySet());
        int halfSide = Math.min(width, height) / 2 - iconSize - 10;
        double ringSpacing = (maxRing + 1) > 0 ? (double) halfSide / (maxRing + 1) : 0;
        for (Map.Entry<Integer, List<DiagramIcon>> entry : groups.entrySet()) {
            int ring = entry.getKey();
            List<DiagramIcon> groupIcons = entry.getValue();
            if (groupIcons.isEmpty())
                continue;
            groupIcons.sort(Comparator.comparingInt(icon -> icon.priority));
            double offset = ringSpacing * (ring + 1);
            int count = groupIcons.size();
            double perimeter = 8 * offset;
            for (int i = 0; i < count; i++) {
                double posAlong = (perimeter * i) / count;
                int posX, posY;
                if (posAlong < 2 * offset) {
                    posX = centerX - (int) offset + (int) posAlong;
                    posY = centerY - (int) offset;
                } else if (posAlong < 4 * offset) {
                    posX = centerX + (int) offset;
                    posY = centerY - (int) offset + (int) (posAlong - 2 * offset);
                } else if (posAlong < 6 * offset) {
                    posX = centerX + (int) offset - (int) (posAlong - 4 * offset);
                    posY = centerY + (int) offset;
                } else {
                    posX = centerX - (int) offset;
                    posY = centerY + (int) offset - (int) (posAlong - 6 * offset);
                }
                positions.put(groupIcons.get(i).id, new Point(posX, posY));
            }
        }
        return positions;
    }


    private Map<Integer, Point> calculateTreeManualPositions() {
        List<DiagramIcon> icons = getIcons();
        Map<Integer, List<DiagramIcon>> levels = new TreeMap<>();
        for (DiagramIcon icon : icons) {
            levels.computeIfAbsent(icon.index, k -> new ArrayList<>()).add(icon);
        }
        Map<Integer, Point> positions = new HashMap<>();
        if (icons.isEmpty()) { // prevent division errors
            return positions;
        }
        int maxLevel = levels.isEmpty() ? 0 : Collections.max(levels.keySet());
        int levelHeight = height / (maxLevel + 2);
        for (Map.Entry<Integer, List<DiagramIcon>> entry : levels.entrySet()) {
            int level = entry.getKey();
            List<DiagramIcon> levelIcons = entry.getValue();
            levelIcons.sort(Comparator.comparingInt(icon -> icon.priority));
            int count = levelIcons.size();
            if(count == 0) {
                continue;
            }
            int horizontalSpacing = (width - 40) / (count + 1);
            for (int i = 0; i < count; i++) {
                int posX = x + 20 + (i + 1) * horizontalSpacing;
                int posY = y + levelHeight * (level + 1);
                positions.put(levelIcons.get(i).id, new Point(posX, posY));
            }
        }
        return positions;
    }


    // --- Arrow Drawing Methods ---
    /**
     * Draws a connection line between two points.
     * If curvedArrows is enabled, draws a quadratic Bezier curve.
     */
    protected void drawConnectionLine(int x1, int y1, int x2, int y2, DiagramConnection conn, boolean dim) {
        int color = getConnectionColor(conn);
        if (!useColorScaling) color = 0xFFFFFFFF;
        if (!curvedArrows) {
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glLineWidth(2.0F);
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            if (dim) { r *= 0.4f; g *= 0.4f; b *= 0.4f; }
            GL11.glColor4f(r, g, b, 1f);
            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex2i(x1, y1);
            GL11.glVertex2i(x2, y2);
            GL11.glEnd();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glPopAttrib();
        } else {
            if (conn.polyline != null && !conn.polyline.isEmpty()) {
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
                GL11.glBegin(GL11.GL_LINE_STRIP);
                for (Point p : conn.polyline) {
                    GL11.glVertex2i(p.x, p.y);
                }
                GL11.glEnd();
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glPopAttrib();
            } else {
                // Fallback to original method if polyline isn’t computed
                Point start = new Point(x1, y1);
                Point end = new Point(x2, y2);
                double dx = x2 - x1, dy = y2 - y1;
                double len = Math.sqrt(dx * dx + dy * dy);
                if (len == 0) len = 1;
                double angleRad = Math.toRadians(curveAngle);
                double offset = len * Math.tan(angleRad) / 2;
                double perpX = -dy / len, perpY = dx / len;
                Point control = new Point((x1 + x2) / 2 + (int) (perpX * offset),
                    (y1 + y2) / 2 + (int) (perpY * offset));
                int segments = 100;
                GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glLineWidth(2.0F);
                float rr = ((color >> 16) & 0xFF) / 255f;
                float gg = ((color >> 8) & 0xFF) / 255f;
                float bb = (color & 0xFF) / 255f;
                if (dim) {
                    rr *= 0.4f;
                    gg *= 0.4f;
                    bb *= 0.4f;
                }
                GL11.glColor4f(rr, gg, bb, 1f);
                GL11.glBegin(GL11.GL_LINE_STRIP);
                for (int i = 0; i <= segments; i++) {
                    double t = (double) i / segments;
                    int bx = (int) ((1 - t) * (1 - t) * start.x + 2 * (1 - t) * t * control.x + t * t * end.x);
                    int by = (int) ((1 - t) * (1 - t) * start.y + 2 * (1 - t) * t * control.y + t * t * end.y);
                    GL11.glVertex2i(bx, by);
                }
                GL11.glEnd();
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glPopAttrib();
            }
        }
    }

    /**
     * Draws the arrow head at the end of a connection.
     */
    protected void drawArrowHead(int x1, int y1, int x2, int y2, DiagramConnection conn, boolean dim) {
        int color = getConnectionColor(conn);
        if (!useColorScaling) color = 0xFFFFFFFF;
        double angle;
        if (!curvedArrows) {
            angle = Math.atan2(y2 - y1, x2 - x1);
        } else {
            if (conn.polyline != null && conn.polyline.size() >= 2) {
                Point last = conn.polyline.get(conn.polyline.size() - 1);
                Point secondLast = conn.polyline.get(conn.polyline.size() - 2);
                double dx = last.x - secondLast.x;
                double dy = last.y - secondLast.y;
                angle = Math.atan2(dy, dx);
            } else {
                // Fallback to original method
                double dx = x2 - x1, dy = y2 - y1;
                double len = Math.sqrt(dx * dx + dy * dy);
                if (len == 0) len = 1;
                double offset = len * Math.tan(Math.toRadians(curveAngle)) / 2;
                double perpX = -dy / len, perpY = dx / len;
                int cx = (x1 + x2) / 2 + (int) (perpX * offset);
                int cy = (y1 + y2) / 2 + (int) (perpY * offset);
                double t = 0.95;
                double bx = (1 - t) * (1 - t) * x1 + 2 * (1 - t) * t * cx + t * t * x2;
                double by = (1 - t) * (1 - t) * y1 + 2 * (1 - t) * t * cy + t * t * y2;
                double dBx = 2 * (1 - t) * (cx - x1) + 2 * t * (x2 - cx);
                double dBy = 2 * (1 - t) * (cy - y1) + 2 * t * (y2 - cy);
                angle = Math.atan2(dBy, dBx);
            }
        }
        float defenderEdgeX = x2 - (slotSize / 2f) * (float)Math.cos(angle);
        float defenderEdgeY = y2 - (slotSize / 2f) * (float)Math.sin(angle);
        int arrowSize = 6;
        float leftX = defenderEdgeX - arrowSize * (float)Math.cos(angle - Math.PI / 6);
        float leftY = defenderEdgeY - arrowSize * (float)Math.sin(angle - Math.PI / 6);
        float rightX = defenderEdgeX - arrowSize * (float)Math.cos(angle + Math.PI / 6);
        float rightY = defenderEdgeY - arrowSize * (float)Math.sin(angle + Math.PI / 6);
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        float rr = ((color >> 16) & 0xFF) / 255f;
        float gg = ((color >> 8) & 0xFF) / 255f;
        float bb = (color & 0xFF) / 255f;
        if(dim){ rr *= 0.4f; gg *= 0.4f; bb *= 0.4f; }
        GL11.glColor4f(rr, gg, bb, 1f);
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

    // --- Standard Icon Box Drawing ---
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

    // --- CHART Layout Drawing ---
    /**
     * Draws the CHART layout.
     * <ul>
     *   <li>The grid has (n+1) columns and rows, where n is the number of icons.</li>
     *   <li>The top row and left column show header icons.</li>
     *   <li>Each inner cell is filled with the connection color (if one exists) or a darker gray otherwise.</li>
     *   <li>Hovering over a header or inner cell highlights the corresponding column/row or cell (and its two header icons),
     *       while dimming non-relevant cells.</li>
     *   <li>Zoom and pan are applied to the entire chart.</li>
     * </ul>
     */
    private void drawChartDiagram(int mouseX, int mouseY, boolean subGui) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int factor = sr.getScaleFactor();

        // 1. Draw the static background for the entire diagram.
        drawRect(x, y, x + width, y + height, 0xFF333333);

        // 2. Setup scissor to clip drawing to the diagram area.
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * factor, (sr.getScaledHeight() - (y + height)) * factor, width * factor, height * factor);

        // 3. Push a matrix and apply pan/zoom for grid, icons, and overlays.
        GL11.glPushMatrix();
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        GL11.glTranslatef(centerX + panX, centerY + panY, 0);
        GL11.glScalef(zoom, zoom, 1.0f);
        GL11.glTranslatef(-centerX, -centerY, 0);

        // 4. Compute grid parameters.
        List<DiagramIcon> icons = getIcons();
        int n = icons.size();
        int cols = n + 1, rows = n + 1;
        int cellSize = Math.min(width / cols, height / rows);
        int gridWidth = cellSize * cols;
        int gridHeight = cellSize * rows;
        int startX = x + (width - gridWidth) / 2;
        int startY = y + (height - gridHeight) / 2;

        // 5. Draw grid cells.
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int cellX = startX + col * cellSize;
                int cellY = startY + row * cellSize;
                int fillColor = 0xFFCCCCCC; // Default for header cells.
                if (row > 0 && col > 0) { // inner cell: use connection color (or darker gray)
                    DiagramIcon leftIcon = icons.get(row - 1);
                    DiagramIcon topIcon = icons.get(col - 1);
                    DiagramConnection conn = getConnectionByIds(leftIcon.id, topIcon.id);
                    fillColor = (conn != null) ? getConnectionColor(conn) : 0xFF777777;
                }
                drawRect(cellX, cellY, cellX + cellSize, cellY + cellSize, fillColor);
            }
        }

        // 6. Draw grid lines.
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0, 0, 0, 1);
        GL11.glLineWidth(1.0f);
        GL11.glBegin(GL11.GL_LINES);
        for (int col = 0; col <= cols; col++) {
            int xLine = startX + col * cellSize;
            GL11.glVertex2i(xLine, startY);
            GL11.glVertex2i(xLine, startY + gridHeight);
        }
        for (int row = 0; row <= rows; row++) {
            int yLine = startY + row * cellSize;
            GL11.glVertex2i(startX, yLine);
            GL11.glVertex2i(startX + gridWidth, yLine);
        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopAttrib();

        // 7. Draw header icons (top and left) scaled to fit within each cell.
        float iconMargin = 1f;  // margin in pixels inside the cell.
        float iconScale = (cellSize - iconMargin) / (float)iconSize;
        for (int i = 0; i < n; i++) {
            // Top header icon:
            int topHeaderCenterX = startX + (i + 1) * cellSize + cellSize / 2;
            int topHeaderCenterY = startY + cellSize / 2;
            GL11.glPushMatrix();
            GL11.glTranslatef(topHeaderCenterX, topHeaderCenterY, 0);
            GL11.glScalef(iconScale, iconScale, 1);
            // renderIcon should draw centered at (0,0)
            renderIcon(icons.get(i), 0, 0, IconRenderState.DEFAULT);
            GL11.glPopMatrix();

            // Left header icon:
            int leftHeaderCenterX = startX + cellSize / 2;
            int leftHeaderCenterY = startY + (i + 1) * cellSize + cellSize / 2;
            GL11.glPushMatrix();
            GL11.glTranslatef(leftHeaderCenterX, leftHeaderCenterY, 0);
            GL11.glScalef(iconScale, iconScale, 1);
            renderIcon(icons.get(i), 0, 0, IconRenderState.DEFAULT);
            GL11.glPopMatrix();
        }

        // 8. Compute effective mouse coordinates (reverse pan/zoom).
        int effectiveMouseX = (int)(((float)mouseX - (centerX + panX)) / zoom + centerX);
        int effectiveMouseY = (int)(((float)mouseY - (centerY + panY)) / zoom + centerY);

        // 9. Determine hover state on the grid.
        boolean hoverInGrid = (effectiveMouseX >= startX && effectiveMouseX < startX + gridWidth &&
            effectiveMouseY >= startY && effectiveMouseY < startY + gridHeight);
        int hoveredRow = -1, hoveredCol = -1;
        Set<Integer> highlightTop = new HashSet<>();
        Set<Integer> highlightLeft = new HashSet<>();
        List<String> tooltipToShow = null;
        if (hoverInGrid) {
            int colIndex = (effectiveMouseX - startX) / cellSize;
            int rowIndex = (effectiveMouseY - startY) / cellSize;
            // If hovering over a top header cell.
            if (rowIndex == 0 && colIndex >= 1) {
                highlightTop.add(colIndex - 1);
                tooltipToShow = getIconTooltip(icons.get(colIndex - 1));
                onIconHover(icons.get(colIndex - 1));
            }
            // If hovering over a left header cell.
            else if (colIndex == 0 && rowIndex >= 1) {
                highlightLeft.add(rowIndex - 1);
                tooltipToShow = getIconTooltip(icons.get(rowIndex - 1));
                onIconHover(icons.get(rowIndex - 1));
            }
            // Else, hovering over an inner cell.
            else if (rowIndex >= 1 && colIndex >= 1) {
                hoveredRow = rowIndex - 1;
                hoveredCol = colIndex - 1;
                highlightTop.add(hoveredCol);
                highlightLeft.add(hoveredRow);
                DiagramIcon leftIcon = icons.get(hoveredRow);
                DiagramIcon topIcon = icons.get(hoveredCol);
                DiagramConnection conn = getConnectionByIds(leftIcon.id, topIcon.id);
                if (conn != null) {
                    tooltipToShow = getConnectionTooltip(conn);
                    onConnectionHover(conn);
                }
            }
        }

        // 10. Draw hover overlay:
        // Dim all inner cells (row>=1 & col>=1) not in highlighted row/column.
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0, 0, 0, 0.5f);
        GL11.glBegin(GL11.GL_QUADS);
        for (int r = 1; r < rows; r++) {
            for (int c = 1; c < cols; c++) {
                boolean keepBright = false;
                if (!highlightTop.isEmpty() && highlightTop.contains(c - 1))
                    keepBright = true;
                if (!highlightLeft.isEmpty() && highlightLeft.contains(r - 1))
                    keepBright = true;
                // Also, if a specific cell is hovered, its entire row/column is bright.
                if (hoveredRow != -1 && hoveredCol != -1) {
                    if (r - 1 == hoveredRow || c - 1 == hoveredCol)
                        keepBright = true;
                }
                if (!keepBright) {
                    int cellX = startX + c * cellSize;
                    int cellY = startY + r * cellSize;
                    GL11.glVertex2i(cellX, cellY);
                    GL11.glVertex2i(cellX + cellSize, cellY);
                    GL11.glVertex2i(cellX + cellSize, cellY + cellSize);
                    GL11.glVertex2i(cellX, cellY + cellSize);
                }
            }
        }
        GL11.glEnd();
        // Then draw a white overlay on the highlighted headers/inner cell.
        GL11.glColor4f(1, 1, 1, 0.3f);
        GL11.glBegin(GL11.GL_QUADS);
        for (int i = 0; i < n; i++) {
            if (highlightTop.contains(i)) {
                int hx = startX + (i + 1) * cellSize;
                int hy = startY;
                GL11.glVertex2i(hx, hy);
                GL11.glVertex2i(hx + cellSize, hy);
                GL11.glVertex2i(hx + cellSize, hy + cellSize);
                GL11.glVertex2i(hx, hy + cellSize);
            }
            if (highlightLeft.contains(i)) {
                int hx = startX;
                int hy = startY + (i + 1) * cellSize;
                GL11.glVertex2i(hx, hy);
                GL11.glVertex2i(hx + cellSize, hy);
                GL11.glVertex2i(hx + cellSize, hy + cellSize);
                GL11.glVertex2i(hx, hy + cellSize);
            }
        }
        if (hoveredRow != -1 && hoveredCol != -1) {
            int cellX = startX + (hoveredCol + 1) * cellSize;
            int cellY = startY + (hoveredRow + 1) * cellSize;
            GL11.glVertex2i(cellX, cellY);
            GL11.glVertex2i(cellX + cellSize, cellY);
            GL11.glVertex2i(cellX + cellSize, cellY + cellSize);
            GL11.glVertex2i(cellX, cellY + cellSize);
        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopAttrib();

        // End pan/zoom block.
        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // 11. Draw tooltip (using raw screen coordinates).
        if (tooltipToShow != null && !tooltipToShow.isEmpty()) {
            drawHoveringText(tooltipToShow, mouseX, mouseY, mc.fontRenderer);
        }
    }



    /**
     * Main method for drawing the diagram.
     * If layout == CHART, delegates to drawChartDiagram().
     * Otherwise, applies pan/zoom, draws connections (with hit-detection fixed for curves)
     * and icons, and shows tooltips and hover events.
     */
    public void drawDiagram(int mouseX, int mouseY, boolean subGui) {
        boolean allowInput = (parent == null || !parent.hasSubGui());
        if (allowInput && isWithin(mouseX, mouseY) && !subGui)
            handleMouseScroll(Mouse.getDWheel());
        if (layout == EnumDiagramLayout.CHART) {
            drawChartDiagram(mouseX, mouseY, subGui);
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int factor = sr.getScaleFactor();
        Map<Integer, Point> positions = calculatePositions();
        if (layout != EnumDiagramLayout.CHART) {
            for (DiagramConnection conn : getConnections()) {
                if (conn.polyline == null && curvedArrows) {
                    DiagramIcon iconFrom = getIconById(conn.idFrom);
                    DiagramIcon iconTo = getIconById(conn.idTo);
                    if (iconFrom == null || iconTo == null || !iconFrom.enabled || !iconTo.enabled)
                        continue;
                    Point pFrom = positions.get(conn.idFrom);
                    Point pTo = positions.get(conn.idTo);
                    if (pFrom == null || pTo == null) continue;
                    int x1 = pFrom.x, y1 = pFrom.y, x2 = pTo.x, y2 = pTo.y;
                    double dx = x2 - x1, dy = y2 - y1;
                    double len = Math.sqrt(dx * dx + dy * dy);
                    if (len == 0) len = 1;
                    double angleRad = Math.toRadians(curveAngle);
                    double offset = len * Math.tan(angleRad) / 2;
                    double perpX = -dy / len, perpY = dx / len;
                    int cx = (x1 + x2) / 2 + (int) (perpX * offset);
                    int cy = (y1 + y2) / 2 + (int) (perpY * offset);
                    conn.polyline = new ArrayList<>();
                    int segments = 50; // Reduced from 100 for performance
                    for (int i = 0; i <= segments; i++) {
                        double t = (double) i / segments;
                        int bx = (int) ((1 - t) * (1 - t) * x1 + 2 * (1 - t) * t * cx + t * t * x2);
                        int by = (int) ((1 - t) * (1 - t) * y1 + 2 * (1 - t) * t * cy + t * t * y2);
                        conn.polyline.add(new Point(bx, by));
                    }
                }
            }
        }

        int centerX = x + width / 2, centerY = y + height / 2;
        int effectiveMouseX = (int)(((float)mouseX - (centerX + panX)) / zoom + centerX);
        int effectiveMouseY = (int)(((float)mouseY - (centerY + panY)) / zoom + centerY);
        Integer hoveredIconId = null;
        int hoveredConnFrom = -1, hoveredConnTo = -1;
        Set<Integer> selectedIconIds = new HashSet<>();
        // Check if mouse is over an icon.
        for (DiagramIcon icon : getIcons()) {
            if (!icon.enabled) continue;
            Point pos = positions.get(icon.id);
            if (pos == null) continue;
            int slotX = pos.x - slotSize / 2, slotY = pos.y - slotSize / 2;
            if (effectiveMouseX >= slotX && effectiveMouseX < slotX + slotSize &&
                effectiveMouseY >= slotY && effectiveMouseY < slotY + slotSize) {
                hoveredIconId = icon.id;
                selectedIconIds.add(icon.id);
                onIconHover(icon);
                break;
            }
        }
        // If no icon was hovered, check connections.
        if (hoveredIconId == null) {
            final double threshold = 5.0;
            for (DiagramConnection conn : getConnections()) {
                DiagramIcon iconFrom = getIconById(conn.idFrom);
                DiagramIcon iconTo = getIconById(conn.idTo);
                if (iconFrom == null || iconTo == null || !iconFrom.enabled || !iconTo.enabled)
                    continue;
                Point pFrom = positions.get(conn.idFrom);
                Point pTo = positions.get(conn.idTo);
                if (pFrom == null || pTo == null) continue;
                double dist;
                if (curvedArrows && conn.polyline != null && !conn.polyline.isEmpty()) {
                    double minDist = Double.MAX_VALUE;
                    for (int i = 0; i < conn.polyline.size() - 1; i++) {
                        Point a = conn.polyline.get(i);
                        Point b = conn.polyline.get(i + 1);
                        double d = pointLineDistance(effectiveMouseX, effectiveMouseY, a.x, a.y, b.x, b.y);
                        if (d < minDist) minDist = d;
                    }
                    dist = minDist;
                } else {
                    dist = pointLineDistance(effectiveMouseX, effectiveMouseY, pFrom.x, pFrom.y, pTo.x, pTo.y);
                }
                if (dist < threshold) {
                    hoveredConnFrom = conn.idFrom;
                    hoveredConnTo = conn.idTo;
                    selectedIconIds.add(conn.idFrom);
                    selectedIconIds.add(conn.idTo);
                    onConnectionHover(conn);
                }
            }
        }
        // Setup scissor.
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * factor, (sr.getScaledHeight() - (y + height)) * factor, width * factor, height * factor);
        drawRect(x, y, x + width, y + height, 0xFF333333);
        GL11.glPushMatrix();
        GL11.glTranslatef(centerX + panX, centerY + panY, 0);
        GL11.glScalef(zoom, zoom, 1.0f);
        GL11.glTranslatef(-centerX, -centerY, 0);
        // Draw connections.
        for (DiagramConnection conn : getConnections()) {
            DiagramIcon iconFrom = getIconById(conn.idFrom);
            DiagramIcon iconTo = getIconById(conn.idTo);
            if (iconFrom == null || iconTo == null || !iconFrom.enabled || !iconTo.enabled)
                continue;
            Point pFrom = positions.get(conn.idFrom);
            Point pTo = positions.get(conn.idTo);
            if (pFrom == null || pTo == null) continue;
            int ax = pFrom.x, ay = pFrom.y, bx = pTo.x, by = pTo.y;
            boolean dim = !selectedIconIds.isEmpty() &&
                !(selectedIconIds.contains(conn.idFrom) && selectedIconIds.contains(conn.idTo));
            drawConnectionLine(ax, ay, bx, by, conn, dim);
        }
        // Draw icons.
        for (DiagramIcon icon : getIcons()) {
            if (!icon.enabled) continue;
            Point pos = positions.get(icon.id);
            if (pos == null) continue;
            int slotX = pos.x - slotSize / 2, slotY = pos.y - slotSize / 2;
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
                int ax = pFrom.x, ay = pFrom.y, bx = pTo.x, by = pTo.y;
                boolean dim = !selectedIconIds.isEmpty() &&
                    !(selectedIconIds.contains(conn.idFrom) && selectedIconIds.contains(conn.idTo));
                drawArrowHead(ax, ay, bx, by, conn, dim);
            }
            GL11.glPopMatrix();
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        // Show tooltips if an icon or connection was hovered.
        if (hoveredIconId != null) {
            DiagramIcon icon = getIconById(hoveredIconId);
            List<String> tooltip = getIconTooltip(icon);
            if (tooltip != null && !tooltip.isEmpty())
                drawHoveringText(tooltip, mouseX, mouseY, mc.fontRenderer);
        } else if (hoveredConnFrom != -1 && hoveredConnTo != -1) {
            DiagramConnection conn = getConnectionByIds(hoveredConnFrom, hoveredConnTo);
            List<String> tooltip = getConnectionTooltip(conn);
            if (tooltip != null && !tooltip.isEmpty())
                drawHoveringText(tooltip, mouseX, mouseY, mc.fontRenderer);
        }
    }

    // --- Utility Methods ---
    protected DiagramIcon getIconById(int id) {
        for (DiagramIcon icon : getIcons()) {
            if (icon.id == id) return icon;
        }
        return null;
    }
    protected DiagramConnection getConnectionByIds(int idFrom, int idTo) {
        for (DiagramConnection conn : getConnections()) {
            if (conn.idFrom == idFrom && conn.idTo == idTo) return conn;
        }
        return null;
    }
    protected int getConnectionColor(DiagramConnection conn) {
        float percent = conn.percent;
        if (percent <= 0.10f) return 0xFF80FF80;
        else if (percent <= 0.40f) return 0xFFCCFF66;
        else if (percent <= 0.50f) return 0xFFFFA500;
        else {
            float ratio = (percent - 0.50f) / 0.50f;
            ratio = Math.min(1f, Math.max(0f, ratio));
            int r1 = 0xFF, g1 = 0xA5, b1 = 0x00;
            int r2 = 0xFF, g2 = 0x00, b2 = 0x00;
            int r = r1 + (int)((r2 - r1) * ratio);
            int g = g1 + (int)((g2 - g1) * ratio);
            int b = b1 + (int)((b2 - b1) * ratio);
            return (0xFF << 24) | (r << 16) | (g << 8) | b;
        }
    }
    private double pointLineDistance(int px, int py, int x1, int y1, int x2, int y2) {
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

    // --- Inner Helper Classes ---
    public static class DiagramIcon {
        public int id;
        public boolean enabled = true;
        public boolean pressable = false;
        public int index = 0;      // For row, ring, or level.
        public int priority = 0;   // For ordering.
        public DiagramIcon(int id) { this.id = id; }
        public DiagramIcon(int id, int index, int priority) {
            this.id = id;
            this.index = index;
            this.priority = priority;
        }
    }
    public static class DiagramConnection {
        public int idFrom, idTo;
        public float percent;
        public String hoverText;
        public List<Point> polyline = null;

        public DiagramConnection(int idFrom, int idTo, float percent, String hoverText) {
            this.idFrom = idFrom;
            this.idTo = idTo;
            this.percent = percent;
            this.hoverText = hoverText;
        }
    }
    // Used by the CHART layout to represent an inner cell.
    private static class ConnectionCell {
        int x, y, size;
        DiagramConnection conn;
        ConnectionCell(int x, int y, int size, DiagramConnection conn) {
            this.x = x; this.y = y; this.size = size; this.conn = conn;
        }
    }
    // Simple helper class for rectangles.
    private static class Rectangle {
        int x, y, width, height;
        Rectangle(int x, int y, int width, int height) {
            this.x = x; this.y = y; this.width = width; this.height = height;
        }
        int centerX() { return x + width / 2; }
        int centerY() { return y + height / 2; }
    }

    public enum IconRenderState {
        DEFAULT,
        HIGHLIGHTED,
        NOT_HIGHLIGHTED;
    }

    // --- Mouse Handling ---
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (parent != null && parent.hasSubGui()) return false;
        if (mouseButton == 0) {
            for (DiagramIcon icon : getIcons()) {
                if (!icon.enabled || !icon.pressable) continue;
                Point pos = calculatePositions().get(icon.id);
                if (pos == null) continue;
                int centerX = x + width / 2, centerY = y + height / 2;
                int effectiveX = (int)(((float)mouseX - (centerX + panX)) / zoom + centerX);
                int effectiveY = (int)(((float)mouseY - (centerY + panY)) / zoom + centerY);
                int slotX = pos.x - slotSize / 2, slotY = pos.y - slotSize / 2;
                if (effectiveX >= slotX && effectiveX < slotX + slotSize &&
                    effectiveY >= slotY && effectiveY < slotY + slotSize) {
                    onIconClick(icon);
                    currentlyPressedIcon = icon;
                    return true;
                }
            }
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
            int dx = mouseX - lastDragX, dy = mouseY - lastDragY;
            panX += dx / zoom * 0.7f;
            panY += dy / zoom * 0.7f;
            lastDragX = mouseX;
            lastDragY = mouseY;
        }
        if (currentlyPressedIcon != null) onIconHeld(currentlyPressedIcon);
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
        if (parent.hasSubGui())
            return false;
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    /**
     * Handles zooming based on mouse wheel scrolling.
     */
    public void handleMouseScroll(int scrollDelta) {
        zoom += scrollDelta * 0.0009f;
        if (zoom < 0.5f) zoom = 0.5f;
        if (zoom > 2.0f) zoom = 2.0f;
    }
    /**
     * Override to render tooltips.
     */
    protected void drawHoveringText(List<String> textLines, int mouseX, int mouseY, FontRenderer font) {}

    // --- Flag Setters ---
    public void setShowArrowHeads(boolean showArrowHeads) {
        this.showArrowHeads = showArrowHeads;
    }
    public void setUseColorScaling(boolean useColorScaling) {
        this.useColorScaling = useColorScaling;
    }

    // --- Icon and Connection Event Callbacks ---
    /**
     * Called when an icon is clicked.
     */
    protected void onIconClick(DiagramIcon icon) {}
    /**
     * Called when an icon is held.
     */
    protected void onIconHeld(DiagramIcon icon) {}
    /**
     * Called when an icon is released.
     */
    protected void onIconRelease(DiagramIcon icon) {}
    /**
     * Called when an icon is hovered over.
     */
    protected void onIconHover(DiagramIcon icon) {}
    /**
     * Called when a connection is hovered over.
     */
    protected void onConnectionHover(DiagramConnection conn) {}
}
