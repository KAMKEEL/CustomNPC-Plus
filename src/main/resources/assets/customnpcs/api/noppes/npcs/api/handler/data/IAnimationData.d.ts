/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface IAnimationData {

    // Methods
    getEntity(): import('../../entity/IAnimatable').IAnimatable;
    updateClient(): void;
    isActive(): boolean;
    isClientAnimating(): boolean;
    setEnabled(enabled: boolean): void;
    enabled(): boolean;
    setAnimation(animation: import('./IAnimation').IAnimation): void;
    getAnimation(): import('./IAnimation').IAnimation;
    getAnimatingTime(): number;

}
