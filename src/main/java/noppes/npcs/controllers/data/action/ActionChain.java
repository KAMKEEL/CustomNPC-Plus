package noppes.npcs.controllers.data.action;

import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.IActionChain;
import noppes.npcs.scripted.ScriptedActionManager;

import java.util.function.Consumer;

/**
 * helper to build a back‐to‐back chain of one‐shot actions
 */
public class ActionChain implements IActionChain {
    private final ScriptedActionManager scriptedActionManager;
    private int offset = 0, index = 0;

    public ActionChain(ScriptedActionManager scriptedActionManager) {
        this.scriptedActionManager = scriptedActionManager;
    }

    /**
     * schedule the next task ‘delay’ ticks after the previous one
     */
    @Override
    public IActionChain after(int delay, String name, Consumer<IAction> task) {
        offset += delay;
        Consumer<IAction> wrapper = act -> {
            task.accept(act);
            act.markDone();
        };
        IAction a = scriptedActionManager.create(name, offset, wrapper);
        index++;
        a.setUpdateEveryXTick(1);
        scriptedActionManager.scheduleAction(a);
        return this;
    }

    @Override
    public IActionChain after(int delay, Consumer<IAction> task) {
        return after(delay, "chain#" + index, task);
    }
}
