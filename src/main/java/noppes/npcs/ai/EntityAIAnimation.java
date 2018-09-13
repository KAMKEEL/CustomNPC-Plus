package noppes.npcs.ai;

import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.constants.EnumMovingType;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIAnimation extends EntityAIBase
{
    private EntityNPCInterface npc;

    private boolean isAttacking = false;
    private boolean isDead = false;
    private boolean isAtStartpoint = false;
    private boolean hasPath = false;
    private int tick = 4;

    public EntityAIAnimation(EntityNPCInterface npc){
        this.npc = npc;
    }

    @Override
    public boolean shouldExecute(){
    	isDead = !npc.isEntityAlive();
    	if(isDead)
    		return npc.currentAnimation != EnumAnimation.LYING;
    	
    	if(npc.stats.aimWhileShooting && npc.isAttacking())
    		return npc.currentAnimation != EnumAnimation.AIMING;
    	if(npc.ai.animationType == EnumAnimation.NONE)
    		return npc.currentAnimation != EnumAnimation.NONE;
    	isAttacking = npc.isAttacking();
    	if(npc.ai.returnToStart)
    		isAtStartpoint = npc.isVeryNearAssignedPlace();
    	hasPath = !npc.getNavigator().noPath();
    	
    	if(npc.ai.movingType == EnumMovingType.Standing && hasNavigation() && npc.currentAnimation.getWalkingAnimation() == 0){
    		return npc.currentAnimation != EnumAnimation.NONE;
    	}
    	
    	return npc.currentAnimation != npc.ai.animationType;
    }

    @Override
    public void updateTask(){
    	if(npc.stats.aimWhileShooting && npc.isAttacking()){
    		setAnimation(EnumAnimation.AIMING);
    		return;
    	}
    	EnumAnimation type = npc.ai.animationType;
    	if(isDead)
    		type = EnumAnimation.LYING;
    	else if(npc.ai.movingType == EnumMovingType.Standing && npc.ai.animationType.getWalkingAnimation() == 0 && hasNavigation())
    		type = EnumAnimation.NONE;
		setAnimation(type);
    }
    
    private void setAnimation(EnumAnimation animation){
    	npc.setCurrentAnimation(animation);
    	npc.updateHitbox();
    	npc.setPosition(npc.posX, npc.posY, npc.posZ);
    }
    
    private boolean hasNavigation() {
    	return (isAttacking || npc.ai.returnToStart && !isAtStartpoint && !npc.isFollower() || hasPath);
    }
}
