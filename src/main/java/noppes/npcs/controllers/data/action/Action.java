package noppes.npcs.controllers.data.action;

import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.actions.IConditionalAction;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.scripted.CustomNPCsException;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class Action implements IAction {
    protected final ActionManager manager;
    protected boolean isScheduled;
    protected final String name;
    protected int startAfterTicks;
    protected int count, maxCount = -1;
    protected int duration, maxDuration = -1;
    protected int updateEveryXTick = 5;
    protected Consumer<IAction> task;
    protected boolean done;
    protected final Map<String, Object> dataStore = new HashMap<>();
    protected boolean isThreaded;
    protected ActionThread actionThread;
    protected ScriptContainer reportTo;

    public Action(ActionManager manager, String name) {
        this.manager = manager;
        this.name = name;

        if (ScriptContainer.Current != null)
            reportTo = ScriptContainer.Current;
    }

    public Action(ActionManager manager, Consumer<IAction> task) {
        this(manager, task.toString(), task);
    }

    public Action(ActionManager manager, String name, Consumer<IAction> task) {
        this(manager, name);
        this.task = task;
    }

    public Action(ActionManager manager, int startAfterTicks, Consumer<IAction> task) {
        this(manager, task);
        this.startAfterTicks = startAfterTicks;
    }

    public Action(ActionManager manager, String name, int startAfterTicks, Consumer<IAction> task) {
        this(manager, name, task);
        this.startAfterTicks = startAfterTicks;
    }

    public Action(ActionManager manager, int maxDuration, int startAfterTicks, Consumer<IAction> task) {
        this(manager, startAfterTicks, task);
        this.maxDuration = maxDuration;
    }

    public Action(ActionManager manager, String name, int maxDuration, int startAfterTicks, Consumer<IAction> task) {
        this(manager, name, startAfterTicks, task);
        this.maxDuration = maxDuration;
    }


    @Override
    public IAction setTask(Consumer<IAction> task) {
        this.task = task;
        return this;
    }

    public void tick(int ticksExisted) {
        if (done) return;
        if (startAfterTicks > 0) {
            startAfterTicks--;
            return;
        }
        if (maxDuration > -1 && duration >= maxDuration || maxCount == 0) {
            markDone();
            return;
        }
        if (ticksExisted % updateEveryXTick == 0 && task != null) {
            if (isThreaded)
                actionThread.execute("task", this::executeTask);
            else
                executeTask();
        }

        if (maxCount > -1 && count >= maxCount)
            markDone();

        duration++;
    }

    protected void executeTask() {
        try {
            task.accept(this);
            count++;
        } catch (Throwable t) {
            String err = "IAction '" + name + "' threw an exception:";

            if (reportTo != null)
                reportTo.appendConsole(err + "\n" + ExceptionUtils.getStackTrace(t));

            System.err.println(err);
            t.printStackTrace();
            markDone();
        }
    }

    @Override
    public ActionManager getManager() {
        return manager;
    }

    @Override
    public boolean isScheduled() {
        return isScheduled;
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
    public IAction setMaxDuration(int ticks) {
        this.maxDuration = Math.max(-1, ticks);
        return this;
    }

    @Override
    public int getMaxCount() {
        return maxCount;
    }

    @Override
    public IAction times(int n) {
        this.maxCount = Math.max(-1, n);
        return this;
    }

    @Override
    public IAction once() {
        this.maxCount = 1;
        return this;
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
    public void kill() {
        if (actionThread != null)
            actionThread.stop();

        dataStore.clear();
        isScheduled = false;
        done = true;
    }

    @Override
    public Object getData(String key) {
        return dataStore.get(key);
    }

    @Override
    public IAction setData(String key, Object v) {
        dataStore.put(key, v);
        return this;
    }

    @Override
    public IAction removeData(String key) {
        dataStore.remove(key);
        return this;
    }

    @Override
    public boolean hasData(String key) {
        return dataStore.containsKey(key);
    }

    @Override
    public int getUpdateEvery() {
        return updateEveryXTick;
    }

    @Override
    public IAction updateEvery(int x) {
        this.updateEveryXTick = Math.max(1, x);
        return this;
    }

    @Override
    public int getStartAfterTicks() {
        return startAfterTicks;
    }

    @Override
    public IAction pauseFor(int ticks) {
        if (isThreaded)
            actionThread.pauseFor(ticks);
        else
            this.startAfterTicks = ticks;

        return this;
    }

    @Override
    public IAction pauseFor(long millis) {
        if (isThreaded) {
            actionThread.pauseFor(millis);
            return this;
        }

        return pauseFor((int) (millis / 50));
    }

    @Override
    public void pause() {
        if (isThreaded)
            actionThread.pause();
        else
            throw new CustomNPCsException("Must threadify() IAction before pausing!");
    }

    @Override
    public void pauseUntil(Function<IAction, Boolean> until) {
        if (isThreaded)
            actionThread.pauseUntil(until);
        else
            throw new CustomNPCsException("Must threadify() IAction before pausing!");
    }

    @Override
    public void resume() {
        if (isThreaded)
            actionThread.resume();
    }

    @Override
    public boolean isPaused() {
        if (isThreaded)
            return actionThread.isPaused();

        return startAfterTicks > 0;
    }

    @Override
    public IAction threadify() {
        if (!isThreaded) {
            isThreaded = true;
            actionThread = new ActionThread(this);
        }

        return this;
    }

    @Override
    public IAction start() {
        manager.start();
        return this;
    }

    @Override
    public IAction getNext() {
        boolean seenMe = false;
        for (IAction a : manager.getActionQueue()) {
            if (seenMe) {
                return a;
            }
            if (a == this) {
                seenMe = true;
            }
        }
        return null;
    }

    @Override
    public IAction getPrevious() {
        IAction prev = null;
        for (IAction a : manager.getActionQueue()) {
            if (a == this) {
                return prev;
            }
            prev = a;
        }
        return null;
    }

    protected Action unscheduledBefore, unscheduledAfter;

    protected LinkedList<Action> allUnscheduledBefore() {
        Action before = unscheduledBefore;

        LinkedList<Action> befores = new LinkedList<>();
        while (before != null) {
            befores.addFirst(before);
            before = before.unscheduledBefore;
        }

        return befores;
    }

    protected void scheduleAllBefore(Deque<IAction> actionQueue) {
        allUnscheduledBefore().forEach((bef) -> {
            if (bef.unscheduledAfter != null)
                bef.unscheduledAfter.unscheduledBefore = null;
            bef.unscheduledAfter = null;

            actionQueue.addLast(bef);
            bef.isScheduled = true;
        });
    }

    protected LinkedList<Action> allUnscheduledAfter() {
        Action after = unscheduledAfter;

        LinkedList<Action> afters = new LinkedList<>();
        while (after != null) {
            afters.add(after);
            after = after.unscheduledAfter;
        }
        return afters;
    }

    protected void scheduleAllAfter(Deque<IAction> actionQueue) {
        allUnscheduledAfter().forEach((aft) -> {
            if (aft.unscheduledBefore != null)
                aft.unscheduledBefore.unscheduledAfter = null;
            aft.unscheduledBefore = null;

            actionQueue.addLast(aft);
            aft.isScheduled = true;
        });
    }
    @Override
    public IAction after(IAction after) {
        if (after instanceof IConditionalAction)
            return conditional((IConditionalAction) after);

        int idx = manager.getIndex(this);
        if (idx >= 0)
            manager.scheduleActionAt(idx + 1, after);
        else {
            Action aft = (Action) after;
            unscheduledAfter = aft;
            aft.unscheduledBefore = this;
        }
        return after;
    }

    @Override
    public IAction after(String name, int maxDuration, int delay, Consumer<IAction> t) {
        return after(manager.create(name, maxDuration, delay, t));
    }

    @Override
    public IAction after(String name, int delay, Consumer<IAction> t) {
        return after(manager.create(name, delay, t));
    }

    @Override
    public IAction after(int delay, Consumer<IAction> t) {
        return after(manager.create(delay, t));
    }

    @Override
    public IAction after(String name, Consumer<IAction> t) {
        return after(manager.create(name, t));
    }

    @Override
    public IAction after(Consumer<IAction> t) {
        return after(manager.create(t));
    }

    @Override
    public IAction before(IAction before) {
        if (before instanceof IConditionalAction)
            return conditional((IConditionalAction) before);

        int idx = manager.getIndex(this);
        if (idx >= 0)
            manager.scheduleActionAt(Math.max(0, idx), before);
        else {
            Action bef = (Action) before;
            unscheduledBefore = bef;
            bef.unscheduledAfter = this;
        }
        return before;
    }

    @Override
    public IAction before(String name, int maxDuration, int delay, Consumer<IAction> t) {
        return before(manager.create(name, maxDuration, delay, t));
    }

    @Override
    public IAction before(String name, int delay, Consumer<IAction> t) {
        return before(manager.create(name, delay, t));
    }

    @Override
    public IAction before(int delay, Consumer<IAction> t) {
        return before(manager.create(delay, t));
    }

    @Override
    public IAction before(String name, Consumer<IAction> t) {
        return before(manager.create(name, t));
    }

    @Override
    public IAction before(Consumer<IAction> t) {
        return before(manager.create(t));
    }

    @Override
    public IConditionalAction conditional(IConditionalAction after) {
        return manager.schedule(after);
    }

    @Override
    public IConditionalAction conditional(Function<IAction, Boolean> condition, Consumer<IAction> task) {
        return conditional(manager.create(condition, task));
    }

    @Override
    public IConditionalAction conditional(String name, Function<IAction, Boolean> condition, Consumer<IAction> task) {
        return conditional(manager.create(name, condition, task));
    }

    @Override
    public IConditionalAction conditional(Function<IAction, Boolean> condition, Consumer<IAction> task, Function<IAction, Boolean> terminateWhen) {
        return conditional(manager.create(condition, task, terminateWhen));
    }

    @Override
    public IConditionalAction conditional(String name, Function<IAction, Boolean> condition, Consumer<IAction> task, Function<IAction, Boolean> terminateWhen) {
        return conditional(manager.create(name, condition, task, terminateWhen));
    }

    @Override
    public IConditionalAction conditional(Function<IAction, Boolean> condition, Consumer<IAction> task, Function<IAction, Boolean> terminateWhen, Consumer<IAction> onTermination) {
        return conditional(manager.create(condition, task, terminateWhen, onTermination));
    }

    @Override
    public IConditionalAction conditional(String name, Function<IAction, Boolean> condition, Consumer<IAction> task, Function<IAction, Boolean> terminateWhen, Consumer<IAction> onTermination) {
        return conditional(manager.create(name, condition, task, terminateWhen, onTermination));
    }

    @Override
    public IAction parallel(IAction after) {
        return manager.scheduleParallel(after);
    }

    @Override
    public IAction parallel(Consumer<IAction> task) {
        return parallel(manager.create(task));
    }

    @Override
    public IAction parallel(int delay, Consumer<IAction> task) {
        return parallel(manager.create(delay, task));
    }

    @Override
    public IAction parallel(String name, Consumer<IAction> task) {
        return parallel(manager.create(name, task));
    }

    @Override
    public IAction parallel(String name, int startAfterTicks, Consumer<IAction> task) {
        return parallel(manager.create(name, startAfterTicks, task));
    }

    @Override
    public IAction parallel(String name, int maxDuration, int delay, Consumer<IAction> t) {
        return parallel(manager.create(name, maxDuration, delay, t));
    }
}
