package noppes.npcs.scripted.event;

import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.interfaces.entity.IEntityLivingBase;

public class ScriptEventAttack extends ScriptEvent{

	private float damage;
	private IEntityLivingBase target;
	private boolean isRanged;
	
	public ScriptEventAttack(float damage, EntityLivingBase target, boolean isRanged){
		this.damage = damage;
		this.isRanged = isRanged;
		this.target = (IEntityLivingBase) NpcAPI.Instance().getIEntity(target);
	}
	
	/**
	 * @return The source of the damage
	 * @deprecated
	 */
	public IEntityLivingBase getTarget(){
		return target;
	}
	
	
	/**
	 * @return Returns the damage value
	 */
	public float getDamage(){
		return damage;
	}
	
	/**
	 * @param damage The new damage value
	 */
	public void setDamage(float damage){
		this.damage = damage;
	}
	
	/**
	 * @return Returns the damage type
	 */
	public boolean isRange(){
		return isRanged;
	}
}
