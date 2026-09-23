package noppes.npcs.guide;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 把活儿从 netty IO 线程挪到服务端主线程上执行。
 *
 * 1.7.10 的 FMLNetworkEvent.ServerCustomPacketEvent 是在 netty 的 IO 线程上派发的，不是服务端
 * 主线程；而 1.7.10 的 MinecraftServer 上没有 addScheduledTask 那类东西（那是 1.8 的
 * IThreadListener 才有的），所以只能自己排一个队，在 ServerTickEvent 里排干。
 *
 * 引路的刷新请求需要遍历 world.loadedEntityList，那个 ArrayList 同时正被主线程增删——在 IO
 * 线程上直接遍历迟早撞上 ConcurrentModificationException，而且异常会被 PacketHandler 的
 * catch(Exception) 吞掉，表现成刷新静默失效。CNPC+ 既有的 request 包都走 getEditingNpc(player)，
 * 没有一个遍历实体表，所以这是引路功能新引入的模式，得自己处理线程归属。
 */
public final class GuideServerTasks {

    private GuideServerTasks() {}

    /**
     * 队列上限。正常情况下每 tick 都会被排干，长不起来；真长起来只可能是服务端已经不 tick 了
     * （关服中、卡死），这时候攒着的任务也没有任何意义，直接丢，免得无限吃内存。
     */
    private static final int MAX_PENDING = 512;

    private static final Queue<Runnable> PENDING = new ConcurrentLinkedQueue<Runnable>();

    /** 任意线程可调。任务会在下一个服务端 tick 的主线程上执行。 */
    public static void enqueue(Runnable task) {
        if (task == null || PENDING.size() >= MAX_PENDING) {
            return;
        }
        PENDING.add(task);
    }

    /**
     * 只在服务端主线程（ServerTickEvent）调用。
     *
     * 先记下当前队列长度再取，这一轮只处理这么多个——任务自己再往队列里塞东西时不会变成
     * 死循环，新塞的留到下一个 tick。
     */
    public static void runPending() {
        int budget = PENDING.size();
        while (budget-- > 0) {
            Runnable task = PENDING.poll();
            if (task == null) {
                return;
            }
            try {
                task.run();
            } catch (Throwable t) {
                // 一个任务炸了不能连累同一 tick 里其它任务，更不能顺着 tick 事件漏出去崩服。
                System.out.println("[CustomNPC+] 引路服务端任务抛出未处理异常：");
                t.printStackTrace();
            }
        }
    }

    /** 关服/换存档时清空，别把上一局的任务带到下一局。 */
    public static void clear() {
        PENDING.clear();
    }
}
