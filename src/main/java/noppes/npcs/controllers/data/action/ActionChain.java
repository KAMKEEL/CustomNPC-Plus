package noppes.npcs.controllers.data.action;

import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.IActionChain;
import noppes.npcs.scripted.ScriptedActionManager;

import java.util.function.Consumer;

/**
 * helper to build a back‐to‐back chain of one‐shot actions
 */
public class ActionChain implements IActionChain {
    protected final ScriptedActionManager manager;
    protected String defaultName = "sequential#";
    protected int index = 0;

    public ActionChain(ScriptedActionManager manager) {
        this.manager = manager;
    }

    /**
     * schedule the next task ‘delay’ ticks after the previous one
     */
    @Override
    public IActionChain after(int delay, String name, Consumer<IAction> task) {
        manager.schedule(name, delay, task).updateEvery(1).once();
        index++;
        return this;
    }

    @Override
    public IActionChain after(int delay, Consumer<IAction> task) {
        return after(delay, defaultName + index, task);
    }

    @Override
    public IActionChain start() {
        manager.start();
        return this;
    }
}
