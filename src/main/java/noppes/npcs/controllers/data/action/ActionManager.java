package noppes.npcs.controllers.data.action;

import noppes.npcs.api.handler.IActionManager;
import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.IActionChain;
import noppes.npcs.api.handler.data.IActionListener;
import noppes.npcs.api.handler.data.IActionQueue;
import noppes.npcs.api.handler.data.actions.IConditionalAction;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.data.action.action.ConditionalAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Full-featured ActionManager:
 * - delayed start, max-duration, tick-interval
 * - chaining (getNext, getPrevious, scheduleAfter/Before)
 * - per-action data
 * - conditional actions
 * - cancellation & clearing
 */
public class ActionManager implements IActionManager {
    public static final ActionManager GLOBAL = new ActionManager("GLOBAL");
    private static final Map<Object, IActionListener> listeners = new ConcurrentHashMap<>();

    protected boolean isWorking = false;
    public ActionLogger LOGGER = new ActionLogger(this);
    protected String name = "";

    protected final ActionQueue sequentialQueue = new ActionQueue(this, "mainSequential");
    protected final ActionQueue parallelQueue = new ActionQueue(this, "mainParallel", true);
    protected final ActionQueue conditionalQueue = new ActionQueue(this, "mainConditional", true);
    protected final Map<String, IActionQueue> otherQueues = new HashMap<>();

    protected boolean debug = false;
    protected ScriptContainer reportTo;

    public ActionManager() {
    }

    public ActionManager(String name) {
        this.name = name;
    }


    @Override
    public IActionManager start() {
        isWorking = true;
        return this;
    }

