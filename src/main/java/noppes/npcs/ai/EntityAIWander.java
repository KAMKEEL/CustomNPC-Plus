package noppes.npcs.ai;

import java.util.Iterator;
import java.util.List;

import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ai.selector.NPCInteractSelector;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.controllers.Line;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIWander extends EntityAIBase
{
    private EntityNPCInterface entity;
    public final IEntitySelector selector;
    private double xPosition;
    private double yPosition;
    private double zPosition;
    private EntityNPCInterface nearbyNPC;

    public EntityAIWander(EntityNPCInterface npc){
        this.entity = npc;
        this.setMutexBits(AiMutex.PASSIVE);
        selector = new NPCInteractSelector(npc);
    }

    @Override
    public boolean shouldExecute(){
        if (this.entity.getAge() >= 100 || !entity.getNavigator().noPath() || entity.isInteracting() || this.entity.getRNG().nextInt(80) != 0){
            return false;
        }
        if(entity.ai.npcInteracting && entity.getRNG().nextInt(4) == 1)
        	nearbyNPC = getNearbyNPC();
        
        if(nearbyNPC != null){
            this.xPosition = MathHelper.floor_double(nearbyNPC.posX);
            this.yPosition = MathHelper.floor_double(nearbyNPC.posY);
            this.zPosition = MathHelper.floor_double(nearbyNPC.posZ);
    		nearbyNPC.addInteract(entity);
        }
        else{
        	Vec3 vec = getVec();
            if (vec == null){
                return false;
            }
            else{
                this.xPosition = vec.xCoord;
                this.yPosition = vec.yCoord;
                this.zPosition = vec.zCoord;
            }
        }
        return true;
    }

    @Override
    public void updateTask() {
    	if(nearbyNPC != null){
    		nearbyNPC.getNavigator().clearPathEntity();
    	}
    }
    private EntityNPCInterface getNearbyNPC() {
		List<EntityNPCInterface> list = entity.worldObj.getEntitiesWithinAABBExcludingEntity(entity, entity.boundingBox.expand(entity.ai.walkingRange, entity.ai.walkingRange > 7?7:entity.ai.walkingRange, entity.ai.walkingRange), selector);
		Iterator<EntityNPCInterface> ita = list.iterator();
		while(ita.hasNext()){
			EntityNPCInterface npc = ita.next();
			if(!npc.ai.stopAndInteract || npc.isAttacking() || !npc.isEntityAlive() || entity.faction.isAggressiveToNpc(npc))
				ita.remove();
		}
		
		if(list.isEmpty())
			return null;
		
		return list.get(entity.getRNG().nextInt(list.size()));
	}

	private Vec3 getVec(){
    	if(entity.ai.walkingRange > 0){
            double distance = this.entity.getDistanceSq(this.entity.getStartXPos(), this.entity.getStartYPos(), this.entity.getStartZPos());
            int range = (int) MathHelper.sqrt_double(this.entity.ai.walkingRange * this.entity.ai.walkingRange - distance);
            if(range > CustomNpcs.NpcNavRange)
            	range = CustomNpcs.NpcNavRange;
            if(range < 3){
                range = this.entity.ai.walkingRange;
                if(range > CustomNpcs.NpcNavRange)
                	range = CustomNpcs.NpcNavRange;
                Vec3 start = Vec3.createVectorHelper(this.entity.getStartXPos(), this.entity.getStartYPos(), this.entity.getStartZPos());
                return RandomPositionGeneratorAlt.findRandomTargetBlockTowards(this.entity, range / 2, range / 2 > 7?7:range / 2, start);
            }
            else{
                return RandomPositionGeneratorAlt.findRandomTarget(this.entity, range, range / 2 > 7?7:range / 2);
            }
    	}
    	return RandomPositionGeneratorAlt.findRandomTarget(this.entity, CustomNpcs.NpcNavRange, 7);
    }

    @Override
    public boolean continueExecuting(){
    	if(nearbyNPC != null && (!selector.isEntityApplicable(nearbyNPC) || entity.getDistanceSqToEntity(nearbyNPC) < entity.width * 1.5))
    		return false;
        return !this.entity.getNavigator().noPath() && this.entity.isEntityAlive() && !entity.isInteracting();
    }

    @Override
    public void startExecuting(){
        this.entity.getNavigator().tryMoveToXYZ(this.xPosition, this.yPosition, this.zPosition, 1);
    }

    @Override
    public void resetTask() {
    	if(nearbyNPC != null && entity.getDistanceSqToEntity(nearbyNPC) < 12){
    		Line line = new Line(".........");
    		line.hideText = true;
    		if(entity.getRNG().nextBoolean())
    			entity.saySurrounding(line);
    		else
    			nearbyNPC.saySurrounding(line);
    		
    		entity.addInteract(nearbyNPC);
    		nearbyNPC.addInteract(entity);
    	}
    	nearbyNPC = null;
    }
}
