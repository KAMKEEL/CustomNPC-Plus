/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface ICustomEffect {

    // Methods
    getID(): number;
    getName(): string;
    setMenuName(name: string): void;
    getMenuName(): string;
    setName(name: string): void;
    getIcon(): string;
    setIcon(icon: string): void;
    getEveryXTick(): number;
    setEveryXTick(everyXTick: number): void;
    getIconX(): number;
    setIconX(iconX: number): void;
    getIconY(): number;
    setIconY(iconY: number): void;
    getWidth(): number;
    setWidth(width: number): void;
    getHeight(): number;
    setHeight(height: number): void;
    isLossOnDeath(): boolean;
    setLossOnDeath(lossOnDeath: boolean): void;
    save(): import('./ICustomEffect').ICustomEffect;
    setID(id: number): void;
    getIndex(): number;

}
