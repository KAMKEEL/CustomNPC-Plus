/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity.data
 */

export interface IModelRotate {

    // Methods
    whileStanding(): boolean;
    whileStanding(whileStanding: boolean): void;
    whileAttacking(): boolean;
    whileAttacking(whileAttacking: boolean): void;
    whileMoving(): boolean;
    whileMoving(whileMoving: boolean): void;
    getPart(part: number): import('./IModelRotatePart').IModelRotatePart;

}
