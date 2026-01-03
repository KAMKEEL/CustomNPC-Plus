/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface IActionChain {

    // Methods
    getName(): string;
    setName(name: string): import('./IActionChain').IActionChain;
    getQueue(): import('./IActionQueue').IActionQueue;
    after(delay: number, name: string, task: Consumer<IAction): import('./IActionChain').IActionChain;
    after(delay: number, task: Consumer<IAction): import('./IActionChain').IActionChain;
    start(): import('./IActionChain').IActionChain;

}
