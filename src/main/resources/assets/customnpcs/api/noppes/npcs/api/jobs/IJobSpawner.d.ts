/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.jobs
 */

export interface IJobSpawner extends IJob {

    // Methods
    setEntity(number: number, entityLivingBase: IEntityLivingBase<?): void;
    removeAllSpawned(): void;
    getNearbySpawned(): [];
    hasPixelmon(): boolean;
    isEmpty(): boolean;
    isOnCooldown(player: IPlayer<EntityPlayerMP): boolean;

}
