package noppes.npcs.scripted.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.interfaces.entity.IEntityLivingBase;

public class ScriptEventDamaged extends ScriptEvent{

	private float damage;
	private boolean clear = false;
	private IEntityLivingBase source;
	private DamageSource damagesource;
	
	public ScriptEventDamaged(float damage, EntityLivingBase attackingEntity, DamageSource damagesource){
		this.damage = damage;
		this.damagesource = damagesource;
		this.source = (IEntityLivingBase) NpcAPI.Instance().getIEntity(attackingEntity);
	}
	
	/**
	 * @return The source of the damage
	 */
	public IEntityLivingBase getSource(){
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
