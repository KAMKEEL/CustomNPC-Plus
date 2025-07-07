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

    @Override
    public void tick() {
        if (isDone())
            return;

        if (duration == 0 && onStart != null)
            execute("start", this::executeOnStart);

        if (isDone())
            return;

        duration++;

        if (manager.inDebugMode())
            manager.logDebug(String.format("Ticking ConditionalAction '%s' (duration = %s/%s, count = %s/%s, checkCount = %s/%s) on queue '%s'", name, duration, maxDuration, count, maxCount, checkCount, maxChecks, getQueueName()));

        if (maxDuration > -1 && duration >= maxDuration || maxCount == 0) {
            if (manager.inDebugMode())
                manager.logDebug(String.format("Reached max duration of Action '%s' on queue '%s'", name, getQueueName()));

            markDone();
            return;
        }

        if (maxChecks > -1 && checkCount > maxChecks) {
            if (manager.inDebugMode())
                manager.logDebug(String.format("Reached max check count of ConditionalAction '%s' on queue '%s'", name, getQueueName()));

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

        if (maxCount > -1 && count >= maxCount) {
            if (manager.inDebugMode())
                manager.logDebug(String.format("Reached max count of ConditionalAction '%s' on queue '%s'", name, getQueueName()));

            markDone();
        }
    }

    protected void executeTask() {
        if (manager.inDebugMode())
            manager.logDebug(String.format("Started executing task of ConditionalAction '%s' on queue '%s'", name, getQueueName()));

        try {
            task.accept(this);
            count++;
            taskExecuted = true;
        } catch (Throwable t) {
            manager.logDebug("Task of " + this + " threw an exception:", t);
            markDone();
        }

        if (manager.inDebugMode())
            manager.logDebug(String.format("Finished executing task of ConditionalAction '%s' on queue '%s'", name, getQueueName()));

    }

    protected void executeOnTermination() {
        if (manager.inDebugMode())
            manager.logDebug(String.format("Started executing onTermination task of ConditionalAction '%s' on queue '%s'", name, getQueueName()));

        try {
            onTermination.accept(this);
        } catch (Throwable t) {
            manager.logDebug("Termination Task of " + this + " threw an exception:", t);
        }

        if (manager.inDebugMode())
            manager.logDebug(String.format("Finished executing onTermination task of ConditionalAction '%s' on queue '%s'", name, getQueueName()));
    }


    public String toString() {
        return String.format("IConditionalAction '%s' [queue='%s', scheduled=%s, done=%s, paused=%s, updateEvery=%s, duration=%d/%d, count=%d/%d, checks=%d/%d, taskExecuted=%s, threaded=%s]",
            name != null ? name : "unnamed", getQueueName(),
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
