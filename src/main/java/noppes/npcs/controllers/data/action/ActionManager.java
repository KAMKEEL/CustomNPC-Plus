package noppes.npcs.controllers.data.action;

import noppes.npcs.api.handler.IActionManager;
import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.IActionChain;
import noppes.npcs.api.handler.data.actions.IConditionalAction;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
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
    private boolean isWorking = false;

    private final Deque<IAction> actionQueue = new ConcurrentLinkedDeque<>();
    private final Deque<IAction> parallelActions = new ConcurrentLinkedDeque<>();
    private final Deque<IConditionalAction> conditionalActions = new ConcurrentLinkedDeque<>();

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

    @Override
    public void start() {
        isWorking = true;
    }

    @Override
    public void stop() {
        isWorking = false;
    }

    @Override
    public IAction schedule(IAction action) {
        if (action instanceof IConditionalAction)
            return schedule((IConditionalAction) action);

        Action act = (Action) action;
        if (act.unscheduledBefore != null)
            act.scheduleAllBefore(actionQueue);

        actionQueue.addLast(action);

        if (act.unscheduledAfter != null)
            act.scheduleAllAfter(actionQueue);

        act.isScheduled = true;
        return action;
    }

    @Override
    public IAction schedule(Consumer<IAction> task) {
        return schedule(create(task));
    }

    @Override
    public IAction schedule(int delay, Consumer<IAction> task) {
        return schedule(create(delay, task));
    }

    @Override
    public IAction schedule(int maxDuration, int delay, Consumer<IAction> task) {
        return schedule(create(maxDuration, delay, task));
    }

    @Override
    public IAction schedule(String name, Consumer<IAction> task) {
        return schedule(create(name, task));
    }
    @Override
    public IAction schedule(String name, int delay, Consumer<IAction> task) {
        return schedule(create(name, delay, task));
    }

    @Override
    public IAction schedule(String name, int maxDuration, int delay, Consumer<IAction> task) {
        return schedule(create(name, maxDuration, delay, task));
    }

    @Override
    public IAction scheduleActionAt(int index, IAction action) {
        int size = actionQueue.size();
        if (index <= 0) {
            actionQueue.addFirst(action);
        } else if (index >= size) {
            actionQueue.addLast(action);
        } else {
            List<IAction> tmp = new ArrayList<>(actionQueue);
            tmp.add(index, action);
            actionQueue.clear();
            actionQueue.addAll(tmp);
        }
        ((Action) action).isScheduled = true;
        return action;
    }

    @Override
    public int getIndex(IAction action) {
        int i = 0;
        for (IAction a : actionQueue) {
            if (a.equals(action)) return i;
            i++;
        }
        return -1;
    }

    @Override
    public IAction getCurrentAction() {
        return actionQueue.peekFirst();
    }

    @Override
    public IAction getAction(String name) {
        for (IAction action : actionQueue) {
            if (action.getName().equals(name))
                return action;
        }

        for (IAction action : parallelActions) {
            if (action.getName().equals(name))
                return action;
        }

        for (IAction action : conditionalActions) {
            if (action.getName().equals(name))
                return action;
        }

        return null;
    }

    @Override
    public Queue<IAction> getActionQueue() {
        return actionQueue;
    }

    @Override
    public boolean hasAction(String name) {
        for (IAction act : actionQueue)
            if (act.getName().equals(name))
                return true;

        return false;
    }
    @Override
    public boolean hasAny(String name) {
        if (hasAction(name))
            return true;
        else if (hasParallel(name))
            return true;
        else if (hasConditional(name))
            return true;

        return false;
    }

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    // Conditionals
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


    @Override
    public IConditionalAction schedule(IConditionalAction action) {
        conditionalActions.add(action);
        ((Action) action).isScheduled = true;
        return action;
    }

    @Override
    public List<IConditionalAction> getConditionalActions() {
        return new ArrayList<>(conditionalActions);
    }

    @Override
    public boolean hasConditional(String name) {
        for (IAction act : conditionalActions)
            if (act.getName().equals(name))
                return true;

        return false;
    }

    @Override
    public IAction scheduleParallel(IAction action) {
        parallelActions.add(action);
        ((Action) action).isScheduled = true;
        return action;
    }
    @Override
    public IAction scheduleParallel(String name, Consumer<IAction> task) {
        return scheduleParallel(create(name, task));
    }
    @Override
    public IAction scheduleParallel(String name, int delay, Consumer<IAction> task) {
        return scheduleParallel(create(name, delay, task));
    }

    @Override
    public IAction scheduleParallel(String name, int maxDuration, int delay, Consumer<IAction> task) {
        return scheduleParallel(create(name, maxDuration, delay, task));
    }


    /**
     * Call once per tick from your main loop.
     *
     * @param ticksExisted the global tick count, used for modulo checks.
     */
    public void tick(int ticksExisted) {
        if (!isWorking) return;

        // ─── Sequential (head only) ─────────────────────────────────
        IAction current = getCurrentAction();
        if (current instanceof Action) {
            Action cab = (Action) current;
            cab.tick(ticksExisted);
            if (cab.isDone()) {
                cab.kill();
                actionQueue.pollFirst();
            }
        }

        // ─── Parallel (all) ───────────────────────────────────────
        Iterator<IAction> pit = parallelActions.iterator();
        while (pit.hasNext()) {
            Action a = (Action) pit.next();
            a.tick(ticksExisted);
            if (a.isDone()) {
                a.kill();
                pit.remove();
            }
        }

        // ─── Conditionals ─────────────────────────────────────────
        Iterator<IConditionalAction> cit = conditionalActions.iterator();
        while (cit.hasNext()) {
            ConditionalAction con = (ConditionalAction) cit.next();
            con.tick(ticksExisted);
            if (con.isDone()) {
                con.kill();
                cit.remove();
            }
        }
    }

    @Override
    public void clear() {
        actionQueue.forEach((act) -> act.kill());
        actionQueue.clear();

        parallelActions.forEach((act) -> act.kill());
        parallelActions.clear();

        conditionalActions.forEach((act) -> act.kill());
        conditionalActions.clear();
    }

    @Override
    public boolean cancelAction(String name) {
        Iterator<IAction> acts = actionQueue.iterator();
        while (acts.hasNext()) {
            IAction act = acts.next();
            if (act.getName().equals(name)) {
                acts.remove();
                return true;
            }
        }

        Iterator<IAction> pit = parallelActions.iterator();
        while (pit.hasNext()) {
            if (pit.next().getName().equals(name)) {
                pit.remove();
                return true;
            }
        }

        Iterator<IConditionalAction> cons = conditionalActions.iterator();
        while (cons.hasNext()) {
            IConditionalAction con = cons.next();
            if (con.getName().equals(name)) {
                cons.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public IActionChain chain() {
        return new ActionChain(this);
    }

    @Override
    public IActionChain parallelChain() {
        return new ParallelActionChain(this);
    }

    @Override
    public Queue<IAction> getParallelActions() {
        return parallelActions;
    }

    @Override
    public boolean hasParallel(String name) {
        for (IAction act : parallelActions)
            if (act.getName().equals(name))
                return true;

        return false;
    }
}
