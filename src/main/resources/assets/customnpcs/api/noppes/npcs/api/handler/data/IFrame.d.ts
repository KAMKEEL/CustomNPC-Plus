/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface IFrame {

    // Methods
    getParts(): import('./IFramePart').IFramePart[];
    addPart(partConfig: import('./IFramePart').IFramePart): import('./IFrame').IFrame;
    removePart(partName: string): import('./IFrame').IFrame;
    removePart(partId: number): import('./IFrame').IFrame;
    clearParts(): import('./IFrame').IFrame;
    getDuration(): number;
    setDuration(duration: number): import('./IFrame').IFrame;
    isCustomized(): boolean;
    setCustomized(customized: boolean): import('./IFrame').IFrame;
    getSpeed(): number;
    setSpeed(speed: number): import('./IFrame').IFrame;
    smoothType(): byte;
    setSmooth(smooth: byte): import('./IFrame').IFrame;

}
