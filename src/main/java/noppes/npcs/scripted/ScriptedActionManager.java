package noppes.npcs.scripted;

import noppes.npcs.api.handler.IActionManager;
import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.IActionChain;
import noppes.npcs.api.handler.data.actions.IConditionalAction;
import noppes.npcs.controllers.data.action.Action;
import noppes.npcs.controllers.data.action.ActionChain;
import noppes.npcs.controllers.data.action.ConditionalAction;
import noppes.npcs.controllers.data.action.ParallelActionChain;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Full-featured ActionManager:
 * - delayed start, max-duration, tick-interval
 * - chaining (getNext, getPrevious, scheduleAfter/Before)
 * - per-action data
 * - conditional actions
 * - cancellation & clearing
 */
public class ScriptedActionManager implements IActionManager {
    private boolean isWorking = false;

    private final Deque<IAction> actionQueue = new LinkedList<>();
    private final List<IAction> parallelActions = new LinkedList<>();
    private final List<IConditionalAction> conditionalActions = new LinkedList<>();

    @Override
    public IAction create(String name) {
        return new Action(this, name);
    }

    @Override
    public IAction create(Consumer<IAction> t) {
        return new Action(this, t);
    }

    @Override
    public IAction create(String name, int maxDuration, int startAfterTicks, Consumer<IAction> task) {
        return new Action(this, name, maxDuration, startAfterTicks, task);
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
    public IConditionalAction create(Supplier<Boolean> predicate, Consumer<IAction> task) {
        return new ConditionalAction(this, predicate, task);
    }

    @Override
    public IConditionalAction create(String name, Supplier<Boolean> predicate, Consumer<IAction> task) {
        return new ConditionalAction(this, name, predicate, task);
    }

    @Override
    public IConditionalAction create(Supplier<Boolean> predicate, Supplier<Boolean> terminateWhen, Consumer<IAction> task) {
        return new ConditionalAction(this, predicate, terminateWhen, task);
    }

    @Override
    public IConditionalAction create(String name, Supplier<Boolean> predicate, Supplier<Boolean> terminateWhen, Consumer<IAction> task) {
        return new ConditionalAction(this, name, predicate, terminateWhen, task);
    }

    @Override
    public IConditionalAction create(Supplier<Boolean> predicate, Supplier<Boolean> terminateWhen, Consumer<IAction> task, Consumer<IAction> onTermination) {
        return new ConditionalAction(this, predicate, terminateWhen, task, onTermination);
    }

    @Override
    public IConditionalAction create(String name, Supplier<Boolean> predicate, Supplier<Boolean> terminateWhen, Consumer<IAction> task, Consumer<IAction> onTermination) {
        return new ConditionalAction(this, name, predicate, terminateWhen, task, onTermination);
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
    public IAction scheduleAction(IAction action) {
        actionQueue.addLast(action);
        return action;
    }

    @Override
    public IAction scheduleAction(String name, int maxDuration, int startAfterTicks, Consumer<IAction> task) {
        return scheduleAction(create(name, maxDuration, startAfterTicks, task));
    }

    @Override
    public IAction scheduleActionAt(int index, IAction action) {
        if (index < 0 || index > actionQueue.size()) {
            actionQueue.addLast(action);
        } else {
            ((LinkedList<IAction>) actionQueue).add(index, action);
        }

        return action;
    }

    @Override
    public int getIndex(IAction action) {
        if (action == null) return -1;
        return ((LinkedList<IAction>) actionQueue).indexOf(action);
    }

    @Override
    public IAction getCurrentAction() {
        return actionQueue.peekFirst();
    }

    @Override
    public Queue<IAction> getActionQueue() {
        return actionQueue;
    }

    @Override
    public void clear() {
        actionQueue.clear();
        parallelActions.clear();
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

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    // Conditionals
    @Override
    public IConditionalAction scheduleAction(String name, Supplier<Boolean> predicate, Consumer<IAction> task) {
        return scheduleAction(new ConditionalAction(this, name, predicate, task));
    }

    @Override
    public IConditionalAction scheduleAction(String name, Supplier<Boolean> predicate, Supplier<Boolean> terminateWhen, Consumer<IAction> task) {
        return scheduleAction(new ConditionalAction(this, name, predicate, terminateWhen, task));
    }

    @Override
    public IConditionalAction scheduleAction(IConditionalAction action) {
        conditionalActions.add(action);
        return action;
    }

    @Override
    public List<IConditionalAction> getConditionalActions() {
        return conditionalActions;
    }

    @Override
    public IAction scheduleParallelAction(IAction action) {
        parallelActions.add(action);
        return action;
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
            if (cab.isDone()) actionQueue.pollFirst();
        }

        // ─── Parallel (all) ───────────────────────────────────────
        Iterator<IAction> pit = parallelActions.iterator();
        while (pit.hasNext()) {
            Action a = (Action) pit.next();
            a.tick(ticksExisted);
            if (a.isDone()) pit.remove();
        }

        // ─── Conditionals ─────────────────────────────────────────
        Iterator<IConditionalAction> cit = conditionalActions.iterator();
        while (cit.hasNext()) {
            ConditionalAction con = (ConditionalAction) cit.next();
            con.tick(ticksExisted);
            if (con.isDone()) cit.remove();
        }
    }

    @Override
    public IActionChain chain() {
        return new ActionChain(this);
    }

    @Override
    public IActionChain parallelChain() {
        return new ParallelActionChain(this);
    }
}
