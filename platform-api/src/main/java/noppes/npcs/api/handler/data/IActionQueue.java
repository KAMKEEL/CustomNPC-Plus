package noppes.npcs.api.handler.data;

import noppes.npcs.api.handler.IActionManager;

import java.util.Queue;
import java.util.function.Consumer;

public interface IActionQueue {

    /**
     * @return starts the processing of scheduled IActions in queue.
     * Queue is on by default
     */
    IActionQueue start();

    /**
     * @return pauses the processing of scheduled IActions
     */
    IActionQueue stop();

    IActionManager getManager();

    String getName();

    /**
     * @return True if parallel, else is sequential
     */
    boolean isParallel();

    /**
     *
     * @return actual java queue which stores all scheduled IActions
     */
    Queue<IAction> getQueue();

    /**
     *
     * @param parallel True to turn queue into parallel, false for sequential
     * @return this IActionQueue for method chaining
     */
    IActionQueue setParallel(boolean parallel);

    /**
     *
     * @return If true, auto-stops queue when empty/no active Actions scheduled using {{@link #stop()}}
     */
    boolean isStoppedWhenEmpty();

    /**
     *
     * @param stopWhenEmpty to auto-stop queue when empty/no active Actions scheduled using {{@link #stop()}}
     * @return this IActionQueue for method chaining
     */
    IActionQueue stopWhenEmpty(boolean stopWhenEmpty);

    /**
     * @return True to auto-remove this queue from the IActionManager when no IActions are scheduled or queue is empty
     * Default: true
     */
    boolean isKilledWhenEmpty();

    /**
     * @return ticks it takes to remove this queue from IActionManager after {{@link #isKilledWhenEmpty()}} is satisfied
     * If an IAction is scheduled after it's satisfied, the kill process is aborted.
     * Default: 100 ticks
     */
    int getKillWhenEmptyAfter();

    IActionQueue killWhenEmpty(boolean killWhenEmpty);

    /**
     * @param ticks to kill queue after when killWhenEmpty and queue has no active tasks
     *              If an IAction is scheduled during the kill process, process is aborted
     * @return this IActionQueue for method chaining
     */
    IActionQueue killWhenEmptyAfter(int ticks);

    /**
     * @return True if queue was removed from IActionManager or killed
     */
    boolean isDead();

    /**
     * @return kills queue and removes it from IActionManager immediately
     */
    IActionQueue kill();

    IAction schedule(IAction action);

    void schedule(IAction... actions);

    void schedule(Consumer<IAction>... tasks);

    IAction schedule(Consumer<IAction> task);

    IAction schedule(int delay, Consumer<IAction> task);

    IAction schedule(int maxDuration, int delay, Consumer<IAction> task);

    IAction schedule(String name, Consumer<IAction> task);

    IAction schedule(String name, int delay, Consumer<IAction> task);

    IAction schedule(String name, int maxDuration, int delay, Consumer<IAction> task);

    IAction scheduleActionAt(int index, IAction action);

    boolean hasActiveTasks();

    /**
     *
     * @param action the action to find in the queue
     * @return index of action in {{@link #getQueue()}}
     * -1 if not in queue.
     */
    int getIndex(IAction action);

    /**
     *
     * @return current IAction the queue is at. For sequential use.
     */
    IAction getCurrentAction();

    boolean has(IAction action);

    boolean has(String actionName);

    IAction get(String actionName);

    boolean cancel(IAction action);

    boolean cancel(String actionName);

    /**
     * Kills all IActions in {{@link #getQueue()}} and empties it
     */
    void clear();

    /**
     * @return an IActionChain based on this queue.
     * If {{@link #isParallel()}}, returns a parallel IActionChain
     */

    IActionChain chain();

    /**
     *
     * @return a string containing all the scheduled actions within this queue
     */
    String printQueue();
}
