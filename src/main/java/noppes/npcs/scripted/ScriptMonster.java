package noppes.npcs.scripted;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityMob;
import noppes.npcs.scripted.constants.EntityType;

public class ScriptMonster<T extends EntityMob> extends ScriptLiving<T> implements IMonster {

	public ScriptMonster(T entity) {
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
