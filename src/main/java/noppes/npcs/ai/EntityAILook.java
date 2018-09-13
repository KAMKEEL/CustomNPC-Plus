package noppes.npcs.ai;

import java.util.Iterator;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.constants.EnumStandingType;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAILook extends EntityAIBase{
    private final EntityNPCInterface npc;
    private int idle = 0;
    private double lookX;
    private double lookZ;
    boolean rotatebody;

    public EntityAILook(EntityNPCInterface npc){
        this.npc = npc;
        this.setMutexBits(AiMutex.LOOK);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute(){
        return !npc.isAttacking() && npc.getNavigator().noPath() && !npc.isPlayerSleeping() && npc.isEntityAlive();
    }
    
    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting(){
    	rotatebody = npc.ai.standingType == EnumStandingType.RotateBody || npc.ai.standingType == EnumStandingType.HeadRotation;
    }
    
    /**
     * Resets the task
     */
    @Override
    public void resetTask(){
    	rotatebody = false;
    }
    
    /**
     * Updates the task
     */
    @Override
    public void updateTask(){	
    	if(npc.ai.standingType == EnumStandingType.Stalking){
    		EntityPlayer player = npc.worldObj.getClosestPlayerToEntity(npc, 16);
    		if(player == null)
    			rotatebody = true;
    		else
    			npc.getLookHelper().setLookPositionWithEntity(player, 10F, npc.getVerticalFaceSpeed());
    	}
    	if(npc.isInteracting()){
    		Iterator<EntityLivingBase> ita = npc.interactingEntities.iterator();
    		EntityLivingBase closest = null;
    		double closestDistance = 12;
    		while(ita.hasNext()){
    			EntityLivingBase entity = ita.next();
    			double distance = entity.getDistanceSqToEntity(npc);
    			if(distance < closestDistance){
    				closestDistance = entity.getDistanceSqToEntity(npc);
    				closest = entity;
    			}
    			else if(distance > 12)
    				ita.remove();
    		}
    		if(closest != null){
    			npc.getLookHelper().setLookPositionWithEntity(closest, 10F, npc.getVerticalFaceSpeed());
    			return;
    		}
    		
    	}
    	
    	if(rotatebody){
	    	if(idle == 0 && npc.getRNG().nextFloat() < 0.02F){
	            double var1 = (Math.PI * 2D) * this.npc.getRNG().nextDouble();
	        	if(npc.ai.standingType == EnumStandingType.HeadRotation)
	        		var1 = Math.PI / 180 * npc.ai.orientation + Math.PI * 0.2 + Math.PI * 0.6 * this.npc.getRNG().nextDouble();
	            this.lookX = Math.cos(var1);
	            this.lookZ = Math.sin(var1);
	            this.idle = 20 + this.npc.getRNG().nextInt(20);
	    	}
	    	if(idle > 0){
	    		idle--;
	            npc.getLookHelper().setLookPosition(npc.posX + lookX, npc.posY + npc.getEyeHeight(), npc.posZ + lookZ, 10F, npc.getVerticalFaceSpeed());
	    	}
    	}
    	
    	if(npc.ai.standingType == EnumStandingType.NoRotation){
    		npc.rotationYawHead = npc.rotationYaw = npc.renderYawOffset = npc.ai.orientation;
    	}
    }
}
