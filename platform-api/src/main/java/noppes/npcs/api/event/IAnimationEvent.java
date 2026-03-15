package noppes.npcs.api.event;

import noppes.npcs.api.Cancelable;
import noppes.npcs.api.entity.IAnimatable;
import noppes.npcs.api.handler.data.IAnimation;
import noppes.npcs.api.handler.data.IAnimationData;
import noppes.npcs.api.handler.data.IFrame;

/**
 * Events fired during entity animation playback.
 */
public interface IAnimationEvent extends ICustomNPCsEvent {

    /** @return the animation being played. */
    IAnimation getAnimation();

    /** @return the animation data managing the playback. */
    IAnimationData getAnimationData();

    /** @return the animatable entity playing the animation. */
    IAnimatable getEntity();

    /**
     * Fired when an animation starts playing. Cancelable.
     * @hookName animationStart
     */
    @Cancelable
    interface Started extends IAnimationEvent {

    }

    /**
     * Fired when an animation finishes playing.
     * @hookName animationEnd
     */
    interface Ended extends IAnimationEvent {

    }

    /** Events fired when animation frames are entered or exited. */
    interface IFrameEvent extends IAnimationEvent {

        /** @return the frame index. */
        int getIndex();

        /** @return the frame data. */
        IFrame getFrame();

        /**
         * @hookName frameEnter
         */
        interface Entered extends IFrameEvent {

        }

        /**
         * @hookName frameExit
         */
        interface Exited extends IFrameEvent {

        }
    }
}
