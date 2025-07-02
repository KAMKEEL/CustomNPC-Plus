package noppes.npcs.controllers.data.action.chain;

import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.IActionChain;
import noppes.npcs.controllers.data.action.ActionManager;

import java.util.function.Consumer;

public class ParallelActionChain extends ActionChain implements IActionChain {
    private int offset;

    public ParallelActionChain(ActionManager manager) {
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
