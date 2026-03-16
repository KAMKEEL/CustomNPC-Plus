package noppes.npcs.controllers.data.action.chain;

import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.IActionChain;
import noppes.npcs.api.handler.data.IActionQueue;
import noppes.npcs.controllers.data.action.ActionManager;

import java.util.function.Consumer;

/**
 * helper to build a back‐to‐back chain of one‐shot actions
 */
public class ActionChain implements IActionChain {
    protected final ActionManager manager;
    protected final IActionQueue queue;
    protected String name;
    protected int index = 0, offset;

    public ActionChain(ActionManager manager, IActionQueue queue, String name) {
        this.manager = manager;
        this.queue = queue;
        this.name = name;
    }

    @Override
    public IActionQueue getQueue() {
        return queue;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public IActionChain setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public IActionChain start() {
        manager.start();
        return this;
    }

    /**
     * schedule the next task ‘delay’ ticks after the previous one
     */
    @Override
    public IActionChain after(int delay, String name, Consumer<IAction> task) {
        offset = queue.isParallel() ? offset + delay : delay;
        queue.schedule(name, offset, task).everyTick().once();
        index++;
        return this;
    }

    @Override
    public IActionChain after(int delay, Consumer<IAction> task) {
        return after(delay, name + index, task);
    }

}
