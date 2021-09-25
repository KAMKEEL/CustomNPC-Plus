package noppes.npcs;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.constants.EnumJobType;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.controllers.DialogOption;
import noppes.npcs.controllers.FactionOptions;
import noppes.npcs.controllers.Line;
import noppes.npcs.controllers.Lines;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.*;

import java.util.HashMap;

public class DataAdvanced {

    public Lines interactLines = new Lines();
    public Lines worldLines = new Lines();
    public Lines attackLines = new Lines();
    public Lines killedLines = new Lines();
    public Lines killLines = new Lines();
    
    public boolean orderedLines = false;

    public String idleSound = "";
    public String angrySound = "";
    public String hurtSound = "minecraft:game.player.hurt";
    public String deathSound = "minecraft:game.player.hurt";
    public String stepSound = "";

    private EntityNPCInterface npc;
    public FactionOptions factions = new FactionOptions();

    public EnumRoleType role = EnumRoleType.None;
    public EnumJobType job = EnumJobType.None;

    public boolean attackOtherFactions = false;
    public boolean defendFaction = false;
	public boolean disablePitch = false;

    public DataAdvanced(EntityNPCInterface npc) {
        this.npc = npc;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("NpcLines", worldLines.writeToNBT());
        compound.setTag("NpcKilledLines", killedLines.writeToNBT());
        compound.setTag("NpcInteractLines", interactLines.writeToNBT());
        compound.setTag("NpcAttackLines", attackLines.writeToNBT());
        compound.setTag("NpcKillLines", killLines.writeToNBT());

        compound.setString("NpcIdleSound", idleSound);
        compound.setString("NpcAngrySound", angrySound);
        compound.setString("NpcHurtSound", hurtSound);
        compound.setString("NpcDeathSound", deathSound);
        compound.setString("NpcStepSound", stepSound);

        compound.setBoolean("OrderedLines", orderedLines);

        compound.setInteger("FactionID", npc.getFaction().id);
        compound.setBoolean("AttackOtherFactions", attackOtherFactions);
        compound.setBoolean("DefendFaction", defendFaction);
        compound.setBoolean("DisablePitch", disablePitch);

        compound.setInteger("Role", role.ordinal());
        compound.setInteger("NpcJob", job.ordinal());
        compound.setTag("FactionPoints", factions.writeToNBT(new NBTTagCompound()));

		compound.setTag("NPCDialogOptions", nbtDialogs(npc.dialogs));
		
        return compound;
    }

    public void readToNBT(NBTTagCompound compound) {
        interactLines.readNBT(compound.getCompoundTag("NpcInteractLines"));
        worldLines.readNBT(compound.getCompoundTag("NpcLines"));
        attackLines.readNBT(compound.getCompoundTag("NpcAttackLines"));
        killedLines.readNBT(compound.getCompoundTag("NpcKilledLines"));
        killLines.readNBT(compound.getCompoundTag("NpcKillLines"));

        idleSound = compound.getString("NpcIdleSound");
        angrySound = compound.getString("NpcAngrySound");
        hurtSound = compound.getString("NpcHurtSound");
        deathSound = compound.getString("NpcDeathSound");
        stepSound = compound.getString("NpcStepSound");

        orderedLines = compound.getBoolean("OrderedLines");

        npc.setFaction(compound.getInteger("FactionID"));
        npc.faction = npc.getFaction();
        attackOtherFactions = compound.getBoolean("AttackOtherFactions");
        defendFaction = compound.getBoolean("DefendFaction");
        disablePitch = compound.getBoolean("DisablePitch");

        setRole(compound.getInteger("Role"));
        setJob(compound.getInteger("NpcJob"));

        factions.readFromNBT(compound.getCompoundTag("FactionPoints"));

		npc.dialogs = getDialogs(compound.getTagList("NPCDialogOptions", 10));	
    }

