/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

/**
 * Events fired during entity animation playback.
  * @javaFqn noppes.npcs.api.event.IAnimationEvent
*/
export interface IAnimationEvent extends import('./ICustomNPCsEvent').ICustomNPCsEvent {
    /** @return the animation being played. */
    getAnimation(): import('../handler/data/IAnimation').IAnimation;
    /** @return the animation data managing the playback. */
    getAnimationData(): import('../handler/data/IAnimationData').IAnimationData;
    /** @return the animatable entity playing the animation. */
    getEntity(): import('../entity/IAnimatable').IAnimatable;
}

export namespace IAnimationEvent {
    
    
    
    /**
     * Fired when an animation starts playing. Cancelable.
     * @hookName animationStart
          * @javaFqn noppes.npcs.api.event.IAnimationEvent.Started
*/
    export interface Started extends IAnimationEvent {
    }
    /**
     * Fired when an animation finishes playing.
     * @hookName animationEnd
          * @javaFqn noppes.npcs.api.event.IAnimationEvent.Ended
*/
    export interface Ended extends IAnimationEvent {
    }
    /** Events fired when animation frames are entered or exited.      * @javaFqn noppes.npcs.api.event.IAnimationEvent.IFrameEvent
*/
    export interface IFrameEvent extends IAnimationEvent {
        /** @return the frame index. */
        getIndex(): import('./int').int;
        /** @return the frame data. */
        getFrame(): import('../handler/data/IFrame').IFrame;
    }
    export namespace IFrameEvent {
        
        
        /**
         * @hookName frameEnter
                  * @javaFqn noppes.npcs.api.event.IAnimationEvent.IFrameEvent.Entered
*/
        export interface Entered extends IFrameEvent {
        }
        /**
         * @hookName frameExit
                  * @javaFqn noppes.npcs.api.event.IAnimationEvent.IFrameEvent.Exited
*/
        export interface Exited extends IFrameEvent {
        }
    }
}
