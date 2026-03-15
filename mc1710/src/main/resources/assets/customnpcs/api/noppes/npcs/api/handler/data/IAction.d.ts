/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Represents a single "task" that can be executed over multiple ticks,
 * supports delayed start, limited duration, repeating intervals, data storage,
 * and chaining to neighboring tasks.
  * @javaFqn noppes.npcs.api.handler.data.IAction
*/
export interface IAction {
    /**
     * @return the queue this action is scheduled on. null if not scheduled
     */
    getQueue(): import('./IActionQueue').IActionQueue;
    /**
     * @param queue Schedules action on this queue. If action was scheduled on different queue, transfers it over from that to this.
     * @return this action
     */
    setQueue(queue: import('./IActionQueue').IActionQueue): import('./IAction').IAction;
    /**
     * @param task code to execute each time the action fires
     * @return this action
     */
    setTask(task: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IAction').IAction;
    /**
     *
     * @return action manager the action is scheduled on
     */
    getManager(): import('../IActionManager').IActionManager;
    /**
     * @return True if was scheduled in the queues using any of the schedule methods, false if was only created.
     */
    isScheduled(): import('./boolean').boolean;
    /**
     * @return how many times this action’s task has been executed
     */
    getCount(): import('./int').int;
    /**
     *
     * @param task Fires right after action gets scheduled at duration 0
     * @return this action
     */
    onStart(task: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IAction').IAction;
    /**
     *
     * @param task Fires right before a marked done action gets removed from it's IActionQueue
     * @return this action
     */
    onDone(task: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IAction').IAction;
    /**
     * @return how many ticks have elapsed since this action actually began (excluding start delay)
     */
    getDuration(): import('./int').int;
    /**
     * @return the name given at creation/scheduling time
     */
    getName(): String;
    /**
     * @return the maximum number of ticks this action is allowed to run before auto marking done
     * <p>
     * P.S: If max duration is reached and this IAction's thread (created using {@link #threadify()}) is paused by any of the pausing methods,
     * the thread is forcibly resumed and finishes the task execution.
     */
    getMaxDuration(): import('./int').int;
    /**
     * @param ticks max duration
     *              default: -1,  infinite
     * @return this action
     */
    setMaxDuration(ticks: import('./int').int): import('./IAction').IAction;
    /**
     * @return the maximum number of counts this action is allowed to run before auto marking done
     */
    getMaxCount(): import('./int').int;
    /**
     * @param n max count, task auto marks done after running for n counts
     *          default: -1, infinite
     * @return this action
     */
    times(n: import('./int').int): import('./IAction').IAction;
    /**
     * Execute task only once, mark done
     * equivalent to times(1)
     *
     * @return this action
     */
    once(): import('./IAction').IAction;
    /**
     * Mark this action as complete and de-schedules it from its queue.
     * Forcibly resumes action if paused or had its thread slept with the IAction pausing methods.
     *
     */
    markDone(): import('./void').void;
    /**
     * @return true if {@link #markDone()} was called (or maxDuration reached)
     */
    isDone(): import('./boolean').boolean;
    /**
     * marks done and safely dumps the action's data and thread
     */
    kill(): import('./void').void;
    /**
     * Retrieve arbitrary per-action data.
     *
     * @param key a string key
     * @return the stored value, or null if not set
     */
    getData(key: String): Object;
    /**
     * Store arbitrary per-action data.
     *
     * @param key   a string key
     * @param value any object to associate with this action
     * @return this action
     */
    setData(key: String, value: Object): import('./IAction').IAction;
    removeData(key: String): import('./IAction').IAction;
    /**
     *
     * @param copyTo copies all of this IAction's data to copyTo
     * @return this action
     */
    copyDataTo(copyTo: import('./IAction').IAction): import('./IAction').IAction;
    /**
     *
     * @return a string containing all the stored data key/values of this IAction
     */
    printData(): String;
    hasData(key: String): import('./boolean').boolean;
    /**
     * @return how many ticks between each execution of the action's task
     * Default is 5 ticks (4 times per second)
     */
    getUpdateEvery(): import('./int').int;
    /**
     * Set how many ticks between each execution of the action task.
     *
     * @param ticks tick interval (e.g. 1 = every tick, 20 = once per second)
     * @return this action
     */
    updateEvery(ticks: import('./int').int): import('./IAction').IAction;
    /**
     * Executes task every tick (Sets updateEvery to 1)
     *
     * @return this action
     */
    everyTick(): import('./IAction').IAction;
    /**
     * Executes task every second (Sets updateEvery to 20)
     *
     * @return this action
     */
    everySecond(): import('./IAction').IAction;
    /**
     * @return how many ticks remain before the action begins (initial delay)
     */
    getStartAfterTicks(): import('./int').int;
    /**
     * @param ticks pauses action for this number of ticks (any subsequent action is paused too)
     *              If action was threaded using {@link #threadify()}, sleeps the thread.
     *              Can be forcibly resumed using {@link #resume()}
     * @return this action
     */
    pauseFor(ticks: import('./int').int): import('./IAction').IAction;
    pauseFor(millis: import('./long').long): import('./IAction').IAction;
    /**
     * Must call {@link #threadify()} before using, else throws exception.
     * Pauses IAction's thread until {@link #resume()} is called.
     */
    pause(): import('./void').void;
    /**
     * Must call {@link #threadify()} before using, else throws exception.
     * Pauses IAction's thread until the supplied condition is satisfied or {@link #resume()} is called.
     *
     * @param until condition to check each tick
     */
    pauseUntil(until: Java.java.util.function.Function<import('./IAction').IAction, Boolean>): import('./void').void;
    /**
     * Resumes thread that was previously paused by {@link #pause()},  {@link #pauseUntil(Function)}, {@link #pauseFor(int)} or {@link #pauseFor(long)}
     * Must be called from a different thread than the IAction one, as that one is paused, so it won't reach this function if it comes after any of the pausing functions.
     */
    resume(): import('./void').void;
    /**
     * @return checks if IAction's getStartAfterTicks &gt; 0, or if IAction's thread is paused if threaded
     * Preferably called from a different thread than the IAction one if it's paused.
     */
    isPaused(): import('./boolean').boolean;
    /**
     *
     * @return "{Type} '{Name}'" of action i.e "Action 'one'" or "ConditionalAction 'two'"
     */
    getIdentifier(): String;
    /**
     * Creates a new thread for task to run into. Allows for pausing and sleeping just this IAction's thread.
     *
     * @return this action
     */
    threadify(): import('./IAction').IAction;
    /**
     *
     * @return starts the IActionManager
     */
    start(): import('./IAction').IAction;
    /**
     * @return the next action in the queue (or null if none or at end)
     */
    getNext(): import('./IAction').IAction;
    /**
     * @return the previous action in the queue (or null if none or at front)
     */
    getPrevious(): import('./IAction').IAction;
    /**
     * Enqueue another action immediately after this one.
     *
     * @param after the action to run next
     * @return the chained action
     */
    after(after: import('./IAction').IAction): import('./IAction').IAction;
    /**
     * Multiple actions chained one after another
     * i.e after(act1,act2,act3,...)
     *
     * @param actions the actions to chain
     */
    after(...actions: import('./IAction').IAction[]): import('./void').void;
    /**
     * Multiple tasks chained one after another
     * i.e after(task1,task2,task3,...)
     *
     * @param tasks the tasks to chain
     */
    after(...tasks: Java.java.util.function.Consumer<import('./IAction').IAction>[]): import('./void').void;
    after(name: String, maxDuration: import('./int').int, delay: import('./int').int, t: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IAction').IAction;
    after(name: String, delay: import('./int').int, t: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IAction').IAction;
    after(delay: import('./int').int, t: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IAction').IAction;
    after(name: String, t: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IAction').IAction;
    after(t: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IAction').IAction;
    /**
     * Enqueue another action immediately before this one (pausing this one until done).
     *
     * @param before the action to run prior
     * @return the chained action
     */
    before(before: import('./IAction').IAction): import('./IAction').IAction;
    before(name: String, maxDuration: import('./int').int, delay: import('./int').int, t: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IAction').IAction;
    before(name: String, delay: import('./int').int, t: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IAction').IAction;
    before(delay: import('./int').int, t: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IAction').IAction;
    before(name: String, t: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IAction').IAction;
    before(t: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IAction').IAction;
    /**
     * Enqueue an IConditionalAction on the conditional chain
     *
     * @param after the scheduled IConditionalAction
     * @return the conditional action
     */
    conditional(after: import('./actions/IConditionalAction').IConditionalAction): import('./actions/IConditionalAction').IConditionalAction;
    conditional(...actions: import('./actions/IConditionalAction').IConditionalAction[]): import('./void').void;
    conditional(condition: Java.java.util.function.Function<import('./IAction').IAction, Boolean>, task: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./actions/IConditionalAction').IConditionalAction;
    conditional(name: String, condition: Java.java.util.function.Function<import('./IAction').IAction, Boolean>, task: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./actions/IConditionalAction').IConditionalAction;
    conditional(condition: Java.java.util.function.Function<import('./IAction').IAction, Boolean>, task: Java.java.util.function.Consumer<import('./IAction').IAction>, terminate: Java.java.util.function.Function<import('./IAction').IAction, Boolean>): import('./actions/IConditionalAction').IConditionalAction;
    conditional(name: String, condition: Java.java.util.function.Function<import('./IAction').IAction, Boolean>, task: Java.java.util.function.Consumer<import('./IAction').IAction>, terminate: Java.java.util.function.Function<import('./IAction').IAction, Boolean>): import('./actions/IConditionalAction').IConditionalAction;
    conditional(condition: Java.java.util.function.Function<import('./IAction').IAction, Boolean>, task: Java.java.util.function.Consumer<import('./IAction').IAction>, terminateWhen: Java.java.util.function.Function<import('./IAction').IAction, Boolean>, onTermination: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./actions/IConditionalAction').IConditionalAction;
    conditional(name: String, condition: Java.java.util.function.Function<import('./IAction').IAction, Boolean>, task: Java.java.util.function.Consumer<import('./IAction').IAction>, terminateWhen: Java.java.util.function.Function<import('./IAction').IAction, Boolean>, onTermination: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./actions/IConditionalAction').IConditionalAction;
    /**
     * Enqueue another IAction on the parallel chain which starts firing simultaneously as this.
     *
     * @param after the scheduled parallel action
     * @return the parallel action
     */
    parallel(after: import('./IAction').IAction): import('./IAction').IAction;
    parallel(...actions: import('./IAction').IAction[]): import('./void').void;
    parallel(task: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IAction').IAction;
    parallel(...tasks: Java.java.util.function.Consumer<import('./IAction').IAction>[]): import('./void').void;
    parallel(delay: import('./int').int, task: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IAction').IAction;
    parallel(name: String, task: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IAction').IAction;
    parallel(name: String, startAfterTicks: import('./int').int, task: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IAction').IAction;
    parallel(name: String, maxDuration: import('./int').int, delay: import('./int').int, t: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IAction').IAction;
}
