package noppes.npcs.controllers.data.action;

import noppes.npcs.api.handler.IActionManager;
import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.IActionListener;
import noppes.npcs.api.handler.data.IActionQueue;
import noppes.npcs.api.handler.data.actions.IConditionalAction;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.scripted.CustomNPCsException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class Action implements IAction {
    protected final ActionManager manager;
    protected IActionQueue queue;
    protected boolean isScheduled;
    protected final String name;
    protected int startAfterTicks;
    protected int count, maxCount = -1;
    protected int duration, maxDuration = -1;
    protected int updateEveryXTick = 5;
    protected Consumer<IAction> task, onStart, onDone;
    protected boolean done;
    protected final Map<String, Object> dataStore = new HashMap<>();
    protected boolean isThreaded;
    protected ActionThread actionThread;
    protected ScriptContainer reportTo;

    //allows chaining .before() and .after() on unscheduled actions
    protected ActionList unscheduledList;

    public Action(ActionManager manager, String name) {
        this.manager = manager;
        this.name = name;

        if (ScriptContainer.Current != null)
            reportTo = ScriptContainer.Current;
    }

    public Action(ActionManager manager, Consumer<IAction> task) {
        this(manager, Integer.toHexString(task.hashCode()), task);
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
    public IActionManager getManager() {
        return manager;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public IActionQueue getQueue() {
        return queue;
    }

    public String getQueueName() {
        return queue != null ? queue.getName() : "null";
    }

    @Override
    public IAction setQueue(IActionQueue queue) {
        if (this.queue == queue)
            return this;

        if (this.queue != null)
            this.queue.getQueue().remove(this);

        this.queue = queue;
        queue.schedule(this);
        return this;
    }

    protected void schedule(IActionQueue queue) {
        queue.getQueue().add(this);

        this.queue = queue;
        this.isScheduled = true;

        if (manager.debug)
            manager.LOGGER.log(String.format("Scheduled %s", getIdentifier()), queue);
    }

    @Override
    public boolean isScheduled() {
        return isScheduled;
    }

    @Override
    public IAction setTask(Consumer<IAction> task) {
        this.task = task;
        return this;
    }

    @Override
    public IAction onStart(Consumer<IAction> task) {
        this.onStart = task;
        return this;
    }

    @Override
    public IAction onDone(Consumer<IAction> task) {
        this.onDone = task;
        return this;
    }

    @Override
    public int getDuration() {
        return duration;
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
    public int getStartAfterTicks() {
        return startAfterTicks;
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
    public IAction everyTick() {
        this.updateEveryXTick = 1;
        return this;
    }

    @Override
    public IAction everySecond() {
        this.updateEveryXTick = 20;
        return this;
    }

    @Override
    public int getCount() {
        return count;
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
    public IAction copyDataTo(IAction to) {
        ((Action) to).dataStore.putAll(dataStore);
        return this;
    }

    @Override
    public String printData() {
        if (dataStore.isEmpty()) {
            return String.format("Action[name=%s] dataStore is empty", name);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Action[name=%s] dataStore contents:\n", name));

        for (Map.Entry<String, Object> entry : dataStore.entrySet()) {
            sb.append(String.format("  %s = %s\n", entry.getKey(), entry.getValue()));
        }

        return sb.toString();
    }

    @Override
    public boolean hasData(String key) {
        return dataStore.containsKey(key);
    }

    @Override
    public void markDone() {
        done = true;

        if (manager.debug)
            manager.LOGGER.log("Marked done!", this);
    }

    @Override
    public boolean isDone() {
        return done;
    }

    ///////////////////////////////////////////////////
    //////////////////////////////////////////////////
    // Listener

//    public IAction listenTo(String hook, Object object) {
//        return listenTo(hook, manager.getOrCreateListener(object));
//    }
//
//    public IAction listenTo(String hook, IActionListener listener) {
//        return listenTo(listener.getOrCreateHook(hook));
//    }
//
//    public IAction listenTo(ActionListener.Hook hook) {
//        hook.schedule(this);
//        return this;
//    }

    ///////////////////////////////////////////////////
    //////////////////////////////////////////////////
    // Handling

    @Override
    public IAction start() {
        manager.start();
        return this;
    }

    public void tick() {
        if (done)
            return;

        if (startAfterTicks > 0) {
            manager.LOGGER.log(String.format("Starts after %s ticks...", startAfterTicks), this);
            startAfterTicks--;
            return;
        }

        if (duration == 0 && onStart != null)
            execute("onStart", this::executeOnStart);

        if (done)
            return;

        duration++;

        inheritedTick();

        if (maxDuration > -1 && duration >= maxDuration || maxCount == 0) {
            if (manager.debug)
                manager.LOGGER.log("Reached max duration", this);

            markDone();
            return;
        }


        if (maxCount > -1 && count >= maxCount) {
            if (manager.debug)
                manager.LOGGER.log("Reached max count", this);

            markDone();
        }

    }

    public void inheritedTick() {
        if (manager.debug)
            manager.LOGGER.log(String.format("Ticking... (duration = %s/%s, count = %s/%s)", duration, maxDuration, count, maxCount), this);


        if (duration % updateEveryXTick == 0 && task != null)
            execute("task", this::executeTask);
    }

    public void execute(String taskName, Runnable task) {
        if (isThreaded)
            actionThread.execute(taskName, task);
        else
            task.run();
    }

    protected void executeOnStart() {
        if (manager.debug)
            manager.LOGGER.log("Executing onStart task...", this);

        try {
            onStart.accept(this);
        } catch (Throwable t) {
            manager.LOGGER.error("onStart Task of " + this + " threw an exception:", t);
        }

        if (manager.debug)
            manager.LOGGER.log("Finished executing onStart task", this);
    }

    protected void executeTask() {
        if (manager.debug)
            manager.LOGGER.log("Executing task...", this);

        try {
            task.accept(this);
            count++;
        } catch (Throwable t) {
            manager.LOGGER.error("Task of " + this + " threw an exception:", t);
            markDone();
        }

        if (manager.debug)
            manager.LOGGER.log(String.format("Finished executing task (count = %s/%s)", count, maxCount), this);
    }

    protected void executeOnDone() {
        if (manager.debug)
            manager.LOGGER.log("Executing onDone task...", this);

        try {
            onDone.accept(this);
        } catch (Throwable t) {
            manager.LOGGER.error("onDone Task of " + this + " threw an exception:", t);
        }

        if (manager.debug)
            manager.LOGGER.log("Finished executing onDone task", this);
    }


    @Override
    public void kill() {
        if (manager.debug)
            manager.LOGGER.log(String.format("Killing %s...", getIdentifier()), queue);

        if (actionThread != null)
            actionThread.stop();

        dataStore.clear();
        isScheduled = false;
        done = true;

    }

    @Override
    public String getIdentifier() {
        return String.format("%s '%s'", getClass().getSimpleName(), name);
    }

    public String toString() {
        return String.format("%s [queue='%s', scheduled=%s, done=%s, paused=%s, updateEvery=%s, duration=%d/%d, count=%d/%d, threaded=%s]", getIdentifier(), getQueueName(), isScheduled, done, isPaused(), updateEveryXTick, duration, maxDuration, count, maxCount, isThreaded);
    }
    ///////////////////////////////////////////////////
    //////////////////////////////////////////////////
    // Thread

    @Override
    public IAction threadify() {
        if (!isThreaded) {
            isThreaded = true;
            actionThread = new ActionThread(this);
        }

        return this;
    }

    @Override
    public void resume() {
        if (isThreaded)
            actionThread.resume();
    }

    @Override
    public void pause() {
        if (isThreaded)
            actionThread.pause();
        else
            throw new CustomNPCsException("Must threadify() IAction before pausing!");
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
    public void pauseUntil(Function<IAction, Boolean> until) {
        if (isThreaded)
            actionThread.pauseUntil(until);
        else
            throw new CustomNPCsException("Must threadify() IAction before pausing!");
    }

    @Override
    public boolean isPaused() {
        if (isThreaded)
            return actionThread.isPaused();

        return startAfterTicks > 0;
    }

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    // Sequential

    @Override
    public IAction getNext() {
        if (queue == null)
            return null;

        boolean seenMe = false;
        for (IAction a : queue.getQueue()) {
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
        if (queue == null)
            return null;

        IAction prev = null;
        for (IAction a : queue.getQueue()) {
            if (a == this) {
                return prev;
            }
            prev = a;
        }
        return null;
    }
    @Override
    public IAction after(IAction after) {
        if (after instanceof IConditionalAction)
            return conditional((IConditionalAction) after);

        int idx = queue == null ? -1 : queue.getIndex(this);
        if (idx >= 0 && !queue.isParallel())
            queue.scheduleActionAt(idx + 1, after);
        else {
            if (unscheduledList == null)
                unscheduledList = new ActionList(this);

            unscheduledList.after(this, (Action) after);
        }
        return after;
    }

    @Override
    public void after(IAction... actions) {
        for (int i = 0; i < actions.length - 1; i++)
            actions[i].after(actions[i + 1]);
    }

    @Override
    public void after(Consumer<IAction>... tasks) {
        IAction[] actions = new IAction[tasks.length];

        for (int i = 0; i < tasks.length; i++)
            actions[i] = manager.create(tasks[i]);

        after(actions);
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

        int idx = queue == null ? -1 : queue.getIndex(this);
        if (idx >= 0 && !queue.isParallel())
            queue.scheduleActionAt(Math.max(0, idx), before);
        else {
            if (unscheduledList == null)
                unscheduledList = new ActionList(this);

            unscheduledList.before(this, (Action) before);
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


    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    // Parallels

    @Override
    public IAction parallel(IAction act) {
        if (unscheduledList != null)
            ((Action) act).unscheduledList = unscheduledList;

        if (queue != null && queue.isParallel() && queue != manager.conditionalQueue)
            return queue.schedule(act);

        return manager.scheduleParallel(act);
    }

    @Override
    public void parallel(IAction... actions) {
        for (IAction act : actions)
            parallel(act);
    }

    @Override
    public IAction parallel(Consumer<IAction> task) {
        return parallel(manager.create(task));
    }

    @Override
    public void parallel(Consumer<IAction>... tasks) {
        for (Consumer<IAction> task : tasks)
            parallel(task);
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

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    // Conditionals

    @Override
    public IConditionalAction conditional(IConditionalAction after) {
        if (unscheduledList != null)
            ((Action) after).unscheduledList = unscheduledList;

        return manager.schedule(after);
    }

    @Override
    public void conditional(IConditionalAction... actions) {
        for (IConditionalAction act : actions)
            conditional(act);
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

}
