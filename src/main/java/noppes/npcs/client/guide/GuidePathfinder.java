package noppes.npcs.client.guide;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * 自研 A*：自己判断方块能不能走、能不能站，不依赖原版寻路（对模组方块经常直接放弃）。
 * 预算用完不返回"找不到"，而是走到搜索过程中离终点最近的地方（见 Search）。
 * 不支持挖方块/放方块/大跳/连续下坠，只需要在家里的箱子之间走动，用不上那么复杂。
 *
 * 移动方式：
 *   - 同层东西南北平移
 *   - 同层斜向平移（斜移经过的两个正交邻格必须都不是实心，避免穿墙角）
 *   - 爬升 1 格 / 下降 1 格
 */
public final class GuidePathfinder {

    private GuidePathfinder() {}

    private static final double COST_CARDINAL = 1.0;
    private static final double COST_DIAGONAL = 1.4142135623730951; // sqrt(2)
    private static final double COST_VERTICAL = 1.4142135623730951; // 爬 1 格/降 1 格，实际位移量跟斜移一样

    private static final int[][] CARDINAL_DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int[][] DIAGONAL_DIRS = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    private static final class Node {
        final int x, y, z;
        double g;
        double f;
        Node parent;

        Node(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    /**
     * 起一次引路用的搜索。预算固定取 GuideRenderConfig 里的值，不像 GTButler 的 /goto
     * 那样不限时——引路是每秒重算一次的常驻功能，必须有硬上限兜底。
     *
     * 预算用完时 Search 不会返回"找不到"，而是返回搜索过程中离终点最近的那条路（见
     * Search.finish() 与 fallbackScore()）。远距离引路正是靠这个性质工作：目标在几百格外
     * 时，搜索会朝目标方向探到已加载区块的边界然后收工，玩家沿路走一段、起点前移、下一秒
     * 再算一段，天然就是分段引路，不需要构造伪目标点。
     */
    public static Search startSearch(World world, int startX, int startY, int startZ,
                                      int goalX, int goalY, int goalZ) {
        return new Search(world, startX, startY, startZ, goalX, goalY, goalZ,
                          GuideRenderConfig.PATHFIND_MAX_NODES,
                          GuideRenderConfig.PATHFIND_TIME_BUDGET_NANOS);
    }

    /**
     * 可以"先停下、下次接着走"的搜索，用来把很长/不限时的预算（比如 /goto）拆成很多小段，
     * 每个 tick 只推进一点（见 step()），不会一次性卡住游戏。核心 A* 逻辑跟一次性跑完的版本
     * 完全一样，只是能在预算内随时中断、保留现场，下次调用接着搜。
     */
    public static final class Search {
        private final World world;
        private final int goalX;
        private final int goalY;
        private final int goalZ;
        private final int maxNodes;
        private final long overallDeadlineNanos;
        private final PriorityQueue<Node> open = new PriorityQueue<>((a, b) -> Double.compare(a.f, b.f));
        private final Map<Long, Double> bestG = new HashMap<>();
        private final Node start;
        private Node bestSoFar;
        private double bestSoFarScore;
        private int nodesExpanded;
        private boolean finished;
        private List<int[]> result;
        private boolean exhausted; // true 表示是搜完了周围所有能走到的地方才停的，不是预算用完

        private Search(World world, int startX, int startY, int startZ, int goalX, int goalY, int goalZ,
                        int maxNodes, long totalBudgetNanos) {
            this.world = world;
            this.goalX = goalX;
            this.goalY = goalY;
            this.goalZ = goalZ;
            this.maxNodes = maxNodes;
            this.overallDeadlineNanos = System.nanoTime() + totalBudgetNanos;
            this.start = new Node(startX, startY, startZ);
            start.g = 0;
            start.f = heuristic(startX, startY, startZ, goalX, goalY, goalZ);
            open.add(start);
            bestG.put(packPos(startX, startY, startZ), 0.0);
            bestSoFar = start;
            bestSoFarScore = fallbackScore(startX, startY, startZ, goalX, goalY, goalZ);
        }

        public boolean isFinished() {
            return finished;
        }

        /**
         * 只有 isFinished() 之后才有意义。预算用完还没到终点时，返回搜索过程中离终点最近的
         * 路径，而不是直接说"没有路"。只有起点自己就是最优（一步都没能往目标靠近）才会是 null。
         */
        public List<int[]> getResult() {
            return result;
        }

        /** 没找到目标时，结果是"周围能走的地方都探索完了"（true）还是"预算用完了还没探索完"（false）。 */
        public boolean isExhausted() {
            return exhausted;
        }

        /** 调试渲染用：搜索还没结束时，目前找到的最优候选路径（从起点走到 bestSoFar）——是一条会
         * 随搜索推进不断刷新、越来越接近终点的"当前最佳猜测路线"，不是搜索过程中碰过的所有节点
         * （那样会把整片探索过的区域都点亮，不是一条路线，看不出规划的是哪条道）。 */
        public List<int[]> getBestPathSoFar() {
            return bestSoFar == start ? null : reconstructPath(bestSoFar);
        }

        /** 处理最多到 stepDeadlineNanos（绝对纳秒时间戳）为止的一小段，调用方每 tick 调一次，
         * 传一个很近的截止时间（比如往后 20 毫秒），搜索会跨很多个 tick 逐步推进直到完成或预算用完。 */
        public void step(long stepDeadlineNanos) {
            if (finished) {
                return;
            }
            while (!open.isEmpty()) {
                long now = System.nanoTime();
                if (now >= stepDeadlineNanos) {
                    return; // 这一小段先让出去，下次调用接着走
                }
                if (nodesExpanded >= maxNodes || now >= overallDeadlineNanos) {
                    finish();
                    return;
                }
                Node current = open.poll();
                Double knownBest = bestG.get(packPos(current.x, current.y, current.z));
                if (knownBest != null && current.g > knownBest + 1.0e-9) {
                    continue; // 懒删除：这个节点已经有更便宜的路径到过了，这份是过时的队列条目
                }
                if (current.x == goalX && current.y == goalY && current.z == goalZ) {
                    result = reconstructPath(current);
                    finished = true;
                    return;
                }
                nodesExpanded++;

                for (Node neighbor : neighborsOf(world, current)) {
                    long key = packPos(neighbor.x, neighbor.y, neighbor.z);
                    Double existing = bestG.get(key);
                    if (existing != null && neighbor.g >= existing - 1.0e-9) {
                        continue; // 已经有不比这次差的路径到过这个位置
                    }
                    bestG.put(key, neighbor.g);
                    double h = heuristic(neighbor.x, neighbor.y, neighbor.z, goalX, goalY, goalZ);
                    neighbor.f = neighbor.g + h;
                    double score = fallbackScore(neighbor.x, neighbor.y, neighbor.z, goalX, goalY, goalZ);
                    if (score < bestSoFarScore) {
                        bestSoFarScore = score;
                        bestSoFar = neighbor;
                    }
                    open.add(neighbor);
                }
            }
            exhausted = true; // 开放列表空了还没到终点：周围能走的地方都试过了，不是预算不够
            finish();
        }

        private void finish() {
            result = bestSoFar == start ? null : reconstructPath(bestSoFar);
            finished = true;
        }
    }

    private static List<Node> neighborsOf(World world, Node from) {
        List<Node> result = new ArrayList<>(12);

        for (int[] dir : CARDINAL_DIRS) {
            addIfWalkable(world, result, from, from.x + dir[0], from.y, from.z + dir[1], COST_CARDINAL);
            addAscendIfWalkable(world, result, from, dir[0], dir[1]);
            addDescendIfWalkable(world, result, from, dir[0], dir[1]);
        }
        // 斜移经过的两个正交邻格（同层）必须都不是实心，否则会像鬼一样穿墙角。这两格只要求身位
        // 过得去，不要求能站。4 个斜方向两两共用同一批正交邻格，先算好这 4 个方向各自的结果，
        // 不用每个斜方向都重新查一遍方块。
        boolean openXPlus = isOpenColumn(world, from.x + 1, from.y, from.z);
        boolean openXMinus = isOpenColumn(world, from.x - 1, from.y, from.z);
        boolean openZPlus = isOpenColumn(world, from.x, from.y, from.z + 1);
        boolean openZMinus = isOpenColumn(world, from.x, from.y, from.z - 1);
        for (int[] dir : DIAGONAL_DIRS) {
            boolean openX = dir[0] > 0 ? openXPlus : openXMinus;
            boolean openZ = dir[1] > 0 ? openZPlus : openZMinus;
            if (!openX || !openZ) {
                continue;
            }
            addIfWalkable(world, result, from, from.x + dir[0], from.y, from.z + dir[1], COST_DIAGONAL);
        }
        return result;
    }

    private static void addIfWalkable(World world, List<Node> out, Node from, int x, int y, int z, double stepCost) {
        if (!isStandable(world, x, y, z)) {
            return;
        }
        Node n = new Node(x, y, z);
        n.parent = from;
        n.g = from.g + stepCost;
        out.add(n);
    }

    private static void addAscendIfWalkable(World world, List<Node> out, Node from, int dx, int dz) {
        int x = from.x + dx;
        int y = from.y + 1;
        int z = from.z + dz;
        if (!isStandable(world, x, y, z)) {
            return;
        }
        // 头顶净空也要跟着抬高一格，不然爬升瞬间会撞到原来那层的天花板。
        if (!isOpen(world, from.x, from.y + 2, from.z)) {
            return;
        }
        Node n = new Node(x, y, z);
        n.parent = from;
        n.g = from.g + COST_VERTICAL;
        out.add(n);
    }

    private static void addDescendIfWalkable(World world, List<Node> out, Node from, int dx, int dz) {
        int x = from.x + dx;
        int y = from.y - 1;
        int z = from.z + dz;
        if (!isStandable(world, x, y, z)) {
            return;
        }
        Node n = new Node(x, y, z);
        n.parent = from;
        n.g = from.g + COST_VERTICAL;
        out.add(n);
    }

    /** 脚下这一格和头顶那一格都过得去，同层斜移抄近道检查用——不要求下面有地板，只是路过。 */
    private static boolean isOpenColumn(World world, int x, int y, int z) {
        return isOpen(world, x, y, z) && isOpen(world, x, y + 1, z);
    }

    /** 能不能站在 (x,y,z)：自己这格和头顶那格都过得去，脚下那格是实心地板。 */
    private static boolean isStandable(World world, int x, int y, int z) {
        return isOpen(world, x, y, z) && isOpen(world, x, y + 1, z) && isSolidFloor(world, x, y - 1, z);
    }

    /** 这一格能不能过身——不是实心方块，另外排除熔岩/火焰这类不算实心但走进去会死的方块。
     * 只取一次方块（isHazard/材质判断共用同一次 world.getBlock()），寻路展开节点时这两个方法
     * 调用量很大，少一次方块查询就少一次。 */
    private static boolean isOpen(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        return !isHazard(block) && !block.getMaterial().blocksMovement();
    }

    /** 这一格能不能当地板站——实心（水/熔岩本身这个值就是 false，天然不会被当成合法地板）。 */
    private static boolean isSolidFloor(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        return !isHazard(block) && block.getMaterial().blocksMovement();
    }

    private static boolean isHazard(Block block) {
        return block == Blocks.lava || block == Blocks.flowing_lava || block == Blocks.fire;
    }

    private static double heuristic(int x, int y, int z, int goalX, int goalY, int goalZ) {
        double dx = x - goalX;
        double dy = y - goalY;
        double dz = z - goalZ;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    // 终点到不了、退而求其次选"搜到的里面最接近的"点时用的评分——跟上面 heuristic() 不是一回事：
    // heuristic() 给 A* 排队用，横竖一视同仁；这里横向（x/z）偏差的权重远大于纵向（y），宁可停在
    // 同一根竖井/同一列只是差几格高度，也不要因为直线距离更近就跑去水平方向不相关的地方。
    private static double fallbackScore(int x, int y, int z, int goalX, int goalY, int goalZ) {
        double dx = x - goalX;
        double dz = z - goalZ;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        double vertical = Math.abs(y - goalY);
        return horizontal * 100.0 + vertical;
    }

    private static List<int[]> reconstructPath(Node goal) {
        List<int[]> reversed = new ArrayList<>();
        for (Node n = goal; n.parent != null; n = n.parent) {
            reversed.add(new int[]{n.x, n.y, n.z});
        }
        Collections.reverse(reversed);
        return reversed;
    }

    // 26 位给 x、26 位给 z（±3300 万格，远超世界边界）、12 位给 y（0-4095，远超 0-255 的世界高度），
    // 正好凑够一个 long。只当哈希表的 key 用，不需要能从 long 反推回 x/y/z。
    private static long packPos(int x, int y, int z) {
        return ((x & 0x3FFFFFFL) << 38) | ((z & 0x3FFFFFFL) << 12) | (y & 0xFFFL);
    }
}
