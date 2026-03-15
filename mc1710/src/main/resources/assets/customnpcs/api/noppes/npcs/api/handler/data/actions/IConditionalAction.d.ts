/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data.actions
 */

/**
 * @javaFqn noppes.npcs.api.handler.data.actions.IConditionalAction
 */
export interface IConditionalAction extends import('../IAction').IAction {
    /**
     * @param condition checked every tick, if it returns true, task is fired
     * @return this action
     */
    setCondition(condition: Java.java.util.function.Function<import('../IAction').IAction, Boolean>): import('./IConditionalAction').IConditionalAction;
    /**
     * @param terminateWhen checked every tick, if it returns true, action is terminated (gets marked done)
     * @return this action
     */
    terminateWhen(terminateWhen: Java.java.util.function.Function<import('../IAction').IAction, Boolean>): import('./IConditionalAction').IConditionalAction;
    /**
     * @param onTermination code to run when the termination condition returns true
     * @return this action
     */
    onTermination(onTermination: Java.java.util.function.Consumer<import('../IAction').IAction>): import('./IConditionalAction').IConditionalAction;
    /**
     * @return true if condition was satisfied and task ran  (i.e can be called in termination task to see if original task was executed
     * then do code based on that, if not return early)
     */
    wasTaskExecuted(): import('./boolean').boolean;
    /**
     * Note: Only for Conditional Actions
     *
     * @return how many times this conditional action has tested its condition
     */
    getCheckCount(): import('./int').int;
    /**
     * Note: Only for Conditional Actions
     *
     * @return the maximum number of checks before auto-expiring, or -1 if unlimited
     */
    getMaxChecks(): import('./int').int;
    /**
     *
     * @return True if condition provided by {{@link #terminateWhen(Function)}} is satisfied
     * Can be called directly in the IAction's task.
     * Can only be true once, as action is marked done immediately after.
     */
    isTerminated(): import('./boolean').boolean;
    /**
     * @param maxChecks maximum times to test condition before auto-cancelling
     * @return this action
     */
    setMaxChecks(maxChecks: import('./int').int): import('./IConditionalAction').IConditionalAction;
}
