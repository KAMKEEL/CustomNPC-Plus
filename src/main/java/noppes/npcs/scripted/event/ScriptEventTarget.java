package noppes.npcs.scripted.event;

import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.scripted.entity.ScriptLivingBase;

public class ScriptEventTarget extends ScriptEvent{

	private ScriptLivingBase target;
	
	public ScriptEventTarget(EntityLivingBase target){
		if(target != null)
			this.target = (ScriptLivingBase)ScriptController.Instance.getScriptForEntity(target);
	}
	

	/**
	 * @return The source of the damage
	 */
	public ScriptLivingBase getTarget(){
		return target;
	}

	public void setTarget(ScriptLivingBase target){
		this.target = target;
	}
}
