package noppes.npcs;


import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.data.PlayerData;

public class AnimationDataShared {
    public Object parent;

    public AnimationPartConfig head = new AnimationPartConfig();
    public AnimationPartConfig larm = new AnimationPartConfig();
    public AnimationPartConfig rarm = new AnimationPartConfig();
    public AnimationPartConfig body = new AnimationPartConfig();
    public AnimationPartConfig lleg = new AnimationPartConfig();
    public AnimationPartConfig rleg = new AnimationPartConfig();

    public boolean allowAnimation = false;

    // Full Model Animation
    public boolean enableFullModel = true;

    public float rotationX, rotationY, rotationZ;
    public boolean rotationEnabledX, rotationEnabledY, rotationEnabledZ;
    public boolean fullAnimate = false;
    public float animRate = 1.0F;
    public boolean fullAngles;
    public boolean interpolate;

    //Client-sided use
    public float modelRotPartialTicks;
    public float[] modelRotations = {0,0,0};

    public AnimationDataShared(Object parent){
        this.parent = parent;
    }

    public void updateClient() {
        Server.sendToAll(EnumPacketClient.PLAYER_UPDATE_MODEL_DATA, ((PlayerData) parent).player.getCommandSenderName(), this.writeToNBT(new NBTTagCompound()));
    }

