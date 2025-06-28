package noppes.npcs.controllers.data.action;

import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.scripted.CustomNPCsException;
import noppes.npcs.scripted.ScriptedActionManager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Action implements IAction {
    protected final ScriptedActionManager manager;
    protected final String name;
    protected int startAfterTicks;
    protected int count;
    protected int duration;
    protected int maxDuration = -1;
    protected int updateEveryXTick = 5;
    protected Consumer<IAction> task;
    protected boolean done;
    protected final Map<String, Object> dataStore = new HashMap<>();
    protected boolean isThreaded;
    protected ActionThread threaded;

    public Action(ScriptedActionManager manager, String name) {
        this.manager = manager;
        this.name = name;
    }

    public Action(ScriptedActionManager manager, Consumer<IAction> task) {
        this(manager, task.toString(), task);
    }

    public Action(ScriptedActionManager manager, String name, int maxDuration, int startAfterTicks, Consumer<IAction> task) {
        this.manager = manager;
        this.name = name;
        this.maxDuration = maxDuration;
        this.startAfterTicks = startAfterTicks;
        this.task = task;
    }

    public Action(ScriptedActionManager manager, String name, int startAfterTicks, Consumer<IAction> task) {
        this(manager, name, task);
        this.startAfterTicks = startAfterTicks;
    }

    public Action(ScriptedActionManager manager, String name, Consumer<IAction> task) {
        this(manager, name);
        this.task = task;
    }

    public Action(ScriptedActionManager manager, int startAfterTicks, Consumer<IAction> task) {
        this(manager, task);
        this.startAfterTicks = startAfterTicks;
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
        if (maxDuration != -1 && duration >= maxDuration) {
            markDone();
            return;
        }
        if (ticksExisted % updateEveryXTick == 0 && task != null) {
            if (isThreaded)
                threaded.run();
            else {
                try {
                    task.accept(this);
                    count++;
                } catch (Throwable t) {
                    System.err.println("Scripted action '" + name + "' threw an exception:");
                    t.printStackTrace();
                    markDone();
                }
            }
        }
        duration++;
    }

    @Override
    public ScriptedActionManager getManager() {
        return manager;
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
    public IAction setMaxDuration(int x) {
        this.maxDuration = x;
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
    public int getUpdateEveryXTick() {
        return updateEveryXTick;
    }

    @Override
    public IAction setUpdateEveryXTick(int x) {
        this.updateEveryXTick = x;
        return this;
    }

    @Override
    public int getStartAfterTicks() {
        return startAfterTicks;
    }

    @Override
    public IAction pauseFor(int ticks) {
        if (isThreaded)
            threaded.pauseFor(ticks);
        else
            this.startAfterTicks = ticks;

        return this;
    }

    @Override
    public IAction pauseFor(long millis) {
        if (isThreaded) {
            threaded.pauseFor(millis);
            return this;
        }

        return pauseFor((int) (millis / 50));
    }

    @Override
    public void pause() {
        if (isThreaded)
            threaded.pause();
        else
            throw new CustomNPCsException("Must threadify() IAction before pausing!");
    }

    @Override
    public void pauseUntil(Supplier<Boolean> until) {
        if (isThreaded)
            threaded.pauseUntil(until);
        else
            throw new CustomNPCsException("Must threadify() IAction before pausing!");
    }

    @Override
    public void resume() {
        if (isThreaded)
            threaded.resume();
    }

    @Override
    public boolean isPaused() {
        if (isThreaded)
            return threaded.isPaused();

        return startAfterTicks > 0;
    }

    @Override
    public IAction threadify() {
        if (!isThreaded) {
            isThreaded = true;
            threaded = new ActionThread(this);
        }

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

    @Override
    public IAction after(IAction after) {
        int idx = manager.getIndex(this);
        if (idx >= 0) manager.scheduleActionAt(idx + 1, after);
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
        int idx = manager.getIndex(this);
        if (idx >= 0) manager.scheduleActionAt(Math.max(0, idx), before);
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
}
