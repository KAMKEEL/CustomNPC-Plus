package noppes.npcs.controllers.data.action;

import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.actions.IConditionalAction;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.function.Consumer;
import java.util.function.Function;


public class ConditionalAction extends Action implements IConditionalAction {
    private Function<IAction, Boolean> condition;
    private Function<IAction, Boolean> terminateWhen;
    private Consumer<IAction> onTermination;
    private int maxChecks = -1;
    private int checkCount;
    private boolean taskExecuted;

    public ConditionalAction(ActionManager manager, Function<IAction, Boolean> condition, Consumer<IAction> task) {
        super(manager, task);
        this.condition = condition;
    }

    public ConditionalAction(ActionManager manager, String name, Function<IAction, Boolean> condition, Consumer<IAction> task) {
        super(manager, name, task);
        this.condition = condition;
    }

    public ConditionalAction(ActionManager manager, Function<IAction, Boolean> condition, Consumer<IAction> task, Function<IAction, Boolean> terminateWhen) {
        this(manager, condition, task);
        this.terminateWhen = terminateWhen;
    }

    public ConditionalAction(ActionManager manager, String name, Function<IAction, Boolean> condition, Consumer<IAction> task, Function<IAction, Boolean> terminateWhen) {
        this(manager, name, condition, task);
        this.terminateWhen = terminateWhen;
    }

    public ConditionalAction(ActionManager manager, Function<IAction, Boolean> condition, Consumer<IAction> task, Function<IAction, Boolean> terminateWhen, Consumer<IAction> onTermination) {
        this(manager, condition, task, terminateWhen);
        this.onTermination = onTermination;
    }

    public ConditionalAction(ActionManager manager, String name, Function<IAction, Boolean> condition, Consumer<IAction> task, Function<IAction, Boolean> terminateWhen, Consumer<IAction> onTermination) {
        this(manager, name, condition, task, terminateWhen);
        this.onTermination = onTermination;
    }

    @Override
    public void tick(int ticksExisted) {
        if (done)
            return;

        if (maxChecks > -1 && checkCount > maxChecks || maxCount == 0) {
            markDone();
            return;
        }

        if (ticksExisted % updateEveryXTick == 0) {
            if (condition != null && condition.apply(this)) {
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

        if (maxCount > -1 && count >= maxCount) {
            markDone();
            return;
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
        return terminateWhen != null && terminateWhen.apply(this);
    }

    @Override
    public IConditionalAction setMaxChecks(int maxChecks) {
        this.maxChecks = maxChecks;
        return this;
    }

    @Override
    public IConditionalAction setCondition(Function<IAction, Boolean> condition) {
        this.condition = condition;
        return this;
    }

    @Override
    public IConditionalAction terminateWhen(Function<IAction, Boolean> terminateWhen) {
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
