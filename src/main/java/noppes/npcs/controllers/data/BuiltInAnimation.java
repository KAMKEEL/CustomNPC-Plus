package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.handler.data.IAnimation;

/**
 * A read-only animation loaded from mod assets.
 * Built-in animations are identified by name only, never by ID.
 * They cannot be saved, deleted, or modified.
 */
public class BuiltInAnimation extends Animation {

    public BuiltInAnimation() {
        super();
        this.id = -1; // No valid ID for built-in animations
    }

    public BuiltInAnimation(String name) {
        super();
        this.name = name;
        this.id = -1;
    }

    /**
     * Built-in animations cannot be saved.
     * @return this animation unchanged
     */
    @Override
    public IAnimation save() {
        // Do nothing - built-in animations are read-only
        return this;
    }

    /**
     * Built-in animations have no valid ID.
     * @return always -1
     */
    @Override
    public int getID() {
        return -1;
    }

    /**
     * Built-in animations cannot have their ID changed.
     */
    @Override
    public void setID(int newID) {
        // Ignore - built-in animations don't use IDs
    }

    /**
     * Check if this is a built-in animation.
     * @return always true for BuiltInAnimation
     */
    public boolean isBuiltIn() {
        return true;
    }

    /**
     * Read animation data from NBT.
     * Preserves the name set in constructor (from filename) rather than reading from NBT.
     */
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        // Preserve the name set in constructor - don't read from NBT
        // (filename is the source of truth for built-in animations)
        String preservedName = this.name;

        speed = compound.getFloat("Speed");
        smooth = compound.getByte("Smooth");
        loop = compound.getInteger("Loop");

        frames.clear();
        net.minecraft.nbt.NBTTagList list = compound.getTagList("Frames", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound item = list.getCompoundTagAt(i);
            Frame frame = new Frame();
            frame.parent = this;
            frame.readFromNBT(item);
            frames.add(frame);
        }

        this.whileStanding = compound.getBoolean("WhileStanding");
        this.whileMoving = compound.getBoolean("WhileWalking");
        this.whileAttacking = compound.getBoolean("WhileAttacking");

        this.currentFrame = compound.getInteger("CurrentFrame");
        this.currentFrameTime = compound.getInteger("CurrentFrameTime");

        // Ensure name and ID are preserved
        this.name = preservedName;
        this.id = -1;
    }
}
