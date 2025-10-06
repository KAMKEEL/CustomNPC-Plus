package noppes.npcs.controllers.data.action.action;

import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.actions.IConditionalAction;
import noppes.npcs.controllers.data.action.Action;
import noppes.npcs.controllers.data.action.ActionManager;

import java.util.function.Consumer;
import java.util.function.Function;


public class ConditionalAction extends Action implements IConditionalAction {
    protected Function<IAction, Boolean> condition;
    protected Function<IAction, Boolean> terminateWhen;
    protected Consumer<IAction> onTermination;
    protected int maxChecks = -1;
    protected int checkCount;
    protected boolean taskExecuted;

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
    public int getCheckCount() {
        return checkCount;
    }

    @Override
    public int getMaxChecks() {
        return maxChecks;
    }

    @Override
    public IConditionalAction setMaxChecks(int maxChecks) {
        this.maxChecks = Math.max(-1, maxChecks);
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
    public boolean isTerminated() {
        return terminateWhen != null && terminateWhen.apply(this);
    }

    public void inheritedTick() {
        if (manager.inDebugMode())
            manager.LOGGER.log(String.format("Ticking... (duration = %s/%s, count = %s/%s, checkCount = %s/%s)", duration, maxDuration, count, maxCount, checkCount, maxChecks), this);

        if (maxChecks > -1 && checkCount >= maxChecks) {
            if (manager.inDebugMode())
                manager.LOGGER.log("Reached max check count", this);

            markDone();
            return;
        }

        if (duration % updateEveryXTick == 0) {
            checkCount++;

            Runnable execute = () -> {
                if (condition != null && condition.apply(this))
                    executeTask();

                if (isTerminated()) {
                    if (onTermination != null)
                        executeOnTermination();
                    markDone();
                }
            };

            execute("task", execute);
        }
    }

    protected void executeTask() {
        if (manager.inDebugMode())
            manager.LOGGER.log("Executing task...", this);

        try {
            task.accept(this);
            count++;
            taskExecuted = true;
        } catch (Throwable t) {
            manager.LOGGER.error("Task of " + this + " threw an exception:", t);
            markDone();
        }

        if (manager.inDebugMode())
            manager.LOGGER.log(String.format("Finished executing task (count = %s/%s)", count, maxCount), this);

    }

    protected void executeOnTermination() {
        if (manager.inDebugMode())
            manager.LOGGER.log("Executing onTermination task...", this);

        try {
            onTermination.accept(this);
        } catch (Throwable t) {
            manager.LOGGER.error("Termination Task of " + this + " threw an exception:", t);
        }

        if (manager.inDebugMode())
            manager.LOGGER.log("Finished executing onTermination task", this);
    }


    public String toString() {
        return String.format("%s [queue='%s', scheduled=%s, done=%s, paused=%s, updateEvery=%s, duration=%d/%d, count=%d/%d, checks=%d/%d, taskExecuted=%s, threaded=%s]", getIdentifier(), getQueueName(),
            isScheduled,
            done,
            isPaused(),
            updateEveryXTick,
            duration,
            maxDuration,
            count,
            maxCount,
            checkCount,
            maxChecks,
            taskExecuted,
            isThreaded
        );
    }
}
