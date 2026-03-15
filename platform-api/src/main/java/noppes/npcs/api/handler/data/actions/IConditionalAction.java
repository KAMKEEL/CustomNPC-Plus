package noppes.npcs.api.handler.data.actions;

import noppes.npcs.api.handler.data.IAction;

import java.util.function.Consumer;
import java.util.function.Function;

public interface IConditionalAction extends IAction {

    /**
     * @param condition checked every tick, if it returns true, task is fired
     * @return this action
     */
    IConditionalAction setCondition(Function<IAction, Boolean> condition);

    /**
     * @param terminateWhen checked every tick, if it returns true, action is terminated (gets marked done)
     * @return this action
     */
    IConditionalAction terminateWhen(Function<IAction, Boolean> terminateWhen);

    /**
     * @param onTermination code to run when the termination condition returns true
     * @return this action
     */
    IConditionalAction onTermination(Consumer<IAction> onTermination);

    /**
     * @return true if condition was satisfied and task ran  (i.e can be called in termination task to see if original task was executed
     * then do code based on that, if not return early)
     */
    boolean wasTaskExecuted();

    /**
     * Note: Only for Conditional Actions
     *
     * @return how many times this conditional action has tested its condition
     */
    int getCheckCount();

    /**
     * Note: Only for Conditional Actions
     *
     * @return the maximum number of checks before auto-expiring, or -1 if unlimited
     */
    int getMaxChecks();

    /**
     *
     * @return True if condition provided by {{@link #terminateWhen(Function)}} is satisfied
     * Can be called directly in the IAction's task.
     * Can only be true once, as action is marked done immediately after.
     */
    boolean isTerminated();

    /**
     * @param maxChecks maximum times to test condition before auto-cancelling
     * @return this action
     */
    IConditionalAction setMaxChecks(int maxChecks);

}
