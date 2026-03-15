/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * @javaFqn noppes.npcs.api.handler.data.IActionQueue
 */
export interface IActionQueue {
    /**
     * @return starts the processing of scheduled IActions in queue.
     * Queue is on by default
     */
    start(): import('./IActionQueue').IActionQueue;
    /**
     * @return pauses the processing of scheduled IActions
     */
    stop(): import('./IActionQueue').IActionQueue;
    getManager(): import('../IActionManager').IActionManager;
    getName(): String;
    /**
     * @return True if parallel, else is sequential
     */
    isParallel(): import('./boolean').boolean;
    /**
     *
     * @return actual java queue which stores all scheduled IActions
     */
    getQueue(): import('./IAction').IAction[];
    /**
     *
     * @param parallel True to turn queue into parallel, false for sequential
     * @return this IActionQueue for method chaining
     */
    setParallel(parallel: import('./boolean').boolean): import('./IActionQueue').IActionQueue;
    /**
     *
     * @return If true, auto-stops queue when empty/no active Actions scheduled using {{@link #stop()}}
     */
    isStoppedWhenEmpty(): import('./boolean').boolean;
    /**
     *
     * @param stopWhenEmpty to auto-stop queue when empty/no active Actions scheduled using {{@link #stop()}}
     * @return this IActionQueue for method chaining
     */
    stopWhenEmpty(stopWhenEmpty: import('./boolean').boolean): import('./IActionQueue').IActionQueue;
    /**
     * @return True to auto-remove this queue from the IActionManager when no IActions are scheduled or queue is empty
     * Default: true
     */
    isKilledWhenEmpty(): import('./boolean').boolean;
    /**
     * @return ticks it takes to remove this queue from IActionManager after {{@link #isKilledWhenEmpty()}} is satisfied
     * If an IAction is scheduled after it's satisfied, the kill process is aborted.
     * Default: 100 ticks
     */
    getKillWhenEmptyAfter(): import('./int').int;
    killWhenEmpty(killWhenEmpty: import('./boolean').boolean): import('./IActionQueue').IActionQueue;
    /**
     * @param ticks to kill queue after when killWhenEmpty and queue has no active tasks
     *              If an IAction is scheduled during the kill process, process is aborted
     * @return this IActionQueue for method chaining
     */
    killWhenEmptyAfter(ticks: import('./int').int): import('./IActionQueue').IActionQueue;
    /**
     * @return True if queue was removed from IActionManager or killed
     */
    isDead(): import('./boolean').boolean;
    /**
     * @return kills queue and removes it from IActionManager immediately
     */
    kill(): import('./IActionQueue').IActionQueue;
    schedule(action: import('./IAction').IAction): import('./IAction').IAction;
    schedule(...actions: import('./IAction').IAction[]): import('./void').void;
    schedule(...tasks: Java.java.util.function.Consumer<import('./IAction').IAction>[]): import('./void').void;
    schedule(task: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IAction').IAction;
    schedule(delay: import('./int').int, task: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IAction').IAction;
    schedule(maxDuration: import('./int').int, delay: import('./int').int, task: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IAction').IAction;
    schedule(name: String, task: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IAction').IAction;
    schedule(name: String, delay: import('./int').int, task: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IAction').IAction;
    schedule(name: String, maxDuration: import('./int').int, delay: import('./int').int, task: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IAction').IAction;
    scheduleActionAt(index: import('./int').int, action: import('./IAction').IAction): import('./IAction').IAction;
    hasActiveTasks(): import('./boolean').boolean;
    /**
     *
     * @param action the action to find in the queue
     * @return index of action in {{@link #getQueue()}}
     * -1 if not in queue.
     */
    getIndex(action: import('./IAction').IAction): import('./int').int;
    /**
     *
     * @return current IAction the queue is at. For sequential use.
     */
    getCurrentAction(): import('./IAction').IAction;
    has(action: import('./IAction').IAction): import('./boolean').boolean;
    has(actionName: String): import('./boolean').boolean;
    get(actionName: String): import('./IAction').IAction;
    cancel(action: import('./IAction').IAction): import('./boolean').boolean;
    cancel(actionName: String): import('./boolean').boolean;
    /**
     * Kills all IActions in {{@link #getQueue()}} and empties it
     */
    clear(): import('./void').void;
    /**
     * @return an IActionChain based on this queue.
     * If {{@link #isParallel()}}, returns a parallel IActionChain
     */
    chain(): import('./IActionChain').IActionChain;
    /**
     *
     * @return a string containing all the scheduled actions within this queue
     */
    printQueue(): String;
}
