package noppes.npcs.scripted;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import noppes.npcs.controllers.ScriptController;

public class ScriptEventAttack extends ScriptEvent{

	private float damage;
	private ScriptLivingBase target;
	private boolean isRanged;
	
	public ScriptEventAttack(float damage, EntityLivingBase target, boolean isRanged){
		this.damage = damage;
		this.isRanged = isRanged;
		this.target = (ScriptLivingBase)ScriptController.Instance.getScriptForEntity(target);
	}
	
	/**
	 * @return The source of the damage
	 * @deprecated
	 */
	public ScriptLivingBase getTarget(){
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
