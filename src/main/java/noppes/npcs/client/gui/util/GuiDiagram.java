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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

/**
 * Abstract GUI class for drawing diagrams with icons (nodes) and connections (arrows).
 * <p>
 * This revised version ensures that non‑Chart layouts use the full diagram bounds for positioning
 * and that curved arrow drawing selects the best control point (using both curveAngle and its negative)
 * to avoid icons.
 */
public abstract class GuiDiagram extends Gui {

    protected RenderItem renderItem = new RenderItem();

    // --- Diagram Area & Controls ---
    protected int x, y, width, height;
    protected float panX = 0, panY = 0;
    protected float zoom = 1.0f;
    protected boolean dragging = false;
    protected int lastDragX, lastDragY;

    // --- Node (Icon) Sizing ---
    protected int iconSize = 16;
    protected int slotPadding = 4;
    protected int slotSize = iconSize + slotPadding;

    // --- Icon Box Appearance ---
    protected int iconBackgroundColor = 0xFF999999;
    protected int iconBorderColor = 0xFF555555;
    protected int iconBorderThickness = 1;

    // --- Connection Options ---
    protected boolean showArrowHeads = true;
    protected boolean useColorScaling = true;

    // --- Arrow Options ---
    protected float arrowSize = 6;
    protected float lineThickness = 2f;

    // This flag determines how two‑way connections are drawn.
    // When true, a two‑way connection is drawn as one line with dual arrowheads and combined tooltips.
    // When false, each direction is drawn as a separate line with its own hitbox and tooltip.
    protected boolean allowTwoWay = false;

    // --- Layout Options ---
    protected EnumDiagramLayout layout = EnumDiagramLayout.CIRCULAR;

    public void setLayout(EnumDiagramLayout layout) {
        this.layout = layout;
        invalidateCache();
    }

    // --- Arrow Drawing Options ---
    protected boolean curvedArrows = true;
    protected int curveAngle = 15; // in degrees

    public void setCurvedArrows(boolean curved) {
        this.curvedArrows = curved;
    }

    public void setCurveAngle(int angle) {
        this.curveAngle = angle;
    }

    // --- Caching for icons and connections ---
    protected Map<Integer, Point> cachedPositions = null;
    protected List<DiagramIcon> iconsCache = null;
    protected List<DiagramConnection> connectionsCache = null;

    public void invalidateCache() {
        cachedPositions = null;
        iconsCache = null;
        connectionsCache = null;
    }

    // --- Parent reference ---
    protected GuiNPCInterface parent;

    public GuiDiagram(GuiNPCInterface parent, int x, int y, int width, int height) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // --- Data Providers (subclasses must implement) ---
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

    protected List<String> getIconTooltip(DiagramIcon icon) {
        return null;
    }

    /**
     * Returns the tooltip for a connection. When allowTwoWay is true the tooltip combines both directions,
     * otherwise it shows only the current connection's info.
     */
    protected List<String> getConnectionTooltip(DiagramConnection conn) {
        List<String> tooltip = new ArrayList<>();
        DiagramIcon iconFrom = getIconById(conn.idFrom);
        DiagramIcon iconTo = getIconById(conn.idTo);
        String nameFrom = iconFrom != null ? getIconName(iconFrom) : "Unknown";
        String nameTo = iconTo != null ? getIconName(iconTo) : "Unknown";
        tooltip.add(nameFrom + " > " + nameTo + ":");
        // Split Hover Text by newlines
        if (conn.hoverText != null && !conn.hoverText.isEmpty()) {
            String[] lines = conn.hoverText.split("\n");
            tooltip.addAll(Arrays.asList(lines));
        }
        if (allowTwoWay) {
            DiagramConnection reverse = getConnectionByIds(conn.idTo, conn.idFrom);
            if (reverse != null) {
                tooltip.add(nameTo + " > " + nameFrom + ":");
                // Split Hover Text by newlines
                if (reverse.hoverText != null && !reverse.hoverText.isEmpty()) {
                    String[] lines = reverse.hoverText.split("\n");
                    tooltip.addAll(Arrays.asList(lines));
                }
            }
        }
        return tooltip;
    }

    protected String getIconName(DiagramIcon icon) {
        return "Icon " + icon.id;
    }

