package noppes.npcs.controllers.data;

import noppes.npcs.api.handler.data.IFramePart;
import noppes.npcs.constants.EnumAnimationPart;
import noppes.npcs.platform.nbt.INBTCompound;
import noppes.npcs.core.NBT;

public class FramePart implements IFramePart {
    public boolean paused = false;
    public static float renderPartialTick = 0;

    public EnumAnimationPart part;
    public float[] rotation = {0, 0, 0};
    public float[] pivot = {0, 0, 0};

    boolean customized = false;

    public float speed = 1.0F;
    //0 - Interpolated, 1 - Linear, 2 - None
    public byte smooth = 0;

    //Client-sided fields (unsaved)
    public float[] prevRotations = {0, 0, 0};
    public float[] prevPivots = {0, 0, 0};
    public float partialRotationTick;
    public float partialPivotTick;

    public FramePart() {
    }

    public FramePart(EnumAnimationPart part) {
        this.part = part;
    }

    public EnumAnimationPart getPart() {
        return part;
    }

    public String getName() {
        return part.name();
    }

    public int getPartId() {
        return part.ordinal();
    }

    public void setPart(EnumAnimationPart part) {
        this.part = part;
    }

    public IFramePart setPart(String name) {
        try {
            this.setPart(EnumAnimationPart.valueOf(name));
        } catch (IllegalArgumentException ignored) {
        }
        return this;
    }

    public IFramePart setPart(int partId) {
        for (EnumAnimationPart enumPart : EnumAnimationPart.values()) {
            if (enumPart.ordinal() == partId) {
                this.setPart(enumPart);
                break;
            }
        }
        return this;
    }

    public float[] getRotations() {
        return rotation;
    }

    public IFramePart setRotations(float[] rotation) {
        this.rotation = rotation;
        return this;
    }

    public float[] getPivots() {
        return pivot;
    }

    public IFramePart setPivots(float[] pivot) {
        this.pivot = pivot;
        return this;
    }

    public boolean isCustomized() {
        return customized;
    }

    public IFramePart setCustomized(boolean customized) {
        this.customized = customized;
        return this;
    }

    public float getSpeed() {
        return speed;
    }

    public IFramePart setSpeed(float speed) {
        this.speed = speed;
        return this;
    }

    public byte isSmooth() {
        return smooth;
    }

    public IFramePart setSmooth(byte smooth) {
        this.smooth = smooth;
        return this;
    }

