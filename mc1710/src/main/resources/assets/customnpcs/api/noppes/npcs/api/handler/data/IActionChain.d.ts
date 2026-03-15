/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * @javaFqn noppes.npcs.api.handler.data.IActionChain
 */
export interface IActionChain {
    getName(): String;
    /**
     * @param name of chain
     * @return this IActionChain for method chaining
     */
    setName(name: String): import('./IActionChain').IActionChain;
    /**
     *
     * @return queue this chain is scheduled on
     */
    getQueue(): import('./IActionQueue').IActionQueue;
    /**
     *
     * @param delay ticks between an IAction and another
     * @param name  name of IAction
     * @param task  task of IAction
     * @return this IActionChain for method chaining
     */
    after(delay: import('./int').int, name: String, task: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IActionChain').IActionChain;
    after(delay: import('./int').int, task: Java.java.util.function.Consumer<import('./IAction').IAction>): import('./IActionChain').IActionChain;
    /**
     * Start IActionManager
     *
     * @return this IActionChain for method chaining
     */
    start(): import('./IActionChain').IActionChain;
}