    // --- Layout Calculation ---

    /**
     * For non‑CHART layouts, returns a mapping from node ID to (x,y) position.
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
            case MANUAL:
                cachedPositions = calculateManualPositions();
                break;
            default:
                cachedPositions = calculateCircularPositions();
                break;
        }
        return cachedPositions;
    }

    protected Map<Integer, Point> calculateManualPositions() {
        Map<Integer, Point> positions = new HashMap<>();
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        for (DiagramIcon icon : getIcons()) {
            positions.put(icon.id, new Point(centerX + icon.index, centerY - icon.priority));
        }
        return positions;
    }

    // --- Standard Layout Methods ---

    // Circular: use the full available radius so that outer icons are flush with bounds.
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

    // Square: fill the entire bounds.
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

    // Tree: distribute levels vertically and icons horizontally.
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
                int posX = (nodesThisLevel == 1) ? x + width / 2 : x + (int) ((width * i) / (nodesThisLevel - 1.0));
                int index = (int) (Math.pow(2, level) - 1 + i);
                if (index < count) {
                    positions.put(icons.get(index).id, new Point(posX, posY));
                }
            }
        }
        return positions;
    }

    // Generated layout methods (unchanged)...
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
        int sqrt = (int) Math.round(Math.sqrt(n));
        return sqrt * sqrt == n;
    }

    // Layout algorithms for clusters (unchanged methods: layoutCycle, layoutTree, layoutSquare, layoutForceDirected)...
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

    private Map<Integer, Point> layoutTree(Set<Integer> cluster, int compX, int compY, int compWidth, int compHeight,
                                           Map<Integer, List<Integer>> fullGraph) {
        Map<Integer, Point> positions = new HashMap<>();
        if (cluster.isEmpty()) return positions;

        Map<Integer, List<Integer>> subgraph = new HashMap<>();
        for (Integer id : cluster) {
            List<Integer> neighbors = new ArrayList<>();
            for (Integer neighbor : fullGraph.get(id)) {
                if (cluster.contains(neighbor))
                    neighbors.add(neighbor);
            }
            subgraph.put(id, neighbors);
        }
        Integer root = Collections.min(cluster);
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
    private Map<Integer, Point> calculateCircularManualPositions() {
        List<DiagramIcon> icons = getIcons();
        Map<Integer, Point> positions = new HashMap<>();
        if (icons.isEmpty()) return positions;
        // Group icons by their manual index.
        Map<Integer, List<DiagramIcon>> groups = new TreeMap<>();
        for (DiagramIcon icon : icons) {
            groups.computeIfAbsent(icon.index, k -> new ArrayList<>()).add(icon);
        }
        int centerX = x + width / 2, centerY = y + height / 2;
        int maxRing = groups.isEmpty() ? 0 : Collections.max(groups.keySet());
        int maxRadius = (Math.min(width, height) - iconSize) / 2;

        // Adjustable parameters:
        // spacingExponent controls the non-linear spacing between rings. Increasing this value will push outer rings farther apart.
        double spacingExponent = 1.5; // default value; adjust as needed
        // ringMultiplier lets you directly scale the computed radius of each ring.
        double ringMultiplier = 1.5; // default value; adjust as needed

        // Calculate positions for each ring using a power function and multiplier.
        for (Map.Entry<Integer, List<DiagramIcon>> entry : groups.entrySet()) {
            int ring = entry.getKey();
            List<DiagramIcon> groupIcons = entry.getValue();
            if (groupIcons.isEmpty()) continue;
            groupIcons.sort(Comparator.comparingInt(icon -> icon.priority));
            double normalizedRing = (ring + 1) / (double) (maxRing + 1);
            double radius = maxRadius * Math.pow(normalizedRing, spacingExponent) * ringMultiplier;
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
        if (icons.isEmpty()) return positions;
        Map<Integer, List<DiagramIcon>> groups = new TreeMap<>();
        for (DiagramIcon icon : icons) {
            groups.computeIfAbsent(icon.index, k -> new ArrayList<>()).add(icon);
        }
        int centerX = x + width / 2, centerY = y + height / 2;
        int maxRing = groups.isEmpty() ? 0 : Collections.max(groups.keySet());
        int halfSide = (Math.min(width, height) - iconSize) / 2;
        double ringSpacing = (maxRing + 1) > 0 ? (double) halfSide / (maxRing + 1) : 0;
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

    private Map<Integer, Point> calculateTreeManualPositions() {
        List<DiagramIcon> icons = getIcons();
        Map<Integer, List<DiagramIcon>> levels = new TreeMap<>();
        for (DiagramIcon icon : icons) {
            levels.computeIfAbsent(icon.index, k -> new ArrayList<>()).add(icon);
        }
        Map<Integer, Point> positions = new HashMap<>();
        if (icons.isEmpty()) return positions;
        int maxLevel = levels.isEmpty() ? 0 : Collections.max(levels.keySet());
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
     */
    private Point computeControlPoint(int x1, int y1, int x2, int y2, int angleDegrees) {
        double dx = x2 - x1, dy = y2 - y1;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len == 0) len = 1;
        double angleRad = Math.toRadians(angleDegrees);
        double offset = len * Math.tan(angleRad) / 2;
        double perpX = -dy / len, perpY = dx / len;
        return new Point((x1 + x2) / 2 + (int) (perpX * offset),
            (y1 + y2) / 2 + (int) (perpY * offset));
    }

    /**
     * Returns the minimum distance from the given control point to any icon (except endpoints).
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

    // --- Arrow Drawing Methods ---

    /**
     * Draws a connection line between two endpoints.
     * If a reverse connection exists and allowTwoWay is false, the line is drawn with a perpendicular offset.
     */
    protected void drawConnectionLine(int x1, int y1, int x2, int y2, DiagramConnection conn, boolean dim) {
        DiagramConnection reverse = getConnectionByIds(conn.idTo, conn.idFrom);
        boolean twoWay = (reverse != null && allowTwoWay);
        boolean separateTwoWay = (reverse != null && !allowTwoWay);
        int effectiveCurveAngle = (conn.customCurveAngle != null ? conn.customCurveAngle : this.curveAngle);
        boolean useCustomCurveAngle = (conn.customAllowCurve != null ? conn.customAllowCurve : this.curvedArrows);

        if (!useCustomCurveAngle) {
            if (separateTwoWay) {
                int offsetAmount = 4;
                double dx = x2 - x1, dy = y2 - y1;
                double len = Math.sqrt(dx * dx + dy * dy);
                if (len == 0) len = 1;
                double offsetX = -dy / len * offsetAmount;
                double offsetY = dx / len * offsetAmount;
                if (conn.idFrom >= conn.idTo) {
                    offsetX = -offsetX;
                    offsetY = -offsetY;
                }
                int newX1 = x1 + (int) offsetX;
                int newY1 = y1 + (int) offsetY;
                int newX2 = x2 + (int) offsetX;
                int newY2 = y2 + (int) offsetY;
                int color = getConnectionColor(conn);
                if (!useColorScaling) {
                    color = 0xFFFFFFFF;
                }
                drawColoredLine(newX1, newY1, newX2, newY2, color, dim);
            } else if (twoWay) {
                int midX = (x1 + x2) / 2;
                int midY = (y1 + y2) / 2;
                int color1 = getConnectionColor(reverse);
                int color2 = getConnectionColor(conn);
                if (!useColorScaling) {
                    color1 = color2 = 0xFFFFFFFF;
                }
                drawColoredLine(x1, y1, midX, midY, color1, dim);
                drawColoredLine(midX, midY, x2, y2, color2, dim);
            } else {
                int color = getConnectionColor(conn);
                if (!useColorScaling) {
                    color = 0xFFFFFFFF;
                }
                drawColoredLine(x1, y1, x2, y2, color, dim);
            }
        } else {
            // Curved arrows
            if (separateTwoWay) {
                int offsetAmount = 4;
                double dx = x2 - x1, dy = y2 - y1;
                double len = Math.sqrt(dx * dx + dy * dy);
                if (len == 0) len = 1;
                double normX = -dy / len;
                double normY = dx / len;
                double sign = (conn.idFrom < conn.idTo) ? 1 : -1;
                Point baseControl = computeControlPoint(x1, y1, x2, y2, effectiveCurveAngle);
                Point offsetControl = new Point(baseControl.x + (int) (normX * offsetAmount * sign),
                    baseControl.y + (int) (normY * offsetAmount * sign));
                int segments = 800;
                int color = getConnectionColor(conn);
                if (!useColorScaling) {
                    color = 0xFFFFFFFF;
                }
                GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_HINT_BIT);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glLineWidth(lineThickness);
                setColor(color, dim);
                GL11.glBegin(GL11.GL_LINE_STRIP);
                for (int i = 0; i <= segments; i++) {
                    double t = (double) i / segments;
                    int bx = (int) ((1 - t) * (1 - t) * x1 + 2 * (1 - t) * t * offsetControl.x + t * t * x2);
                    int by = (int) ((1 - t) * (1 - t) * y1 + 2 * (1 - t) * t * offsetControl.y + t * t * y2);
                    GL11.glVertex2i(bx, by);
                }
                GL11.glEnd();
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glPopAttrib();
            } else if (twoWay) {
                Point cp1 = computeControlPoint(x1, y1, x2, y2, effectiveCurveAngle);
                Point cp2 = computeControlPoint(x1, y1, x2, y2, -effectiveCurveAngle);
                double d1 = getMinDistanceToIcon(cp1, conn);
                double d2 = getMinDistanceToIcon(cp2, conn);
                Point control = (d1 >= d2) ? cp1 : cp2;
                int segments = 800;
                int color1 = getConnectionColor(reverse);
                int color2 = getConnectionColor(conn);
                if (!useColorScaling) {
                    color1 = color2 = 0xFFFFFFFF;
                }
                int halfSegments = segments / 2;
                GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_HINT_BIT);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glLineWidth(lineThickness);
                setColor(color1, dim);
                GL11.glBegin(GL11.GL_LINE_STRIP);
                for (int i = 0; i <= halfSegments; i++) {
                    double t = (double) i / halfSegments;
                    int bx = (int) ((1 - t) * (1 - t) * x1 + 2 * (1 - t) * t * control.x + t * t * x2);
                    int by = (int) ((1 - t) * (1 - t) * y1 + 2 * (1 - t) * t * control.y + t * t * y2);
                    GL11.glVertex2i(bx, by);
                }
                GL11.glEnd();
                setColor(color2, dim);
                GL11.glBegin(GL11.GL_LINE_STRIP);
                for (int i = halfSegments; i <= segments; i++) {
                    double t = (double) i / segments;
                    int bx = (int) ((1 - t) * (1 - t) * x1 + 2 * (1 - t) * t * control.x + t * t * x2);
                    int by = (int) ((1 - t) * (1 - t) * y1 + 2 * (1 - t) * t * control.y + t * t * y2);
                    GL11.glVertex2i(bx, by);
                }
                GL11.glEnd();
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glPopAttrib();
            } else {
                int color = getConnectionColor(conn);
                if (!useColorScaling) {
                    color = 0xFFFFFFFF;
                }
                // Compute both candidate control points.
                Point cp1 = computeControlPoint(x1, y1, x2, y2, effectiveCurveAngle);
                Point cp2 = computeControlPoint(x1, y1, x2, y2, -effectiveCurveAngle);
                double d1 = getMinDistanceToIcon(cp1, conn);
                double d2 = getMinDistanceToIcon(cp2, conn);
                Point cp = (d1 >= d2) ? cp1 : cp2;
                int segments = 800;
                GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_HINT_BIT);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glLineWidth(lineThickness);
                setColor(color, dim);
                GL11.glBegin(GL11.GL_LINE_STRIP);
                for (int i = 0; i <= segments; i++) {
                    double t = (double) i / segments;
                    int bx = (int) ((1 - t) * (1 - t) * x1 + 2 * (1 - t) * t * cp.x + t * t * x2);
                    int by = (int) ((1 - t) * (1 - t) * y1 + 2 * (1 - t) * t * cp.y + t * t * y2);
                    GL11.glVertex2i(bx, by);
                }
                GL11.glEnd();
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glPopAttrib();
            }
        }
    }

    // Helper method to draw a straight colored line.
    private void drawColoredLine(int x1, int y1, int x2, int y2, int color, boolean dim) {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(lineThickness);
        setColor(color, dim);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2i(x1, y1);
        GL11.glVertex2i(x2, y2);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopAttrib();
    }

    // Helper to set color with dimming applied.
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

    /**
     * Draws the arrow head for a connection.
     * When the connection is drawn separately (two-way with allowTwoWay false),
     * the arrow head is positioned using the same offset.
     */
    // Fixed drawArrowHead:
    protected void drawArrowHead(int x1, int y1, int x2, int y2, DiagramConnection conn, boolean dim) {
        DiagramConnection reverse = getConnectionByIds(conn.idFrom, conn.idTo);
        int color = (reverse != null) ? getConnectionColor(reverse) : getConnectionColor(conn);
        if (!useColorScaling)
            color = 0xFFFFFFFF;
        boolean separateTwoWay = (reverse != null && !allowTwoWay);
        double angle;
        boolean useCustomCurveAngle = (conn.customAllowCurve != null ? conn.customAllowCurve : this.curvedArrows);

        if (!useCustomCurveAngle) {
            if (separateTwoWay) {
                int offsetAmount = 4;
                double dx = x2 - x1, dy = y2 - y1;
                double len = Math.sqrt(dx * dx + dy * dy);
                if (len == 0) len = 1;
                double offsetX = -dy / len * offsetAmount;
                double offsetY = dx / len * offsetAmount;
                if (conn.idFrom >= conn.idTo) {
                    offsetX = -offsetX;
                    offsetY = -offsetY;
                }
                int newX2 = x2 + (int) offsetX;
                int newY2 = y2 + (int) offsetY;
                angle = Math.atan2(newY2 - y1, newX2 - x1);
                x2 = newX2;
                y2 = newY2;
            } else {
                angle = Math.atan2(y2 - y1, x2 - x1);
            }
        } else {
            int effectiveCurveAngle = (conn.customCurveAngle != null ? conn.customCurveAngle : this.curveAngle);
            if (separateTwoWay) {
                int offsetAmount = 4;
                double dx = x2 - x1, dy = y2 - y1;
                double len = Math.sqrt(dx * dx + dy * dy);
                if (len == 0) len = 1;
                double normX = -dy / len;
                double normY = dx / len;
                double sign = (conn.idFrom < conn.idTo) ? 1 : -1;
                Point baseControl = computeControlPoint(x1, y1, x2, y2, effectiveCurveAngle);
                Point offsetControl = new Point(baseControl.x + (int) (normX * offsetAmount * sign),
                    baseControl.y + (int) (normY * offsetAmount * sign));
                double t = 0.95;
                double bx = (1 - t) * (1 - t) * x1 + 2 * (1 - t) * t * offsetControl.x + t * t * x2;
                double by = (1 - t) * (1 - t) * y1 + 2 * (1 - t) * t * offsetControl.y + t * t * y2;
                double dBx = 2 * (1 - t) * (offsetControl.x - x1) + 2 * t * (x2 - offsetControl.x);
                double dBy = 2 * (1 - t) * (offsetControl.y - y1) + 2 * t * (y2 - offsetControl.y);
                angle = Math.atan2(dBy, dBx);
            } else {
                Point cp1 = computeControlPoint(x1, y1, x2, y2, effectiveCurveAngle);
                Point cp2 = computeControlPoint(x1, y1, x2, y2, -effectiveCurveAngle);
                double d1 = getMinDistanceToIcon(cp1, conn);
                double d2 = getMinDistanceToIcon(cp2, conn);
                Point control = (d1 >= d2) ? cp1 : cp2;
                double t = 0.95;
                double bx = (1 - t) * (1 - t) * x1 + 2 * (1 - t) * t * control.x + t * t * x2;
                double by = (1 - t) * (1 - t) * y1 + 2 * (1 - t) * t * control.y + t * t * y2;
                double dBx = 2 * (1 - t) * (control.x - x1) + 2 * t * (x2 - control.x);
                double dBy = 2 * (1 - t) * (control.y - y1) + 2 * t * (y2 - control.y);
                angle = Math.atan2(dBy, dBx);
            }
        }
        float defenderEdgeX = x2 - (slotSize / 2f) * (float) Math.cos(angle);
        float defenderEdgeY = y2 - (slotSize / 2f) * (float) Math.sin(angle);
        float leftX = defenderEdgeX - arrowSize * (float) Math.cos(angle - Math.PI / 6);
        float leftY = defenderEdgeY - arrowSize * (float) Math.sin(angle - Math.PI / 6);
        float rightX = defenderEdgeX - arrowSize * (float) Math.cos(angle + Math.PI / 6);
        float rightY = defenderEdgeY - arrowSize * (float) Math.sin(angle + Math.PI / 6);
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        float rr = ((color >> 16) & 0xFF) / 255f;
        float gg = ((color >> 8) & 0xFF) / 255f;
        float bb = (color & 0xFF) / 255f;
        if (dim) {
            rr *= 0.4f;
            gg *= 0.4f;
            bb *= 0.4f;
        }
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


    // --- Hit-detection helpers ---
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
        float iconScale = (cellSize - iconMargin) / (float) iconSize;
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
        int effectiveMouseX = (int) (((float) mouseX - (centerX + panX)) / zoom + centerX);
        int effectiveMouseY = (int) (((float) mouseY - (centerY + panY)) / zoom + centerY);

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
     * Applies pan/zoom, draws connections (using adjusted endpoints for separate two-way lines),
     * icons, and shows tooltips.
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
                boolean separateTwoWay = (getConnectionByIds(conn.idTo, conn.idFrom) != null && !allowTwoWay);
                boolean useCustomCurveAngle = (conn.customAllowCurve != null ? conn.customAllowCurve : this.curvedArrows);
                if (separateTwoWay) {
                    if (useCustomCurveAngle) {
                        int offsetAmount = 4;
                        double dx = pTo.x - pFrom.x, dy = pTo.y - pFrom.y;
                        double len = Math.sqrt(dx * dx + dy * dy);
                        if (len == 0) len = 1;
                        double normX = -dy / len;
                        double normY = dx / len;
                        double sign = (conn.idFrom < conn.idTo) ? 1 : -1;
                        int effectiveCurveAngle = (conn.customCurveAngle != null ? conn.customCurveAngle : this.curveAngle);
                        Point baseControl = computeControlPoint(pFrom.x, pFrom.y, pTo.x, pTo.y, effectiveCurveAngle);
                        Point offsetControl = new Point(baseControl.x + (int) (normX * offsetAmount * sign),
                            baseControl.y + (int) (normY * offsetAmount * sign));
                        dist = distanceToBezier(pFrom, offsetControl, pTo, 200, effectiveMouseX, effectiveMouseY);
                    } else {
                        int offsetAmount = 4;
                        double dx = pTo.x - pFrom.x, dy = pTo.y - pFrom.y;
                        double len = Math.sqrt(dx * dx + dy * dy);
                        if (len == 0) len = 1;
                        double offsetX = -dy / len * offsetAmount;
                        double offsetY = dx / len * offsetAmount;
                        if (conn.idFrom >= conn.idTo) {
                            offsetX = -offsetX;
                            offsetY = -offsetY;
                        }
                        int newX1 = pFrom.x + (int) offsetX;
                        int newY1 = pFrom.y + (int) offsetY;
                        int newX2 = pTo.x + (int) offsetX;
                        int newY2 = pTo.y + (int) offsetY;
                        dist = pointLineDistance(effectiveMouseX, effectiveMouseY, newX1, newY1, newX2, newY2);
                    }
                } else {
                    if (useCustomCurveAngle) {
                        int effectiveCurveAngle = (conn.customCurveAngle != null ? conn.customCurveAngle : this.curveAngle);
                        Point cp1 = computeControlPoint(pFrom.x, pFrom.y, pTo.x, pTo.y, effectiveCurveAngle);
                        Point cp2 = computeControlPoint(pFrom.x, pFrom.y, pTo.x, pTo.y, -effectiveCurveAngle);
                        double d1 = getMinDistanceToIcon(cp1, conn);
                        double d2 = getMinDistanceToIcon(cp2, conn);
                        Point control = (d1 >= d2) ? cp1 : cp2;
                        dist = distanceToBezier(pFrom, control, pTo, 200, effectiveMouseX, effectiveMouseY);
                    } else {
                        dist = pointLineDistance(effectiveMouseX, effectiveMouseY, pFrom.x, pFrom.y, pTo.x, pTo.y);
                    }
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
        for (DiagramConnection conn : getConnections()) {
            DiagramIcon iconFrom = getIconById(conn.idFrom);
            DiagramIcon iconTo = getIconById(conn.idTo);
            if (iconFrom == null || iconTo == null || !iconFrom.enabled || !iconTo.enabled)
                continue;
            Point pFrom = positions.get(conn.idFrom);
            Point pTo = positions.get(conn.idTo);
            if (pFrom == null || pTo == null) continue;
            int ax = pFrom.x, ay = pFrom.y, bx = pTo.x, by = pTo.y;
            boolean dim;
            if (hoveredIconId != null) {
                dim = (conn.idFrom != hoveredIconId);
            } else {
                dim = !selectedIconIds.isEmpty() && !(selectedIconIds.contains(conn.idFrom) && selectedIconIds.contains(conn.idTo));
            }
            drawConnectionLine(ax, ay, bx, by, conn, dim);
        }
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
        if (showArrowHeads) {
            GL11.glPushMatrix();
            GL11.glTranslatef(centerX + panX, centerY + panY, 0);
            GL11.glScalef(zoom, zoom, 1.0f);
            GL11.glTranslatef(-centerX, -centerY, 0);
            for (DiagramConnection conn : getConnections()) {
                boolean showArrow = (conn.showArrowHead != null ? conn.showArrowHead : this.showArrowHeads);
                if (!showArrow)
                    continue;

                DiagramIcon iconFrom = getIconById(conn.idFrom);
                DiagramIcon iconTo = getIconById(conn.idTo);
                if (iconFrom == null || iconTo == null || !iconFrom.enabled || !iconTo.enabled)
                    continue;
                Point pFrom = positions.get(conn.idFrom);
                Point pTo = positions.get(conn.idTo);
                if (pFrom == null || pTo == null) continue;
                int ax = pFrom.x, ay = pFrom.y, bx = pTo.x, by = pTo.y;
                boolean dim;
                if (hoveredIconId != null) {
                    dim = (conn.idFrom != hoveredIconId);
                } else {
                    dim = !selectedIconIds.isEmpty() && !(selectedIconIds.contains(conn.idFrom) && selectedIconIds.contains(conn.idTo));
                }
                drawArrowHead(ax, ay, bx, by, conn, dim);
            }
            GL11.glPopMatrix();
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
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
        int a = (int) (a1 + (a2 - a1) * ratio);
        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);
        return (a << 24) | (r << 16) | (g << 8) | b;
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
        if (conn.customColor != null) {
            return conn.customColor;
        }
        float value = Math.max(-1f, Math.min(1f, conn.percent));
        int r, g, b;
        if (value >= 0f) {
            float t = value;
            r = (int) (144 + (0 - 144) * t);
            g = (int) (238 + (100 - 238) * t);
            b = (int) (144 + (0 - 144) * t);
        } else {
            float t = -value;
            r = (int) (255 + (139 - 255) * t);
            g = (int) (200 + (0 - 200) * t);
            b = 0;
        }
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }

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

    public void handleMouseScroll(int scrollDelta) {
        zoom += scrollDelta * 0.0009f;
        if (zoom < 0.2f) zoom = 0.2f;
        if (zoom > 3.0f) zoom = 3.0f;
    }

    protected void drawHoveringText(List<String> textLines, int mouseX, int mouseY, FontRenderer font) {
    }

    // --- Flag Setters ---
    public void setShowArrowHeads(boolean showArrowHeads) {
        this.showArrowHeads = showArrowHeads;
    }

    public void setUseColorScaling(boolean useColorScaling) {
        this.useColorScaling = useColorScaling;
    }

    // --- Icon and Connection Event Callbacks ---
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

    public static class DiagramConnection {
        public int idFrom, idTo;
        public float percent;
        public String hoverText;
        public Integer customCurveAngle = null;
        public Integer customColor = null;
        public Boolean customAllowCurve = null;
        public Boolean showArrowHead = null;

        public DiagramConnection(int idFrom, int idTo, float percent, String hoverText) {
            this.idFrom = idFrom;
            this.idTo = idTo;
            this.percent = percent;
            this.hoverText = hoverText;
        }
    }

    public enum IconRenderState {
        DEFAULT,
        HIGHLIGHTED,
        NOT_HIGHLIGHTED;
    }
}
