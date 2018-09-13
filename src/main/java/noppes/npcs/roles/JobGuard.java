package noppes.npcs.roles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.entity.EntityNPCInterface;

public class JobGuard extends JobInterface{

	public boolean attacksAnimals = false;
	public boolean attackHostileMobs = true;
	public boolean attackCreepers = false;
	
	public List<String> targets = new ArrayList<String>();
	public boolean specific = false;
	
	public JobGuard(EntityNPCInterface npc) {
		super(npc);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setBoolean("GuardAttackAnimals", attacksAnimals);
		nbttagcompound.setBoolean("GuardAttackMobs", attackHostileMobs);
		nbttagcompound.setBoolean("GuardAttackCreepers", attackCreepers);
		nbttagcompound.setBoolean("GuardSpecific", specific);
		
		nbttagcompound.setTag("GuardTargets", NBTTags.nbtStringList(targets));
		return nbttagcompound;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		attacksAnimals = nbttagcompound.getBoolean("GuardAttackAnimals");
		attackHostileMobs = nbttagcompound.getBoolean("GuardAttackMobs");
		attackCreepers = nbttagcompound.getBoolean("GuardAttackCreepers");
		specific = nbttagcompound.getBoolean("GuardSpecific");

		targets = NBTTags.getStringList(nbttagcompound.getTagList("GuardTargets", 10));		
	}
	
	public boolean isEntityApplicable(Entity entity) {
    	if(entity instanceof EntityPlayer || entity instanceof EntityNPCInterface)
    		return false;
    	if(specific && targets.contains("entity." + EntityList.getEntityString(entity) + ".name"))
    		return true;
    	
    	if(entity instanceof EntityAnimal){
    		if(!attacksAnimals || entity instanceof EntityTameable && ((EntityTameable)entity).getOwner() != null)
    			return false;
    		return true;
    	}
    	else if (entity instanceof EntityCreeper) {
			return attackCreepers;
    	}
    	else if(entity instanceof IMob || entity instanceof EntityDragon){
    		return attackHostileMobs;
    	}
		return false;
	}
}
