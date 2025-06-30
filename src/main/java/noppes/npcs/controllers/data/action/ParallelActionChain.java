package noppes.npcs.controllers.data.action;

import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.IActionChain;
import noppes.npcs.scripted.ScriptedActionManager;

import java.util.function.Consumer;

public class ParallelActionChain extends ActionChain implements IActionChain {
    private int offset;

    public ParallelActionChain(ScriptedActionManager manager) {
        super(manager);
        defaultName = "parallel#";
    }

    /**
     * schedule the next task ‘delay’ ticks after the previous one, fully in parallel
     */
    @Override
    public IActionChain after(int delay, String name, Consumer<IAction> task) {
        offset += delay;
        manager.scheduleParallel(name, offset, task).updateEvery(1).once();
        index++;
        return this;
    }
}
