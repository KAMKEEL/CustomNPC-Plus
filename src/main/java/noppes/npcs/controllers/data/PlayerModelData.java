package noppes.npcs.controllers.data;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.Server;
import noppes.npcs.api.handler.IPlayerModelData;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.roles.JobPuppet;

public class PlayerModelData implements IPlayerModelData {
    public PlayerData parent;
    public final EntityPlayer player;

    public JobPuppet.PartConfig head = new JobPuppet.PartConfig();
    public JobPuppet.PartConfig larm = new JobPuppet.PartConfig();
    public JobPuppet.PartConfig rarm = new JobPuppet.PartConfig();
    public JobPuppet.PartConfig body = new JobPuppet.PartConfig();
    public JobPuppet.PartConfig lleg = new JobPuppet.PartConfig();
    public JobPuppet.PartConfig rleg = new JobPuppet.PartConfig();

    public boolean enabled = false;
    public boolean whileStanding = true;
    public boolean whileAttacking = false;
    public boolean whileMoving = false;
    public boolean fullAngles = false;

    public int rotationX, rotationY, rotationZ;
    public boolean rotationEnabledX, rotationEnabledY, rotationEnabledZ;

    public boolean animate = false;
    public float animRate = 1.0F;

    //Client-sided use
    public float modelRotPartialTicks;
    public float[] modelRotations = {0,0,0};

    public float[] bipedRotsHead = {0,0,0};
    public float[] bipedRotsBody = {0,0,0};
    public float[] bipedRotsLeftArm = {0,0,0};
    public float[] bipedRotsRightArm = {0,0,0};
    public float[] bipedRotsLeftLeg = {0,0,0};
    public float[] bipedRotsRightLeg = {0,0,0};

    public PlayerModelData(PlayerData parent) {
        this.parent = parent;
        this.player = parent.player;
    }

    public PlayerModelData(EntityPlayer player) {
        this.player = player;
    }

