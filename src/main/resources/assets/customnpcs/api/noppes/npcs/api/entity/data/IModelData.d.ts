/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity.data
 */

export interface IModelData {

    // Methods
    headWear(config: byte): void;
    headWear(): byte;
    bodyWear(config: byte): void;
    bodyWear(): byte;
    rightArmWear(config: byte): void;
    rightArmWear(): byte;
    leftArmWear(config: byte): void;
    leftArmWear(): byte;
    rightLegWear(config: byte): void;
    rightLegWear(): byte;
    leftLegWear(config: byte): void;
    leftLegWear(): byte;
    hidePart(part: number, hide: byte): void;
    hidden(part: number): number;
    enableRotation(enableRotation: boolean): void;
    enableRotation(): boolean;
    getRotation(): import('./IModelRotate').IModelRotate;
    getScale(): import('./IModelScale').IModelScale;
    setEntity(string: string): void;
    getEntity(): string;

}
