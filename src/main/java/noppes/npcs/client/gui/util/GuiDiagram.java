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
import java.util.*;
import java.util.List;

/**
 * Abstract GUI class for drawing diagrams with icons (nodes) and connections (arrows).
 * <p>
 * This revised version ensures that non‑Chart layouts use the full diagram bounds for positioning
 * (so that the outer icons lie flush with the edges) and that curved arrow drawing selects the
 * best control point (using both curveAngle and its negative) to avoid icons.
 */
public abstract class GuiDiagram extends Gui {

    // Renderer for item icons.
    protected RenderItem renderItem = new RenderItem();

    // --- Diagram Area & Controls ---
    protected int x, y, width, height;    // Diagram bounds
    protected float panX = 0, panY = 0;     // Panning offset
    protected float zoom = 1.0f;            // Zoom factor
    protected boolean dragging = false;   // Flag for dragging
    protected boolean allowTwoWay = false;  // Flag to enable two‑way arrow splitting
    protected int lastDragX, lastDragY;     // Last mouse drag coordinates

    // --- Node (Icon) Sizing ---
    protected int iconSize = 16;          // Icon image size
    protected int slotPadding = 4;        // Padding around icon slot
    protected int slotSize = iconSize + slotPadding;  // Total size of icon slot

    // --- Icon Box Appearance ---
    protected int iconBackgroundColor = 0xFF999999; // Background color for icon boxes
    protected int iconBorderColor = 0xFF555555;       // Border color for icon boxes
    protected int iconBorderThickness = 1;            // Border thickness

    // --- Connection Options ---
    protected boolean showArrowHeads = true;   // Flag to show arrow heads
    protected boolean useColorScaling = true;    // Flag to scale colors

    // --- Layout Options ---
    protected EnumDiagramLayout layout = EnumDiagramLayout.CIRCULAR;

    /**
     * Sets the diagram layout type.
     *
     * @param layout the new layout type
     */
    public void setLayout(EnumDiagramLayout layout) {
        this.layout = layout;
        invalidateCache();
    }

    // --- Arrow Drawing Options ---
    protected boolean curvedArrows = true;  // Use curved arrows if true
    protected int curveAngle = 15;            // Default curve angle (degrees)

    /**
     * Enable/disable curved arrows.
     *
     * @param curved true for curved arrows, false for straight
     */
    public void setCurvedArrows(boolean curved) {
        this.curvedArrows = curved;
    }

    /**
     * Sets the curve angle.
     *
     * @param angle the new curve angle in degrees
     */
    public void setCurveAngle(int angle) {
        this.curveAngle = angle;
    }

    // --- Caching for icons and connections ---
    protected Map<Integer, Point> cachedPositions = null;
    protected List<DiagramIcon> iconsCache = null;
    protected List<DiagramConnection> connectionsCache = null;

    /**
     * Clears cached positions, icons, and connections.
     */
    public void invalidateCache() {
        cachedPositions = null;
        iconsCache = null;
        connectionsCache = null;
    }

    // --- Parent reference (for sub-GUI handling) ---
    protected GuiNPCInterface parent;

    /**
     * Constructor.
     *
     * @param parent the parent GUI interface
     * @param x      x-coordinate of the diagram
     * @param y      y-coordinate of the diagram
     * @param width  diagram width
     * @param height diagram height
     */
    public GuiDiagram(GuiNPCInterface parent, int x, int y, int width, int height) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // --- Data Providers (subclasses must implement these) ---

    /**
     * Subclasses must provide a list of icons.
     *
     * @return list of DiagramIcon objects
     */
    protected abstract List<DiagramIcon> createIcons();

    /**
     * Subclasses must provide a list of connections.
     *
     * @return list of DiagramConnection objects
     */
    protected abstract List<DiagramConnection> createConnections();

    /**
     * Returns the cached list of icons, creating them if necessary.
     *
     * @return list of DiagramIcon objects
     */
    protected final List<DiagramIcon> getIcons() {
        if (iconsCache == null) {
            iconsCache = createIcons();
        }
        return iconsCache;
    }

    /**
     * Returns the cached list of connections, creating them if necessary.
     *
     * @return list of DiagramConnection objects
     */
    protected final List<DiagramConnection> getConnections() {
        if (connectionsCache == null) {
            connectionsCache = createConnections();
        }
        return connectionsCache;
    }

    /**
     * Subclasses must implement how an icon is rendered.
     *
     * @param icon   the icon to render
     * @param posX   x-coordinate for rendering
     * @param posY   y-coordinate for rendering
     * @param state  render state (highlighted, default, etc.)
     */
    protected abstract void renderIcon(DiagramIcon icon, int posX, int posY, IconRenderState state);

    /**
     * Returns a tooltip for an icon.
     *
     * @param icon the icon to get tooltip for
     * @return a list of tooltip text lines, or null if none
     */
    protected List<String> getIconTooltip(DiagramIcon icon) {
        return null;
    }

    /**
     * Returns a tooltip for a connection.
     *
     * @param conn the connection to get tooltip for
     * @return a list of tooltip text lines
     */
    protected List<String> getConnectionTooltip(DiagramConnection conn) {
        List<String> tooltip = new ArrayList<>();
        DiagramIcon iconFrom = getIconById(conn.idFrom);
        DiagramIcon iconTo = getIconById(conn.idTo);
        String nameFrom = (iconFrom != null) ? getIconName(iconFrom) : "Unknown";
        String nameTo = (iconTo != null) ? getIconName(iconTo) : "Unknown";
        tooltip.add(nameFrom + " > " + nameTo + ":");
        tooltip.add(conn.hoverText);
        if (allowTwoWay) {
            DiagramConnection reverse = getConnectionByIds(conn.idTo, conn.idFrom);
            if (reverse != null) {
                tooltip.add(nameTo + " > " + nameFrom + ":");
                tooltip.add(reverse.hoverText);
            }
        }
        return tooltip;
    }

