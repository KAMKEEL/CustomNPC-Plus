package noppes.npcs.controllers.data.action;

import noppes.npcs.api.handler.IActionManager;
import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.IActionChain;
import noppes.npcs.api.handler.data.IActionQueue;
import noppes.npcs.api.handler.data.actions.IConditionalAction;
import noppes.npcs.controllers.data.action.action.ConditionalAction;

import java.util.HashMap;
import java.util.Map;
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
    protected boolean isWorking = false;

    protected final IActionQueue sequentialQueue = new ActionQueue(this, "mainSequential");
    protected final IActionQueue parallelQueue = new ActionQueue(this, "mainParallel").setParallel(true);
    protected final IActionQueue conditionalQueue = new ActionQueue(this, "mainConditional").setParallel(true);

    protected final Map<String, IActionQueue> otherQueues = new HashMap<>();

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
    public IConditionalAction create(Function<IAction,Boolean> condition, Consumer<IAction> task) {
        return new ConditionalAction(this, condition, task);
    }

    @Override
    public IConditionalAction create(String name,Function<IAction,Boolean> condition, Consumer<IAction> task) {
        return new ConditionalAction(this, name, condition, task);
    }

    @Override
    public IConditionalAction create(Function<IAction,Boolean> condition, Consumer<IAction> task,Function<IAction,Boolean> terminateWhen) {
        return new ConditionalAction(this, condition, task, terminateWhen);
    }

    @Override
    public IConditionalAction create(String name,Function<IAction,Boolean> condition, Consumer<IAction> task,Function<IAction,Boolean> terminateWhen) {
        return new ConditionalAction(this, name, condition, task, terminateWhen);
    }

    @Override
    public IConditionalAction create(Function<IAction,Boolean> condition, Consumer<IAction> task,Function<IAction,Boolean> terminateWhen, Consumer<IAction> onTermination) {
        return new ConditionalAction(this, condition, task, terminateWhen, onTermination);
    }

    @Override
    public IConditionalAction create(String name,Function<IAction,Boolean> condition, Consumer<IAction> task,Function<IAction,Boolean> terminateWhen, Consumer<IAction> onTermination) {
        return new ConditionalAction(this, name, condition, task, terminateWhen, onTermination);
    }

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    // Queues

    public IActionQueue createQueue(String name) {
        return createQueue(name, false);
    }

    public IActionQueue createQueue(String name, boolean isParallel) {
        IActionQueue queue = new ActionQueue(this, name, isParallel);
        otherQueues.put(name, queue);
        return queue;
    }

    public IActionQueue getOrCreateQueue(String name) {
        return getOrCreateQueue(name, false);
    }

    public IActionQueue getOrCreateQueue(String name, boolean isParallel) {
        IActionQueue queue = getQueue(name);

        if (queue == null)
            queue = createQueue(name, isParallel);
        return queue;
    }

    public IActionQueue getQueue(String name) {
        return otherQueues.get(name);
    }

    public boolean removeQueue(String name) {
        IActionQueue queue = otherQueues.get(name);

        if (queue == null)
            return false;

        otherQueues.remove(name);
        queue.clear();
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
    public IConditionalAction schedule(Function<IAction,Boolean> condition, Consumer<IAction> task) {
        return schedule(new ConditionalAction(this, condition, task));
    }

    @Override
    public IConditionalAction schedule(Function<IAction,Boolean> condition, Consumer<IAction> task, Function<IAction,Boolean> terminateWhen) {
        return schedule(new ConditionalAction(this, condition, task, terminateWhen));
    }

    @Override
    public IConditionalAction schedule(Function<IAction,Boolean> condition, Consumer<IAction> task, Function<IAction,Boolean> terminateWhen, Consumer<IAction> onTermination) {
        return schedule(new ConditionalAction(this, condition, task, terminateWhen, onTermination));
    }


    @Override
    public IConditionalAction schedule(String name, Function<IAction,Boolean> condition, Consumer<IAction> task) {
        return schedule(new ConditionalAction(this, name, condition, task));
    }

    @Override
    public IConditionalAction schedule(String name, Function<IAction,Boolean> condition, Consumer<IAction> task, Function<IAction,Boolean> terminateWhen) {
        return schedule(new ConditionalAction(this, name, condition, task, terminateWhen));
    }

    @Override
    public IConditionalAction schedule(String name, Function<IAction,Boolean> condition, Consumer<IAction> task, Function<IAction,Boolean> terminateWhen, Consumer<IAction> onTermination) {
        return schedule(new ConditionalAction(this, name, condition, task, terminateWhen, onTermination));
    }

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    // Handling

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
     *
     * @param ticksExisted the global tick count, used for modulo checks.
     */
    public void tick(int ticksExisted) {
        if (!isWorking) return;

        // ─── Sequential (head only) ─────────────────────────────────
        ((ActionQueue) sequentialQueue).tick(ticksExisted);

        // ─── Parallel (all) ───────────────────────────────────────
        ((ActionQueue) parallelQueue).tick(ticksExisted);

        // ─── Conditionals ─────────────────────────────────────────
        ((ActionQueue) conditionalQueue).tick(ticksExisted);

        // ─── Other Queues ─────────────────────────────────────────
        otherQueues.forEach((name, queue) -> ((ActionQueue) queue).tick(ticksExisted));
    }

    @Override
    public void clear() {
        sequentialQueue.clear();
        parallelQueue.clear();
        conditionalQueue.clear();
        otherQueues.forEach((name, queue) -> queue.clear());
    }

    @Override
    public IActionChain chain() {
        return sequentialQueue.chain();
    }

    @Override
    public IActionChain parallelChain() {
        return parallelQueue.chain();
    }
}
