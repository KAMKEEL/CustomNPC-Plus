package noppes.npcs.guide;

import java.util.ArrayList;
import java.util.List;

/**
 * 按名字从一批候选 NPC 里挑一个。逐级放宽：精确 → 忽略大小写 → 剥掉颜色代码后的子串；
 * 同一级命中多个就取离玩家最近的。
 *
 * 刻意不引用任何 Minecraft 类型——候选用 Candidate 这个小 POJO 承载，真正的 NPC 实体
 * 塞在 payload 里由调用方取回。这样匹配规则本身能被单元测试完整覆盖。
 */
public final class GuideNameMatcher {

    private GuideNameMatcher() {}

    public static final class Candidate {
        /** NPC 的显示名，可能带颜色代码。 */
        public final String name;
        /** 到玩家的距离平方。只用来比大小，不开方。 */
        public final double distanceSq;
        /** 调用方自己的载荷，匹配逻辑不碰它。服务端塞的是 EntityNPCInterface。 */
        public final Object payload;

        public Candidate(String name, double distanceSq, Object payload) {
            this.name = name == null ? "" : name;
            this.distanceSq = distanceSq;
            this.payload = payload;
        }
    }

    public static final class Result {
        /** 选中的候选；三级都没命中时为 null。 */
        public final Candidate chosen;
        /** 与 chosen 同处一个匹配级别的候选总数，用来提示"找到 N 个同名"。 */
        public final int matchCount;

        public Result(Candidate chosen, int matchCount) {
            this.chosen = chosen;
            this.matchCount = matchCount;
        }
    }

    private enum Level {
        EXACT,
        IGNORE_CASE,
        CONTAINS
    }

    public static Result match(List<Candidate> candidates, String query) {
        if (candidates == null || candidates.isEmpty() || query == null || query.isEmpty()) {
            return new Result(null, 0);
        }
        for (Level level : Level.values()) {
            Result r = pick(candidates, query, level);
            if (r.chosen != null) {
                return r;
            }
        }
        return new Result(null, 0);
    }

    private static Result pick(List<Candidate> candidates, String query, Level level) {
        List<Candidate> hits = new ArrayList<Candidate>();
        for (Candidate c : candidates) {
            if (matches(c.name, query, level)) {
                hits.add(c);
            }
        }
        if (hits.isEmpty()) {
            return new Result(null, 0);
        }
        Candidate nearest = hits.get(0);
        for (Candidate c : hits) {
            if (c.distanceSq < nearest.distanceSq) {
                nearest = c;
            }
        }
        return new Result(nearest, hits.size());
    }

    private static boolean matches(String name, String query, Level level) {
        switch (level) {
            case EXACT:
                return name.equals(query);
            case IGNORE_CASE:
                return name.equalsIgnoreCase(query);
            case CONTAINS:
                return stripColors(name).toLowerCase().contains(stripColors(query).toLowerCase());
            default:
                return false;
        }
    }

    /**
     * 去掉 §x 形式的颜色/格式代码。末尾孤立的 § 后面没有格式字符，原样保留，不越界。
     */
    public static String stripColors(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '§' && i + 1 < s.length()) {
                i++; // 连同后面那个格式字符一起跳过
                continue;
            }
            out.append(c);
        }
        return out.toString();
    }
}
