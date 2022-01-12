package noppes.npcs.controllers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.controllers.data.PlayerDataScript;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataTimers;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.util.NBTJsonUtil;

import java.io.File;
import java.io.FileInputStream;

public class PlayerData implements IExtendedEntityProperties {
    public PlayerDialogData dialogData = new PlayerDialogData();
    public PlayerBankData bankData = new PlayerBankData();
    public PlayerQuestData questData = new PlayerQuestData();
    public PlayerTransportData transportData = new PlayerTransportData();
    public PlayerFactionData factionData = new PlayerFactionData();
    public PlayerItemGiverData itemgiverData = new PlayerItemGiverData();
    public PlayerMailData mailData = new PlayerMailData();
    public DataTimers timers = new DataTimers(this);

    public EntityNPCInterface editingNpc;
    public NBTTagCompound cloned;

    public EntityPlayer player;

    public String playername = "";
    public String uuid = "";

    private EntityNPCInterface activeCompanion = null;
    public int companionID = 0;

    public boolean isGUIOpen = false;

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        PlayerDataController.instance.savePlayerData(getNBT(), player.getPersistentID());
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        PlayerDataController.instance.loadPlayerData(
          player.getUniqueID()
        ).thenAccept(this::setNBT);
    }

    public void setNBT(NBTTagCompound data) {
        dialogData.loadNBTData(data);
        bankData.loadNBTData(data);
        questData.loadNBTData(data);
        transportData.loadNBTData(data);
        factionData.loadNBTData(data);
        itemgiverData.loadNBTData(data);
        mailData.loadNBTData(data);
        timers.readFromNBT(data);

        if (player != null) {
            playername = player.getCommandSenderName();
            uuid = player.getPersistentID().toString();
        } else {
            playername = data.getString("PlayerName");
            uuid = data.getString("UUID");
        }
        companionID = data.getInteger("PlayerCompanionId");
        if (data.hasKey("PlayerCompanion") && !hasCompanion()) {
            EntityCustomNpc npc = new EntityCustomNpc(player.worldObj);
            npc.readEntityFromNBT(data.getCompoundTag("PlayerCompanion"));
            npc.setPosition(player.posX, player.posY, player.posZ);
            if (npc.advanced.role == EnumRoleType.Companion) {
                setCompanion(npc);
                ((RoleCompanion) npc.roleInterface).setSitting(false);
                player.worldObj.spawnEntityInWorld(npc);
            }
        }
        isGUIOpen = data.getBoolean("isGUIOpen");
    }

    public NBTTagCompound getNBT() {
        if (player != null) {
            playername = player.getCommandSenderName();
            uuid = player.getPersistentID().toString();
        }
        NBTTagCompound compound = new NBTTagCompound();
        dialogData.saveNBTData(compound);
        bankData.saveNBTData(compound);
        questData.saveNBTData(compound);
        transportData.saveNBTData(compound);
        factionData.saveNBTData(compound);
        itemgiverData.saveNBTData(compound);
        mailData.saveNBTData(compound);
        timers.writeToNBT(compound);

        compound.setString("PlayerName", playername);
        compound.setString("UUID", uuid);
        compound.setInteger("PlayerCompanionId", companionID);
        compound.setBoolean("isGUIOpen", isGUIOpen);

        if (hasCompanion()) {
            NBTTagCompound nbt = new NBTTagCompound();
            if (activeCompanion.writeToNBTOptional(nbt))
                compound.setTag("PlayerCompanion", nbt);
        }
        return compound;
    }

    @Override
    public void init(Entity entity, World world) {

    }

    public void setGUIOpen(boolean bool) {
        isGUIOpen = bool;
        saveNBTData(null);
    }

    public boolean getGUIOpen() {
        loadNBTData(null);
        return isGUIOpen;
    }

    public boolean hasCompanion() {
        return activeCompanion != null && !activeCompanion.isDead;
    }

    public void setCompanion(EntityNPCInterface npc) {
        if (npc != null && npc.advanced.role != EnumRoleType.Companion)//shouldnt happen
            return;
        companionID++;
        activeCompanion = npc;
        if (npc != null)
            ((RoleCompanion) npc.roleInterface).companionID = companionID;
        saveNBTData(null);
    }

    public void updateCompanion(World world) {
        if (!hasCompanion() || world == activeCompanion.worldObj)
            return;
        RoleCompanion role = (RoleCompanion) activeCompanion.roleInterface;
        role.owner = player;
        if (!role.isFollowing())
            return;
        NBTTagCompound nbt = new NBTTagCompound();
        activeCompanion.writeToNBTOptional(nbt);
        activeCompanion.isDead = true;

        EntityCustomNpc npc = new EntityCustomNpc(world);
        npc.readEntityFromNBT(nbt);
        npc.setPosition(player.posX, player.posY, player.posZ);
        setCompanion(npc);
        ((RoleCompanion) npc.roleInterface).setSitting(false);
        world.spawnEntityInWorld(npc);
    }
}
