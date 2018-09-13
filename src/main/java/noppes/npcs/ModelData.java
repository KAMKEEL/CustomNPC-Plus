package noppes.npcs;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.entity.EntityNPCInterface;

public class ModelData extends ModelDataShared{
	
	public EntityLivingBase getEntity(EntityNPCInterface npc){
		if(entityClass == null)
			return null;
		if(entity == null){
			try {
				entity = entityClass.getConstructor(new Class[] {World.class}).newInstance(new Object[] {npc.worldObj});

				entity.readEntityFromNBT(extra);
				
				if(entity instanceof EntityLiving){
					EntityLiving living = (EntityLiving)entity;
					living.setCurrentItemOrArmor(0, npc.getHeldItem() != null?npc.getHeldItem():npc.getOffHand());
					living.setCurrentItemOrArmor(1, npc.inventory.armorItemInSlot(3));
					living.setCurrentItemOrArmor(2, npc.inventory.armorItemInSlot(2));
					living.setCurrentItemOrArmor(3, npc.inventory.armorItemInSlot(1));
					living.setCurrentItemOrArmor(4, npc.inventory.armorItemInSlot(0));
				}
				if(PixelmonHelper.isPixelmon(entity) && npc.worldObj.isRemote){
					if(extra.hasKey("Name"))
						PixelmonHelper.setName(entity, extra.getString("Name"));
					else
						PixelmonHelper.setName(entity, "Abra");
				}
			} catch (Exception e) {
			} 
		}
		return entity;
	}
	
	public ModelData copy(){
		ModelData data = new ModelData();
		data.readFromNBT(this.writeToNBT());
		return data;
	}
}
