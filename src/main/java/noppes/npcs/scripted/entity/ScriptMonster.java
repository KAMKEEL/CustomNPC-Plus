package noppes.npcs.scripted.entity;

import net.minecraft.entity.monster.EntityMob;
import noppes.npcs.scripted.constants.EntityType;
import noppes.npcs.api.entity.IMonster;

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
