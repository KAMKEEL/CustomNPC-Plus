package noppes.npcs.controllers.data;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.handler.data.IFramePart;
import noppes.npcs.client.ClientEventHandler;
import noppes.npcs.constants.EnumAnimationPart;
import noppes.npcs.constants.animation.EnumFrameType;
import noppes.npcs.util.Ease;
import noppes.npcs.util.ValueUtil;

import java.util.HashMap;
import java.util.Map;

public class FramePart implements IFramePart {

    public Animation parent;
    public EnumAnimationPart part;
    public float[] rotation = {0, 0, 0};
    public float[] pivot = {0, 0, 0};

    public HashMap<EnumFrameType, Value> values = new HashMap<>();

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

    public void setParent(Animation parent) {
        this.parent = parent;
    }

    public Animation getParent() {
        return parent;
    }

    public EnumAnimationPart getPart() {
        return part;
    }

    public String getName() {
        return part.name();
    }

    public int getPartId() {
        return part.id;
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
            if (enumPart.id == partId) {
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

    public void readFromNBT(NBTTagCompound compound) {
        part = EnumAnimationPart.valueOf(compound.getString("Part"));
        for (int i = 0; i < 3; i++) {
            if (compound.hasKey("Rotation" + i)) {
                rotation[i] = compound.getFloat("Rotation" + i);
                addValue(EnumFrameType.values()[i], rotation[i]);
            }
        }
        for (int i = 0; i < 3; i++) {
            if (compound.hasKey("Pivot" + i)) {
                pivot[i] = compound.getFloat("Pivot" + i);
                addValue(EnumFrameType.values()[3 + i], rotation[i]);
            }
        }

        if (compound.hasKey("Rotation0")) {
            for (int i = 0; i < 3; i++) {
                rotation[i] = compound.getFloat("Rotation" + i);
                addValue(EnumFrameType.values()[i], rotation[i]);
                pivot[i] = compound.getFloat("Pivot" + i);
                addValue(EnumFrameType.values()[3 + i], rotation[i]);
            }
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

        for (EnumFrameType type : EnumFrameType.values()) {
            String name = type.toString().toLowerCase();
            if (compound.hasKey(name)) {
                NBTTagCompound cmpd = compound.getCompoundTag(name);
                Ease easing = Ease.valueOf(cmpd.getString("easing"));
                addValue(type, cmpd.getFloat("value"), easing);
            }
        }
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("Part", part.toString());
        for (int i = 0; i < 3; i++) {
            compound.setFloat("Rotation" + i, rotation[i]);
        }
        for (int i = 0; i < 3; i++) {
            compound.setFloat("Pivot" + i, pivot[i]);
        }

        for (Map.Entry<EnumFrameType, Value> entry : values.entrySet()) {
            EnumFrameType type = entry.getKey();
            compound.setTag(type.toString().toLowerCase(), entry.getValue().writeToNBT());
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

    @SideOnly(Side.CLIENT)
    public void interpolateAngles() {
        if (parent != null && parent.paused)
            return;

        float pi = (float) Math.PI / 180;
        if (this.partialRotationTick != ClientEventHandler.partialRenderTick) {
            this.partialRotationTick = ClientEventHandler.partialRenderTick;
            boolean newLogic = true;
            if (newLogic && parent != null) {
                Frame next = (Frame) parent.getFrame(parent.currentFrame + 1);
                if (next != null) {
                    FramePart nextPart = next.frameParts.get(part);
                    if (nextPart != null) {
                        float value = Ease.OUTBOUNCE.apply(getInterpolationValue());
                        this.prevRotations[0] = ValueUtil.lerp(values.get(EnumFrameType.ROTATION_X).value * pi, nextPart.values.get(EnumFrameType.ROTATION_X).value * pi, value);
                        this.prevRotations[1] = ValueUtil.lerp(values.get(EnumFrameType.ROTATION_Y).value * pi, nextPart.values.get(EnumFrameType.ROTATION_Y).value * pi, value);
                        this.prevRotations[2] = ValueUtil.lerp(values.get(EnumFrameType.ROTATION_Z).value * pi, nextPart.values.get(EnumFrameType.ROTATION_Z).value * pi, value);

                        this.prevRotations[0] = ValueUtil.lerp(this.rotation[0] * pi, nextPart.rotation[0] * pi, value);
                        this.prevRotations[1] = ValueUtil.lerp(this.rotation[1] * pi, nextPart.rotation[1] * pi, value);
                        this.prevRotations[2] = ValueUtil.lerp(this.rotation[2] * pi, nextPart.rotation[2] * pi, value);
                    }
                }
            } else { /**
             * Smoothing 0 - Interpolated => equivalent to {@link Ease#INSINE}
             */
                float value = Math.abs(speed) / 20f;
                this.prevRotations[0] = ValueUtil.lerp(this.prevRotations[0], this.rotation[0] * pi, value);
                this.prevRotations[1] = ValueUtil.lerp(this.prevRotations[1], this.rotation[1] * pi, value);
                this.prevRotations[2] = ValueUtil.lerp(this.prevRotations[2], this.rotation[2] * pi, value);
            }
        }
    }

    public float getInterpolationValue() { // a 0-1 that lerps between currentFrame startTick and nextFrame startTick
        float currentTick = parent.getCurrentTick();
        if (currentTick == 0)
            return 0;

        Frame current = parent.frames.get(parent.currentFrame);
        float partialTicks = parent.paused ? 0 : partialRotationTick;
        currentTick += partialTicks;
        float ratio = (currentTick - current.getStartTick()) / current.duration; //

        System.out.println("currentTick " + currentTick + ", interp_value " + ratio);
        return ratio;
    }

    /**
     * Smoothing 2 - None => equivalent to {@link Ease#CONSTANT}
     */
    public void constant() {
        float pi = (float) Math.PI / 180;
        this.prevRotations[0] = this.rotation[0] * pi;
        this.prevRotations[1] = this.rotation[1] * pi;
        this.prevRotations[2] = this.rotation[2] * pi;
    }

    /**
     * Smoothing 1 - Linear => equivalent to {@link Ease#LINEAR}
     */
    public void linear() {
        float pi = (float) Math.PI / 180;
        int directionX = Float.compare(this.rotation[0] * pi, this.prevRotations[0]);
        this.prevRotations[0] += directionX * this.speed / 10f;
        this.prevRotations[0] = directionX == 1 ? Math.min(this.rotation[0] * pi, this.prevRotations[0]) : Math.max(this.rotation[0] * pi, this.prevRotations[0]);
        int directionY = Float.compare(this.rotation[1] * pi, this.prevRotations[1]);
        this.prevRotations[1] += directionY * this.speed / 10f;
        this.prevRotations[1] = directionY == 1 ? Math.min(this.rotation[1] * pi, this.prevRotations[1]) : Math.max(this.rotation[1] * pi, this.prevRotations[1]);
        int directionZ = Float.compare(this.rotation[2] * pi, this.prevRotations[2]);
        this.prevRotations[2] += directionZ * this.speed / 10f;
        this.prevRotations[2] = directionZ == 1 ? Math.min(this.rotation[2] * pi, this.prevRotations[2]) : Math.max(this.rotation[2] * pi, this.prevRotations[2]);
    }

    @SideOnly(Side.CLIENT)
    public void interpolateOffset() {
        if (parent != null && parent.paused)
            return;

        if (this.smooth == 2) {
            this.prevPivots[0] = this.pivot[0];
            this.prevPivots[1] = this.pivot[1];
            this.prevPivots[2] = this.pivot[2];
        } else if (this.partialPivotTick != ClientEventHandler.partialRenderTick) {
            this.partialPivotTick = ClientEventHandler.partialRenderTick;
            if (this.smooth == 0) {
                this.prevPivots[0] = (this.pivot[0] - this.prevPivots[0]) * Math.abs(this.speed) / 10f + this.prevPivots[0];
                this.prevPivots[1] = (this.pivot[1] - this.prevPivots[1]) * Math.abs(this.speed) / 10f + this.prevPivots[1];
                this.prevPivots[2] = (this.pivot[2] - this.prevPivots[2]) * Math.abs(this.speed) / 10f + this.prevPivots[2];
            } else {
                int directionX = Float.compare(this.pivot[0], this.prevPivots[0]);
                this.prevPivots[0] += directionX * this.speed / 10f;
                this.prevPivots[0] = directionX == 1 ? Math.min(this.pivot[0], this.prevPivots[0]) : Math.max(this.pivot[0], this.prevPivots[0]);
                int directionY = Float.compare(this.pivot[1], this.prevPivots[1]);
                this.prevPivots[1] += directionY * this.speed / 10f;
                this.prevPivots[1] = directionY == 1 ? Math.min(this.pivot[1], this.prevPivots[1]) : Math.max(this.pivot[1], this.prevPivots[1]);
                int directionZ = Float.compare(this.pivot[2], this.prevPivots[2]);
                this.prevPivots[2] += directionZ * this.speed / 10f;
                this.prevPivots[2] = directionZ == 1 ? Math.min(this.pivot[2], this.prevPivots[2]) : Math.max(this.pivot[2], this.prevPivots[2]);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void jumpToCurrentFrame() {
        this.partialRotationTick = ClientEventHandler.partialRenderTick;
        this.partialPivotTick = ClientEventHandler.partialRenderTick;

        this.prevPivots[0] = this.pivot[0];
        this.prevPivots[1] = this.pivot[1];
        this.prevPivots[2] = this.pivot[2];

        float pi = (float) Math.PI / 180;
        this.prevRotations[0] = this.rotation[0] * pi;
        this.prevRotations[1] = this.rotation[1] * pi;
        this.prevRotations[2] = this.rotation[2] * pi;
    }

    public IFramePart addValue(String type, float value) {
        addValue(EnumFrameType.valueOf(type.toUpperCase()), value, Ease.INSINE);
        return this;
    }

    public IFramePart addValue(String type, float value, String easing) {
        addValue(EnumFrameType.valueOf(type.toUpperCase()), value, Ease.valueOf(easing.toUpperCase()));
        return this;
    }

    public void addValue(EnumFrameType type, float value) {
        addValue(type, value, Ease.INSINE);
    }

    public void addValue(EnumFrameType type, float value, Ease ease) {
        Value val = new Value(value, ease);

        if (!values.containsKey(type))
            values.put(type, val);
        else
            values.replace(type, val);
    }

    public void setEase(EnumFrameType type, Ease ease) {
        if (values.containsKey(type))
            values.get(type).easing = ease;
    }

    public class Value {
        public float value;
        public Ease easing;

        public Value(float value, Ease ease) {
            this.value = value;
            this.easing = ease;
        }

        public Value(float value) {
            this(value, Ease.INOUTSINE);
        }

        public NBTTagCompound writeToNBT() {
            NBTTagCompound cmpd = new NBTTagCompound();
            cmpd.setFloat("value", value);
            cmpd.setString("easing", easing.toString());
            return cmpd;
        }
    }
}
