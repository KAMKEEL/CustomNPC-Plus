package noppes.npcs.ai.selector;

import java.util.List;

import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.EnumCompanionJobs;
import noppes.npcs.constants.EnumJobType;
import noppes.npcs.constants.EnumMovingType;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobGuard;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.companion.CompanionGuard;

public class NPCAttackSelector implements IEntitySelector
{
	private EntityNPCInterface npc;
	
	public NPCAttackSelector(EntityNPCInterface npc){
		this.npc = npc;
	}
    /**
     * Return whether the specified entity is applicable to this filter.
     */
	@Override
    public boolean isEntityApplicable(Entity entity){
    	if(!entity.isEntityAlive() || entity == npc || npc.getDistanceToEntity(entity) > npc.stats.aggroRange || !(entity instanceof EntityLivingBase) || ((EntityLivingBase)entity).getHealth() < 1)
    		return false;
        if (this.npc.ai.directLOS && !this.npc.getEntitySenses().canSee(entity))
        	return false;
        
        if(!npc.stats.attackInvisible &&((EntityLivingBase)entity).isPotionActive(Potion.invisibility) && npc.getDistanceSqToEntity(entity) < 9)
        	return false;

    	//prevent the npc from going on an endless killing spree
    	if(!npc.isFollower() && npc.ai.returnToStart){ 
	    	int allowedDistance = npc.stats.aggroRange * 2;
	    	if(npc.ai.movingType == EnumMovingType.Wandering)
	    		allowedDistance += npc.ai.walkingRange;
	    	double distance = entity.getDistanceSq(npc.getStartXPos(), npc.getStartYPos(), npc.getStartZPos());
	    	if(npc.ai.movingType == EnumMovingType.MovingPath){
	    		int[] arr = npc.ai.getCurrentMovingPath();
		    	distance = entity.getDistanceSq(arr[0], arr[1], arr[2]);
	    	}
	    	
	    	if(distance > allowedDistance * allowedDistance)
	    		return false;
    	}

    	if(npc.advanced.job == EnumJobType.Guard && ((JobGuard)npc.jobInterface).isEntityApplicable(entity))
    		return true;
    	
    	if(npc.advanced.role == EnumRoleType.Companion){
    		RoleCompanion role = (RoleCompanion)npc.roleInterface;
    		if(role.job == EnumCompanionJobs.GUARD && ((CompanionGuard)role.jobInterface).isEntityApplicable(entity))
    			return true;
    	}
    	if(entity instanceof EntityPlayerMP){
    		if(npc.faction.isAggressiveToPlayer((EntityPlayer) entity)){
    			if(PixelmonHelper.Enabled && npc.advanced.job == EnumJobType.Spawner)
    				return PixelmonHelper.canBattle((EntityPlayerMP)entity, npc);
    			return !((EntityPlayerMP)entity).capabilities.disableDamage;
    		}
    		return false;
    	}

    	if(entity instanceof EntityNPCInterface){
    		if(((EntityNPCInterface)entity).isKilled())
    			return false;
    		if(npc.advanced.attackOtherFactions)
    			return npc.faction.isAggressiveToNpc((EntityNPCInterface)entity);
    	}
    	
        return false;
    }
}
