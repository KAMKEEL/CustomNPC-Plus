package noppes.npcs.scripted.event;

import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.interfaces.entity.IEntityLivingBase;

public class ScriptEventTarget extends ScriptEvent {

	private IEntityLivingBase target;
	
	public ScriptEventTarget(EntityLivingBase target){
		if(target != null)
			this.target = (IEntityLivingBase) NpcAPI.Instance().getIEntity(target);
	}

	/**
	 * @return The source of the damage
	 */
	public IEntityLivingBase getTarget(){
		return target;
	}

	public void setTarget(IEntityLivingBase target){
		this.target = target;
	}
}
