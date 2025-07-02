package noppes.npcs.controllers.data.action;

import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.IActionQueue;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

public class ActionQueue implements IActionQueue {
    protected final Deque<IAction> queue = new ConcurrentLinkedDeque<>();
    protected final ActionManager manager;
    protected final String name;

    protected boolean isParallel;
    protected boolean isWorking;

    public ActionQueue(ActionManager manager, String name) {
        this.manager = manager;
        this.name = name;
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
    public ActionManager getManager() {
        return manager;
    }

    @Override
    public String getName() {
        return name;
    }
    @Override
    public boolean isParallel() {
        return isParallel;
    }

    @Override
    public Queue<IAction> getQueue() {
        return queue;
    }

    @Override
    public IAction schedule(IAction action) {
        Action act = (Action) action;
        if (act.unscheduledBefore != null)
            act.scheduleAllBefore(queue);

        queue.addLast(action);

        if (act.unscheduledAfter != null)
            act.scheduleAllAfter(queue);

        act.isScheduled = true;
        return action;
    }

    @Override
    public void schedule(IAction... actions) {
        for (IAction act : actions)
            schedule(act);
    }

    @Override
    public void schedule(Consumer<IAction>... tasks) {
        for (Consumer<IAction> task : tasks)
            schedule(task);
    }

    @Override
    public IAction schedule(Consumer<IAction> task) {
        return schedule(manager.create(task));
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
        ((Action) action).isScheduled = true;
        return action;
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
        return cancel(action.getName());
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

    protected void tick(int ticksExisted) {
        if (!isWorking)
            return;


        if (!isParallel) {
            IAction current = getCurrentAction();
            if (current instanceof Action) {
                Action cab = (Action) current;
                cab.tick(ticksExisted);
                if (cab.isDone()) {
                    cab.kill();
                    queue.pollFirst();
                }
            }
        } else {
            Iterator<IAction> pit = queue.iterator();
            while (pit.hasNext()) {
                Action a = (Action) pit.next();
                a.tick(ticksExisted);
                if (a.isDone()) {
                    a.kill();
                    pit.remove();
                }
            }
        }
    }

    @Override
    public void clear() {
        queue.forEach((act) -> act.kill());
        queue.clear();
    }
}
