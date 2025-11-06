package noppes.npcs.roles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

public class RoleMount extends RoleInterface {

    private float offsetX;
    private float offsetY;
    private float offsetZ;
    private boolean storedReturnToStart;

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
