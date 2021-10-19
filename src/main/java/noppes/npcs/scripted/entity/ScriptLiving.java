package noppes.npcs.scripted.entity;

import net.minecraft.entity.EntityLiving;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.scripted.interfaces.IEntityLiving;

public class ScriptLiving<T extends EntityLiving> extends ScriptLivingBase<T> implements IEntityLiving {

	private T entity;
	public ScriptLiving(T entity) {
		super(entity);
		this.entity = entity;
	}

	@Override
	public boolean isAttacking(){
		return super.isAttacking() || entity.getAttackTarget() != null;
	}

	@Override
	public void setAttackTarget(ScriptLivingBase living){
		if(living == null)
			entity.setAttackTarget(null);
		else
			entity.setAttackTarget(living.entity);
		super.setAttackTarget(living);
	}

	@Override
	public ScriptLivingBase getAttackTarget(){
		ScriptLivingBase base = (ScriptLivingBase)ScriptController.Instance.getScriptForEntity(entity.getAttackTarget());
		return (base != null)? base: super.getAttackTarget();
	}
	
	/**
	 * Start path finding toward this target
	 * @param x Destination x position
	 * @param y Destination x position
	 * @param z Destination x position
	 * @param speed Walking speed of the entity 0.7 is default
	 */
	public void navigateTo(double x, double y, double z, double speed){
		entity.getNavigator().tryMoveToXYZ(x, y, z, speed);
	}
	
	/**
	 * Stop navigating wherever this npc was walking to
	 */
	public void clearNavigation(){
		entity.getNavigator().clearPathEntity();
	}
	
	/**
	 * @return Whether or not this entity is navigating somewhere
	 */
	public boolean isNavigating(){
		return !entity.getNavigator().noPath();
	}
	
	@Override
	public boolean canSeeEntity(ScriptEntity entity){
		return this.entity.getEntitySenses().canSee(entity.entity);
	}
}
