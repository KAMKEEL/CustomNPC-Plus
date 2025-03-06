package noppes.npcs.controllers.data;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.EventHooks;
import noppes.npcs.api.handler.data.IAnimation;
import noppes.npcs.api.handler.data.IAnimationData;
import noppes.npcs.api.handler.data.IFrame;
import noppes.npcs.constants.EnumAnimationPart;
import noppes.npcs.controllers.AnimationController;

import java.util.ArrayList;


public class Animation implements IAnimation {
    public AnimationData parent; //Client-sided only
    public int id = -1; // Only for internal usage

    public ArrayList<Frame> frames = new ArrayList<>();
    public int currentFrame = 0;
    public int currentFrameTime = 0;

    public String name = "";
    public float speed = 1.0F;
    public byte smooth = 0;
    public int loop = -1; //If greater than 0 and less than the amount of frames, the animation will begin looping when it reaches this frame.

    public boolean whileStanding = true;
    public boolean whileAttacking = true;
    public boolean whileMoving = true;

    //Client-sided
    public boolean paused;

    public Animation(){}

    public Animation(int id, String name){
        this.name = name;
        this.id = id;
    }

    public Animation(int id, String name, float speed, byte smooth){
        this.name = name;
        this.speed = speed;
        this.smooth = smooth;

        this.id = id;
    }

    public IAnimationData getParent() {
        return this.parent;
    }

    public IFrame currentFrame() {
        return currentFrame < frames.size() ? frames.get(currentFrame) : null;
    }

    public IFrame[] getFrames() {
        return frames.toArray(new Frame[0]);
    }

    public IAnimation setFrames(IFrame[] frames) {
        this.clearFrames();
        for (IFrame frame : frames) {
            this.frames.add((Frame) frame);
        }
        return this;
    }

    public IAnimation clearFrames() {
        this.frames.clear();
        return this;
    }

    public IAnimation addFrame(IFrame frame) {
        this.frames.add((Frame) frame);
        return this;
    }

    public IAnimation addFrame(int index, IFrame frame) {
        this.frames.add(index, (Frame) frame);
        return this;
    }

    public IAnimation removeFrame(IFrame frame) {
        this.frames.remove((Frame) frame);
        return this;
    }

    public IAnimation setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public IAnimation setSpeed(float speed) {
        this.speed = speed;
        return this;
    }

    public float getSpeed() {
        return this.speed;
    }

    public IAnimation setSmooth(byte smooth) {
        this.smooth = smooth;
        return this;
    }

    public byte isSmooth() {
        return this.smooth;
    }

    public IAnimation doWhileStanding(boolean whileStanding) {
        this.whileStanding = whileStanding;
        return this;
    }

    public boolean doWhileStanding() {
        return this.whileStanding;
    }

    public IAnimation doWhileMoving(boolean whileMoving) {
        this.whileMoving = whileMoving;
        return this;
    }

    public boolean doWhileMoving() {
        return this.whileMoving;
    }

    public IAnimation doWhileAttacking(boolean whileAttacking) {
        this.whileAttacking = whileAttacking;
        return this;
    }

    public boolean doWhileAttacking() {
        return this.whileAttacking;
    }

    public IAnimation setLoop(int loopAtFrame) {
        this.loop = loopAtFrame;
        return this;
    }

    public int loop() {
        return this.loop;
    }

