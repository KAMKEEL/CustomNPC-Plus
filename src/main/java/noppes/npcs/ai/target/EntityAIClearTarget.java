package noppes.npcs.ai.target;

import java.util.Collections;
import java.util.List;

import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIClearTarget extends EntityAITarget
{
	private EntityNPCInterface npc;
	private EntityLivingBase target;
    public EntityAIClearTarget(EntityNPCInterface npc){
    	super(npc, false);
    	this.npc = npc;
    }

    @Override
    public boolean shouldExecute(){
    	target = taskOwner.getAttackTarget();
        if (target == null)
            return false;
        
        if(target instanceof EntityPlayer && ((EntityPlayer)target).capabilities.disableDamage)
        	return true;
        
        int distance = npc.stats.aggroRange * 2 * npc.stats.aggroRange;
        if(npc.getOwner() != null && npc.getDistanceSqToEntity(npc.getOwner()) > distance){
        	return true;
        }
        
        return npc.getDistanceSqToEntity(target) > distance;
    }

    @Override
    public void startExecuting(){
        this.taskOwner.setAttackTarget(null);
        if(target == taskOwner.getAITarget())
        	this.taskOwner.setRevengeTarget(null);
        super.startExecuting();
    }
}
