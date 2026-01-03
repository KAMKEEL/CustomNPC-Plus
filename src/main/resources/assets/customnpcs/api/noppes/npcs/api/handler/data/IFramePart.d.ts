/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface IFramePart {

    // Methods
    getName(): string;
    getPartId(): number;
    setPart(name: string): import('./IFramePart').IFramePart;
    setPart(partId: number): import('./IFramePart').IFramePart;
    getRotations(): number[];
    setRotations(rotation: number[]): import('./IFramePart').IFramePart;
    getPivots(): number[];
    setPivots(pivot: number[]): import('./IFramePart').IFramePart;
    isCustomized(): boolean;
    setCustomized(customized: boolean): import('./IFramePart').IFramePart;
    getSpeed(): number;
    setSpeed(speed: number): import('./IFramePart').IFramePart;
    isSmooth(): byte;
    setSmooth(smooth: byte): import('./IFramePart').IFramePart;

}
