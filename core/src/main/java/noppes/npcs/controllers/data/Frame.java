package noppes.npcs.controllers.data;

import noppes.npcs.api.handler.data.IFrame;
import noppes.npcs.api.handler.data.IFramePart;
import noppes.npcs.constants.EnumAnimationPart;
import noppes.npcs.platform.nbt.INBTCompound;
import noppes.npcs.platform.nbt.INBTList;
import noppes.npcs.core.NBT;

import java.util.HashMap;
import java.util.Map;


public class Frame implements IFrame {
    public float parentSpeed = 1.0f;
    public byte parentSmooth = 0;

    public HashMap<EnumAnimationPart, FramePart> frameParts = new HashMap<>();
    public int duration = 0;

    boolean customized = false;
    public float speed = 1.0F;
    public byte smooth = 0;

    private int colorMarker = 0xFFFFFF;
    private String comment = "";

    public Frame() {
    }

    public Frame(int duration) {
        this.duration = duration;
    }

    public Frame(int duration, float speed, byte smooth) {
        this.duration = duration;
        this.speed = speed;
        this.smooth = smooth;
        this.customized = true;
    }

    public HashMap<EnumAnimationPart, FramePart> getFrameMap() {
        return frameParts;
    }

    public IFramePart[] getParts() {
        return frameParts.values().toArray(new IFramePart[0]);
    }

    public IFrame addPart(IFramePart partConfig) {
        this.frameParts.put(((FramePart) partConfig).getPart(), (FramePart) partConfig);
        return this;
    }

    public IFrame removePart(String partName) {
        try {
            this.frameParts.remove(EnumAnimationPart.valueOf(partName));
        } catch (IllegalArgumentException ignored) {
        }
        return this;
    }

    public IFrame removePart(int partId) {
        for (EnumAnimationPart part : EnumAnimationPart.values()) {
            this.frameParts.remove(part);
        }
        return this;
    }

    public IFrame clearParts() {
        frameParts.clear();
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public IFrame setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public boolean isCustomized() {
        return customized;
    }

    public IFrame setCustomized(boolean customized) {
        this.customized = customized;
        return this;
    }

    public float getSpeed() {
        return speed;
    }

    public IFrame setSpeed(float speed) {
        this.speed = speed;
        return this;
    }

    public byte smoothType() {
        return smooth;
    }

    public IFrame setSmooth(byte smooth) {
        this.smooth = smooth;
        return this;
    }

    public int getColorMarker() {
        return this.colorMarker;
    }

    public void setColorMarker(int color) {
        this.colorMarker = color;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void readFromNBT(INBTCompound compound) {
        duration = compound.getInteger("Duration");
        if (compound.hasKey("ColorMarker")) {
            this.setColorMarker(compound.getInteger("ColorMarker"));
        }
        if (compound.hasKey("Comment")) {
            this.comment = compound.getString("Comment");
        }

        // Customized = TRUE if Speed or Smooth Exist
        if (compound.hasKey("Speed")) {
            customized = true;
            speed = compound.getFloat("Speed");
        }
        if (compound.hasKey("Smooth")) {
            customized = true;
            smooth = compound.getByte("Smooth");
        }

        if (!customized) {
            this.speed = parentSpeed;
            this.smooth = parentSmooth;
        }

        HashMap<EnumAnimationPart, FramePart> frameParts = new HashMap<>();
        INBTList list = compound.getList("FrameParts", 10);
        for (int i = 0; i < list.size(); i++) {
            INBTCompound item = list.getCompound(i);
            FramePart framePart = new FramePart();
            framePart.readFromNBT(item);
            if (!framePart.customized) {
                framePart.smooth = this.smooth;
                framePart.speed = this.speed;
            }
            frameParts.put(framePart.part, framePart);
        }
        this.frameParts = frameParts;
    }

    public INBTCompound writeToNBT() {
        INBTCompound compound = NBT.compound();
        compound.setInteger("Duration", duration);
        compound.setInteger("ColorMarker", this.colorMarker);
        compound.setString("Comment", this.comment);

        if (customized) {
            compound.setFloat("Speed", speed);
            compound.setByte("Smooth", smooth);
        }

        INBTList list = NBT.list();
        for (FramePart framePart : frameParts.values()) {
            INBTCompound item = framePart.writeToNBT();
            list.addCompound(item);
        }
        compound.setList("FrameParts", list);
        return compound;
    }

    public Frame copy() {
        Frame frame = new Frame(this.duration);
        HashMap<EnumAnimationPart, FramePart> frameParts = this.frameParts;
        for (Map.Entry<EnumAnimationPart, FramePart> entry : frameParts.entrySet()) {
            frame.frameParts.put(entry.getKey(), entry.getValue().copy());
        }
        frame.parentSpeed = this.parentSpeed;
        frame.parentSmooth = this.parentSmooth;
        frame.duration = this.duration;
        frame.customized = this.customized;
        frame.speed = this.speed;
        frame.smooth = this.smooth;
        frame.colorMarker = this.colorMarker;
        return frame;
    }
}
