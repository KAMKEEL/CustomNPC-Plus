package noppes.npcs.scripted;

import net.minecraft.entity.passive.EntityAnimal;
import noppes.npcs.scripted.constants.EntityType;

public class ScriptAnimal extends ScriptLiving{
	
	public ScriptAnimal(EntityAnimal entity) {
		super(entity);
	}
	
	@Override
	public int getType(){
		return EntityType.ANIMAL;
	}

	@Override
	public boolean typeOf(int type){
		return type == EntityType.ANIMAL?true:super.typeOf(type);
	}
}
