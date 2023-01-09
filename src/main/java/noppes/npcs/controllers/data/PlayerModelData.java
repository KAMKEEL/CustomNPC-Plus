package noppes.npcs.controllers.data;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.Server;
import noppes.npcs.api.handler.IPlayerModelData;
import noppes.npcs.api.handler.data.IModelPart;
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
    public float rotationX, rotationY, rotationZ;
    public boolean rotationEnabledX, rotationEnabledY, rotationEnabledZ;

    public boolean animate = false;
    public float animRate = 1.0F;
    public boolean fullAngles;
    public boolean interpolate;

    //Client-sided use
    public float modelRotPartialTicks;
    public float[] modelRotations = {0,0,0};

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

    public IModelPart getPart(int part) {
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
        compound.setTag("PuppetHead", head.writeNBT());
        compound.setTag("PuppetLArm", larm.writeNBT());
        compound.setTag("PuppetRArm", rarm.writeNBT());
        compound.setTag("PuppetBody", body.writeNBT());
        compound.setTag("PuppetLLeg", lleg.writeNBT());
        compound.setTag("PuppetRLeg", rleg.writeNBT());

        compound.setBoolean("PuppetEnabled", enabled);

        compound.setFloat("PuppetRotationX",rotationX);
        compound.setFloat("PuppetRotationY",rotationY);
        compound.setFloat("PuppetRotationZ",rotationZ);
        compound.setBoolean("PuppetRotationEnabledX",rotationEnabledX);
        compound.setBoolean("PuppetRotationEnabledY",rotationEnabledY);
        compound.setBoolean("PuppetRotationEnabledZ",rotationEnabledZ);

        compound.setBoolean("PuppetFullAngles", fullAngles);
        compound.setBoolean("PuppetInterpolate", interpolate);
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
        animate = compound.getBoolean("PuppetAnimate");
        animRate = compound.getFloat("PuppetAnimSpeed");
    }

    public void setAnimated(boolean animated) {
        for (int i = 0; i < 6; i++) {
            this.getPart(i).setAnimated(animated);
        }
    }

    public void setInterpolated(boolean interpolate) {
        for (int i = 0; i < 6; i++) {
            this.getPart(i).setAnimated(interpolate);
        }
    }

    public void setFullAngles(boolean fullAngles) {
        for (int i = 0; i < 6; i++) {
            this.getPart(i).setFullAngles(fullAngles);
        }
    }

    public void setAnimRate(float animRate) {
        if (animRate < 0)
            animRate = 0;
        for (int i = 0; i < 6; i++) {
            this.getPart(i).setAnimRate(animRate);
        }
    }
}