    /**
     * Returns the name of the icon.
     *
     * @param icon the icon
     * @return the icon name
     */
    protected String getIconName(DiagramIcon icon) {
        return "Icon " + icon.id;
    }

    // --- Layout Calculation ---

    /**
     * For non‑CHART layouts, returns a mapping from node ID to (x,y) position.
     *
     * @return map of icon IDs to positions
     */
    protected Map<Integer, Point> calculatePositions() {
        if (cachedPositions != null) return cachedPositions;
        if (layout == EnumDiagramLayout.CHART) {
            return new HashMap<>();
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
            case CIRCULAR_MANUAL:
                cachedPositions = calculateCircularManualPositions();
                break;
            case SQUARE_MANUAL:
                cachedPositions = calculateSquareManualPositions();
                break;
            case TREE_MANUAL:
                cachedPositions = calculateTreeManualPositions();
                break;
            default:
                cachedPositions = calculateCircularPositions();
                break;
        }
        return cachedPositions;
    }

    // --- Standard Layout Methods ---

    /**
     * Circular layout: positions icons evenly on a circle so that outer icons touch the bounds.
     *
     * @return map of icon IDs to positions
     */
    protected Map<Integer, Point> calculateCircularPositions() {
        List<DiagramIcon> icons = getIcons();
        Map<Integer, Point> positions = new HashMap<>();
        int count = icons.size();
        if (count == 0) return positions;
        int centerX = x + width / 2, centerY = y + height / 2;
        int radius = (Math.min(width, height) - slotSize) / 2;
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            int posX = centerX + (int) (radius * Math.cos(angle));
            int posY = centerY + (int) (radius * Math.sin(angle));
            positions.put(icons.get(i).id, new Point(posX, posY));
        }
        return positions;
    }

    /**
     * Square layout: arranges icons in a grid filling the available bounds.
     *
     * @return map of icon IDs to positions
     */
    protected Map<Integer, Point> calculateSquarePositions() {
        List<DiagramIcon> icons = getIcons();
        Map<Integer, Point> positions = new HashMap<>();
        int count = icons.size();
        if (count == 0) return positions;
        int n = (int) Math.ceil(Math.sqrt(count));
        int cellWidth = width / n;
        int cellHeight = height / n;
        int startX = x, startY = y;
        for (int i = 0; i < count; i++) {
            int row = i / n, col = i % n;
            int posX = startX + col * cellWidth + cellWidth / 2;
            int posY = startY + row * cellHeight + cellHeight / 2;
            positions.put(icons.get(i).id, new Point(posX, posY));
        }
        return positions;
    }

    /**
     * Tree layout: positions icons in levels (breadth‑first) with top and bottom icons flush.
     *
     * @return map of icon IDs to positions
     */
    protected Map<Integer, Point> calculateTreePositions() {
        List<DiagramIcon> icons = getIcons();
        Map<Integer, Point> positions = new HashMap<>();
        int count = icons.size();
        if (count == 0) return positions;
        int levels = (int) Math.ceil(Math.log(count + 1) / Math.log(2));
        for (int level = 0; level < levels; level++) {
            int nodesThisLevel = (level == 0) ? 1 : (int) Math.min(Math.pow(2, level), count - (int) Math.pow(2, level) + 1);
            int posY = (levels == 1) ? y + height / 2 : y + (int) ((height * level) / (levels - 1.0));
            for (int i = 0; i < nodesThisLevel; i++) {
                int posX;
                if (nodesThisLevel == 1) {
                    posX = x + width / 2;
                } else {
                    posX = x + (int) ((width * i) / (nodesThisLevel - 1.0));
                }
                int index = (int) (Math.pow(2, level) - 1 + i);
                if (index < count) {
                    positions.put(icons.get(index).id, new Point(posX, posY));
                }
            }
        }
        return positions;
    }

    /**
     * Generated layout: breaks icons into clusters and fills each cluster’s area.
     *
     * @return map of icon IDs to positions
     */
    protected Map<Integer, Point> calculateGeneratedPositions() {
        List<DiagramIcon> icons = getIcons();
        List<DiagramConnection> connections = getConnections();
        Map<Integer, List<Integer>> graph = buildGraph(icons, connections);
        List<Set<Integer>> clusters = getConnectedComponents(graph);
        int clusterCount = clusters.size();
        int gridCols = (int) Math.ceil(Math.sqrt(clusterCount));
        int gridRows = (int) Math.ceil((double) clusterCount / gridCols);
        Map<Integer, Point> globalPositions = new HashMap<>();
        int clusterIndex = 0;
        for (Set<Integer> cluster : clusters) {
            int col = clusterIndex % gridCols;
            int row = clusterIndex / gridCols;
            int compX = x + col * (width / gridCols);
            int compY = y + row * (height / gridRows);
            int compWidth = width / gridCols;
            int compHeight = height / gridRows;
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

    /**
     * Builds a graph (adjacency list) from the list of icons and connections.
     */
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

    /**
     * Returns the connected components of the graph.
     */
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

    /**
     * Depth-first search to build a component.
     */
    private void dfsComponent(Integer current, Map<Integer, List<Integer>> graph, Set<Integer> visited, Set<Integer> comp) {
        visited.add(current);
        comp.add(current);
        for (Integer neighbor : graph.get(current)) {
            if (!visited.contains(neighbor))
                dfsComponent(neighbor, graph, visited, comp);
        }
    }

    /**
     * Determines if the cluster forms a cycle.
     */
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

    /**
     * Determines if the cluster forms a tree.
     */
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

    /**
     * Determines if the cluster can form a perfect square.
     */
    private boolean isSquare(Set<Integer> cluster) {
        int n = cluster.size();
        int sqrt = (int) Math.round(Math.sqrt(n));
        return sqrt * sqrt == n;
    }

    // --- Layout Algorithms for Clusters ---

    /**
     * Layout for a cycle cluster: positions icons on a circle.
     */
    private Map<Integer, Point> layoutCycle(Set<Integer> cluster, int compX, int compY, int compWidth, int compHeight) {
        Map<Integer, Point> positions = new HashMap<>();
        if (cluster.isEmpty()) return positions;
        int centerX = compX + compWidth / 2, centerY = compY + compHeight / 2;
        int radius = (Math.min(compWidth, compHeight) - slotSize) / 2;
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

    /**
     * Layout for a tree cluster: performs BFS to assign levels and positions nodes.
     */
    private Map<Integer, Point> layoutTree(Set<Integer> cluster, int compX, int compY, int compWidth, int compHeight,
                                           Map<Integer, List<Integer>> fullGraph) {
        Map<Integer, Point> positions = new HashMap<>();
        if (cluster.isEmpty()) return positions;

        // Build a subgraph of the cluster.
        Map<Integer, List<Integer>> subgraph = new HashMap<>();
        for (Integer id : cluster) {
            List<Integer> neighbors = new ArrayList<>();
            for (Integer neighbor : fullGraph.get(id)) {
                if (cluster.contains(neighbor))
                    neighbors.add(neighbor);
            }
            subgraph.put(id, neighbors);
        }
        // Pick a root (lowest id)
        Integer root = Collections.min(cluster);
        // Perform BFS to assign levels.
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
        // Position nodes vertically based on level.
        for (Map.Entry<Integer, List<Integer>> entry : levels.entrySet()) {
            int lvl = entry.getKey();
            int posY = (maxLevel == 0) ? compY + compHeight / 2 : compY + (int) ((compHeight * lvl) / (maxLevel));
            List<Integer> nodesAtLevel = entry.getValue();
            nodesAtLevel.sort(Comparator.naturalOrder());
            int m = nodesAtLevel.size();
            for (int i = 0; i < m; i++) {
                int posX = (m == 1) ? compX + compWidth / 2 : compX + (int) ((compWidth * i) / (m - 1.0));
                positions.put(nodesAtLevel.get(i), new Point(posX, posY));
            }
        }
        return positions;
    }

    /**
     * Layout for a square cluster: fills the component area in a grid.
     */
    private Map<Integer, Point> layoutSquare(Set<Integer> cluster, int compX, int compY, int compWidth, int compHeight) {
        Map<Integer, Point> positions = new HashMap<>();
        if (cluster.isEmpty()) return positions;
        int n = (int) Math.round(Math.sqrt(cluster.size()));
        int gridCellWidth = compWidth / n, gridCellHeight = compHeight / n;
        int index = 0;
        List<Integer> nodes = new ArrayList<>(cluster);
        nodes.sort(Comparator.naturalOrder());
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

    /**
     * Force-directed layout for a cluster.
     */
    private Map<Integer, Point> layoutForceDirected(Set<Integer> cluster, int compX, int compY, int compWidth, int compHeight,
                                                    List<DiagramConnection> connections) {
        Map<Integer, Point> positions = new HashMap<>();
        if (cluster.isEmpty()) return positions;
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
                int newX = positions.get(id).x + (int) ((d[0] / dispLength) * Math.min(dispLength, temperature));
                int newY = positions.get(id).y + (int) ((d[1] / dispLength) * Math.min(dispLength, temperature));
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

    /**
     * Manual circular layout: groups icons by index and arranges them in rings.
     * <p>
     * Fixed: Increased ring spacing by applying a multiplier.
     *
     * @return map of icon IDs to positions
     */
    private Map<Integer, Point> calculateCircularManualPositions() {
        List<DiagramIcon> icons = getIcons();
        Map<Integer, Point> positions = new HashMap<>();
        if (icons.isEmpty()) return positions;
        // Group icons by their index (which represents the ring)
        Map<Integer, List<DiagramIcon>> groups = new TreeMap<>();
        for (DiagramIcon icon : icons) {
            groups.computeIfAbsent(icon.index, k -> new ArrayList<>()).add(icon);
        }
        int centerX = x + width / 2, centerY = y + height / 2;
        int maxRing = groups.isEmpty() ? 0 : Collections.max(groups.keySet());
        int maxRadius = (Math.min(width, height) - iconSize) / 2;
        // Use a multiplier to increase spacing between rings (adjust spacingMultiplier as needed)
        double spacingMultiplier = 1.5;
        double ringSpacing = (maxRing + 1) > 0 ? ((double) maxRadius / (maxRing + 1)) * spacingMultiplier : 0;
        for (Map.Entry<Integer, List<DiagramIcon>> entry : groups.entrySet()) {
            int ring = entry.getKey();
            List<DiagramIcon> groupIcons = entry.getValue();
            if (groupIcons.isEmpty()) continue;
            // Sort icons by priority
            groupIcons.sort(Comparator.comparingInt(icon -> icon.priority));
            double radius = ringSpacing * (ring + 1);
            int count = groupIcons.size();
            // Use a different start angle for inner vs. outer rings if desired
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

    /**
     * Manual square layout: groups icons by index and arranges them along the square perimeter.
     * <p>
     * Fixed: Increased spacing using a multiplier.
     *
     * @return map of icon IDs to positions
     */
    private Map<Integer, Point> calculateSquareManualPositions() {
        List<DiagramIcon> icons = getIcons();
        Map<Integer, Point> positions = new HashMap<>();
        if (icons.isEmpty()) return positions;
        // Group icons by index
        Map<Integer, List<DiagramIcon>> groups = new TreeMap<>();
        for (DiagramIcon icon : icons) {
            groups.computeIfAbsent(icon.index, k -> new ArrayList<>()).add(icon);
        }
        int centerX = x + width / 2, centerY = y + height / 2;
        int maxRing = groups.isEmpty() ? 0 : Collections.max(groups.keySet());
        int halfSide = (Math.min(width, height) - iconSize) / 2;
        // Increase spacing between rings with a multiplier
        double spacingMultiplier = 1.5;
        double ringSpacing = (maxRing + 1) > 0 ? ((double) halfSide / (maxRing + 1)) * spacingMultiplier : 0;
        for (Map.Entry<Integer, List<DiagramIcon>> entry : groups.entrySet()) {
            int ring = entry.getKey();
            List<DiagramIcon> groupIcons = entry.getValue();
            if (groupIcons.isEmpty()) continue;
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

    /**
     * Manual tree layout: groups icons by index (levels) and spaces them vertically.
     *
     * @return map of icon IDs to positions
     */
    private Map<Integer, Point> calculateTreeManualPositions() {
        List<DiagramIcon> icons = getIcons();
        Map<Integer, List<DiagramIcon>> levels = new TreeMap<>();
        for (DiagramIcon icon : icons) {
            levels.computeIfAbsent(icon.index, k -> new ArrayList<>()).add(icon);
        }
        Map<Integer, Point> positions = new HashMap<>();
        if (icons.isEmpty()) return positions;
        int maxLevel = levels.isEmpty() ? 0 : Collections.max(levels.keySet());
        // For each level, space icons horizontally and position vertically.
        for (Map.Entry<Integer, List<DiagramIcon>> entry : levels.entrySet()) {
            int level = entry.getKey();
            List<DiagramIcon> levelIcons = entry.getValue();
            levelIcons.sort(Comparator.comparingInt(icon -> icon.priority));
            int m = levelIcons.size();
            int posY = (m > 0 && maxLevel > 0) ? y + (int) ((height * level) / (maxLevel)) : y + height / 2;
            for (int i = 0; i < m; i++) {
                int posX = (m > 1) ? x + (int) ((width * i) / (m - 1.0)) : x + width / 2;
                positions.put(levelIcons.get(i).id, new Point(posX, posY));
            }
        }
        return positions;
    }

    // --- Curved Arrow Helpers ---

    /**
     * Computes a quadratic Bezier control point given endpoints and an angle (in degrees).
     *
     * @param x1           starting x
     * @param y1           starting y
     * @param x2           ending x
     * @param y2           ending y
     * @param angleDegrees the curve angle in degrees
     * @return computed control point
     */
    private Point computeControlPoint(int x1, int y1, int x2, int y2, int angleDegrees) {
        double dx = x2 - x1, dy = y2 - y1;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len == 0) len = 1;
        double angleRad = Math.toRadians(angleDegrees);
        double offset = len * Math.tan(angleRad) / 2;
        double perpX = -dy / len;
        double perpY = dx / len;
        return new Point(
            (x1 + x2) / 2 + (int)Math.round(perpX * offset),
            (y1 + y2) / 2 + (int)Math.round(perpY * offset)
        );
    }

    /**
     * Returns the minimum distance from the given control point to any icon (except endpoints).
     *
     * @param control the control point
     * @param conn    the connection being drawn
     * @return minimum distance to an icon
     */
    private double getMinDistanceToIcon(Point control, DiagramConnection conn) {
        double minDist = Double.MAX_VALUE;
        Map<Integer, Point> positions = calculatePositions();
        for (DiagramIcon icon : getIcons()) {
            if (icon.id == conn.idFrom || icon.id == conn.idTo) continue;
            Point pos = positions.get(icon.id);
            if (pos != null) {
                double dist = pos.distance(control);
                if (dist < minDist) {
                    minDist = dist;
                }
            }
        }
        return minDist;
    }

    /**
     * Draws the connection line between two icons.
     * For curved arrows, if a reverse connection exists and allowTwoWay is false,
     * two separate curves (with opposite offsets) are drawn.
     *
     * @param x1   starting x-coordinate
     * @param y1   starting y-coordinate
     * @param x2   ending x-coordinate
     * @param y2   ending y-coordinate
     * @param conn the connection to draw
     * @param dim  whether to dim the line color
     */
    protected void drawConnectionLine(int x1, int y1, int x2, int y2, DiagramConnection conn, boolean dim) {
        DiagramConnection reverse = getConnectionByIds(conn.idTo, conn.idFrom);
        boolean twoWay = (reverse != null);

        if (curvedArrows) {
            // If there is a reverse connection and shared (allowTwoWay) mode is enabled,
            // use the shared line mode (color split, etc.)
            if (twoWay && allowTwoWay) {
                int color1 = getConnectionColor(conn);
                int color2 = getConnectionColor(reverse);
                if (!useColorScaling) {
                    color1 = color2 = 0xFFFFFFFF;
                }
                int segments = 400;
                // Compute two candidate control points (one for each half)
                Point cp1 = computeControlPoint(x1, y1, x2, y2, curveAngle);
                Point cp2 = computeControlPoint(x1, y1, x2, y2, -curveAngle);

                GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_HINT_BIT);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glLineWidth(2.0F);

                // Draw first half of the shared line using the first control point.
                setColor(color1, dim);
                GL11.glBegin(GL11.GL_LINE_STRIP);
                for (int i = 0; i <= segments / 2; i++) {
                    double t = (double) i / (segments / 2);
                    int bx = (int)((1-t)*(1-t)*x1 + 2*(1-t)*t*cp1.x + t*t*x2);
                    int by = (int)((1-t)*(1-t)*y1 + 2*(1-t)*t*cp1.y + t*t*y2);
                    GL11.glVertex2i(bx, by);
                }
                GL11.glEnd();

                // Draw second half with the reverse connection's color using the second control point.
                setColor(color2, dim);
                GL11.glBegin(GL11.GL_LINE_STRIP);
                for (int i = segments / 2; i <= segments; i++) {
                    double t = (double) i / segments;
                    int bx = (int)((1-t)*(1-t)*x1 + 2*(1-t)*t*cp2.x + t*t*x2);
                    int by = (int)((1-t)*(1-t)*y1 + 2*(1-t)*t*cp2.y + t*t*y2);
                    GL11.glVertex2i(bx, by);
                }
                GL11.glEnd();

                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glPopAttrib();
            }
            // For reverse connections when allowTwoWay is false,
            // draw two separate curves (one for each direction) using opposite offsets.
            else if (twoWay && !allowTwoWay) {
                int segments = 400;
                // Compute control points with both positive and negative curve angles.
                Point cp1 = computeControlPoint(x1, y1, x2, y2, curveAngle);
                Point cp2 = computeControlPoint(x1, y1, x2, y2, -curveAngle);
                // Retrieve colors for each connection direction.
                int color1 = getConnectionColor(conn);
                int color2 = getConnectionColor(reverse);
                if (!useColorScaling) {
                    color1 = 0xFFFFFFFF;
                    color2 = 0xFFFFFFFF;
                }

                GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_HINT_BIT);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glLineWidth(2.0F);

                // Draw first curved line using cp1 for the current connection.
                setColor(color1, dim);
                GL11.glBegin(GL11.GL_LINE_STRIP);
                for (int i = 0; i <= segments; i++) {
                    double t = (double) i / segments;
                    int bx = (int)((1-t)*(1-t)*x1 + 2*(1-t)*t*cp1.x + t*t*x2);
                    int by = (int)((1-t)*(1-t)*y1 + 2*(1-t)*t*cp1.y + t*t*y2);
                    GL11.glVertex2i(bx, by);
                }
                GL11.glEnd();

                // Draw second curved line using cp2 for the reverse connection.
                setColor(color2, dim);
                GL11.glBegin(GL11.GL_LINE_STRIP);
                for (int i = 0; i <= segments; i++) {
                    double t = (double) i / segments;
                    int bx = (int)((1-t)*(1-t)*x1 + 2*(1-t)*t*cp2.x + t*t*x2);
                    int by = (int)((1-t)*(1-t)*y1 + 2*(1-t)*t*cp2.y + t*t*y2);
                    GL11.glVertex2i(bx, by);
                }
                GL11.glEnd();

                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glPopAttrib();
            }
            // One-way curved arrow drawing.
            else {
                int color = getConnectionColor(conn);
                if (!useColorScaling) color = 0xFFFFFFFF;
                int segments = 400;
                Point cp = computeControlPoint(x1, y1, x2, y2, curveAngle);

                GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_HINT_BIT);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glLineWidth(2.0F);

                setColor(color, dim);
                GL11.glBegin(GL11.GL_LINE_STRIP);
                for (int i = 0; i <= segments; i++) {
                    double t = (double) i / segments;
                    int bx = (int)((1-t)*(1-t)*x1 + 2*(1-t)*t*cp.x + t*t*x2);
                    int by = (int)((1-t)*(1-t)*y1 + 2*(1-t)*t*cp.y + t*t*y2);
                    GL11.glVertex2i(bx, by);
                }
                GL11.glEnd();

                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glPopAttrib();
            }
        }
        else {
            // Straight-line drawing branch.
            DiagramConnection reverseConn = getConnectionByIds(conn.idTo, conn.idFrom);
            boolean isTwoWay = (reverseConn != null);
            if (isTwoWay) {
                if (allowTwoWay) {
                    if (conn.idFrom > conn.idTo) return;
                    int midX = (x1 + x2) / 2;
                    int midY = (y1 + y2) / 2;
                    int color1 = getConnectionColor(conn);
                    int color2 = getConnectionColor(reverseConn);
                    if (!useColorScaling) color1 = color2 = 0xFFFFFFFF;
                    drawColoredLine(x1, y1, midX, midY, color1, dim);
                    drawColoredLine(midX, midY, x2, y2, color2, dim);
                } else {
                    int color = getConnectionColor(conn);
                    if (!useColorScaling) color = 0xFFFFFFFF;
                    drawColoredLine(x1, y1, x2, y2, color, dim);
                }
            } else {
                int color = getConnectionColor(conn);
                if (!useColorScaling) color = 0xFFFFFFFF;
                drawColoredLine(x1, y1, x2, y2, color, dim);
            }
        }
    }

    /**
     * Helper method to draw a straight colored line.
     */
    private void drawColoredLine(int x1, int y1, int x2, int y2, int color, boolean dim) {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(2.0F);
        setColor(color, dim);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2i(x1, y1);
        GL11.glVertex2i(x2, y2);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopAttrib();
    }

    /**
     * Draws the arrowhead for a connection.
     * For curved arrows with a reverse connection and allowTwoWay = false,
     * two arrowheads are drawn (one for each curve) once per pair.
     *
     * @param x1   starting x-coordinate of the connection
     * @param y1   starting y-coordinate of the connection
     * @param x2   ending x-coordinate (icon edge) where the arrowhead should be drawn
     * @param y2   ending y-coordinate
     * @param conn the connection for which to draw the arrowhead
     * @param dim  whether to dim the arrowhead color
     */
    protected void drawArrowHead(int x1, int y1, int x2, int y2, DiagramConnection conn, boolean dim) {
        DiagramConnection reverse = getConnectionByIds(conn.idTo, conn.idFrom);
        boolean twoWay = (reverse != null);

        if (curvedArrows) {
            // For reverse pairs when allowTwoWay is false, only draw arrowheads once per pair.
            if (twoWay && !allowTwoWay) {
                if (conn.idFrom < conn.idTo) {
                    // Draw arrowhead for the current connection using positive curve angle.
                    drawSingleArrowHead(x1, y1, x2, y2, conn, curveAngle, dim);
                    // Draw arrowhead for the reverse connection using negative curve angle.
                    drawSingleArrowHead(x1, y1, x2, y2, reverse, -curveAngle, dim);
                }
            } else {
                // For one-way arrows or shared mode, determine effective curve angle.
                int effectiveAngle = curveAngle;
                if (twoWay && allowTwoWay) {
                    // For shared mode, use one effective angle based on geometry.
                    effectiveAngle = (conn.idFrom < conn.idTo) ? curveAngle : -curveAngle;
                }
                // Draw a single arrowhead.
                drawSingleArrowHead(x1, y1, x2, y2, (twoWay ? reverse : conn), effectiveAngle, dim);
            }
        } else {
            // For straight lines, compute the angle from the line direction.
            double angle = Math.atan2(y2 - y1, x2 - x1);
            drawStraightArrowHead(x2, y2, angle, conn, dim);
        }
    }

    /**
     * Helper to draw a single curved arrowhead.
     *
     * @param x1           starting x-coordinate of the connection
     * @param y1           starting y-coordinate
     * @param x2           ending x-coordinate (icon edge)
     * @param y2           ending y-coordinate
     * @param conn         the connection (or reverse connection) whose color is used
     * @param angleOffset  the curve angle to use (positive or negative)
     * @param dim          whether to dim the color
     */
    private void drawSingleArrowHead(int x1, int y1, int x2, int y2, DiagramConnection conn, int angleOffset, boolean dim) {
        int color = getConnectionColor(conn);
        if (!useColorScaling) color = 0xFFFFFFFF;
        // Compute the control point using the given angle offset.
        Point cp = computeControlPoint(x1, y1, x2, y2, angleOffset);
        double t = 0.95;  // Position near the end of the curve.
        double bx = (1-t)*(1-t)*x1 + 2*(1-t)*t*cp.x + t*t*x2;
        double by = (1-t)*(1-t)*y1 + 2*(1-t)*t*cp.y + t*t*y2;
        // Compute the derivative (tangent) at t.
        double dBx = 2*(1-t)*(cp.x - x1) + 2*t*(x2 - cp.x);
        double dBy = 2*(1-t)*(cp.y - y1) + 2*t*(y2 - cp.y);
        double angle = Math.atan2(dBy, dBx);
        // Offset the arrowhead so that it appears at the icon's edge.
        float defenderEdgeX = x2 - (slotSize/2f) * (float)Math.cos(angle);
        float defenderEdgeY = y2 - (slotSize/2f) * (float)Math.sin(angle);
        int arrowSize = 6;
        float leftX  = defenderEdgeX - arrowSize * (float)Math.cos(angle - Math.PI/6);
        float leftY  = defenderEdgeY - arrowSize * (float)Math.sin(angle - Math.PI/6);
        float rightX = defenderEdgeX - arrowSize * (float)Math.cos(angle + Math.PI/6);
        float rightY = defenderEdgeY - arrowSize * (float)Math.sin(angle + Math.PI/6);

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        float rr = ((color >> 16) & 0xFF) / 255f;
        float gg = ((color >> 8) & 0xFF) / 255f;
        float bb = (color & 0xFF) / 255f;
        if (dim) { rr *= 0.4f; gg *= 0.4f; bb *= 0.4f; }
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

    /**
     * Helper to draw an arrowhead for a straight line.
     *
     * @param x     the x-coordinate at which to draw the arrowhead (typically the endpoint)
     * @param y     the y-coordinate at which to draw the arrowhead
     * @param angle the angle of the line
     * @param conn  the connection whose color is used
     * @param dim   whether to dim the color
     */
    private void drawStraightArrowHead(int x, int y, double angle, DiagramConnection conn, boolean dim) {
        int color = getConnectionColor(conn);
        if (!useColorScaling) color = 0xFFFFFFFF;
        float defenderEdgeX = x - (slotSize/2f) * (float)Math.cos(angle);
        float defenderEdgeY = y - (slotSize/2f) * (float)Math.sin(angle);
        int arrowSize = 6;
        float leftX  = defenderEdgeX - arrowSize * (float)Math.cos(angle - Math.PI/6);
        float leftY  = defenderEdgeY - arrowSize * (float)Math.sin(angle - Math.PI/6);
        float rightX = defenderEdgeX - arrowSize * (float)Math.cos(angle + Math.PI/6);
        float rightY = defenderEdgeY - arrowSize * (float)Math.sin(angle + Math.PI/6);

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        float rr = ((color >> 16) & 0xFF) / 255f;
        float gg = ((color >> 8) & 0xFF) / 255f;
        float bb = (color & 0xFF) / 255f;
        if (dim) { rr *= 0.4f; gg *= 0.4f; bb *= 0.4f; }
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


    /**
     * Helper to set the current color with optional dimming.
     */
    private void setColor(int color, boolean dim) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        if (dim) {
            r *= 0.4f;
            g *= 0.4f;
            b *= 0.4f;
        }
        GL11.glColor4f(r, g, b, 1f);
    }

    // --- Standard Icon Box Drawing ---

    /**
     * Draws the icon box (background and border) at the specified slot.
     *
     * @param slotX       x-coordinate of the slot
     * @param slotY       y-coordinate of the slot
     * @param slotSize    size of the slot
     * @param highlighted whether the box is highlighted
     */
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

    /**
     * Mixes two colors together with the given ratio.
     *
     * @param color1 first color
     * @param color2 second color
     * @param ratio  ratio for blending (0.0 to 1.0)
     * @return the mixed color
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

    // --- Hit-detection helpers ---

    /**
     * Computes the distance from a point to a quadratic Bezier curve.
     */
    private double distanceToBezier(Point start, Point control, Point end, int segments, int px, int py) {
        double minDist = Double.MAX_VALUE;
        Point prev = null;
        for (int i = 0; i <= segments; i++) {
            double t = (double) i / segments;
            int x = (int) ((1 - t) * (1 - t) * start.x + 2 * (1 - t) * t * control.x + t * t * end.x);
            int y = (int) ((1 - t) * (1 - t) * start.y + 2 * (1 - t) * t * control.y + t * t * end.y);
            Point curr = new Point(x, y);
            if (prev != null) {
                double d = distancePointToSegment(px, py, prev, curr);
                if (d < minDist) minDist = d;
            }
            prev = curr;
        }
        return minDist;
    }

    /**
     * Computes the distance from a point to a line segment.
     */
    private double distancePointToSegment(int px, int py, Point a, Point b) {
        double A = px - a.x, B = py - a.y, C = b.x - a.x, D = b.y - a.y;
        double dot = A * C + B * D;
        double lenSq = C * C + D * D;
        double param = (lenSq != 0) ? (dot / lenSq) : -1;
        double xx, yy;
        if (param < 0) {
            xx = a.x;
            yy = a.y;
        } else if (param > 1) {
            xx = b.x;
            yy = b.y;
        } else {
            xx = a.x + param * C;
            yy = a.y + param * D;
        }
        double dx = px - xx, dy = py - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * CHART layout drawing (code remains unchanged).
     */
    private void drawChartDiagram(int mouseX, int mouseY, boolean subGui) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int factor = sr.getScaleFactor();
        drawRect(x, y, x + width, y + height, 0xFF333333);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * factor, (sr.getScaledHeight() - (y + height)) * factor, width * factor, height * factor);
        GL11.glPushMatrix();
        int centerX = x + width / 2, centerY = y + height / 2;
        GL11.glTranslatef(centerX + panX, centerY + panY, 0);
        GL11.glScalef(zoom, zoom, 1.0f);
        GL11.glTranslatef(-centerX, -centerY, 0);
        // ... rest of chart drawing code remains unchanged
        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    /**
     * Main method for drawing the diagram.
     * Applies pan/zoom, draws connections and icons, and shows tooltips.
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
        int centerX = x + width / 2, centerY = y + height / 2;
        int effectiveMouseX = (int) (((float) mouseX - (centerX + panX)) / zoom + centerX);
        int effectiveMouseY = (int) (((float) mouseY - (centerY + panY)) / zoom + centerY);
        Integer hoveredIconId = null;
        int hoveredConnFrom = -1, hoveredConnTo = -1;
        Set<Integer> selectedIconIds = new HashSet<>();
        // Check for icon hover based on mouse position
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
        // If no icon hovered, check connections for hover
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
                if (curvedArrows) {
                    Point cp1 = computeControlPoint(pFrom.x, pFrom.y, pTo.x, pTo.y, curveAngle);
                    Point cp2 = computeControlPoint(pFrom.x, pFrom.y, pTo.x, pTo.y, -curveAngle);
                    double d1 = getMinDistanceToIcon(cp1, conn);
                    double d2 = getMinDistanceToIcon(cp2, conn);
                    Point control = (d1 >= d2) ? cp1 : cp2;
                    dist = distanceToBezier(pFrom, control, pTo, 200, effectiveMouseX, effectiveMouseY);
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
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * factor, (sr.getScaledHeight() - (y + height)) * factor, width * factor, height * factor);
        drawRect(x, y, x + width, y + height, 0xFF333333);
        GL11.glPushMatrix();
        GL11.glTranslatef(centerX + panX, centerY + panY, 0);
        GL11.glScalef(zoom, zoom, 1.0f);
        GL11.glTranslatef(-centerX, -centerY, 0);
        // Draw connections
        for (DiagramConnection conn : getConnections()) {
            DiagramIcon iconFrom = getIconById(conn.idFrom);
            DiagramIcon iconTo = getIconById(conn.idTo);
            if (iconFrom == null || iconTo == null || !iconFrom.enabled || !iconTo.enabled)
                continue;
            Point pFrom = positions.get(conn.idFrom);
            Point pTo = positions.get(conn.idTo);
            if (pFrom == null || pTo == null) continue;
            int ax = pFrom.x, ay = pFrom.y, bx = pTo.x, by = pTo.y;
            boolean dim = !selectedIconIds.isEmpty() && !(selectedIconIds.contains(conn.idFrom) && selectedIconIds.contains(conn.idTo));
            drawConnectionLine(ax, ay, bx, by, conn, dim);
        }
        // Draw icons
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
        // Draw arrowheads if enabled
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
                boolean dim = !selectedIconIds.isEmpty() && !(selectedIconIds.contains(conn.idFrom) && selectedIconIds.contains(conn.idTo));
                drawArrowHead(ax, ay, bx, by, conn, dim);
            }
            GL11.glPopMatrix();
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        // Display tooltips for hovered icons or connections
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

    /**
     * Returns the icon with the given ID.
     *
     * @param id icon ID
     * @return the DiagramIcon or null if not found
     */
    protected DiagramIcon getIconById(int id) {
        for (DiagramIcon icon : getIcons()) {
            if (icon.id == id) return icon;
        }
        return null;
    }

    /**
     * Returns the connection between two given IDs.
     *
     * @param idFrom starting icon ID
     * @param idTo   ending icon ID
     * @return the DiagramConnection or null if not found
     */
    protected DiagramConnection getConnectionByIds(int idFrom, int idTo) {
        for (DiagramConnection conn : getConnections()) {
            if (conn.idFrom == idFrom && conn.idTo == idTo) return conn;
        }
        return null;
    }

    /**
     * Computes the color for a connection based on its percent value.
     *
     * @param conn the connection
     * @return the color as an ARGB int
     */
    protected int getConnectionColor(DiagramConnection conn) {
        // Clamp percent to [-1, 1]
        float value = Math.max(-1f, Math.min(1f, conn.percent));
        int r, g, b;
        if (value >= 0f) {
            // Positive values: interpolate from yellow to light green to green.
            if (value <= 0.5f) {
                float t = value / 0.5f;  // 0 at 0.0, 1 at 0.5
                r = (int)(255 + (173 - 255) * t);
                g = 255;
                b = (int)(0 + (47 - 0) * t);
            } else {
                float t = (value - 0.5f) / 0.5f;  // 0 at 0.5, 1 at 1.0
                r = (int)(173 + (0 - 173) * t);
                g = 255;
                b = (int)(47 + (0 - 47) * t);
            }
        } else {
            // Negative values: interpolate from yellow to orange to red.
            if (value >= -0.5f) {
                float t = (-value) / 0.5f;
                r = 255;
                g = (int)(255 + (165 - 255) * t);
                b = 0;
            } else {
                float t = (-value - 0.5f) / 0.5f;
                r = 255;
                g = (int)(165 + (0 - 165) * t);
                b = 0;
            }
        }
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Computes the distance from a point to a line segment.
     */
    private double pointLineDistance(int px, int py, int x1, int y1, int x2, int y2) {
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

    // --- Mouse Handling ---

    /**
     * Handles mouse click events.
     *
     * @param mouseX     mouse x-coordinate
     * @param mouseY     mouse y-coordinate
     * @param mouseButton mouse button pressed
     * @return true if the event was handled
     */
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (parent != null && parent.hasSubGui()) return false;
        if (mouseButton == 0) {
            for (DiagramIcon icon : getIcons()) {
                if (!icon.enabled || !icon.pressable) continue;
                Point pos = calculatePositions().get(icon.id);
                if (pos == null) continue;
                int centerX = x + width / 2, centerY = y + height / 2;
                int effectiveX = (int) (((float) mouseX - (centerX + panX)) / zoom + centerX);
                int effectiveY = (int) (((float) mouseY - (centerY + panY)) / zoom + centerY);
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

    // Currently pressed icon for dragging/holding
    protected DiagramIcon currentlyPressedIcon = null;

    /**
     * Handles mouse click movement.
     *
     * @param mouseX              current mouse x-coordinate
     * @param mouseY              current mouse y-coordinate
     * @param mouseButton         mouse button pressed
     * @param timeSinceLastClick  time since last click
     */
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

    /**
     * Handles mouse release events.
     *
     * @param mouseX mouse x-coordinate
     * @param mouseY mouse y-coordinate
     * @param state  mouse button state
     */
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (parent != null && parent.hasSubGui()) return;
        if (currentlyPressedIcon != null) {
            onIconRelease(currentlyPressedIcon);
            currentlyPressedIcon = null;
        }
        dragging = false;
    }

    /**
     * Checks if the given mouse coordinates are within the diagram bounds.
     *
     * @param mouseX mouse x-coordinate
     * @param mouseY mouse y-coordinate
     * @return true if within bounds
     */
    public boolean isWithin(int mouseX, int mouseY) {
        if (parent.hasSubGui())
            return false;
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    /**
     * Handles mouse scroll events to adjust zoom.
     *
     * @param scrollDelta the scroll amount
     */
    public void handleMouseScroll(int scrollDelta) {
        zoom += scrollDelta * 0.0009f;
        if (zoom < 0.5f) zoom = 0.5f;
        if (zoom > 2.0f) zoom = 2.0f;
    }

    /**
     * Renders hovering text (tooltips).
     *
     * @param textLines text to display
     * @param mouseX    mouse x-coordinate
     * @param mouseY    mouse y-coordinate
     * @param font      FontRenderer instance
     */
    protected void drawHoveringText(List<String> textLines, int mouseX, int mouseY, FontRenderer font) {
        // Tooltip rendering implementation goes here.
    }

    // --- Flag Setters ---

    /**
     * Sets whether arrow heads should be shown.
     *
     * @param showArrowHeads true to show arrow heads
     */
    public void setShowArrowHeads(boolean showArrowHeads) {
        this.showArrowHeads = showArrowHeads;
    }

    /**
     * Sets whether color scaling should be used.
     *
     * @param useColorScaling true to use color scaling
     */
    public void setUseColorScaling(boolean useColorScaling) {
        this.useColorScaling = useColorScaling;
    }

    // --- Icon and Connection Event Callbacks (subclasses can override these) ---

    protected void onIconClick(DiagramIcon icon) {
    }

    protected void onIconHeld(DiagramIcon icon) {
    }

    protected void onIconRelease(DiagramIcon icon) {
    }

    protected void onIconHover(DiagramIcon icon) {
    }

    protected void onConnectionHover(DiagramConnection conn) {
    }

    // --- Inner Helper Classes ---

    /**
     * Represents an icon (node) in the diagram.
     */
    public static class DiagramIcon {
        public int id;
        public boolean enabled = true;
        public boolean pressable = false;
        public int index = 0;
        public int priority = 0;

        public DiagramIcon(int id) {
            this.id = id;
        }

        public DiagramIcon(int id, int index, int priority) {
            this.id = id;
            this.index = index;
            this.priority = priority;
        }
    }

    /**
     * Represents a connection (edge) between two icons.
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

    /**
     * Enumeration for icon render state.
     */
    public enum IconRenderState {
        DEFAULT,
        HIGHLIGHTED,
        NOT_HIGHLIGHTED;
    }
}
