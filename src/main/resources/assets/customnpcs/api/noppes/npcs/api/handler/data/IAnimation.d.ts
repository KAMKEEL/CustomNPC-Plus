/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface IAnimation {

    // Methods
    getParent(): import('./IAnimationData').IAnimationData;
    currentFrame(): import('./IFrame').IFrame;
    getFrames(): import('./IFrame').IFrame[];
    setFrames(frames: import('./IFrame').IFrame[]): import('./IAnimation').IAnimation;
    clearFrames(): import('./IAnimation').IAnimation;
    addFrame(frame: import('./IFrame').IFrame): import('./IAnimation').IAnimation;
    addFrame(index: number, frame: import('./IFrame').IFrame): import('./IAnimation').IAnimation;
    removeFrame(frame: import('./IFrame').IFrame): import('./IAnimation').IAnimation;
    setName(name: string): import('./IAnimation').IAnimation;
    getName(): string;
    setSpeed(speed: number): import('./IAnimation').IAnimation;
    getSpeed(): number;
    setSmooth(smooth: byte): import('./IAnimation').IAnimation;
    isSmooth(): byte;
    doWhileStanding(whileStanding: boolean): import('./IAnimation').IAnimation;
    doWhileStanding(): boolean;
    doWhileMoving(whileMoving: boolean): import('./IAnimation').IAnimation;
    doWhileMoving(): boolean;
    doWhileAttacking(whileAttacking: boolean): import('./IAnimation').IAnimation;
    doWhileAttacking(): boolean;
    setLoop(loopAtFrame: number): import('./IAnimation').IAnimation;
    loop(): number;
    save(): import('./IAnimation').IAnimation;
    getID(): number;
    setID(id: number): void;
    getTotalTime(): number;

}
