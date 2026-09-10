package noppes.npcs.client.guide;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 会话节奏与寻路预算这几个常量之间是有约束关系的，写死成互不相干的数字就会出现
 * "永远命中不了的时限""永远触发不了的超时"。这里把那些关系钉住。
 */
public class GuideRenderConfigTest {

    /** 一个游戏 tick 的名义墙钟长度，毫秒。 */
    private static final long TICK_MILLIS = 50L;

    @Test
    public void staleTimeoutEqualsThreeRefreshCycles() {
        // 设计里的结束条件是"连续 3 次刷新（15 秒）都找不到"。本地兜底的超时必须跟它等价，
        // 否则一边靠服务端回包、一边靠本地时钟，两条路会在不同的时刻判跟丢。
        assertEquals(GuideRenderConfig.REFRESH_INTERVAL_TICKS * GuideRenderConfig.MAX_MISSED_REFRESHES,
            GuideRenderConfig.TARGET_STALE_TIMEOUT_TICKS);
        assertEquals(300, GuideRenderConfig.TARGET_STALE_TIMEOUT_TICKS); // 15 秒 @ 20 tps
    }

    @Test
    public void staleTimeoutLeavesRoomForAtLeastOneRoundTrip() {
        // 超时必须显著大于一个刷新周期，否则请求刚发出去、回包还在路上就判了跟丢。
        assertTrue("本地超时不能短于一个刷新周期",
            GuideRenderConfig.TARGET_STALE_TIMEOUT_TICKS > GuideRenderConfig.REFRESH_INTERVAL_TICKS);
    }

    @Test
    public void pathfindTimeBudgetIsReachableWithinOneReplanCycle() {
        // 每 REPLAN_INTERVAL_TICKS 就会起一个新搜索覆盖旧的，一次搜索最多活这么长墙钟时间。
        // 总时限要是比这还长（改之前写死的是 3 秒）就永远命中不了，等于根本没有这条兜底。
        long oneReplanCycleNanos = GuideRenderConfig.REPLAN_INTERVAL_TICKS * TICK_MILLIS * 1_000_000L;
        assertTrue("寻路总时限必须能在一个 replan 周期内命中",
            GuideRenderConfig.PATHFIND_TIME_BUDGET_NANOS <= oneReplanCycleNanos);
        assertTrue(GuideRenderConfig.PATHFIND_TIME_BUDGET_NANOS > 0L);
    }

    @Test
    public void perTickStepBudgetFitsInsideOneTick() {
        // 每 tick 给 A* 的时间片不能吃满整个 tick，否则渲染和其它逻辑一点余量都不剩。
        assertTrue("单 tick 时间片不能超过一个 tick",
            GuideRenderConfig.STEP_BUDGET_NANOS < TICK_MILLIS * 1_000_000L);
    }
}
