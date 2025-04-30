package noppes.npcs.controllers.data.action;

import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.actions.IConditionalAction;
import noppes.npcs.scripted.ScriptedActionManager;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConditionalAction extends Action implements IConditionalAction {
    private Supplier<Boolean> condition;
    private Supplier<Boolean> terminateWhen;
    private Consumer<IAction> onTermination;
    private int maxChecks = -1;
    private int checkCount;
    private boolean taskExecuted;

    public ConditionalAction(ScriptedActionManager manager, Supplier<Boolean> condition, Consumer<IAction> task) {
        super(manager, "conditional", task);
        this.condition = condition;
    }

    public ConditionalAction(ScriptedActionManager manager, String name, Supplier<Boolean> condition, Consumer<IAction> task) {
        super(manager, name, task);
        this.condition = condition;
    }

    public ConditionalAction(ScriptedActionManager manager, Supplier<Boolean> condition, Consumer<IAction> task, Supplier<Boolean> terminateWhen) {
        this(manager, condition, task);
        this.terminateWhen = terminateWhen;
    }

    public ConditionalAction(ScriptedActionManager manager, String name, Supplier<Boolean> condition, Consumer<IAction> task, Supplier<Boolean> terminateWhen) {
        this(manager, name, condition, task);
        this.terminateWhen = terminateWhen;
    }

    public ConditionalAction(ScriptedActionManager manager, Supplier<Boolean> condition, Consumer<IAction> task, Supplier<Boolean> terminateWhen, Consumer<IAction> onTermination) {
        this(manager, condition, task, terminateWhen);
        this.onTermination = onTermination;
    }

    public ConditionalAction(ScriptedActionManager manager, String name, Supplier<Boolean> condition, Consumer<IAction> task, Supplier<Boolean> terminateWhen, Consumer<IAction> onTermination) {
        this(manager, name, condition, task, terminateWhen);
        this.onTermination = onTermination;
    }

    @Override
    public void tick(int ticksExisted) {
        if (isDone()) return;
        if (ticksExisted % updateEveryXTick == 0) {
            checkCount++;
            boolean terminated = terminateWhen != null && terminateWhen.get();
            if (terminated && onTermination != null)
                onTermination.accept(this);

            if ((maxChecks >= 0 && checkCount > maxChecks) || terminated) {
                markDone();
                return;
            }

            if (condition != null && condition.get()) {
                task.accept(this);
                taskExecuted = true;
            }
        }
        duration++;
    }

    @Override
    public IConditionalAction setMaxChecks(int maxChecks) {
        this.maxChecks = maxChecks;
        return this;
    }

    @Override
    public IConditionalAction setCondition(Supplier<Boolean> condition) {
        this.condition = condition;
        return this;
    }

    @Override
    public IConditionalAction terminateWhen(Supplier<Boolean> terminateWhen) {
        this.terminateWhen = terminateWhen;
        return this;
    }

    @Override
    public IConditionalAction onTermination(Consumer<IAction> onTermination) {
        this.onTermination = onTermination;
        return this;
    }

    @Override
    public boolean wasTaskExecuted() {
        return taskExecuted;
    }

    @Override
    public int getCheckCount() {
        return checkCount;
    }

    @Override
    public int getMaxChecks() {
        return maxChecks;
    }

    @Override
    public IConditionalAction after(IConditionalAction after) {
        return ((ScriptedActionManager) manager).scheduleAction(after);
    }

    @Override
    public IConditionalAction after(Supplier<Boolean> condition, Consumer<IAction> task) {
        return after(manager.create(condition, task));
    }

    @Override
    public IConditionalAction after(String name, Supplier<Boolean> condition, Consumer<IAction> task) {
        return after(manager.create(name, condition, task));
    }

    @Override
    public IConditionalAction after(Supplier<Boolean> condition, Consumer<IAction> task, Supplier<Boolean> terminateWhen) {
        return after(manager.create(condition, task, terminateWhen));
    }

    @Override
    public IConditionalAction after(String name, Supplier<Boolean> condition, Consumer<IAction> task, Supplier<Boolean> terminateWhen) {
        return after(manager.create(name, condition, task, terminateWhen));
    }

    @Override
    public IConditionalAction after(Supplier<Boolean> condition, Consumer<IAction> task, Supplier<Boolean> terminateWhen, Consumer<IAction> onTermination) {
        return after(manager.create(condition, task, terminateWhen, onTermination));
    }

    @Override
    public IConditionalAction after(String name, Supplier<Boolean> condition, Consumer<IAction> task, Supplier<Boolean> terminateWhen, Consumer<IAction> onTermination) {
        return after(manager.create(name, condition, task, terminateWhen, onTermination));
    }
}
