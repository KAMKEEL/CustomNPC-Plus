/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Manages a queue of {@link IAction} instances, allowing scheduling of delayed,
 * repeating, or conditional "tasks" for NPC scripting.
  * @javaFqn noppes.npcs.api.handler.IActionManager
*/
export interface IActionManager {
    /**
     * Begin processing scheduled actions.  Must be called once.
     *
     * @return this action manager
     */
    start(): import('./IActionManager').IActionManager;
    /**
     * Halt processing of actions.  Queued actions remain but will not run until
     * {@link #start()} is called again.
     *
     * @return this action manager
     */
    stop(): import('./IActionManager').IActionManager;
    /**
     * Create a new action instance without immediately scheduling it.
     *
     * @param name        a unique name for this action
     * @param maxDuration the maximum lifetime of the action in ticks
     * @param delay       number of ticks to wait before the first run
     * @param action      code to execute each time the action "fires"
     * @return a fresh {@link IAction} object
     */
    create(name: String, maxDuration: import('./int').int, delay: import('./int').int, action: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/IAction').IAction;
    create(maxDuration: import('./int').int, delay: import('./int').int, task: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/IAction').IAction;
    create(name: String, delay: import('./int').int, t: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/IAction').IAction;
    create(delay: import('./int').int, t: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/IAction').IAction;
    create(name: String, t: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/IAction').IAction;
    getName(): String;
    setName(name: String): import('./IActionManager').IActionManager;
    inDebugMode(): import('./boolean').boolean;
    /**
     * Enabling prints to the console the life cycle of IActionManager, it's IActionQueues and the scheduled IActions
     *
     * @param debug whether to enable debug logging
     * @return this action manager
     */
    setDebugMode(debug: import('./boolean').boolean): import('./IActionManager').IActionManager;
    create(name: String): import('./data/IAction').IAction;
    create(t: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/IAction').IAction;
    /**
     * Schedule a conditional action that gives up after at most maxChecks attempts.
     *
     * @param condition checked every tick, if it returns true, task is fired
     * @param task      code to run once condition first becomes true
     * @return the action scheduled
     */
    create(condition: Java.java.util.function.Function<import('./data/IAction').IAction, Boolean>, task: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/actions/IConditionalAction').IConditionalAction;
    /**
     * Schedule a conditional action that gives up after at most maxChecks attempts.
     *
     * @param name      unique name
     * @param condition checked every tick, if it returns true, task is fired
     * @param task      code to run once condition first becomes true
     * @return the action scheduled
     */
    create(name: String, condition: Java.java.util.function.Function<import('./data/IAction').IAction, Boolean>, task: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/actions/IConditionalAction').IConditionalAction;
    /**
     * Schedule a conditional action that gives up after at most maxChecks attempts.
     *
     * @param condition     checked every tick, if it returns true, task is fired
     * @param task          code to run once condition first becomes true
     * @param terminateWhen checked every tick, if it returns true, action is terminated (gets marked done)
     * @return the action scheduled
     */
    create(condition: Java.java.util.function.Function<import('./data/IAction').IAction, Boolean>, task: Java.java.util.function.Consumer<import('./data/IAction').IAction>, terminateWhen: Java.java.util.function.Function<import('./data/IAction').IAction, Boolean>): import('./data/actions/IConditionalAction').IConditionalAction;
    /**
     * Schedule a conditional action that gives up after at most maxChecks attempts.
     *
     * @param name          unique name
     * @param condition     checked every tick, if it returns true, task is fired
     * @param task          code to run once condition first becomes true
     * @param terminateWhen checked every tick, if it returns true, action is terminated (gets marked done)
     * @return the action scheduled
     */
    create(name: String, condition: Java.java.util.function.Function<import('./data/IAction').IAction, Boolean>, task: Java.java.util.function.Consumer<import('./data/IAction').IAction>, terminateWhen: Java.java.util.function.Function<import('./data/IAction').IAction, Boolean>): import('./data/actions/IConditionalAction').IConditionalAction;
    /**
     * Schedule a conditional action that gives up after at most maxChecks attempts.
     *
     * @param condition     checked every tick, if it returns true, task is fired
     * @param task          code to run once condition first becomes true
     * @param terminateWhen checked every tick, if it returns true, action is terminated (gets marked done)
     * @param onTermination code to run when the termination condition returns true
     * @return the action scheduled
     */
    create(condition: Java.java.util.function.Function<import('./data/IAction').IAction, Boolean>, task: Java.java.util.function.Consumer<import('./data/IAction').IAction>, terminateWhen: Java.java.util.function.Function<import('./data/IAction').IAction, Boolean>, onTermination: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/actions/IConditionalAction').IConditionalAction;
    /**
     * Schedule a conditional action that gives up after at most maxChecks attempts.
     *
     * @param name          unique name
     * @param condition     checked every tick, if it returns true, task is fired
     * @param task          code to run once condition first becomes true
     * @param terminateWhen checked every tick, if it returns true, action is terminated (gets marked done)
     * @param onTermination code to run when the termination condition returns true
     * @return the action scheduled
     */
    create(name: String, condition: Java.java.util.function.Function<import('./data/IAction').IAction, Boolean>, task: Java.java.util.function.Consumer<import('./data/IAction').IAction>, terminateWhen: Java.java.util.function.Function<import('./data/IAction').IAction, Boolean>, onTermination: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/actions/IConditionalAction').IConditionalAction;
    createQueue(name: String): import('./data/IActionQueue').IActionQueue;
    createQueue(name: String, isParallel: import('./boolean').boolean): import('./data/IActionQueue').IActionQueue;
    getOrCreateQueue(name: String): import('./data/IActionQueue').IActionQueue;
    getOrCreateQueue(name: String, isParallel: import('./boolean').boolean): import('./data/IActionQueue').IActionQueue;
    getQueue(name: String): import('./data/IActionQueue').IActionQueue;
    hasQueue(name: String): import('./boolean').boolean;
    /**
     * @param name the name for the new action
     * @return True if queue successfully removed from IActionManager and cleared
     */
    removeQueue(name: String): import('./boolean').boolean;
    /**
     * Retrieve the entire action queue.
     *
     * @return live reference to the internal {@link Queue} of actions
     */
    getSequentialQueue(): import('./data/IActionQueue').IActionQueue;
    /**
     * Schedule an existing action for execution.
     *
     * @param action the action to enqueue
     * @return the action scheduled
     */
    schedule(action: import('./data/IAction').IAction): import('./data/IAction').IAction;
    /**
     * Multiple actions chained one after another
     * i.e schedule(act1,act2,act3,...)
     *
     * @param actions the actions to schedule
     */
    schedule(...actions: import('./data/IAction').IAction[]): import('./void').void;
    /**
     * Convenience for {@link #create(Consumer)} + enqueue.
     *
     * @param task code to execute each time the task "fires"
     * @return the task scheduled
     */
    schedule(task: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/IAction').IAction;
    /**
     * Multiple tasks chained one after another
     * i.e schedule(task1,task2,task3,...)
     *
     * @param tasks the task consumers to schedule
     */
    schedule(...tasks: Java.java.util.function.Consumer<import('./data/IAction').IAction>[]): import('./void').void;
    /**
     * Convenience for {@link #create(String, Consumer)} + enqueue.
     *
     * @param delay number of ticks to wait before the first task run
     * @param task  code to execute each time the task "fires"
     * @return the task scheduled
     */
    schedule(delay: import('./int').int, task: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/IAction').IAction;
    /**
     * Convenience for {@link #create(String, Consumer)} + enqueue.
     *
     * @param name a unique name for this action
     * @param task code to execute each time the task "fires"
     * @return the task scheduled
     */
    schedule(name: String, task: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/IAction').IAction;
    /**
     * Convenience for {@link #create(String, int, Consumer)} + enqueue.
     *
     * @param name  a unique name for this action
     * @param delay number of ticks to wait before the first task run
     * @param task  code to execute each time the task "fires"
     * @return the task scheduled
     */
    schedule(name: String, delay: import('./int').int, task: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/IAction').IAction;
    /**
     * Convenience for {@link #create(String, int, int, Consumer)} + enqueue.
     *
     * @param name        a unique name for this action
     * @param maxDuration the maximum lifetime of the action in ticks
     * @param delay       number of ticks to wait before the first task run
     * @param task        code to execute each time the task "fires"
     * @return the task scheduled
     */
    schedule(name: String, maxDuration: import('./int').int, delay: import('./int').int, task: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/IAction').IAction;
    schedule(maxDuration: import('./int').int, delay: import('./int').int, task: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/IAction').IAction;
    /**
     * Insert an action at a specific position in the queue.
     *
     * @param index  zero-based queue position to insert at
     * @param action the action to insert
     * @return the action scheduled
     */
    scheduleActionAt(index: import('./int').int, action: import('./data/IAction').IAction): import('./data/IAction').IAction;
    /**
     * @return the list of all conditional actions scheduled
     */
    getConditionalQueue(): import('./data/IActionQueue').IActionQueue;
    schedule(action: import('./data/actions/IConditionalAction').IConditionalAction): import('./data/actions/IConditionalAction').IConditionalAction;
    /**
     * Multiple conditionals
     * i.e schedule(act1,act2,act3,...)
     *
     * @param actions the conditional actions to schedule
     */
    schedule(...actions: import('./data/actions/IConditionalAction').IConditionalAction[]): import('./void').void;
    /**
     * Schedule a conditional action that gives up after at most maxChecks attempts.
     *
     * @param condition checked every tick, if it returns true, task is fired
     * @param task      code to run once condition first becomes true
     * @return the action scheduled
     */
    schedule(condition: Java.java.util.function.Function<import('./data/IAction').IAction, Boolean>, task: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/actions/IConditionalAction').IConditionalAction;
    /**
     * Schedule a conditional action that gives up after at most maxChecks attempts.
     *
     * @param condition     checked every tick, if it returns true, task is fired
     * @param task          code to run once condition first becomes true
     * @param terminateWhen checked every tick, if it returns true, action is terminated (gets marked done)
     * @return the action scheduled
     */
    schedule(condition: Java.java.util.function.Function<import('./data/IAction').IAction, Boolean>, task: Java.java.util.function.Consumer<import('./data/IAction').IAction>, terminateWhen: Java.java.util.function.Function<import('./data/IAction').IAction, Boolean>): import('./data/actions/IConditionalAction').IConditionalAction;
    /**
     * Schedule a conditional action that gives up after at most maxChecks attempts.
     *
     * @param condition     checked every tick, if it returns true, task is fired
     * @param task          code to run once condition first becomes true
     * @param terminateWhen checked every tick, if it returns true, action is terminated (gets marked done)
     * @param onTermination code to run when the termination condition returns true
     * @return the action scheduled
     */
    schedule(condition: Java.java.util.function.Function<import('./data/IAction').IAction, Boolean>, task: Java.java.util.function.Consumer<import('./data/IAction').IAction>, terminateWhen: Java.java.util.function.Function<import('./data/IAction').IAction, Boolean>, onTermination: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/actions/IConditionalAction').IConditionalAction;
    /**
     * Schedule a conditional action that gives up after at most maxChecks attempts.
     *
     * @param name      unique name
     * @param condition checked every tick, if it returns true, task is fired
     * @param task      code to run once condition first becomes true
     * @return the action scheduled
     */
    schedule(name: String, condition: Java.java.util.function.Function<import('./data/IAction').IAction, Boolean>, task: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/actions/IConditionalAction').IConditionalAction;
    /**
     * Schedule a conditional action that gives up after at most maxChecks attempts.
     *
     * @param name          unique name
     * @param condition     checked every tick, if it returns true, task is fired
     * @param task          code to run once condition first becomes true
     * @param terminateWhen checked every tick, if it returns true, action is terminated (gets marked done)
     * @return the action scheduled
     */
    schedule(name: String, condition: Java.java.util.function.Function<import('./data/IAction').IAction, Boolean>, task: Java.java.util.function.Consumer<import('./data/IAction').IAction>, terminateWhen: Java.java.util.function.Function<import('./data/IAction').IAction, Boolean>): import('./data/actions/IConditionalAction').IConditionalAction;
    /**
     * Schedule a conditional action that gives up after at most maxChecks attempts.
     *
     * @param name          unique name
     * @param condition     checked every tick, if it returns true, task is fired
     * @param task          code to run once condition first becomes true
     * @param terminateWhen checked every tick, if it returns true, action is terminated (gets marked done)
     * @param onTermination code to run when the termination condition returns true
     * @return the action scheduled
     */
    schedule(name: String, condition: Java.java.util.function.Function<import('./data/IAction').IAction, Boolean>, task: Java.java.util.function.Consumer<import('./data/IAction').IAction>, terminateWhen: Java.java.util.function.Function<import('./data/IAction').IAction, Boolean>, onTermination: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/actions/IConditionalAction').IConditionalAction;
    /**
     * @return the list of all parallel actions scheduled
     */
    getParallelQueue(): import('./data/IActionQueue').IActionQueue;
    /**
     * Schedules actions on the parallelQueue, where all actions are executed simultaneously
     *
     * @param action the action to add to the parallel chain
     * @return the parallel action
     */
    scheduleParallel(action: import('./data/IAction').IAction): import('./data/IAction').IAction;
    /**
     * Multiple actions in parallel
     * i.e scheduleParallel(act1,act2,act3,...)
     *
     * @param actions the actions to run in parallel
     */
    scheduleParallel(...actions: import('./data/IAction').IAction[]): import('./void').void;
    scheduleParallel(task: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/IAction').IAction;
    scheduleParallel(...tasks: Java.java.util.function.Consumer<import('./data/IAction').IAction>[]): import('./void').void;
    scheduleParallel(delay: import('./int').int, task: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/IAction').IAction;
    scheduleParallel(maxDuration: import('./int').int, delay: import('./int').int, task: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/IAction').IAction;
    scheduleParallel(name: String, task: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/IAction').IAction;
    scheduleParallel(name: String, delay: import('./int').int, task: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/IAction').IAction;
    scheduleParallel(name: String, maxDuration: import('./int').int, delay: import('./int').int, task: Java.java.util.function.Consumer<import('./data/IAction').IAction>): import('./data/IAction').IAction;
    /**
     * @return All the IActionQueues within this Manager, including main sequential, parallel and conditional
     */
    getAllQueues(): import('./data/IActionQueue').IActionQueue[];
    /**
     * @param name action name to check for
     * @return true if action is scheduled in any of the available  queues
     */
    hasAny(name: String): import('./boolean').boolean;
    /**
     * @param name the name for the new parallel action
     * @return Checks through all available queues and fetches the first IAction with given name
     */
    getAny(name: String): import('./data/IAction').IAction;
    /**
     * Checks through all available queues and cancels (remove) the first action with the given name.
     *
     * @param name the name assigned when scheduling
     * @return true if one was found and removed, false otherwise
     */
    cancelAny(name: String): import('./boolean').boolean;
    /**
     * Clears all available queues and kills all of their scheduled IActions.
     */
    clear(): import('./void').void;
    /**
     * @return a chain that can be used to fire Actions sequentially based on sequentialQueue
     */
    chain(): import('./data/IActionChain').IActionChain;
    /**
     *
     * @return a chain that can be used to fire Actions in parallel based on parallelQueue
     */
    parallelChain(): import('./data/IActionChain').IActionChain;
    /**
     *
     * @return a string containing all the IActionQueues active within the manager
     */
    printQueues(): String;
    LOGGER: import('./ActionLogger').ActionLogger;
}
