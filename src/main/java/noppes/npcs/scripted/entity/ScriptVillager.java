package noppes.npcs.scripted.entity;

import net.minecraft.entity.passive.EntityVillager;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IVillager;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.constants.EntityType;

public class ScriptVillager<T extends EntityVillager> extends ScriptLiving<T> implements IVillager {

	public ScriptVillager(T entity) {
		super(entity);
	}

    @Override
    public int getProfession() {
        return entity.getProfession();
    }

    @Override
    public boolean getIsTrading() {
        return entity.isTrading();
    }

    @Override
    public IEntityLivingBase getCustomer()
    {
        return (IEntityLivingBase) NpcAPI.Instance().getPlayer(entity.getCustomer().getCommandSenderName());
    }

	@Override
	public int getType(){
		return EntityType.VILLAGER;
	}

    @Override
    public boolean typeOf(int type){
        return type == EntityType.VILLAGER || super.typeOf(type);
    }

}
