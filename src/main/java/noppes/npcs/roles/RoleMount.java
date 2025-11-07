package noppes.npcs.roles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

public class RoleMount extends RoleInterface {

    private float offsetX;
    private float offsetY;
    private float offsetZ;
    private static final float MIN_JUMP_STRENGTH = 0.1F;
    private static final float MAX_JUMP_STRENGTH = 3.0F;
    private static final float MIN_DESCEND_SPEED = 0.0F;
    private static final float MAX_DESCEND_SPEED = 2.0F;
    private static final float MIN_ASCEND_SPEED = 0.0F;
    private static final float MAX_ASCEND_SPEED = 2.0F;
    private static final float DEFAULT_ASCEND_SPEED = 0.45F;
    private static final float DEFAULT_DESCEND_SPEED = 0.30F;

    private boolean storedReturnToStart;
    private float jumpStrength = 1.0F;
    private boolean allowSprint = true;
    private boolean flyingMountEnabled = false;
    private boolean hoverMode = false;
    private float flyingAscendSpeed = DEFAULT_ASCEND_SPEED;
    private float flyingDescendSpeed = DEFAULT_DESCEND_SPEED;

    public RoleMount(EntityNPCInterface npc) {
        super(npc);
        this.storedReturnToStart = npc.ais.returnToStart;
        npc.ais.returnToStart = false;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setFloat("MountOffsetX", offsetX);
        compound.setFloat("MountOffsetY", offsetY);
        compound.setFloat("MountOffsetZ", offsetZ);
        compound.setBoolean("MountReturnFlag", storedReturnToStart);
        compound.setFloat("MountJumpStrength", jumpStrength);
        compound.setBoolean("MountAllowSprint", allowSprint);
        compound.setBoolean("MountAllowFlying", flyingMountEnabled);
        compound.setBoolean("MountHoverMode", hoverMode);
        compound.setFloat("MountFlyingAscendSpeed", flyingAscendSpeed);
        compound.setFloat("MountFlyingDescendSpeed", flyingDescendSpeed);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        setOffsetX(compound.getFloat("MountOffsetX"));
        setOffsetY(compound.getFloat("MountOffsetY"));
        setOffsetZ(compound.getFloat("MountOffsetZ"));
        if (compound.hasKey("MountReturnFlag")) {
            storedReturnToStart = compound.getBoolean("MountReturnFlag");
        } else {
            storedReturnToStart = npc.ais.returnToStart;
        }
        if (compound.hasKey("MountJumpStrength")) {
            setJumpStrength(compound.getFloat("MountJumpStrength"));
        } else {
            jumpStrength = 1.0F;
        }
        allowSprint = !compound.hasKey("MountAllowSprint") || compound.getBoolean("MountAllowSprint");
        flyingMountEnabled = compound.hasKey("MountAllowFlying") && compound.getBoolean("MountAllowFlying");
        hoverMode = compound.hasKey("MountHoverMode") && compound.getBoolean("MountHoverMode");
        if (compound.hasKey("MountFlyingAscendSpeed")) {
            setFlyingAscendSpeed(compound.getFloat("MountFlyingAscendSpeed"));
        } else {
            flyingAscendSpeed = DEFAULT_ASCEND_SPEED;
        }
        if (compound.hasKey("MountFlyingDescendSpeed")) {
            setFlyingDescendSpeed(compound.getFloat("MountFlyingDescendSpeed"));
        } else if (compound.hasKey("MountFlyingFallSpeed")) {
            setFlyingDescendSpeed(compound.getFloat("MountFlyingFallSpeed"));
        } else {
            flyingDescendSpeed = DEFAULT_DESCEND_SPEED;
        }
        npc.ais.returnToStart = false;
    }

    @Override
    public void interact(EntityPlayer player) {
        if (player == null || npc.worldObj.isRemote) {
            return;
        }
        if (npc.riddenByEntity != null && npc.riddenByEntity != player) {
            return;
        }
        if (player.ridingEntity != null && player.ridingEntity != npc) {
            player.mountEntity(null);
        }
        player.fallDistance = 0.0F;
        npc.fallDistance = 0.0F;
        player.mountEntity(npc);
        npc.getNavigator().clearPathEntity();
        npc.setAttackTarget(null);
        npc.setRevengeTarget(null);
    }

    @Override
    public boolean aiShouldExecute() {
        if (npc.ais.returnToStart) {
            storedReturnToStart = true;
            npc.ais.returnToStart = false;
        }
        return false;
    }

    public void setOffsetX(float value) {
        this.offsetX = clamp(value);
    }

    public void setOffsetY(float value) {
        this.offsetY = clamp(value);
    }

    public void setOffsetZ(float value) {
        this.offsetZ = clamp(value);
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public float getOffsetZ() {
        return offsetZ;
    }

    public void setJumpStrength(float value) {
        this.jumpStrength = ValueUtil.clamp(value, MIN_JUMP_STRENGTH, MAX_JUMP_STRENGTH);
    }

    public float getJumpStrength() {
        return jumpStrength;
    }

    public void setSprintAllowed(boolean allowSprint) {
        this.allowSprint = allowSprint;
    }

    public boolean isSprintAllowed() {
        return allowSprint;
    }

    public void setFlyingMountEnabled(boolean enabled) {
        this.flyingMountEnabled = enabled;
    }

    public boolean isFlyingMountEnabled() {
        return flyingMountEnabled;
    }

    public void setHoverModeEnabled(boolean enabled) {
        this.hoverMode = enabled;
    }

    public boolean isHoverModeEnabled() {
        return hoverMode;
    }

    public void setFlyingAscendSpeed(float value) {
        this.flyingAscendSpeed = ValueUtil.clamp(value, MIN_ASCEND_SPEED, MAX_ASCEND_SPEED);
    }

    public float getFlyingAscendSpeed() {
        return flyingAscendSpeed;
    }

    public void setFlyingDescendSpeed(float value) {
        this.flyingDescendSpeed = ValueUtil.clamp(value, MIN_DESCEND_SPEED, MAX_DESCEND_SPEED);
    }

    public float getFlyingDescendSpeed() {
        return flyingDescendSpeed;
    }

    public void setReturnToStartPreference(boolean value) {
        this.storedReturnToStart = value;
        npc.ais.returnToStart = false;
    }

    public boolean getReturnToStartPreference() {
        return storedReturnToStart;
    }

    private float clamp(float value) {
        return ValueUtil.clamp(value, -5.0F, 5.0F);
    }

    public void resetOffsets() {
        offsetX = 0.0F;
        offsetY = 0.0F;
        offsetZ = 0.0F;
    }

    public void onDisable() {
        npc.ais.returnToStart = storedReturnToStart;
    }

    @Override
    public void delete() {
        onDisable();
    }
}
