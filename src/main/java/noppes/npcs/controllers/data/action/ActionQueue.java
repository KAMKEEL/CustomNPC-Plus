package noppes.npcs.controllers.data.action;

import noppes.npcs.api.handler.IActionManager;
import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.IActionChain;
import noppes.npcs.api.handler.data.IActionQueue;
import noppes.npcs.controllers.data.action.chain.ActionChain;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

public class ActionQueue implements IActionQueue {
    protected final Deque<IAction> queue = new ConcurrentLinkedDeque<>();
    protected final ActionManager manager;
    protected final String name;

    protected boolean isWorking = true;
    protected boolean isParallel;

    protected boolean isDead;
    protected boolean killWhenEmpty;
    protected int killWhenEmptyAfter = 100;

    protected Action autoKill;

    public ActionQueue(ActionManager manager, String name) {
        this.manager = manager;
        this.name = name;
    }

    public ActionQueue(ActionManager manager, String name, boolean isParallel) {
        this(manager, name);
        this.isParallel = isParallel;
    }

    @Override
    public IActionQueue start() {
        isWorking = true;
        return this;
    }

    @Override
    public IActionQueue stop() {
        isWorking = false;
        return this;
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
    public Queue<IAction> getQueue() {
        return queue;
    }

    @Override
    public boolean isParallel() {
        return isParallel;
    }

    @Override
    public IActionQueue setParallel(boolean parallel) {
        isParallel = parallel;
        return this;
    }
    @Override
    public boolean isKilledWhenEmpty() {
        return killWhenEmpty;
    }

    @Override
    public int getKillWhenEmptyAfter() {
        return killWhenEmptyAfter;
    }

    @Override
    public IActionQueue killWhenEmpty(boolean killWhenEmpty) {
        this.killWhenEmpty = killWhenEmpty;
        return this;
    }

    @Override
    public IActionQueue killWhenEmptyAfter(int ticks) {
        this.killWhenEmptyAfter = ticks;
        return this;
    }

    @Override
    public boolean isDead() {
        return isDead;
    }

    @Override
    public IActionQueue kill() {
        this.isDead = true;

        if (manager.debug)
            manager.LOGGER.log(String.format("Killing queue '%s' on '%s'", name, manager.getInternalName()), this);
        return this;
    }

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    // Schedulers

    @Override
    public IAction schedule(IAction action) {
        Action act = (Action) action;

        if (!isParallel && act.unscheduledList != null) {
            act.unscheduledList.scheduleAll(this).forEach((act1) -> act1.unscheduledList = null).kill();
            return action;
        }

        act.schedule(this);
        return action;
    }

    @Override
    public void schedule(IAction... actions) {
        for (IAction act : actions)
            schedule(act);
    }

    @Override
    public IAction schedule(Consumer<IAction> task) {
        return schedule(manager.create(task));
    }

    @Override
    public void schedule(Consumer<IAction>... tasks) {
        for (Consumer<IAction> task : tasks)
            schedule(task);
    }


    @Override
    public IAction schedule(int delay, Consumer<IAction> task) {
        return schedule(manager.create(delay, task));
    }

    @Override
    public IAction schedule(int maxDuration, int delay, Consumer<IAction> task) {
        return schedule(manager.create(maxDuration, delay, task));
    }

    @Override
    public IAction schedule(String name, Consumer<IAction> task) {
        return schedule(manager.create(name, task));
    }

    @Override
    public IAction schedule(String name, int delay, Consumer<IAction> task) {
        return schedule(manager.create(name, delay, task));
    }

    @Override
    public IAction schedule(String name, int maxDuration, int delay, Consumer<IAction> task) {
        return schedule(manager.create(name, maxDuration, delay, task));
    }

    @Override
    public IAction scheduleActionAt(int index, IAction action) {
        int size = queue.size();
        if (index <= 0) {
            queue.addFirst(action);
        } else if (index >= size) {
            queue.addLast(action);
        } else {
            List<IAction> tmp = new ArrayList<>(queue);
            tmp.add(index, action);
            queue.clear();
            queue.addAll(tmp);
        }
        ((Action) action).queue = this;
        ((Action) action).isScheduled = true;
        return action;
    }

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    // Queue Data

    @Override
    public boolean hasActiveTasks() {
        return !queue.isEmpty();
    }

    @Override
    public int getIndex(IAction action) {
        int i = 0;
        for (IAction a : queue) {
            if (a.equals(action))
                return i;
            i++;
        }
        return -1;
    }

    @Override
    public IAction getCurrentAction() {
        return queue.peekFirst();
    }

    @Override
    public boolean has(IAction action) {
        return has(action.getName());
    }

    @Override
    public boolean has(String actionName) {
        for (IAction act : queue)
            if (act.getName().equals(actionName))
                return true;

        return false;
    }

    @Override
    public IAction get(String actionName) {
        for (IAction action : queue) {
            if (action.getName().equals(actionName))
                return action;
        }

        return null;
    }

    @Override
    public boolean cancel(IAction action) {
        if (queue.remove(action)) {
            action.kill();
            return true;
        }

        return false;
    }

    @Override
    public boolean cancel(String actionName) {
        Iterator<IAction> acts = queue.iterator();
        while (acts.hasNext()) {
            IAction act = acts.next();
            if (act.getName().equals(actionName)) {
                act.kill();
                acts.remove();
                return true;
            }
        }
        return false;
    }

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    // Handling

    public boolean finish(Action a) {
        if (a.isDone()) {
            if (a.onDone != null)
                a.execute("onDone", a::executeOnDone);

            a.kill();

            if (manager.debug)
                manager.LOGGER.finish(String.format("Removing %s from queue", a.getIdentifier()), this);
            return true;
        }

        return false;
    }
    protected boolean tick(Action a) {
        if (a == null)
            return false;

        boolean wasDone = a.isDone();
        if (manager.debug && !wasDone)
            manager.LOGGER.push(this).log(String.format("Started ticking %s", a.getIdentifier(), a.name), this).push(a);

        a.tick();

        if (manager.debug && !wasDone)
            manager.LOGGER.pop().finish(String.format("Finished ticking %s", a.getIdentifier(), a.name), this).pop();

        return finish(a);
    }

    protected void tick() {
        if (!isWorking || isDead)
            return;

        boolean active = hasActiveTasks();
        if (manager.debug && active)
            manager.LOGGER.push(manager).log(String.format("Started ticking queue '%s' ", name), manager).push(this);

        if (!isParallel) {
            if (tick((Action) getCurrentAction()))
                    queue.pollFirst();
        } else {
            Iterator<IAction> pit = queue.iterator();
            while (pit.hasNext())
                if (tick((Action) pit.next()))
                    pit.remove();
        }

        killWhenEmpty();

        if (manager.debug && active)
            manager.LOGGER.pop().finish(String.format("Finished ticking queue '%s' ", name), manager).pop();
    }

    protected void killWhenEmpty() {
        if (killWhenEmpty && !hasActiveTasks() && autoKill == null) {
            if (manager.debug)
                manager.LOGGER.log(String.format("Queue is empty! Killing in %s ticks", killWhenEmptyAfter), this);

            autoKill = (Action) manager.create(String.format("Kill '%s'", name), killWhenEmptyAfter, (act) -> {
                if (hasActiveTasks()) {
                    if (manager.debug)
                        manager.LOGGER.log("Scheduled Actions found! Aborted kill process", this);
                    return;
                }

                manager.removeQueue(name);
            }).everyTick().once();
        }

        if (autoKill != null) {
            autoKill.tick();
            if (autoKill.isDone())
                autoKill = null;
        }
    }

    @Override
    public void clear() {
        queue.forEach((act) -> act.kill());
        queue.clear();

        if (manager.debug)
            manager.LOGGER.log("Cleared queue!", this);
    }

    @Override
    public IActionChain chain() {
        return new ActionChain(manager, this, isParallel ? "parallel#" : "sequential#");
    }

    @Override
    public String printQueue() {
        if (queue.isEmpty()) {
            return String.format("ActionQueue[name=%s] is empty", name);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s\n", this));

        int i = 0;
        for (IAction action : queue) {
            sb.append(String.format("  [%d] %s\n", i++, action.toString()));
        }

        return sb.toString();
    }

    public String toString() {
        return String.format("ActionQueue '%s' [size=%d, parallel=%s, working=%s, dead=%s, killWhenEmpty=%s, killAfter=%d]", name, queue.size(), isParallel, isWorking, isDead, killWhenEmpty, killWhenEmptyAfter);
    }

}