    // SWITCH TO IAnimatePart
    public AnimationPartConfig getPart(int part) {
        switch (part) {
            case 0:
                return this.head;
            case 1:
                return this.body;
            case 2:
                return this.larm;
            case 3:
                return this.rarm;
            case 4:
                return this.lleg;
            case 5:
                return this.rleg;
        }
        return null;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {

        compound.setBoolean("PuppetEnabled", allowAnimation);

        // ALWAYS SAVE allowAnimation
        if(allowAnimation){
            // GROUP INTO ANIMATION TAG

            compound.setTag("PuppetHead", head.writeNBT());
            compound.setTag("PuppetLArm", larm.writeNBT());
            compound.setTag("PuppetRArm", rarm.writeNBT());
            compound.setTag("PuppetBody", body.writeNBT());
            compound.setTag("PuppetLLeg", lleg.writeNBT());
            compound.setTag("PuppetRLeg", rleg.writeNBT());

            // GROUP THIS INTO "PuppetFullModel"
            compound.setBoolean("PuppetFullModelEnabled", enableFullModel);
            if(enableFullModel){
                compound.setFloat("PuppetRotationX",rotationX);
                compound.setFloat("PuppetRotationY",rotationY);
                compound.setFloat("PuppetRotationZ",rotationZ);
                compound.setBoolean("PuppetRotationEnabledX",rotationEnabledX);
                compound.setBoolean("PuppetRotationEnabledY",rotationEnabledY);
                compound.setBoolean("PuppetRotationEnabledZ",rotationEnabledZ);

                compound.setBoolean("PuppetFullAngles", fullAngles);
                compound.setBoolean("PuppetInterpolate", interpolate);
                compound.setBoolean("PuppetAnimate", fullAnimate);
                compound.setFloat("PuppetAnimSpeed", animRate);
            }
        }

        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        allowAnimation = compound.getBoolean("PuppetEnabled");
        if(allowAnimation) {
            head.readNBT(compound.getCompoundTag("PuppetHead"));
            larm.readNBT(compound.getCompoundTag("PuppetLArm"));
            rarm.readNBT(compound.getCompoundTag("PuppetRArm"));
            body.readNBT(compound.getCompoundTag("PuppetBody"));
            lleg.readNBT(compound.getCompoundTag("PuppetLLeg"));
            rleg.readNBT(compound.getCompoundTag("PuppetRLeg"));

            enableFullModel = compound.getBoolean("PuppetFullModelEnabled");
            if (enableFullModel) {
                rotationX = compound.getFloat("PuppetRotationX");
                rotationY = compound.getFloat("PuppetRotationY");
                rotationZ = compound.getFloat("PuppetRotationZ");
                rotationEnabledX = compound.getBoolean("PuppetRotationEnabledX");
                rotationEnabledY = compound.getBoolean("PuppetRotationEnabledY");
                rotationEnabledZ = compound.getBoolean("PuppetRotationEnabledZ");

                fullAngles = compound.getBoolean("PuppetFullAngles");
                if (!compound.hasKey("PuppetInterpolate")) {
                    interpolate = true;
                } else {
                    interpolate = compound.getBoolean("PuppetInterpolate");
                }
                fullAnimate = compound.getBoolean("PuppetAnimate");
                animRate = compound.getFloat("PuppetAnimSpeed");
            }
        }
        else {
            // IF HAS ANIMATION TAG
            // REMOVE IT!
        }
    }

    public void setEnabled(boolean enabled) {
        this.allowAnimation = enabled;
    }

    public boolean enabled() {
        return this.allowAnimation;
    }

    public void setAnimated(boolean animated) {
        this.fullAnimate = animated;
    }

    public boolean isAnimated() {
        return this.fullAnimate;
    }

    public void setInterpolated(boolean interpolate) {
        this.interpolate = interpolate;
    }

    public boolean isInterpolated() {
        return this.interpolate;
    }

    public void setFullAngles(boolean limit) {
        fullAngles = limit;
    }

    public boolean fullAngles() {
        return fullAngles;
    }

    public void setAnimRate(float animRate) {
        if (animRate < 0)
            animRate = 0;
        this.animRate = animRate;
    }

    public float getAnimRate() {
        return this.animRate;
    }

    public void setRotation(float rotationX, float rotationY, float rotationZ) {
        this.setRotationX(rotationX);
        this.setRotationY(rotationY);
        this.setRotationZ(rotationZ);
    }

    public void setRotationX(float rotationX) {
        this.rotationX = rotationX;
    }

    public void setRotationY(float rotationY) {
        this.rotationY = rotationY;
    }

    public void setRotationZ(float rotationZ) {
        this.rotationZ = rotationZ;
    }

    public float getRotationX() {
        return rotationX;
    }

    public float getRotationY() {
        return rotationY;
    }

    public float getRotationZ() {
        return rotationZ;
    }

    public void setRotationEnabled(boolean enabledX, boolean enabledY, boolean enabledZ) {
        this.setRotationEnabledX(enabledX);
        this.setRotationEnabledY(enabledY);
        this.setRotationEnabledZ(enabledZ);
    }

    public void setRotationEnabledX(boolean enabled) {
        this.rotationEnabledX = enabled;
    }

    public void setRotationEnabledY(boolean enabled) {
        this.rotationEnabledY = enabled;
    }

    public void setRotationEnabledZ(boolean enabled) {
        this.rotationEnabledZ = enabled;
    }

    public boolean rotationEnabledX() {
        return rotationEnabledX;
    }

    public boolean rotationEnabledY() {
        return rotationEnabledY;
    }

    public boolean rotationEnabledZ() {
        return rotationEnabledZ;
    }

    public void allEnabled(boolean enabled) {
        for (int i = 0; i < 6; i++) {
            this.getPart(i).setEnabled(enabled);
        }
    }

    public void allAnimated(boolean animated) {
        for (int i = 0; i < 6; i++) {
            this.getPart(i).setAnimated(animated);
        }
    }

    public void allInterpolated(boolean interpolate) {
        for (int i = 0; i < 6; i++) {
            this.getPart(i).setAnimated(interpolate);
        }
    }

    public void allFullAngles(boolean fullAngles) {
        for (int i = 0; i < 6; i++) {
            this.getPart(i).setFullAngles(fullAngles);
        }
    }

    public void allAnimRate(float animRate) {
        if (animRate < 0)
            animRate = 0;
        for (int i = 0; i < 6; i++) {
            this.getPart(i).setAnimRate(animRate);
        }
    }
}