	private HashMap<Integer, DialogOption> getDialogs(NBTTagList tagList) {
		HashMap<Integer, DialogOption> map = new HashMap<Integer, DialogOption>();
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
			int slot = nbttagcompound.getInteger("DialogSlot");
			DialogOption option = new DialogOption();
			option.readNBT(nbttagcompound.getCompoundTag("NPCDialog"));
			map.put(slot, option);

		}
		return map;
	}


	private NBTTagList nbtDialogs(HashMap<Integer, DialogOption> dialogs2) {
		NBTTagList nbttaglist = new NBTTagList();
		for (int slot : dialogs2.keySet()) {
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setInteger("DialogSlot", slot);
			nbttagcompound.setTag("NPCDialog", dialogs2.get(slot)
					.writeNBT());
			nbttaglist.appendTag(nbttagcompound);
		}
		return nbttaglist;
	}

    public Line getInteractLine() {
        return interactLines.getLine(!orderedLines);
    }

    public Line getAttackLine() {
        return attackLines.getLine(!orderedLines);
    }

    public Line getKilledLine() {
        return killedLines.getLine(!orderedLines);
    }

    public Line getKillLine() {
        return killLines.getLine(!orderedLines);
    }

    public Line getWorldLine() {
        return worldLines.getLine(!orderedLines);
    }

    public void setRole(int i) {
        if (EnumRoleType.values().length <= i) {
            i -= 2;
        }
        role = EnumRoleType.values()[i];
        if(role == EnumRoleType.None)
            npc.roleInterface = null;
        else if(role == EnumRoleType.Bank && !(npc.roleInterface instanceof RoleBank))
            npc.roleInterface = new RoleBank(npc);
        else if(role == EnumRoleType.Follower && !(npc.roleInterface instanceof RoleFollower))
            npc.roleInterface = new RoleFollower(npc);
        else if(role == EnumRoleType.Postman && !(npc.roleInterface instanceof RolePostman))
            npc.roleInterface = new RolePostman(npc);
        else if(role == EnumRoleType.Trader && !(npc.roleInterface instanceof RoleTrader))
            npc.roleInterface = new RoleTrader(npc);
        else if(role == EnumRoleType.Transporter && !(npc.roleInterface instanceof RoleTransporter))
            npc.roleInterface = new RoleTransporter(npc);
        else if(role == EnumRoleType.Companion && !(npc.roleInterface instanceof RoleCompanion))
            npc.roleInterface = new RoleCompanion(npc);
    }

    public void setJob(int i) {
        if(npc.jobInterface != null && !npc.worldObj.isRemote)
        	npc.jobInterface.reset();
        
        job = EnumJobType.values()[i % EnumJobType.values().length];
        if (job == EnumJobType.None)
            npc.jobInterface = null;
        else if (job == EnumJobType.Bard && !(npc.jobInterface instanceof JobBard)) 
            npc.jobInterface = new JobBard(npc);
        else if (job == EnumJobType.Healer && !(npc.jobInterface instanceof JobHealer)) 
            npc.jobInterface = new JobHealer(npc);
        else if (job == EnumJobType.Guard && !(npc.jobInterface instanceof JobGuard)) 
            npc.jobInterface = new JobGuard(npc);
        else if (job == EnumJobType.ItemGiver && !(npc.jobInterface instanceof JobItemGiver)) 
            npc.jobInterface = new JobItemGiver(npc);
        else if (job == EnumJobType.Follower && !(npc.jobInterface instanceof JobFollower)) 
            npc.jobInterface = new JobFollower(npc);
        else if (job == EnumJobType.Spawner && !(npc.jobInterface instanceof JobSpawner)) 
            npc.jobInterface = new JobSpawner(npc);
        else if (job == EnumJobType.Conversation && !(npc.jobInterface instanceof JobConversation)) 
            npc.jobInterface = new JobConversation(npc);
        else if (job == EnumJobType.ChunkLoader && !(npc.jobInterface instanceof JobChunkLoader))
            npc.jobInterface = new JobChunkLoader(npc);
        else if (job == EnumJobType.Puppet && !(npc.jobInterface instanceof JobPuppet))
            npc.jobInterface = new JobPuppet(npc);
    }

    public boolean hasWorldLines() {
        return !worldLines.isEmpty();
    }
}
