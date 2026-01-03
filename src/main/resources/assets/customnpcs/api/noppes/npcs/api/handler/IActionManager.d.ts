/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

export interface IActionManager {

    // Methods
    start(): import('./IActionManager').IActionManager;
    stop(): import('./IActionManager').IActionManager;
    create(name: string, maxDuration: number, delay: number, action: Consumer<IAction): import('./data/IAction').IAction;
    create(maxDuration: number, delay: number, task: Consumer<IAction): import('./data/IAction').IAction;
    create(name: string, delay: number, t: Consumer<IAction): import('./data/IAction').IAction;
    create(delay: number, t: Consumer<IAction): import('./data/IAction').IAction;
    create(name: string, t: Consumer<IAction): import('./data/IAction').IAction;
    getName(): string;
    setName(name: string): import('./IActionManager').IActionManager;
    inDebugMode(): boolean;
    setDebugMode(debug: boolean): import('./IActionManager').IActionManager;
    create(name: string): import('./data/IAction').IAction;
    create(t: Consumer<IAction): import('./data/IAction').IAction;
    create(condition: Function<IAction, Boolean, task: Consumer<IAction): import('./data/actions/IConditionalAction').IConditionalAction;
    create(name: string, condition: Function<IAction, Boolean, task: Consumer<IAction): import('./data/actions/IConditionalAction').IConditionalAction;
    create(condition: Function<IAction, Boolean, task: Consumer<IAction, terminateWhen: Function<IAction, Boolean): import('./data/actions/IConditionalAction').IConditionalAction;
    create(name: string, condition: Function<IAction, Boolean, task: Consumer<IAction, terminateWhen: Function<IAction, Boolean): import('./data/actions/IConditionalAction').IConditionalAction;
    create(condition: Function<IAction, Boolean, task: Consumer<IAction, terminateWhen: Function<IAction, Boolean, onTermination: Consumer<IAction): import('./data/actions/IConditionalAction').IConditionalAction;
    create(name: string, condition: Function<IAction, Boolean, task: Consumer<IAction, terminateWhen: Function<IAction, Boolean, onTermination: Consumer<IAction): import('./data/actions/IConditionalAction').IConditionalAction;
    createQueue(name: string): import('./data/IActionQueue').IActionQueue;
    createQueue(name: string, isParallel: boolean): import('./data/IActionQueue').IActionQueue;
    getOrCreateQueue(name: string): import('./data/IActionQueue').IActionQueue;
    getOrCreateQueue(name: string, isParallel: boolean): import('./data/IActionQueue').IActionQueue;
    getQueue(name: string): import('./data/IActionQueue').IActionQueue;
    hasQueue(name: string): boolean;
    removeQueue(name: string): boolean;
    getSequentialQueue(): import('./data/IActionQueue').IActionQueue;
    schedule(action: import('./data/IAction').IAction): import('./data/IAction').IAction;
    schedule(actions: ): void;
    schedule(task: Consumer<IAction): import('./data/IAction').IAction;
    schedule(tasks: ): void;
    schedule(delay: number, task: Consumer<IAction): import('./data/IAction').IAction;
    schedule(name: string, task: Consumer<IAction): import('./data/IAction').IAction;
    schedule(name: string, delay: number, task: Consumer<IAction): import('./data/IAction').IAction;
    schedule(name: string, maxDuration: number, delay: number, task: Consumer<IAction): import('./data/IAction').IAction;
    schedule(maxDuration: number, delay: number, task: Consumer<IAction): import('./data/IAction').IAction;
    scheduleActionAt(index: number, action: import('./data/IAction').IAction): import('./data/IAction').IAction;
    getConditionalQueue(): import('./data/IActionQueue').IActionQueue;
    schedule(action: import('./data/actions/IConditionalAction').IConditionalAction): import('./data/actions/IConditionalAction').IConditionalAction;
    schedule(actions: ): void;
    schedule(condition: Function<IAction, Boolean, task: Consumer<IAction): import('./data/actions/IConditionalAction').IConditionalAction;
    schedule(condition: Function<IAction, Boolean, task: Consumer<IAction, terminateWhen: Function<IAction, Boolean): import('./data/actions/IConditionalAction').IConditionalAction;
    schedule(condition: Function<IAction, Boolean, task: Consumer<IAction, terminateWhen: Function<IAction, Boolean, onTermination: Consumer<IAction): import('./data/actions/IConditionalAction').IConditionalAction;
    schedule(name: string, condition: Function<IAction, Boolean, task: Consumer<IAction): import('./data/actions/IConditionalAction').IConditionalAction;
    schedule(name: string, condition: Function<IAction, Boolean, task: Consumer<IAction, terminateWhen: Function<IAction, Boolean): import('./data/actions/IConditionalAction').IConditionalAction;
    schedule(name: string, condition: Function<IAction, Boolean, task: Consumer<IAction, terminateWhen: Function<IAction, Boolean, onTermination: Consumer<IAction): import('./data/actions/IConditionalAction').IConditionalAction;
    getParallelQueue(): import('./data/IActionQueue').IActionQueue;
    scheduleParallel(action: import('./data/IAction').IAction): import('./data/IAction').IAction;
    scheduleParallel(actions: ): void;
    scheduleParallel(task: Consumer<IAction): import('./data/IAction').IAction;
    scheduleParallel(tasks: ): void;
    scheduleParallel(delay: number, task: Consumer<IAction): import('./data/IAction').IAction;
    scheduleParallel(maxDuration: number, delay: number, task: Consumer<IAction): import('./data/IAction').IAction;
    scheduleParallel(name: string, task: Consumer<IAction): import('./data/IAction').IAction;
    scheduleParallel(name: string, delay: number, task: Consumer<IAction): import('./data/IAction').IAction;
    scheduleParallel(name: string, maxDuration: number, delay: number, task: Consumer<IAction): import('./data/IAction').IAction;
    getAllQueues(): import('./data/IActionQueue').IActionQueue[];
    hasAny(name: string): boolean;
    getAny(name: string): import('./data/IAction').IAction;
    cancelAny(name: string): boolean;
    clear(): void;
    chain(): import('./data/IActionChain').IActionChain;
    parallelChain(): import('./data/IActionChain').IActionChain;
    printQueues(): string;

}
