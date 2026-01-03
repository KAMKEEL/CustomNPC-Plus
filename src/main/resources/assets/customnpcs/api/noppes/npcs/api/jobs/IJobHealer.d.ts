/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.jobs
 */

export interface IJobHealer extends IJob {

    // Methods
    heal(entity: IEntityLivingBase, amount: number): void;
    setRange(range: number): void;
    getRange(): number;
    setSpeed(speed: number): void;
    getSpeed(): number;

}
