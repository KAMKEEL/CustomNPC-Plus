package noppes.npcs.scripted;

import noppes.npcs.api.handler.IActionManager;
import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.IActionChain;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;
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

    @Override
    public IAction create(String name, int maxDuration, int startAfterTicks, Consumer<IAction> task) {
        return new Action(name, maxDuration, startAfterTicks, task);
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
    public IAction scheduleConditionalAction(String name,
                                          int checkIntervalTicks,
                                          Supplier<Boolean> predicate,
                                          Consumer<IAction> task) {
        return scheduleConditionalAction(name, checkIntervalTicks, predicate, task, -1);
    }

    @Override
    public IAction scheduleConditionalAction(String name,
                                          int checkIntervalTicks,
                                          Supplier<Boolean> predicate,
                                          Consumer<IAction> task,
                                          int maxChecks) {
        return scheduleAction(new ConditionalAction(name, checkIntervalTicks, predicate, task, maxChecks));
    }

    @Override
    public IAction scheduleConditionalAction(String name, int checkIntervalTicks, Supplier<Boolean> predicate, Supplier<Boolean> terminateWhen, Consumer<IAction> task, int maxChecks) {
        return scheduleAction(new ConditionalAction(name, checkIntervalTicks, predicate, terminateWhen, task, maxChecks));
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
            if (cab.isDone() || cab.getDuration() >= cab.getMaxDuration()) {
                actionQueue.pollFirst();
            }
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
        protected int duration = 0;
        protected final int maxDuration;
        protected int updateEveryXTick = 5;
        protected final Consumer<IAction> task;
        private boolean done = false;

        protected ActionBase(String name, int maxDuration, int startAfterTicks, Consumer<IAction> task) {
            this.name = name;
            this.maxDuration = maxDuration;
            this.startAfterTicks = startAfterTicks;
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

        @Override
        public Object getData(String key) {
            return dataStore.get(key);
        }

        @Override
        public void addData(String key, Object v) {
            dataStore.put(key, v);
        }

        @Override
        public int getUpdateEveryXTick() {
            return updateEveryXTick;
        }

        @Override
        public void setUpdateEveryXTick(int x) {
            updateEveryXTick = x;
        }

        @Override
        public int getStartAfterTicks() {
            return startAfterTicks;
        }

        @Override
        public IAction create(String n, int md, int sat, Consumer<IAction> t) {
            return ScriptedActionManager.this.create(n, md, sat, t);
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

        @Override
        public IAction after(IAction after) {
            int idx = getIndex(this);
            if (idx >= 0) scheduleActionAt(idx + 1, after);
            return after;
        }



        @Override
        public IAction before(IAction before) {
            int idx = getIndex(this);
            if (idx >= 0) scheduleActionAt(Math.max(0, idx), before);
            return before;
        }

        private final java.util.Map<String, Object> dataStore = new java.util.HashMap<>();
    }

    private class Action extends ActionBase {
        public Action(String name, int maxDuration, int startAfterTicks, Consumer<IAction> task) {
            super(name, maxDuration, startAfterTicks, task);
        }

        @Override public int getCheckCount() { return 0; }

        @Override public int getMaxChecks()   { return 0; }
    }

    private class ConditionalAction extends ActionBase {
        private final Supplier<Boolean> predicate;
        private Supplier<Boolean> terminate;
        private final int maxChecks;
        private int checkCount = 0;

        public ConditionalAction(String name,
                                 int checkIntervalTicks,
                                 Supplier<Boolean> predicate,
                                 Consumer<IAction> task,
                                 int maxChecks) {
            super(name, Integer.MAX_VALUE, 0, task);
            this.predicate       = predicate;
            this.updateEveryXTick = checkIntervalTicks;
            this.maxChecks       = maxChecks;
        }

        public ConditionalAction(String name, int checkIntervalTicks, Supplier<Boolean> predicate, Supplier<Boolean> terminate, Consumer<IAction> task, int maxChecks) {
            this(name, checkIntervalTicks, predicate, task, maxChecks);
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

        @Override public int getCheckCount() { return checkCount; }

        @Override public int getMaxChecks()   { return maxChecks;   }
    }
}
