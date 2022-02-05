package noppes.npcs.scripted;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityMob;
import noppes.npcs.scripted.constants.EntityType;

public class ScriptMonster extends ScriptLiving{

	public ScriptMonster(EntityMob entity) {
		super(entity);
	}
	
	@Override
	public int getType(){
		return EntityType.MONSTER;
	}

	@Override
	public boolean typeOf(int type){
		return type == EntityType.MONSTER?true:super.typeOf(type);
	}

}
