/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

export interface IAnimationEvent extends ICustomNPCsEvent {

    // Methods
    getAnimation(): import('../handler/data/IAnimation').IAnimation;
    getAnimationData(): import('../handler/data/IAnimationData').IAnimationData;
    getEntity(): import('../entity/IAnimatable').IAnimatable;

    // Nested interfaces
    interface Started extends IAnimationEvent {
    }
    interface Ended extends IAnimationEvent {
    }
    interface IFrameEvent extends IAnimationEvent {
        getIndex(): number;
        getFrame(): import('../handler/data/IFrame').IFrame;
    }
    interface Entered extends IFrameEvent {
    }
    interface Exited extends IFrameEvent {
    }

}