    public IAnimation save() {
        return AnimationController.Instance.saveAnimation(this);
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void setID(int newID) {
        id = newID;
    }

    public long getTotalTime() {
        long time = 0;
        for (Frame frame : this.frames) {
            time += frame.duration;
        }
        return time;
    }

    public void readFromNBT(NBTTagCompound compound){
        if(compound.hasKey("ID")){
            id = compound.getInteger("ID");
        }
        else if (AnimationController.Instance != null) {
            id = AnimationController.Instance.getUnusedId();
        }

        name = compound.getString("Name");
        speed = compound.getFloat("Speed");
        smooth = compound.getByte("Smooth");
        loop = compound.getInteger("Loop");

        ArrayList<Frame> frames = new ArrayList<Frame>();
        NBTTagList list = compound.getTagList("Frames", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound item = list.getCompoundTagAt(i);
            Frame frame = new Frame();
            frame.parent = this;
            frame.readFromNBT(item);
            frames.add(frame);
        }
        this.frames = frames;

        this.whileStanding = compound.getBoolean("WhileStanding");
        this.whileMoving = compound.getBoolean("WhileWalking");
        this.whileAttacking = compound.getBoolean("WhileAttacking");

        this.currentFrame = compound.getInteger("CurrentFrame");
        this.currentFrameTime = compound.getInteger("CurrentFrameTime");
    }

    public NBTTagCompound writeToNBT(){
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("ID", id);
        compound.setString("Name", name);
        compound.setFloat("Speed", speed);
        compound.setByte("Smooth", smooth);
        compound.setInteger("Loop",loop);

        NBTTagList list = new NBTTagList();
        for(Frame frame : frames){
            NBTTagCompound item = frame.writeToNBT();
            list.appendTag(item);
        }
        compound.setTag("Frames", list);

        compound.setBoolean("WhileStanding", whileStanding);
        compound.setBoolean("WhileWalking", whileMoving);
        compound.setBoolean("WhileAttacking", whileAttacking);

        compound.setInteger("CurrentFrame", currentFrame);
        compound.setInteger("CurrentFrameTime", currentFrameTime);
        return compound;
    }

    public boolean increaseTime() {
        if (paused)
            return false;

        if (this.parent != null && this.currentFrame < this.frames.size()) {
            this.parent.finishedFrame = this.currentFrame;
        }

        this.currentFrameTime++;
        if (this.currentFrame() != null && this.currentFrameTime == this.currentFrame().getDuration()) {
            EventHooks.onAnimationFrameExited(this, this.currentFrame());

            Frame prevFrame = (Frame) this.currentFrame();
            Frame nextFrame = null;
            this.currentFrameTime = 0;
            this.currentFrame++;
            if (this.currentFrame < this.frames.size()) {
                nextFrame = this.frames.get(this.currentFrame);
            } else if (this.loop >= 0 && this.loop < this.frames.size()) {
                this.currentFrame = this.loop;
            }

            if (this.currentFrame() != null) {
                EventHooks.onAnimationFrameEntered(this, this.currentFrame());
            } else {
                EventHooks.onAnimationEnded(this);
            }

            if (nextFrame != null) {
                for (EnumAnimationPart part : EnumAnimationPart.values()) {
                    if (prevFrame.frameParts.containsKey(part) && nextFrame.frameParts.containsKey(part)) {
                        nextFrame.frameParts.get(part).prevRotations = prevFrame.frameParts.get(part).prevRotations;
                        nextFrame.frameParts.get(part).prevPivots = prevFrame.frameParts.get(part).prevPivots;
                    }
                }
            } else if (this.parent != null && this.currentFrame > this.loop) {
                this.parent.finishedTime = this.parent.getMCEntity().getAge();
            }
        }

        return true;
    }

    @SideOnly(Side.CLIENT)
    public void jumpToCurrentFrame() {
        this.jumpToFrameAtTime(this.currentFrame, 0);
    }

    @SideOnly(Side.CLIENT)
    public void jumpToFrameAtTime(int frameIndex, int time) {
        this.currentFrame = frameIndex;
        this.currentFrameTime = time;

        Frame frame = (Frame) this.currentFrame();
        if(frame != null){
            for (EnumAnimationPart part : EnumAnimationPart.values()) {
                if(part != null){
                    if (frame.frameParts.containsKey(part)) {
                        frame.frameParts.get(part).jumpToCurrentFrame();
                    }
                }
            }
        }
    }
}
