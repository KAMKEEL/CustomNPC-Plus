package noppes.npcs.api.handler;

import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.IActionChain;
import noppes.npcs.api.handler.data.IActionQueue;
import noppes.npcs.api.handler.data.actions.IConditionalAction;

import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;


/**
 * Manages a queue of {@link IAction} instances, allowing scheduling of delayed,
 * repeating, or conditional "tasks" for NPC scripting.
 */
public interface IActionManager {

    /**
     * Begin processing scheduled actions.  Must be called once.
     *
     * @return this action manager
     */
    IActionManager start();

    /**
     * Halt processing of actions.  Queued actions remain but will not run until
     * {@link #start()} is called again.
     *
     * @return this action manager
     */
    IActionManager stop();

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    // Creators

    /**
     * Create a new action instance without immediately scheduling it.
     *
     * @param name        a unique name for this action
     * @param maxDuration the maximum lifetime of the action in ticks
     * @param delay       number of ticks to wait before the first run
     * @param action      code to execute each time the action "fires"
     * @return a fresh {@link IAction} object
     */
    IAction create(String name, int maxDuration, int delay, Consumer<IAction> action);

    IAction create(int maxDuration, int delay, Consumer<IAction> task);

    IAction create(String name, int delay, Consumer<IAction> t);

    IAction create(int delay, Consumer<IAction> t);

    IAction create(String name, Consumer<IAction> t);

    String getName();

    IActionManager setName(String name);

    boolean inDebugMode();

    /**
     * Enabling prints to the console the life cycle of IActionManager, it's IActionQueues and the scheduled IActions
     *
     * @param debug whether to enable debug logging
     * @return this action manager
     */
    IActionManager setDebugMode(boolean debug);

    IAction create(String name);

    IAction create(Consumer<IAction> t);

    /**
     * Schedule a conditional action that gives up after at most maxChecks attempts.
     *
     * @param condition checked every tick, if it returns true, task is fired
     * @param task      code to run once condition first becomes true
     * @return the action scheduled
     */
    IConditionalAction create(Function<IAction, Boolean> condition, Consumer<IAction> task);

    /**
     * Schedule a conditional action that gives up after at most maxChecks attempts.
     *
     * @param name      unique name
     * @param condition checked every tick, if it returns true, task is fired
     * @param task      code to run once condition first becomes true
     * @return the action scheduled
     */
    IConditionalAction create(String name, Function<IAction, Boolean> condition, Consumer<IAction> task);

    /**
     * Schedule a conditional action that gives up after at most maxChecks attempts.
     *
     * @param condition     checked every tick, if it returns true, task is fired
     * @param task          code to run once condition first becomes true
     * @param terminateWhen checked every tick, if it returns true, action is terminated (gets marked done)
     * @return the action scheduled
     */
    IConditionalAction create(Function<IAction, Boolean> condition, Consumer<IAction> task, Function<IAction, Boolean> terminateWhen);

    /**
     * Schedule a conditional action that gives up after at most maxChecks attempts.
     *
     * @param name          unique name
     * @param condition     checked every tick, if it returns true, task is fired
     * @param task          code to run once condition first becomes true
     * @param terminateWhen checked every tick, if it returns true, action is terminated (gets marked done)
     * @return the action scheduled
     */
    IConditionalAction create(String name, Function<IAction, Boolean> condition, Consumer<IAction> task, Function<IAction, Boolean> terminateWhen);

    /**
     * Schedule a conditional action that gives up after at most maxChecks attempts.
     *
     * @param condition     checked every tick, if it returns true, task is fired
     * @param task          code to run once condition first becomes true
     * @param terminateWhen checked every tick, if it returns true, action is terminated (gets marked done)
     * @param onTermination code to run when the termination condition returns true
     * @return the action scheduled
     */
    IConditionalAction create(Function<IAction, Boolean> condition, Consumer<IAction> task, Function<IAction, Boolean> terminateWhen, Consumer<IAction> onTermination);

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
    IConditionalAction create(String name, Function<IAction, Boolean> condition, Consumer<IAction> task, Function<IAction, Boolean> terminateWhen, Consumer<IAction> onTermination);

