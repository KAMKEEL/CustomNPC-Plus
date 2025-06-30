package noppes.npcs.controllers.data.action;

import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.actions.IConditionalAction;
import noppes.npcs.scripted.ScriptedActionManager;
import org.apache.commons.lang3.exception.ExceptionUtils;

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
        if (done)
            return;

        if (maxChecks > -1 && checkCount > maxChecks) {
            markDone();
            return;
        }

        if (ticksExisted % updateEveryXTick == 0) {
            if (condition != null && condition.get()) {
                if (isThreaded)
                    actionThread.execute("task", this::executeTask);
                else
                    executeTask();
            }

            if (isTerminated()) {
                if (onTermination != null) {
                    if (isThreaded)
                        actionThread.execute("onTermination", this::executeOnTermination);
                    else
                        executeOnTermination();
                }
                markDone();
                return;
            }

            checkCount++;
        }
        duration++;
    }

    protected void executeTask() {
        try {
            task.accept(this);
            count++;
            taskExecuted = true;
        } catch (Throwable t) {
            String err = "IConditionalAction '" + getName() + "' threw an exception:";

            if (reportTo != null)
                reportTo.appendConsole(err + "\n" + ExceptionUtils.getStackTrace(t));

            System.err.println(err);
            t.printStackTrace();
            markDone();
        }
    }

    protected void executeOnTermination() {
        try {
            onTermination.accept(this);
        } catch (Throwable t) {
            String err = "IConditionalAction's onTermination '" + getName() + "' threw an exception:";

            if (reportTo != null)
                reportTo.appendConsole(err + "\n" + ExceptionUtils.getStackTrace(t));

            System.err.println(err);
            t.printStackTrace();
        }
    }

    @Override
    public boolean isTerminated() {
        return terminateWhen != null && terminateWhen.get();
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

}