    public void updateClient() {
        Server.sendToAll(EnumPacketClient.PLAYER_UPDATE_MODEL_DATA, ((PlayerData) parent).player.getCommandSenderName(), this.writeToNBT(new NBTTagCompound()));
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("PuppetHead", head.writeNBT());
        compound.setTag("PuppetLArm", larm.writeNBT());
        compound.setTag("PuppetRArm", rarm.writeNBT());
        compound.setTag("PuppetBody", body.writeNBT());
        compound.setTag("PuppetLLeg", lleg.writeNBT());
        compound.setTag("PuppetRLeg", rleg.writeNBT());

        compound.setBoolean("PuppetEnabled", enabled);
        compound.setBoolean("PuppetStanding", whileStanding);
        compound.setBoolean("PuppetAttacking", whileAttacking);
        compound.setBoolean("PuppetMoving", whileMoving);
        compound.setBoolean("PuppetFullAngles", fullAngles);

        compound.setInteger("PuppetRotationX",rotationX);
        compound.setInteger("PuppetRotationY",rotationY);
        compound.setInteger("PuppetRotationZ",rotationZ);
        compound.setBoolean("PuppetRotationEnabledX",rotationEnabledX);
        compound.setBoolean("PuppetRotationEnabledY",rotationEnabledY);
        compound.setBoolean("PuppetRotationEnabledZ",rotationEnabledZ);

        compound.setBoolean("PuppetAnimate", animate);
        compound.setFloat("PuppetAnimSpeed", animRate);
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        head.readNBT(compound.getCompoundTag("PuppetHead"));
        larm.readNBT(compound.getCompoundTag("PuppetLArm"));
        rarm.readNBT(compound.getCompoundTag("PuppetRArm"));
        body.readNBT(compound.getCompoundTag("PuppetBody"));
        lleg.readNBT(compound.getCompoundTag("PuppetLLeg"));
        rleg.readNBT(compound.getCompoundTag("PuppetRLeg"));

        enabled = compound.getBoolean("PuppetEnabled");
        whileStanding = compound.getBoolean("PuppetStanding");
        whileAttacking = compound.getBoolean("PuppetAttacking");
        whileMoving = compound.getBoolean("PuppetMoving");
        fullAngles = compound.getBoolean("PuppetFullAngles");

        rotationX = compound.getInteger("PuppetRotationX");
        rotationY = compound.getInteger("PuppetRotationY");
        rotationZ = compound.getInteger("PuppetRotationZ");
        rotationEnabledX = compound.getBoolean("PuppetRotationEnabledX");
        rotationEnabledY = compound.getBoolean("PuppetRotationEnabledY");
        rotationEnabledZ = compound.getBoolean("PuppetRotationEnabledZ");

        animate = compound.getBoolean("PuppetAnimate");
        animRate = compound.getFloat("PuppetAnimSpeed");
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean enabled() {
        return this.enabled;
    }

    public void setAnimated(boolean animated) {
        this.animate = animated;
    }

    public boolean isAnimated() {
        return this.animate;
    }

    public void setFullAngles(boolean limit) {
        fullAngles = limit;
    }

    public boolean fullAngles() {
        return fullAngles;
    }

    public void setAnimRate(float animRate) {
        this.animRate = animRate;
    }

    public float getAnimRate() {
        return this.animRate;
    }

    public void doWhileStanding(boolean whileStanding) {
        this.whileStanding = whileStanding;
    }

    public boolean doWhileStanding() {
        return this.whileStanding;
    }

    public void doWhileAttacking(boolean whileAttacking) {
        this.whileAttacking = whileAttacking;
    }

    public boolean doWhileAttacking() {
        return this.whileAttacking;
    }

    public void doWhileMoving(boolean whileMoving) {
        this.whileMoving = whileMoving;
    }

    public boolean doWhileMoving() {
        return this.whileMoving;
    }

    public void setRotation(int rotationX, int rotationY, int rotationZ) {
        this.setRotationX(rotationX);
        this.setRotationY(rotationY);
        this.setRotationZ(rotationZ);
    }

    public void setRotationEnabled(boolean enabledX, boolean enabledY, boolean enabledZ) {
        this.setRotationEnabledX(enabledX);
        this.setRotationEnabledY(enabledY);
        this.setRotationEnabledZ(enabledZ);
    }

    public void setRotationX(int rotationX) {
        this.rotationX = rotationX;
    }

    public void setRotationY(int rotationY) {
        this.rotationY = rotationY;
    }

    public void setRotationZ(int rotationZ) {
        this.rotationZ = rotationZ;
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

    public int getRotationX() {
        return rotationX;
    }

    public int getRotationY() {
        return rotationY;
    }

    public int getRotationZ() {
        return rotationZ;
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

    /**
     * @since 1.7.10c
     * @param part Body part (0:head, 1:body, 2:leftarm, 3:rightarm, 4:leftleg, 5:rightleg)
     * @return Returns X rotation in degrees (0-360)
     */
    public int getRotationX(int part){
        if(part == 0)
            return floatToInt(head.rotationX);
        if(part == 1)
            return floatToInt(body.rotationX);
        if(part == 2)
            return floatToInt(larm.rotationX);
        if(part == 3)
            return floatToInt(rarm.rotationX);
        if(part == 4)
            return floatToInt(lleg.rotationX);
        if(part == 5)
            return floatToInt(rleg.rotationX);
        return 0;
    }

    /**
     * @since 1.7.10c
     * @param part Body part (0:head, 1:body, 2:leftarm, 3:rightarm, 4:leftleg, 5:rightleg)
     * @return Returns Y rotation in degrees (0-360)
     */
    public int getRotationY(int part){
        if(part == 0)
            return floatToInt(head.rotationY);
        if(part == 1)
            return floatToInt(body.rotationY);
        if(part == 2)
            return floatToInt(larm.rotationY);
        if(part == 3)
            return floatToInt(rarm.rotationY);
        if(part == 4)
            return floatToInt(lleg.rotationY);
        if(part == 5)
            return floatToInt(rleg.rotationY);
        return 0;
    }

    /**
     * @since 1.7.10c
     * @param part Body part (0:head, 1:body, 2:leftarm, 3:rightarm, 4:leftleg, 5:rightleg)
     * @return Returns Z rotation in degrees (0-360)
     */
    public int getRotationZ(int part){
        if(part == 0)
            return floatToInt(head.rotationZ);
        if(part == 1)
            return floatToInt(body.rotationZ);
        if(part == 2)
            return floatToInt(larm.rotationZ);
        if(part == 3)
            return floatToInt(rarm.rotationZ);
        if(part == 4)
            return floatToInt(lleg.rotationZ);
        if(part == 5)
            return floatToInt(rleg.rotationZ);
        return 0;
    }

    public void setRotation(int part, int rotationX, int rotationY, int rotationZ) {
        this.setRotationX(part,rotationX);
        this.setRotationY(part,rotationY);
        this.setRotationZ(part,rotationZ);
    }

    /**
     * @since 1.7.10c
     * @param part Body part (0:head, 1:body, 2:leftarm, 3:rightarm, 4:leftleg, 5:rightleg)
     * @param rotation Rotation the of the body part
     */
    public void setRotationX(int part, int rotation){
        float f = rotation / 360f - 0.5f;

        if(part == 0)
            head.rotationX = f;
        if(part == 1)
            body.rotationX = f;
        if(part == 2)
            larm.rotationX = f;
        if(part == 3)
            rarm.rotationX = f;
        if(part == 4)
            lleg.rotationX = f;
        if(part == 5)
            rleg.rotationX = f;
    }

    /**
     * @since 1.7.10c
     * @param part Body part (0:head, 1:body, 2:leftarm, 3:rightarm, 4:leftleg, 5:rightleg)
     * @param rotation Rotation the of the body part
     */
    public void setRotationY(int part, int rotation){
        float f = rotation / 360f - 0.5f;

        if(part == 0)
            head.rotationY = f;
        if(part == 1)
            body.rotationY = f;
        if(part == 2)
            larm.rotationY = f;
        if(part == 3)
            rarm.rotationY = f;
        if(part == 4)
            lleg.rotationY = f;
        if(part == 5)
            rleg.rotationY = f;
    }

    /**
     * @since 1.7.10c
     * @param part Body part (0:head, 1:body, 2:leftarm, 3:rightarm, 4:leftleg, 5:rightleg)
     * @param rotation Rotation the of the body part
     */
    public void setRotationZ(int part, int rotation){
        float f = rotation / 360f - 0.5f;

        if(part == 0)
            head.rotationZ = f;
        if(part == 1)
            body.rotationZ = f;
        if(part == 2)
            larm.rotationZ = f;
        if(part == 3)
            rarm.rotationZ = f;
        if(part == 4)
            lleg.rotationZ = f;
        if(part == 5)
            rleg.rotationZ = f;
    }

    public int getOffsetX(int part) {
        if(part == 0)
            return floatToInt(head.pivotX);
        if(part == 1)
            return floatToInt(body.pivotX);
        if(part == 2)
            return floatToInt(larm.pivotX);
        if(part == 3)
            return floatToInt(rarm.pivotX);
        if(part == 4)
            return floatToInt(lleg.pivotX);
        if(part == 5)
            return floatToInt(rleg.pivotX);
        return 0;
    }

    public int getOffsetY(int part) {
        if(part == 0)
            return floatToInt(head.pivotY);
        if(part == 1)
            return floatToInt(body.pivotY);
        if(part == 2)
            return floatToInt(larm.pivotY);
        if(part == 3)
            return floatToInt(rarm.pivotY);
        if(part == 4)
            return floatToInt(lleg.pivotY);
        if(part == 5)
            return floatToInt(rleg.pivotY);
        return 0;
    }

    public int getOffsetZ(int part) {
        if(part == 0)
            return floatToInt(head.pivotZ);
        if(part == 1)
            return floatToInt(body.pivotZ);
        if(part == 2)
            return floatToInt(larm.pivotZ);
        if(part == 3)
            return floatToInt(rarm.pivotZ);
        if(part == 4)
            return floatToInt(lleg.pivotZ);
        if(part == 5)
            return floatToInt(rleg.pivotZ);
        return 0;
    }

    public void setOffset(int part, int offsetX, int offsetY, int offsetZ) {
        this.setOffsetX(part,offsetX);
        this.setOffsetY(part,offsetY);
        this.setOffsetZ(part,offsetZ);
    }

    public void setOffsetX(int part, int offset) {
        if(part == 0)
            head.pivotX = offset;
        if(part == 1)
            body.pivotX = offset;
        if(part == 2)
            larm.pivotX = offset;
        if(part == 3)
            rarm.pivotX = offset;
        if(part == 4)
            lleg.pivotX = offset;
        if(part == 5)
            rleg.pivotX = offset;
    }

    public void setOffsetY(int part, int offset) {
        if(part == 0)
            head.pivotY = offset;
        if(part == 1)
            body.pivotY = offset;
        if(part == 2)
            larm.pivotY = offset;
        if(part == 3)
            rarm.pivotY = offset;
        if(part == 4)
            lleg.pivotY = offset;
        if(part == 5)
            rleg.pivotY = offset;
    }

    public void setOffsetZ(int part, int offset) {
        if(part == 0)
            head.pivotZ = offset;
        if(part == 1)
            body.pivotZ = offset;
        if(part == 2)
            larm.pivotZ = offset;
        if(part == 3)
            rarm.pivotZ = offset;
        if(part == 4)
            lleg.pivotZ = offset;
        if(part == 5)
            rleg.pivotZ = offset;
    }

    /**
     * @since 1.7.10c
     * @param part Body part (0:head, 1:body, 2:leftarm, 3:rightarm, 4:leftleg, 5:rightleg)
     * @return Returns whether or not the body part is enabled
     */
    public boolean isEnabled(int part){
        if(part == 0)
            return !head.disabled;
        if(part == 1)
            return !body.disabled;
        if(part == 2)
            return !larm.disabled;
        if(part == 3)
            return !rarm.disabled;
        if(part == 4)
            return !lleg.disabled;
        if(part == 5)
            return !rleg.disabled;

        return false;
    }

    /**
     * @since 1.7.10c
     * @param part Body part (0:head, 1:body, 2:leftarm, 3:rightarm, 4:leftleg, 5:rightleg)
     * @param bo Whether or not the body part is enabled
     */
    public void setEnabled(int part, boolean bo){
        if(part == 0)
            head.disabled = !bo;
        if(part == 1)
            body.disabled = !bo;
        if(part == 2)
            larm.disabled = !bo;
        if(part == 3)
            rarm.disabled = !bo;
        if(part == 4)
            lleg.disabled = !bo;
        if(part == 5)
            rleg.disabled = !bo;
    }

    private int floatToInt(float f){
        return (int)((f + 0.5) * 360);
    }
}
