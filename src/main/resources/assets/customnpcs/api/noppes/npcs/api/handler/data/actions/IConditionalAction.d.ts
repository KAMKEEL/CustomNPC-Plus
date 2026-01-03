/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data.actions
 */

export interface IConditionalAction extends IAction {

    // Methods
    setCondition(condition: Function<IAction, Boolean): import('./IConditionalAction').IConditionalAction;
    terminateWhen(terminateWhen: Function<IAction, Boolean): import('./IConditionalAction').IConditionalAction;
    onTermination(onTermination: Consumer<IAction): import('./IConditionalAction').IConditionalAction;
    wasTaskExecuted(): boolean;
    getCheckCount(): number;
    getMaxChecks(): number;
    isTerminated(): boolean;
    setMaxChecks(maxChecks: number): import('./IConditionalAction').IConditionalAction;

}
