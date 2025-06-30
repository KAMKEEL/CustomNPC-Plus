package noppes.npcs.controllers.data.action;

import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.IActionChain;
import noppes.npcs.scripted.ScriptedActionManager;

import java.util.function.Consumer;

public class ParallelActionChain implements IActionChain {
    private final ScriptedActionManager manager;
    private int offset = 0, idx = 0;

    public ParallelActionChain(ScriptedActionManager scriptedActionManager) {
        this.manager = scriptedActionManager;
    }

    /**
     * schedule the next task ‘delay’ ticks after the previous one, fully in parallel
     */
    @Override
    public IActionChain after(int delay, String name, Consumer<IAction> task) {
        offset += delay;
        idx++;

        manager.scheduleParallel(name, offset, task).updateEvery(1).once();
        return this;
    }

    @Override
    public IActionChain after(int delay, Consumer<IAction> task) {
        return after(delay, "parallel#" + idx, task);
    }

    @Override
    public IActionChain start() {
        manager.start();
        return this;
    }
}
