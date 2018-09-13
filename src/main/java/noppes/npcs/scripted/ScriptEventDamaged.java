package noppes.npcs.scripted;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import noppes.npcs.controllers.ScriptController;

public class ScriptEventDamaged extends ScriptEvent{

	private float damage;
	private boolean clear = false;
	private ScriptLivingBase source;
	private DamageSource damagesource;
	
	public ScriptEventDamaged(float damage, EntityLivingBase attackingEntity, DamageSource damagesource){
		this.damage = damage;
		this.damagesource = damagesource;
		this.source = (ScriptLivingBase)ScriptController.Instance.getScriptForEntity(attackingEntity);
	}
	
	/**
	 * @return The source of the damage
	 */
	public ScriptLivingBase getSource(){
		return source;
	}
	
	public void setClearTarget(boolean bo){
		this.clear = bo;
	}
	
	public boolean getClearTarget(){
		return clear;
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
	public String getType(){
		return damagesource.damageType;
	}
}
