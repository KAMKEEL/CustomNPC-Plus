/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface IActionQueue {

    // Methods
    start(): import('./IActionQueue').IActionQueue;
    stop(): import('./IActionQueue').IActionQueue;
    getManager(): import('../IActionManager').IActionManager;
    getName(): string;
    isParallel(): boolean;
    getQueue(): Queue<IAction;
    setParallel(parallel: boolean): import('./IActionQueue').IActionQueue;
    isStoppedWhenEmpty(): boolean;
    stopWhenEmpty(stopWhenEmpty: boolean): import('./IActionQueue').IActionQueue;
    isKilledWhenEmpty(): boolean;
    getKillWhenEmptyAfter(): number;
    killWhenEmpty(killWhenEmpty: boolean): import('./IActionQueue').IActionQueue;
    killWhenEmptyAfter(ticks: number): import('./IActionQueue').IActionQueue;
    isDead(): boolean;
    kill(): import('./IActionQueue').IActionQueue;
    schedule(action: import('./IAction').IAction): import('./IAction').IAction;
    schedule(actions: ): void;
    schedule(tasks: ): void;
    schedule(task: Consumer<IAction): import('./IAction').IAction;
    schedule(delay: number, task: Consumer<IAction): import('./IAction').IAction;
    schedule(maxDuration: number, delay: number, task: Consumer<IAction): import('./IAction').IAction;
    schedule(name: string, task: Consumer<IAction): import('./IAction').IAction;
    schedule(name: string, delay: number, task: Consumer<IAction): import('./IAction').IAction;
    schedule(name: string, maxDuration: number, delay: number, task: Consumer<IAction): import('./IAction').IAction;
    scheduleActionAt(index: number, action: import('./IAction').IAction): import('./IAction').IAction;
    hasActiveTasks(): boolean;
    getIndex(action: import('./IAction').IAction): number;
    getCurrentAction(): import('./IAction').IAction;
    has(action: import('./IAction').IAction): boolean;
    has(actionName: string): boolean;
    get(actionName: string): import('./IAction').IAction;
    cancel(action: import('./IAction').IAction): boolean;
    cancel(actionName: string): boolean;
    clear(): void;
    chain(): import('./IActionChain').IActionChain;
    printQueue(): string;

}
