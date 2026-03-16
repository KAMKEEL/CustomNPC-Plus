package noppes.npcs.roles;

import noppes.npcs.constants.ClientOnly;
import kamkeel.npcs.util.IVector3;
import kamkeel.npcs.controllers.data.ability.preview.PreviewEntityHandler;
import kamkeel.npcs.controllers.data.ability.gui.SubGuiAbilityConfig;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityConfigCallback;
import kamkeel.npcs.controllers.data.ability.gui.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.IChainedAbilityFieldProvider;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityFieldProvider;
import noppes.npcs.entity.EntityNPCInterface;
import kamkeel.npcs.entity.EntityEnergyDome;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import kamkeel.npcs.entity.EntityEnergyPanel;
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.entity.EntityAbilityLaser;
import kamkeel.npcs.entity.EntityAbilityDisc;
import kamkeel.npcs.entity.EntityAbilityBeam;
import kamkeel.npcs.entity.EntityEnergyProjectile;
import kamkeel.npcs.util.ByteBufUtils;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import noppes.npcs.util.ValueUtil;

public class RoleMount extends RoleInterface {

    private float offsetX;
    private float offsetY;
    private float offsetZ;
    private boolean storedReturnToStart;
    private float jumpStrength = 1.0F;
    private boolean allowSprint = true;
    private boolean flyingMountEnabled = false;
    private boolean hoverMode = false;
    private float flyingAscendSpeed = 0.60F;
    private float flyingDescendSpeed = 0.35F;

    public RoleMount(EntityNPCInterface npc) {
        super(npc);
        this.storedReturnToStart = npc.ais.returnToStart;
        npc.ais.returnToStart = false;
    }

    @Override
    public INbt writeToNBT(INbt compound) {
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
    public void readFromNBT(INbt compound) {
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
            flyingAscendSpeed = 0.60F;
        }
        if (compound.hasKey("MountFlyingDescendSpeed")) {
            setFlyingDescendSpeed(compound.getFloat("MountFlyingDescendSpeed"));
        } else if (compound.hasKey("MountFlyingFallSpeed")) {
            setFlyingDescendSpeed(compound.getFloat("MountFlyingFallSpeed"));
        } else {
            flyingDescendSpeed = 0.35F;
        }
        npc.ais.returnToStart = false;
    }

    @Override
    public void interact(IPlayer player) {
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
        this.jumpStrength = ValueUtil.clamp(value, 0.1F, 3.0F);
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
        this.flyingAscendSpeed = ValueUtil.clamp(value, 0.1F, 3.0F);
    }

    public float getFlyingAscendSpeed() {
        return flyingAscendSpeed;
    }

    public void setFlyingDescendSpeed(float value) {
        this.flyingDescendSpeed = ValueUtil.clamp(value, 0.05F, 3.0F);
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
        // Dismount any rider when mount role is disabled
        if (npc.riddenByEntity != null) {
            npc.riddenByEntity.mountEntity(null);
        }
        npc.ais.returnToStart = storedReturnToStart;
    }

    @Override
    public void delete() {
        onDisable();
    }
}
