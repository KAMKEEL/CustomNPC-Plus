package noppes.npcs.scripted.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.interfaces.IDamageSource;
import noppes.npcs.scripted.interfaces.entity.IEntityLivingBase;

public class ScriptEventKilled extends ScriptEvent{
	private IEntityLivingBase source;
	private DamageSource damagesource;
	
	public ScriptEventKilled(EntityLivingBase target, DamageSource damagesource){
		this.damagesource = damagesource;
		this.source = (IEntityLivingBase) NpcAPI.Instance().getIEntity(target);
	}

	/**
	 * @return The source of the damage
	 */
	public IEntityLivingBase getSource(){
		return source;
	}

	public IDamageSource getDamageSource() { return NpcAPI.Instance().getIDamageSource(damagesource); }

	/**
	 * @return Returns the damage type
	 */
	public String getType(){
		return damagesource.damageType;
	}
}
