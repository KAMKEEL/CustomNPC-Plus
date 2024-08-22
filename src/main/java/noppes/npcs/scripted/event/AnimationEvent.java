package noppes.npcs.scripted.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.entity.IAnimatable;
import noppes.npcs.api.event.IAnimationEvent;
import noppes.npcs.api.handler.data.IAnimation;
import noppes.npcs.api.handler.data.IAnimationData;
import noppes.npcs.api.handler.data.IFrame;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.Frame;

public abstract class AnimationEvent extends CustomNPCsEvent implements IAnimationEvent {
    protected final IAnimation animation;
    protected final IAnimationData animationData;
    protected final IAnimatable entity;

    public AnimationEvent(IAnimation animation) {
        this.animation = animation;
        this.animationData = animation.getParent();
        this.entity = this.animationData.getEntity();
    }

    @Override
    public IAnimation getAnimation() {
        return this.animation;
    }

    @Override
    public IAnimationData getAnimationData() {
        return this.animationData;
    }

    @Override
    public IAnimatable getEntity() {
        return this.entity;
    }

    @Cancelable
    public static class Started extends AnimationEvent {

        public Started(IAnimation animation) {
            super(animation);
        }

        @Override
        public String getHookName() {
            return EnumScriptType.ANIMATION_START.function;
        }
    }

    public static class Ended extends AnimationEvent {

        public Ended(IAnimation animation) {
            super(animation);
        }

        @Override
        public String getHookName() {
            return EnumScriptType.ANIMATION_END.function;
        }
    }

    public static abstract class FrameEvent extends AnimationEvent implements IFrameEvent {
        private final IFrame frame;
        private final int index;

        public FrameEvent(IAnimation animation, IFrame frame) {
            super(animation);
            this.frame = frame;
            this.index = ((Animation)this.getAnimation()).frames.indexOf((Frame) this.frame);
        }

        @Override
        public int getIndex() {
            return this.index;
        }

        @Override
        public IFrame getFrame() {
            return this.frame;
        }

        public static class Entered extends FrameEvent {

            public Entered(IAnimation animation, IFrame frame) {
                super(animation, frame);
            }

            @Override
            public String getHookName() {
                return EnumScriptType.ANIMATION_FRAME_ENTER.function;
            }
        }

        public static class Exited extends FrameEvent {

            public Exited(IAnimation animation, IFrame frame) {
                super(animation, frame);
            }

            @Override
            public String getHookName() {
                return EnumScriptType.ANIMATION_FRAME_EXIT.function;
            }
        }
    }
}