    /// ////////////////////////////////////////////////
    /// ////////////////////////////////////////////////
    // Queues

    IActionQueue createQueue(String name);

    IActionQueue createQueue(String name, boolean isParallel);

    IActionQueue getOrCreateQueue(String name);

    IActionQueue getOrCreateQueue(String name, boolean isParallel);

    IActionQueue getQueue(String name);

    boolean hasQueue(String name);

    /**
     * @param name the name for the new action
     * @return True if queue successfully removed from IActionManager and cleared
     */
    boolean removeQueue(String name);

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    // Sequential

    /**
     * Retrieve the entire action queue.
     *
     * @return live reference to the internal {@link Queue} of actions
     */
    IActionQueue getSequentialQueue();

    /**
     * Schedule an existing action for execution.
     *
     * @param action the action to enqueue
     * @return the action scheduled
     */
    IAction schedule(IAction action);

    /**
     * Multiple actions chained one after another
     * i.e schedule(act1,act2,act3,...)
     *
     * @param actions the actions to schedule
     */
    void schedule(IAction... actions);

    /**
     * Convenience for {@link #create(Consumer)} + enqueue.
     *
     * @param task code to execute each time the task "fires"
     * @return the task scheduled
     */
    IAction schedule(Consumer<IAction> task);

    /**
     * Multiple tasks chained one after another
     * i.e schedule(task1,task2,task3,...)
     *
     * @param tasks the task consumers to schedule
     */
    void schedule(Consumer<IAction>... tasks);

    /**
     * Convenience for {@link #create(String, Consumer)} + enqueue.
     *
     * @param delay number of ticks to wait before the first task run
     * @param task  code to execute each time the task "fires"
     * @return the task scheduled
     */
    IAction schedule(int delay, Consumer<IAction> task);

    /**
     * Convenience for {@link #create(String, Consumer)} + enqueue.
     *
     * @param name a unique name for this action
     * @param task code to execute each time the task "fires"
     * @return the task scheduled
     */
    IAction schedule(String name, Consumer<IAction> task);

    /**
     * Convenience for {@link #create(String, int, Consumer)} + enqueue.
     *
     * @param name  a unique name for this action
     * @param delay number of ticks to wait before the first task run
     * @param task  code to execute each time the task "fires"
     * @return the task scheduled
     */
    IAction schedule(String name, int delay, Consumer<IAction> task);

    /**
     * Convenience for {@link #create(String, int, int, Consumer)} + enqueue.
     *
     * @param name        a unique name for this action
     * @param maxDuration the maximum lifetime of the action in ticks
     * @param delay       number of ticks to wait before the first task run
     * @param task        code to execute each time the task "fires"
     * @return the task scheduled
     */
    IAction schedule(String name, int maxDuration, int delay, Consumer<IAction> task);

    IAction schedule(int maxDuration, int delay, Consumer<IAction> task);

    /**
     * Insert an action at a specific position in the queue.
     *
     * @param index  zero-based queue position to insert at
     * @param action the action to insert
     * @return the action scheduled
     */
    IAction scheduleActionAt(int index, IAction action);

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    // Conditionals

    /**
     * @return the list of all conditional actions scheduled
     */
    IActionQueue getConditionalQueue();

    IConditionalAction schedule(IConditionalAction action);

    /**
     * Multiple conditionals
     * i.e schedule(act1,act2,act3,...)
     *
     * @param actions the conditional actions to schedule
     */
    void schedule(IConditionalAction... actions);

    /**
     * Schedule a conditional action that gives up after at most maxChecks attempts.
     *
     * @param condition checked every tick, if it returns true, task is fired
     * @param task      code to run once condition first becomes true
     * @return the action scheduled
     */
    IConditionalAction schedule(Function<IAction, Boolean> condition, Consumer<IAction> task);