    @Override
    public IActionManager stop() {
        isWorking = false;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public IActionManager setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public boolean inDebugMode() {
        return debug;
    }

    @Override
    public IActionManager setDebugMode(boolean debug) {
        this.debug = debug;

        if (debug)
            LOGGER.log("Enabled debug mode", this);
        return this;
    }

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    // Listeners

    // IActionListener Comments
//    public IActionListener getOrCreateListener(Object obj) {
//        return listeners.computeIfAbsent(obj, o -> new ActionListener(o));
//    }

//    public IActionListener getListener(Object obj) {
//        return listeners.get(obj);
//    }

//    public boolean removeListener(Object obj) {
//        if (listeners.containsKey(obj)) {
//            listeners.remove(obj);
//            return true;
//        }
//
//        return false;
//    }

//    public boolean fireListener(String hookName, Object obj) {
//        IActionListener listener = getListener(obj);
//        if (listener != null)
//            return listener.fire(hookName);
//
//        return false;
//    }

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    // Creators

    @Override
    public IAction create(String name) {
        return new Action(this, name);
    }

    @Override
    public IAction create(Consumer<IAction> t) {
        return new Action(this, t);
    }

    @Override
    public IAction create(int maxDuration, int delay, Consumer<IAction> task) {
        return new Action(this, maxDuration, delay, task);
    }

    @Override
    public IAction create(String name, int maxDuration, int delay, Consumer<IAction> task) {
        return new Action(this, name, maxDuration, delay, task);
    }

    @Override
    public IAction create(String name, int delay, Consumer<IAction> t) {
        return new Action(this, name, delay, t);
    }

    @Override
    public IAction create(int delay, Consumer<IAction> t) {
        return new Action(this, delay, t);
    }

    @Override
    public IAction create(String name, Consumer<IAction> t) {
        return new Action(this, name, t);
    }

    @Override
    public IConditionalAction create(Function<IAction, Boolean> condition, Consumer<IAction> task) {
        return new ConditionalAction(this, condition, task);
    }

    @Override
    public IConditionalAction create(String name, Function<IAction, Boolean> condition, Consumer<IAction> task) {
        return new ConditionalAction(this, name, condition, task);
    }

    @Override
    public IConditionalAction create(Function<IAction, Boolean> condition, Consumer<IAction> task, Function<IAction, Boolean> terminateWhen) {
        return new ConditionalAction(this, condition, task, terminateWhen);
    }

    @Override
    public IConditionalAction create(String name, Function<IAction, Boolean> condition, Consumer<IAction> task, Function<IAction, Boolean> terminateWhen) {
        return new ConditionalAction(this, name, condition, task, terminateWhen);
    }

    @Override
    public IConditionalAction create(Function<IAction, Boolean> condition, Consumer<IAction> task, Function<IAction, Boolean> terminateWhen, Consumer<IAction> onTermination) {
        return new ConditionalAction(this, condition, task, terminateWhen, onTermination);
    }

    @Override
    public IConditionalAction create(String name, Function<IAction, Boolean> condition, Consumer<IAction> task, Function<IAction, Boolean> terminateWhen, Consumer<IAction> onTermination) {
        return new ConditionalAction(this, name, condition, task, terminateWhen, onTermination);
    }

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    // Queues

    @Override
    public IActionQueue createQueue(String name) {
        return createQueue(name, false);
    }

    @Override
    public IActionQueue createQueue(String name, boolean isParallel) {
        IActionQueue queue = new ActionQueue(this, name, isParallel).killWhenEmpty(true);
        otherQueues.put(name, queue);
        return queue;
    }

    @Override
    public IActionQueue getOrCreateQueue(String name) {
        return getOrCreateQueue(name, false);
    }

    @Override
    public IActionQueue getOrCreateQueue(String name, boolean isParallel) {
        IActionQueue queue = getQueue(name);

        if (queue == null)
            queue = createQueue(name, isParallel);
        return queue;
    }

    @Override
    public IActionQueue getQueue(String name) {
        return otherQueues.get(name);
    }

    @Override
    public boolean hasQueue(String name) {
        return otherQueues.containsKey(name);
    }

    @Override
    public boolean removeQueue(String name) {
        IActionQueue queue = otherQueues.get(name);

        if (queue == null)
            return false;

        queue.kill();
        return true;
    }

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    // Sequential

    @Override
    public IActionQueue getSequentialQueue() {
        return sequentialQueue;
    }

    @Override
    public IAction schedule(IAction action) {
        if (action instanceof IConditionalAction)
            return conditionalQueue.schedule(action);

        return sequentialQueue.schedule(action);
    }

    @Override
    public void schedule(IAction... actions) {
        sequentialQueue.schedule(actions);
    }

    @Override
    public void schedule(Consumer<IAction>... tasks) {
        sequentialQueue.schedule(tasks);
    }

    @Override
    public IAction schedule(Consumer<IAction> task) {
        return sequentialQueue.schedule(task);
    }

    @Override
    public IAction schedule(int delay, Consumer<IAction> task) {
        return sequentialQueue.schedule(delay, task);
    }

    @Override
    public IAction schedule(int maxDuration, int delay, Consumer<IAction> task) {
        return sequentialQueue.schedule(maxDuration, delay, task);
    }

    @Override
    public IAction schedule(String name, Consumer<IAction> task) {
        return sequentialQueue.schedule(name, task);
    }

    @Override
    public IAction schedule(String name, int delay, Consumer<IAction> task) {
        return sequentialQueue.schedule(name, delay, task);
    }

    @Override
    public IAction schedule(String name, int maxDuration, int delay, Consumer<IAction> task) {
        return sequentialQueue.schedule(name, maxDuration, delay, task);
    }

    @Override
    public IAction scheduleActionAt(int index, IAction action) {
        return sequentialQueue.scheduleActionAt(index, action);
    }

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    // Parallels

    @Override
    public IActionQueue getParallelQueue() {
        return parallelQueue;
    }

    @Override
    public IAction scheduleParallel(IAction action) {
        return parallelQueue.schedule(action);
    }

    @Override
    public void scheduleParallel(IAction... actions) {
        parallelQueue.schedule(actions);
    }

    @Override
    public void scheduleParallel(Consumer<IAction>... tasks) {
        parallelQueue.schedule(tasks);
    }

    @Override
    public IAction scheduleParallel(Consumer<IAction> task) {
        return parallelQueue.schedule(task);
    }

    @Override
    public IAction scheduleParallel(int delay, Consumer<IAction> task) {
        return parallelQueue.schedule(delay, task);
    }

    @Override
    public IAction scheduleParallel(int maxDuration, int delay, Consumer<IAction> task) {
        return parallelQueue.schedule(maxDuration, delay, task);
    }

    @Override
    public IAction scheduleParallel(String name, Consumer<IAction> task) {
        return parallelQueue.schedule(name, task);
    }

    @Override
    public IAction scheduleParallel(String name, int delay, Consumer<IAction> task) {
        return parallelQueue.schedule(delay, task);
    }

    @Override
    public IAction scheduleParallel(String name, int maxDuration, int delay, Consumer<IAction> task) {
        return parallelQueue.schedule(name, maxDuration, delay, task);
    }

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    // Conditionals

    @Override
    public IActionQueue getConditionalQueue() {
        return conditionalQueue;
    }

    @Override
    public IConditionalAction schedule(IConditionalAction action) {
        return (IConditionalAction) conditionalQueue.schedule(action);
    }

    @Override
    public void schedule(IConditionalAction... actions) {
        conditionalQueue.schedule(actions);
    }

    @Override
    public IConditionalAction schedule(Function<IAction, Boolean> condition, Consumer<IAction> task) {
        return schedule(new ConditionalAction(this, condition, task));
    }

    @Override
    public IConditionalAction schedule(Function<IAction, Boolean> condition, Consumer<IAction> task, Function<IAction, Boolean> terminateWhen) {
        return schedule(new ConditionalAction(this, condition, task, terminateWhen));
    }

    @Override
    public IConditionalAction schedule(Function<IAction, Boolean> condition, Consumer<IAction> task, Function<IAction, Boolean> terminateWhen, Consumer<IAction> onTermination) {
        return schedule(new ConditionalAction(this, condition, task, terminateWhen, onTermination));
    }


    @Override
    public IConditionalAction schedule(String name, Function<IAction, Boolean> condition, Consumer<IAction> task) {
        return schedule(new ConditionalAction(this, name, condition, task));
    }

    @Override
    public IConditionalAction schedule(String name, Function<IAction, Boolean> condition, Consumer<IAction> task, Function<IAction, Boolean> terminateWhen) {
        return schedule(new ConditionalAction(this, name, condition, task, terminateWhen));
    }

    @Override
    public IConditionalAction schedule(String name, Function<IAction, Boolean> condition, Consumer<IAction> task, Function<IAction, Boolean> terminateWhen, Consumer<IAction> onTermination) {
        return schedule(new ConditionalAction(this, name, condition, task, terminateWhen, onTermination));
    }

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    // Handling

    @Override
    public IActionQueue[] getAllQueues() {
        ArrayList<IActionQueue> allQueues = new ArrayList<>();

        allQueues.add(sequentialQueue);
        allQueues.add(parallelQueue);
        allQueues.add(conditionalQueue);

        allQueues.addAll(otherQueues.values());

        return allQueues.toArray(new IActionQueue[0]);
    }

    public boolean hasActiveTasks() {
        return Arrays.stream(getAllQueues()).anyMatch(queue -> queue.hasActiveTasks());
    }

    @Override
    public boolean hasAny(String name) {
        return getAny(name) != null;
    }

    @Override
    public IAction getAny(String name) {
        IAction act = sequentialQueue.get(name);
        if (act != null)
            return act;

        act = parallelQueue.get(name);
        if (act != null)
            return act;

        act = conditionalQueue.get(name);
        if (act != null)
            return act;

        for (IActionQueue q : otherQueues.values()) {
            act = q.get(name);
            if (act != null)
                return act;
        }

        return null;
    }

    @Override
    public boolean cancelAny(String name) {
        boolean canceled = sequentialQueue.cancel(name);
        if (canceled)
            return true;

        canceled = parallelQueue.cancel(name);
        if (canceled)
            return true;

        canceled = conditionalQueue.cancel(name);
        if (canceled)
            return true;

        for (IActionQueue queue : otherQueues.values())
            if (queue.cancel(name))
                return true;

        return false;
    }

    /**
     * Call once per tick from your main loop.
     */
    public void tick() {
        if (!isWorking) return;

        boolean active = hasActiveTasks();
        if (debug && active)
            LOGGER.beginTick(this).log("Started Tick Cycle", this);

        // ─── Sequential (head only) ─────────────────────────────────
        sequentialQueue.tick();

        // ─── Parallel (all) ───────────────────────────────────────
        parallelQueue.tick();

        // ─── Conditionals ─────────────────────────────────────────
        conditionalQueue.tick();

        // ─── Other Queues ─────────────────────────────────────────
        Iterator<IActionQueue> it = otherQueues.values().iterator();
        while (it.hasNext()) {
            IActionQueue other = it.next();
            ((ActionQueue) other).tick();

            if (other.isDead()) {
                other.clear();
                if (debug)
                    LOGGER.log(String.format("Removing queue '%s' from '%s'", other.getName(), getInternalName()), this);
                it.remove();
            }
        }
        // ─── All Listeners ─────────────────────────────────────────
        // IActionListener Comments
//        if (this == GLOBAL) {
//            listeners.forEach((obj, listener) -> ((ActionListener) listener).tick());
//        }

        if (debug && active)
            LOGGER.finish("Finished Tick Cycle", this);
    }

    @Override
    public void clear() {
        sequentialQueue.clear();
        parallelQueue.clear();
        conditionalQueue.clear();
        otherQueues.forEach((name, queue) -> queue.clear());

        if (debug)
            LOGGER.log("Cleared all queues!", this);
    }

    public String getInternalName() {
        return name.isEmpty() ? "Action Manager" : name;
    }

    @Override
    public IActionChain chain() {
        return sequentialQueue.chain();
    }

    @Override
    public IActionChain parallelChain() {
        return parallelQueue.chain();
    }

    @Override
    public String printQueues() {

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("ActionManager [working=%s]\n", isWorking));


        int i = 0;
        for (IActionQueue action : getAllQueues()) {
            sb.append(String.format("  [%d] %s\n", i++, action.toString()));
        }

        return sb.toString();
    }
}
