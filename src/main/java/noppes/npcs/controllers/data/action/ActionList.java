package noppes.npcs.controllers.data.action;

import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.IActionQueue;

import java.util.LinkedList;
import java.util.function.Consumer;

public class ActionList {

    protected final LinkedList<Action> list = new LinkedList<>();
    protected final Action first;
    protected IActionQueue originalQueue;

    public ActionList(Action action) {
        list.addFirst(first = action);
        originalQueue = action.getQueue();
    }

    public Action after(Action current, Action aft) {
        int currentIndex = list.indexOf(current);

        if (currentIndex == -1)
            list.addLast(aft);
        else
            list.add(currentIndex + 1, aft);

        aft.unscheduledList = this;
        return aft;
    }

    public Action before(Action current, Action bef) {
        int currentIndex = list.indexOf(current);

        if (currentIndex == -1)
            list.addLast(bef);
        else
            list.add(Math.max(0, currentIndex), bef);

        bef.unscheduledList = this;
        return bef;
    }

    public ActionList scheduleAll(IActionQueue queue) {
        list.forEach(act -> {
            queue.getQueue().add(act);
            act.queue = queue;
            act.isScheduled = true;
        });

        return this;
    }

    public ActionList forEach(Consumer<Action> task) {
        list.forEach(task);
        return this;
    }

    public ActionList kill() {
        list.clear();
        return this;
    }
}
