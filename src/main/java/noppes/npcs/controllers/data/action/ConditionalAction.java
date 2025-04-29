package noppes.npcs.controllers.data.action;

import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.actions.IConditionalAction;
import noppes.npcs.scripted.ScriptedActionManager;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConditionalAction extends Action implements IConditionalAction {
    private Supplier<Boolean> predicate;
    private Supplier<Boolean> terminateWhen;
    private Consumer<IAction> onTermination;
    private int maxChecks = -1;
    private int checkCount;
    private boolean taskExecuted;

    public ConditionalAction(ScriptedActionManager manager, Supplier<Boolean> predicate, Consumer<IAction> task) {
        super(manager, "conditional", task);
        this.predicate = predicate;
    }

    public ConditionalAction(ScriptedActionManager manager, String name, Supplier<Boolean> predicate, Consumer<IAction> task) {
        super(manager, name, task);
        this.predicate = predicate;
    }

    public ConditionalAction(ScriptedActionManager manager, Supplier<Boolean> predicate, Consumer<IAction> task, Supplier<Boolean> terminateWhen) {
        this(manager, predicate, task);
        this.terminateWhen = terminateWhen;
    }

    public ConditionalAction(ScriptedActionManager manager, String name, Supplier<Boolean> predicate, Consumer<IAction> task, Supplier<Boolean> terminateWhen) {
        this(manager, name, predicate, task);
        this.terminateWhen = terminateWhen;
    }

    public ConditionalAction(ScriptedActionManager manager, Supplier<Boolean> predicate, Consumer<IAction> task, Supplier<Boolean> terminateWhen, Consumer<IAction> onTermination) {
        this(manager, predicate, task, terminateWhen);
        this.onTermination = onTermination;
    }

    public ConditionalAction(ScriptedActionManager manager, String name, Supplier<Boolean> predicate, Consumer<IAction> task, Supplier<Boolean> terminateWhen, Consumer<IAction> onTermination) {
        this(manager, name, predicate, task, terminateWhen);
        this.onTermination = onTermination;
    }

    @Override
    public void tick(int ticksExisted) {
        if (isDone()) return;
        if (ticksExisted % updateEveryXTick == 0) {
            checkCount++;
            boolean terminated = terminateWhen != null && terminateWhen.get();
            if (terminated && onTermination != null) {
                onTermination.accept(this);
            }
            if ((maxChecks >= 0 && checkCount > maxChecks) || terminated) {
                markDone();
                return;
            }
            if (predicate.get()) {
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
    public IConditionalAction setPredicate(Supplier<Boolean> predicate) {
        this.predicate = predicate;
        return this;
    }

    @Override
    public IConditionalAction setTerminationPredicate(Supplier<Boolean> terminateWhen) {
        this.terminateWhen = terminateWhen;
        return this;
    }

    @Override
    public IConditionalAction setTerminationTask(Consumer<IAction> onTermination) {
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
    public IConditionalAction after(Supplier<Boolean> predicate, Consumer<IAction> task) {
        return after(manager.create(predicate, task));
    }

    @Override
    public IConditionalAction after(String name, Supplier<Boolean> predicate, Consumer<IAction> task) {
        return after(manager.create(name, predicate, task));
    }

    @Override
    public IConditionalAction after(Supplier<Boolean> predicate, Consumer<IAction> task, Supplier<Boolean> terminateWhen) {
        return after(manager.create(predicate, task, terminateWhen));
    }

    @Override
    public IConditionalAction after(String name, Supplier<Boolean> predicate, Consumer<IAction> task, Supplier<Boolean> terminateWhen) {
        return after(manager.create(name, predicate, task, terminateWhen));
    }

    @Override
    public IConditionalAction after(Supplier<Boolean> predicate, Consumer<IAction> task, Supplier<Boolean> terminateWhen, Consumer<IAction> onTermination) {
        return after(manager.create(predicate, task, terminateWhen, onTermination));
    }

    @Override
    public IConditionalAction after(String name, Supplier<Boolean> predicate, Consumer<IAction> task, Supplier<Boolean> terminateWhen, Consumer<IAction> onTermination) {
        return after(manager.create(name, predicate, task, terminateWhen, onTermination));
    }
}
