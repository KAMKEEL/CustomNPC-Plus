/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface IAction {

    // Methods
    getQueue(): import('./IActionQueue').IActionQueue;
    setQueue(queue: import('./IActionQueue').IActionQueue): import('./IAction').IAction;
    setTask(task: Consumer<IAction): import('./IAction').IAction;
    getManager(): import('../IActionManager').IActionManager;
    isScheduled(): boolean;
    getCount(): number;
    onStart(task: Consumer<IAction): import('./IAction').IAction;
    onDone(task: Consumer<IAction): import('./IAction').IAction;
    getDuration(): number;
    getName(): string;
    getMaxDuration(): number;
    setMaxDuration(ticks: number): import('./IAction').IAction;
    getMaxCount(): number;
    times(n: number): import('./IAction').IAction;
    once(): import('./IAction').IAction;
    markDone(): void;
    isDone(): boolean;
    kill(): void;
    getData(key: string): any;
    setData(key: string, value: any): import('./IAction').IAction;
    removeData(key: string): import('./IAction').IAction;
    copyDataTo(copyTo: import('./IAction').IAction): import('./IAction').IAction;
    printData(): string;
    hasData(key: string): boolean;
    getUpdateEvery(): number;
    updateEvery(ticks: number): import('./IAction').IAction;
    everyTick(): import('./IAction').IAction;
    everySecond(): import('./IAction').IAction;
    getStartAfterTicks(): number;
    pauseFor(ticks: number): import('./IAction').IAction;
    pauseFor(millis: number): import('./IAction').IAction;
    pause(): void;
    pauseUntil(until: Function<IAction,Boolean): void;
    resume(): void;
    isPaused(): boolean;
    getIdentifier(): string;
    threadify(): import('./IAction').IAction;
    start(): import('./IAction').IAction;
    getNext(): import('./IAction').IAction;
    getPrevious(): import('./IAction').IAction;
    after(after: import('./IAction').IAction): import('./IAction').IAction;
    after(actions: ): void;
    after(tasks: ): void;
    after(name: string, maxDuration: number, delay: number, t: Consumer<IAction): import('./IAction').IAction;
    after(name: string, delay: number, t: Consumer<IAction): import('./IAction').IAction;
    after(delay: number, t: Consumer<IAction): import('./IAction').IAction;
    after(name: string, t: Consumer<IAction): import('./IAction').IAction;
    after(t: Consumer<IAction): import('./IAction').IAction;
    before(before: import('./IAction').IAction): import('./IAction').IAction;
    before(name: string, maxDuration: number, delay: number, t: Consumer<IAction): import('./IAction').IAction;
    before(name: string, delay: number, t: Consumer<IAction): import('./IAction').IAction;
    before(delay: number, t: Consumer<IAction): import('./IAction').IAction;
    before(name: string, t: Consumer<IAction): import('./IAction').IAction;
    before(t: Consumer<IAction): import('./IAction').IAction;
    conditional(after: import('./actions/IConditionalAction').IConditionalAction): import('./actions/IConditionalAction').IConditionalAction;
    conditional(actions: ): void;
    conditional(condition: Function<IAction,Boolean, task: Consumer<IAction): import('./actions/IConditionalAction').IConditionalAction;
    conditional(name: string, condition: Function<IAction,Boolean, task: Consumer<IAction): import('./actions/IConditionalAction').IConditionalAction;
    conditional(condition: Function<IAction,Boolean, task: Consumer<IAction, terminate: Function<IAction,Boolean): import('./actions/IConditionalAction').IConditionalAction;
    conditional(name: string, condition: Function<IAction,Boolean, task: Consumer<IAction, terminate: Function<IAction,Boolean): import('./actions/IConditionalAction').IConditionalAction;
    conditional(condition: Function<IAction,Boolean, task: Consumer<IAction, terminateWhen: Function<IAction,Boolean, onTermination: Consumer<IAction): import('./actions/IConditionalAction').IConditionalAction;
    conditional(name: string, condition: Function<IAction,Boolean, task: Consumer<IAction, terminateWhen: Function<IAction,Boolean, onTermination: Consumer<IAction): import('./actions/IConditionalAction').IConditionalAction;
    parallel(after: import('./IAction').IAction): import('./IAction').IAction;
    parallel(actions: ): void;
    parallel(task: Consumer<IAction): import('./IAction').IAction;
    parallel(tasks: ): void;
    parallel(delay: number, task: Consumer<IAction): import('./IAction').IAction;
    parallel(name: string, task: Consumer<IAction): import('./IAction').IAction;
    parallel(name: string, startAfterTicks: number, task: Consumer<IAction): import('./IAction').IAction;
    parallel(name: string, maxDuration: number, delay: number, t: Consumer<IAction): import('./IAction').IAction;

}