    public void readFromNBT(INBTCompound compound) {
        part = EnumAnimationPart.valueOf(compound.getString("Part"));
        for (int i = 0; i < 3; i++) {
            rotation[i] = compound.getFloat("Rotation" + i);
        }
        for (int i = 0; i < 3; i++) {
            pivot[i] = compound.getFloat("Pivot" + i);
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
    }

    public INBTCompound writeToNBT() {
        INBTCompound compound = NBT.compound();
        compound.setString("Part", part.toString());
        for (int i = 0; i < 3; i++) {
            compound.setFloat("Rotation" + i, rotation[i]);
        }
        for (int i = 0; i < 3; i++) {
            compound.setFloat("Pivot" + i, pivot[i]);
        }

        if (customized) {
            compound.setFloat("Speed", speed);
            compound.setByte("Smooth", smooth);
        }

        return compound;
    }

    public FramePart copy() {
        FramePart part = new FramePart(this.part);
        part.rotation = new float[]{this.rotation[0], this.rotation[1], this.rotation[2]};
        part.pivot = new float[]{this.pivot[0], this.pivot[1], this.pivot[2]};
        part.customized = this.customized;
        part.speed = this.speed;
        part.smooth = this.smooth;
        return part;
    }

    public void interpolateAngles() {
        if (paused)
            return;

        float pi = (float) Math.PI / 180;
        if (this.smooth == 2) {
            this.prevRotations[0] = this.rotation[0] * pi;
            this.prevRotations[1] = this.rotation[1] * pi;
            this.prevRotations[2] = this.rotation[2] * pi;
        } else if (this.partialRotationTick != FramePart.renderPartialTick) {
            this.partialRotationTick = FramePart.renderPartialTick;
            if (this.smooth == 0) {
                this.prevRotations[0] = (this.rotation[0] * pi - this.prevRotations[0]) * Math.abs(this.speed) / 10f + this.prevRotations[0];
                this.prevRotations[1] = (this.rotation[1] * pi - this.prevRotations[1]) * Math.abs(this.speed) / 10f + this.prevRotations[1];
                this.prevRotations[2] = (this.rotation[2] * pi - this.prevRotations[2]) * Math.abs(this.speed) / 10f + this.prevRotations[2];
            } else {
                int directionX = Float.compare(this.rotation[0] * pi, this.prevRotations[0]);
                this.prevRotations[0] += directionX * this.speed / 10f;
                this.prevRotations[0] = directionX == 1 ?
                    Math.min(this.rotation[0] * pi, this.prevRotations[0]) : Math.max(this.rotation[0] * pi, this.prevRotations[0]);
                int directionY = Float.compare(this.rotation[1] * pi, this.prevRotations[1]);
                this.prevRotations[1] += directionY * this.speed / 10f;
                this.prevRotations[1] = directionY == 1 ?
                    Math.min(this.rotation[1] * pi, this.prevRotations[1]) : Math.max(this.rotation[1] * pi, this.prevRotations[1]);
                int directionZ = Float.compare(this.rotation[2] * pi, this.prevRotations[2]);
                this.prevRotations[2] += directionZ * this.speed / 10f;
                this.prevRotations[2] = directionZ == 1 ?
                    Math.min(this.rotation[2] * pi, this.prevRotations[2]) : Math.max(this.rotation[2] * pi, this.prevRotations[2]);
            }
        }
    }

    public void interpolateOffset() {
        if (paused)
            return;

        if (this.smooth == 2) {
            this.prevPivots[0] = this.pivot[0];
            this.prevPivots[1] = this.pivot[1];
            this.prevPivots[2] = this.pivot[2];
        } else if (this.partialPivotTick != FramePart.renderPartialTick) {
            this.partialPivotTick = FramePart.renderPartialTick;
            if (this.smooth == 0) {
                this.prevPivots[0] = (this.pivot[0] - this.prevPivots[0]) * Math.abs(this.speed) / 10f + this.prevPivots[0];
                this.prevPivots[1] = (this.pivot[1] - this.prevPivots[1]) * Math.abs(this.speed) / 10f + this.prevPivots[1];
                this.prevPivots[2] = (this.pivot[2] - this.prevPivots[2]) * Math.abs(this.speed) / 10f + this.prevPivots[2];
            } else {
                int directionX = Float.compare(this.pivot[0], this.prevPivots[0]);
                this.prevPivots[0] += directionX * this.speed / 10f;
                this.prevPivots[0] = directionX == 1 ?
                    Math.min(this.pivot[0], this.prevPivots[0]) : Math.max(this.pivot[0], this.prevPivots[0]);
                int directionY = Float.compare(this.pivot[1], this.prevPivots[1]);
                this.prevPivots[1] += directionY * this.speed / 10f;
                this.prevPivots[1] = directionY == 1 ?
                    Math.min(this.pivot[1], this.prevPivots[1]) : Math.max(this.pivot[1], this.prevPivots[1]);
                int directionZ = Float.compare(this.pivot[2], this.prevPivots[2]);
                this.prevPivots[2] += directionZ * this.speed / 10f;
                this.prevPivots[2] = directionZ == 1 ?
                    Math.min(this.pivot[2], this.prevPivots[2]) : Math.max(this.pivot[2], this.prevPivots[2]);
            }
        }
    }

    public void jumpToCurrentFrame() {
        this.partialRotationTick = FramePart.renderPartialTick;
        this.partialPivotTick = FramePart.renderPartialTick;

        this.prevPivots[0] = this.pivot[0];
        this.prevPivots[1] = this.pivot[1];
        this.prevPivots[2] = this.pivot[2];

        float pi = (float) Math.PI / 180;
        this.prevRotations[0] = this.rotation[0] * pi;
        this.prevRotations[1] = this.rotation[1] * pi;
        this.prevRotations[2] = this.rotation[2] * pi;
    }
}