    /**
     * Schedule a conditional action that gives up after at most maxChecks attempts.
     *
     * @param condition     checked every tick, if it returns true, task is fired
     * @param task          code to run once condition first becomes true
     * @param terminateWhen checked every tick, if it returns true, action is terminated (gets marked done)
     * @return the action scheduled
     */
    IConditionalAction schedule(Function<IAction, Boolean> condition, Consumer<IAction> task, Function<IAction, Boolean> terminateWhen);

    /**
     * Schedule a conditional action that gives up after at most maxChecks attempts.
     *
     * @param condition     checked every tick, if it returns true, task is fired
     * @param task          code to run once condition first becomes true
     * @param terminateWhen checked every tick, if it returns true, action is terminated (gets marked done)
     * @param onTermination code to run when the termination condition returns true
     * @return the action scheduled
     */
    IConditionalAction schedule(Function<IAction, Boolean> condition, Consumer<IAction> task, Function<IAction, Boolean> terminateWhen, Consumer<IAction> onTermination);

    /**
     * Schedule a conditional action that gives up after at most maxChecks attempts.
     *
     * @param name      unique name
     * @param condition checked every tick, if it returns true, task is fired
     * @param task      code to run once condition first becomes true
     * @return the action scheduled
     */
    IConditionalAction schedule(String name, Function<IAction, Boolean> condition, Consumer<IAction> task);

    /**
     * Schedule a conditional action that gives up after at most maxChecks attempts.
     *
     * @param name          unique name
     * @param condition     checked every tick, if it returns true, task is fired
     * @param task          code to run once condition first becomes true
     * @param terminateWhen checked every tick, if it returns true, action is terminated (gets marked done)
     * @return the action scheduled
     */
    IConditionalAction schedule(String name, Function<IAction, Boolean> condition, Consumer<IAction> task, Function<IAction, Boolean> terminateWhen);

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
    IConditionalAction schedule(String name, Function<IAction, Boolean> condition, Consumer<IAction> task, Function<IAction, Boolean> terminateWhen, Consumer<IAction> onTermination);

    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////
    // Parallels

    /**
     * @return the list of all parallel actions scheduled
     */
    IActionQueue getParallelQueue();

    /**
     * Schedules actions on the parallelQueue, where all actions are executed simultaneously
     *
     * @param action the action to add to the parallel chain
     * @return the parallel action
     */

    IAction scheduleParallel(IAction action);

    /**
     * Multiple actions in parallel
     * i.e scheduleParallel(act1,act2,act3,...)
     *
     * @param actions the actions to run in parallel
     */
    void scheduleParallel(IAction... actions);

    IAction scheduleParallel(Consumer<IAction> task);

    void scheduleParallel(Consumer<IAction>... tasks);

    IAction scheduleParallel(int delay, Consumer<IAction> task);

    IAction scheduleParallel(int maxDuration, int delay, Consumer<IAction> task);

    IAction scheduleParallel(String name, Consumer<IAction> task);

    IAction scheduleParallel(String name, int delay, Consumer<IAction> task);

    IAction scheduleParallel(String name, int maxDuration, int delay, Consumer<IAction> task);

    /**
     * @return All the IActionQueues within this Manager, including main sequential, parallel and conditional
     */
    IActionQueue[] getAllQueues();

    /**
     * @param name action name to check for
     * @return true if action is scheduled in any of the available  queues
     */
    boolean hasAny(String name);

    /**
     * @param name the name for the new parallel action
     * @return Checks through all available queues and fetches the first IAction with given name
     */
    IAction getAny(String name);

    /**
     * Checks through all available queues and cancels (remove) the first action with the given name.
     *
     * @param name the name assigned when scheduling
     * @return true if one was found and removed, false otherwise
     */
    boolean cancelAny(String name);

    /**
     * Clears all available queues and kills all of their scheduled IActions.
     */
    void clear();

    /**
     * @return a chain that can be used to fire Actions sequentially based on sequentialQueue
     */
    IActionChain chain();

    /**
     *
     * @return a chain that can be used to fire Actions in parallel based on parallelQueue
     */
    IActionChain parallelChain();

    /**
     *
     * @return a string containing all the IActionQueues active within the manager
     */
    String printQueues();
}
