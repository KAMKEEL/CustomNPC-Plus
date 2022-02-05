package noppes.npcs;

import java.util.HashMap;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;


public class ModelDataShared{
	public ModelPartConfig arms = new ModelPartConfig();
	public ModelPartConfig body = new ModelPartConfig();
	public ModelPartConfig legs = new ModelPartConfig();
	public ModelPartConfig head = new ModelPartConfig();

	public ModelPartData legParts = new ModelPartData();
	
	public Class<? extends EntityLivingBase> entityClass;
	public EntityLivingBase entity;
	
	public NBTTagCompound extra = new NBTTagCompound();
	
	private HashMap<String,ModelPartData> parts = new HashMap<String,ModelPartData>();
	public byte breasts = 0;
	
	public byte headwear = 2;
			
	public NBTTagCompound writeToNBT(){
		NBTTagCompound compound = new NBTTagCompound();

		if(entityClass != null)
			compound.setString("EntityClass", entityClass.getCanonicalName());

		compound.setTag("ArmsConfig", arms.writeToNBT());
		compound.setTag("BodyConfig", body.writeToNBT());
		compound.setTag("LegsConfig", legs.writeToNBT());
		compound.setTag("HeadConfig", head.writeToNBT());

		compound.setTag("LegParts", legParts.writeToNBT());

		compound.setByte("Headwear", headwear);
		compound.setByte("Breasts", breasts);
		compound.setTag("ExtraData", extra);
		
		NBTTagList list = new NBTTagList();
		for(String name : parts.keySet()){
			NBTTagCompound item = parts.get(name).writeToNBT();
			item.setString("PartName", name);
			list.appendTag(item);
		}
		compound.setTag("Parts", list);
		
		return compound;
	}
	
	public void readFromNBT(NBTTagCompound compound){
		setEntityClass(compound.getString("EntityClass"));
		
		arms.readFromNBT(compound.getCompoundTag("ArmsConfig"));
		body.readFromNBT(compound.getCompoundTag("BodyConfig"));
		legs.readFromNBT(compound.getCompoundTag("LegsConfig"));
		head.readFromNBT(compound.getCompoundTag("HeadConfig"));

		legParts.readFromNBT(compound.getCompoundTag("LegParts"));

		headwear = compound.getByte("Headwear");
		breasts = compound.getByte("Breasts");
		extra = compound.getCompoundTag("ExtraData");
				
		HashMap<String,ModelPartData> parts = new HashMap<String,ModelPartData>();
		NBTTagList list = compound.getTagList("Parts", 10);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound item = list.getCompoundTagAt(i);
			ModelPartData part = new ModelPartData();
			part.readFromNBT(item);
			parts.put(item.getString("PartName"), part);
		}
		this.parts = parts;
		
	}

	private void setEntityClass(String string) {
		entityClass = null;
		entity = null;
		try {
			Class<?> cls = Class.forName(string);
            if (EntityLivingBase.class.isAssignableFrom(cls)) 
            	entityClass = cls.asSubclass(EntityLivingBase.class);
            
		} catch (ClassNotFoundException e) {
			
		}
	}
	
	public void setEntityClass(Class<? extends EntityLivingBase> entityClass){
		this.entityClass = entityClass;
		entity = null;
		extra = new NBTTagCompound();
		if(entityClass == EntityHorse.class)
			extra.setInteger("Type", -1);
	}
	
	public Class<? extends EntityLivingBase> getEntityClass(){
		return entityClass;
	}

	public float offsetY() {
		if(entity == null)
			return -getBodyY();
		return entity.height - 1.8f;
	}

	public void clearEntity() {
		entity = null;
	}
	
	
	public ModelPartData getPartData(String type){
		return parts.get(type);
	}

	public void removePart(String type) {
		parts.remove(type);
	}

	public ModelPartData getOrCreatePart(String type) {
		ModelPartData part = parts.get(type);
		if(part == null)
			parts.put(type, part = new ModelPartData());
		return part;
	}
	
	public float getBodyY(){
		if(legParts.type == 3)
			return (0.9f - body.scaleY) * 0.75f + getLegsY();
		if(legParts.type == 3)
			return (0.5f - body.scaleY) * 0.75f + getLegsY();
		return (1 - body.scaleY) * 0.75f + getLegsY();
	}

	public float getLegsY() {
		if(legParts.type == 3)
			return (0.87f - legs.scaleY) * 1f;
		return (1 - legs.scaleY) * 0.75f;
	}
}
