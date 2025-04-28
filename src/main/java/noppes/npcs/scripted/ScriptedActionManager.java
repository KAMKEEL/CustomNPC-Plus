package noppes.npcs.scripted;

import noppes.npcs.api.handler.IActionManager;
import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.IActionChain;
import noppes.npcs.api.handler.data.actions.IConditionalAction;

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
    private final List<IConditionalAction> conditionalActions = new LinkedList<>();

    @Override
    public IAction create(String name, int maxDuration, int startAfterTicks, Consumer<IAction> task) {
        return new Action(name, maxDuration, startAfterTicks, task);
    }

    @Override
    public IAction create(String name, int delay, Consumer<IAction> t) {
        return new Action(name, delay, t);
    }

    @Override
    public IAction create(int delay, Consumer<IAction> t) {
        return new Action(delay, t);
    }

    @Override
    public IAction create(String name, Consumer<IAction> t) {
        return new Action(name, t);
    }

    @Override
    public IAction create(Consumer<IAction> t) {
        return new Action(t);
    }

    @Override
    public IConditionalAction create(Supplier<Boolean> predicate, Consumer<IAction> task) {
        return new ConditionalAction(predicate, task);
    }

    @Override
    public IConditionalAction create(String name, Supplier<Boolean> predicate, Consumer<IAction> task) {
        return new ConditionalAction(name, predicate, task);
    }

    @Override
    public IConditionalAction create(Supplier<Boolean> predicate, Supplier<Boolean> terminate, Consumer<IAction> task) {
        return new ConditionalAction(predicate, terminate, task);
    }

    @Override
    public IConditionalAction create(String name, Supplier<Boolean> predicate, Supplier<Boolean> terminate, Consumer<IAction> task) {
        return new ConditionalAction(name, predicate, terminate, task);
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
    }

    @Override
    public boolean cancelAction(String name) {
        for (IAction act : actionQueue) {
            if (act.getName().equals(name)) {
                ((LinkedList<IAction>) actionQueue).remove(act);
                return true;
            }
        }
        return false;
    }


    @Override
    public IConditionalAction scheduleConditionalAction(String name, Supplier<Boolean> predicate, Consumer<IAction> task) {
        return scheduleConditionalAction(new ConditionalAction(name, predicate, task));
    }

    @Override
    public IConditionalAction scheduleConditionalAction(String name, Supplier<Boolean> predicate, Supplier<Boolean> terminateWhen, Consumer<IAction> task) {
        return scheduleConditionalAction(new ConditionalAction(name, predicate, terminateWhen, task));
    }

    @Override
    public IConditionalAction scheduleConditionalAction(IConditionalAction action) {
        conditionalActions.add(action);
        return action;
    }

    /**
     * Call once per tick from your main loop.
     *
     * @param ticksExisted the global tick count, used for modulo checks.
     */
    public void tick(int ticksExisted) {
        if (!isWorking) return;
        IAction current = getCurrentAction();
        if (current instanceof ActionBase) {
            ActionBase cab = (ActionBase) current;
            cab.tick(ticksExisted);
            if (cab.isDone())
                actionQueue.pollFirst();
        }

        Iterator<IConditionalAction> it = conditionalActions.iterator();
        while (it.hasNext()) {
            ConditionalAction con = (ConditionalAction) it.next();
            con.tick(ticksExisted);
            if (con.isDone())
                it.remove();
        }

    }

    @Override
    public IActionChain chain() {
        return new ActionChain();
    }

    /** helper to build a back‐to‐back chain of one‐shot actions */
    public class ActionChain implements IActionChain {
        private int offset = 0, index = 0;

        /** schedule the next task ‘delay’ ticks after the previous one */
        @Override
        public IActionChain after(int delay, Consumer<IAction> task) {
            offset += delay;
            Consumer<IAction> wrapper = act -> {
                task.accept(act);
                act.markDone();
            };
            IAction a = create("chain#" + (index++), Integer.MAX_VALUE, offset, wrapper);
            a.setUpdateEveryXTick(1);
            scheduleAction(a);
            return this;
        }
    }

    // ──── base class for all custom actions ──────────────────────────────────

    private abstract class ActionBase implements IAction {
        protected final String name;
        protected int startAfterTicks;
        protected int count = 0;
        protected int duration;
        protected int maxDuration = -1;
        protected int updateEveryXTick = 5;
        protected final Consumer<IAction> task;
        private boolean done = false;

        protected ActionBase(String name, int maxDuration, int startAfterTicks, Consumer<IAction> task) {
            this.name = name;
            this.maxDuration = maxDuration;
            this.startAfterTicks = startAfterTicks;
            this.task = task;
        }

        protected ActionBase(String name, int startAfterTicks, Consumer<IAction> task) {
            this.name = name;
            this.startAfterTicks = startAfterTicks;
            this.task = task;
        }

        protected ActionBase(String name, Consumer<IAction> task) {
            this.name = name;
            this.task = task;
        }

        protected ActionBase(int startAfterTicks, Consumer<IAction> task) {
            this(task);
            this.startAfterTicks = startAfterTicks;
        }

        protected ActionBase(Consumer<IAction> task) {
            this.name = task.toString();
            this.task = task;
        }
        /**
         * Called once per global tick; respects delay, interval, duration & done‐flag.
         */
        public void tick(int ticksExisted) {
            if (done) return;

            if (startAfterTicks > 0) {
                startAfterTicks--;
                return;
            }

            if (maxDuration != -1 && duration >= maxDuration)
                markDone();
            // only run on our update tick
            if (ticksExisted % updateEveryXTick == 0) {
                task.accept(this);
                count++;
            }
            duration++;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public int getDuration() {
            return duration;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getMaxDuration() {
            return maxDuration;
        }

        @Override
        public void markDone() {
            done = true;
        }

        @Override
        public boolean isDone() {
            return done;
        }

        private final java.util.Map<String, Object> dataStore = new java.util.HashMap<>();
        @Override
        public Object getData(String key) {
            return dataStore.get(key);
        }

        @Override
        public IAction addData(String key, Object v) {
            dataStore.put(key, v);
            return this;
        }

        @Override
        public int getUpdateEveryXTick() {
            return updateEveryXTick;
        }

        @Override
        public IAction setUpdateEveryXTick(int x) {
            updateEveryXTick = x;
            return this;
        }

        @Override
        public int getStartAfterTicks() {
            return startAfterTicks;
        }

        @Override
        public IAction getNext() {
            int idx = getIndex(this);
            return (idx >= 0 && idx + 1 < actionQueue.size())
                ? ((LinkedList<IAction>) actionQueue).get(idx + 1)
                : null;
        }

        @Override
        public IAction getPrevious() {
            int idx = getIndex(this);
            return (idx > 0)
                ? ((LinkedList<IAction>) actionQueue).get(idx - 1)
                : null;
        }

        /////////////////////////////////////////////////
        /////////////////////////////////////////////////
        //After chains
        @Override
        public IAction after(IAction after) {
            int idx = getIndex(this);
            if (idx >= 0) scheduleActionAt(idx + 1, after);
            return after;
        }

        @Override
        public IAction after(String name, int maxDuration, int delay, Consumer<IAction> t) {
            return after(create(name, maxDuration, delay, t));
        }

        @Override
        public IAction after(String name, int delay, Consumer<IAction> t) {
            return after(create(name, delay, t));
        }

        @Override
        public IAction after(int delay, Consumer<IAction> t) {
            return after(create(delay, t));
        }

        @Override
        public IAction after(String name, Consumer<IAction> t) {
            return after(create(name, t));
        }

        @Override
        public IAction after(Consumer<IAction> t) {
            return after(create(t));

        }

        /////////////////////////////////////////////////
        /////////////////////////////////////////////////
        //Before chains
        @Override
        public IAction before(IAction before) {
            int idx = getIndex(this);
            if (idx >= 0) scheduleActionAt(Math.max(0, idx), before);
            return before;
        }

        @Override
        public IAction before(String name, int maxDuration, int delay, Consumer<IAction> t) {
            return before(create(name, maxDuration, delay, t));
        }

        @Override
        public IAction before(String name, int delay, Consumer<IAction> t) {
            return before(create(name, delay, t));
        }

        @Override
        public IAction before(int delay, Consumer<IAction> t) {
            return before(create(delay, t));
        }

        @Override
        public IAction before(String name, Consumer<IAction> t) {
            return before(create(name, t));
        }

        @Override
        public IAction before(Consumer<IAction> t) {
            return before(create(t));

        }
    }

    private class Action extends ActionBase {
        public Action(String name, int maxDuration, int startAfterTicks, Consumer<IAction> task) {
            super(name, maxDuration, startAfterTicks, task);
        }

        public Action(String name, int startAfterTicks, Consumer<IAction> task) {
            super(name, startAfterTicks, task);
        }

        public Action(String name, Consumer<IAction> task) {
            super(name, task);
        }

        public Action(int startAfterTicks, Consumer<IAction> task) {
            super(startAfterTicks, task);
        }

        public Action(Consumer<IAction> task) {
            super(task);
        }

        @Override public int getCheckCount() { return 0; }

        @Override public int getMaxChecks()   { return 0; }
    }

    private class ConditionalAction extends ActionBase implements IConditionalAction {
        private final Supplier<Boolean> predicate;
        private Supplier<Boolean> terminate;
        private int maxChecks = -1;
        private int checkCount = 0;

        public ConditionalAction(Supplier<Boolean> predicate, Consumer<IAction> task) {
            super(task);
            this.predicate = predicate;
        }

        public ConditionalAction(String name, Supplier<Boolean> predicate, Consumer<IAction> task) {
            super(name, task);
            this.predicate = predicate;
        }

        public ConditionalAction(Supplier<Boolean> predicate, Supplier<Boolean> terminate, Consumer<IAction> task) {
            this(predicate, task);
            this.terminate = terminate;
        }

        public ConditionalAction(String name, Supplier<Boolean> predicate, Supplier<Boolean> terminate, Consumer<IAction> task) {
            this(name, predicate, task);
            this.terminate = terminate;
        }

        @Override
        public void tick(int ticksExisted) {
            if (isDone()) return;
            if (ticksExisted % updateEveryXTick == 0) {
                checkCount++;
                if ((maxChecks >= 0 && checkCount > maxChecks) || terminate != null && terminate.get()) {
                    markDone();
                    return;
                }
                if (predicate.get())
                    task.accept(this);
            }
        }

        @Override
        public IConditionalAction setMaxChecks(int maxChecks) {
            this.maxChecks = maxChecks;
            return this;
        }

        @Override public int getCheckCount() { return checkCount; }

        @Override public int getMaxChecks()   { return maxChecks;   }

        /////////////////////////////////////////////////
        /////////////////////////////////////////////////
        //Before chains
        @Override
        public IConditionalAction after(IConditionalAction after) {
            return scheduleConditionalAction(after);
        }

        @Override
        public IConditionalAction after(Supplier<Boolean> predicate, Consumer<IAction> task) {
            return after(create(predicate, task));
        }

        @Override
        public IConditionalAction after(String name, Supplier<Boolean> predicate, Consumer<IAction> task) {
            return after(create(name, predicate, task));
        }

        @Override
        public IConditionalAction after(Supplier<Boolean> predicate, Supplier<Boolean> terminate, Consumer<IAction> task) {
            return after(create(predicate, terminate, task));
        }

        @Override
        public IConditionalAction after(String name, Supplier<Boolean> predicate, Supplier<Boolean> terminate, Consumer<IAction> task) {
            return after(create(name, predicate, terminate, task));
        }


    }
}
