package noppes.npcs.entity.data;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.entity.data.IModelData;
import noppes.npcs.api.entity.data.IModelRotate;
import noppes.npcs.api.entity.data.IModelScale;
import noppes.npcs.util.ValueUtil;

import java.util.HashMap;


public class ModelDataShared implements IModelData {
	public ModelScale modelScale = new ModelScale();

	public boolean enableRotation = false;
	public ModelRotate rotation = new ModelRotate();

	public ModelPartData legParts = new ModelPartData();
	
	public Class<? extends EntityLivingBase> entityClass;
	public EntityLivingBase entity;
	
	public NBTTagCompound extra = new NBTTagCompound();
	
	private HashMap<String,ModelPartData> parts = new HashMap<String,ModelPartData>();
	public byte breasts = 0;

	public byte headwear = 2;
	public byte bodywear = 0;
	public byte armwear = 0;
	public byte legwear = 0;

	// Solid or normal arm/legwear [0: None, 1: Both, 2: Right, 3: Left]
	public byte solidArmwear = 0;
	public byte solidLegwear = 0;

	// Hide Body Parts [0: None, 1: Both, 2: Right, 3: Left],
	public byte hideHead = 0;
	public byte hideBody = 0;
	public byte hideArms = 0;
	public byte hideLegs = 0;

	public NBTTagCompound writeToNBT(){
		NBTTagCompound compound = new NBTTagCompound();

		if(entityClass != null)
			compound.setString("EntityClass", entityClass.getCanonicalName());

		compound = this.modelScale.writeToNBT(compound);

		compound.setTag("LegParts", legParts.writeToNBT());

		compound.setByte("Headwear", headwear);
		compound.setByte("Bodywear", bodywear);
		compound.setByte("Armwear", armwear);
		compound.setByte("Legwear", legwear);

		compound.setByte("SolidArmwear", solidArmwear);
		compound.setByte("SolidLegwear", solidLegwear);

		compound.setByte("hideHead", hideHead);
		compound.setByte("hideBody", hideBody);
		compound.setByte("hideArms", hideArms);
		compound.setByte("hideLegs", hideLegs);

		compound.setBoolean("EnableRotation", enableRotation);
		if(enableRotation)
			compound.setTag("ModelRotation", rotation.writeToNBT());

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
		setEntity(compound.getString("EntityClass"));

		this.modelScale.readFromNBT(compound);

		legParts.readFromNBT(compound.getCompoundTag("LegParts"));

		headwear = compound.getByte("Headwear");
		bodywear = compound.getByte("Bodywear");
		armwear = compound.getByte("Armwear");
		legwear = compound.getByte("Legwear");

		solidArmwear = compound.getByte("SolidArmwear");
		solidLegwear = compound.getByte("SolidLegwear");

		hideHead = compound.getByte("hideHead");
		hideBody = compound.getByte("hideBody");
		hideArms = compound.getByte("hideArms");
		hideLegs = compound.getByte("hideLegs");

		enableRotation = compound.getBoolean("EnableRotation");
		if(enableRotation)
			rotation.readFromNBT(compound.getCompoundTag("ModelRotation"));

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
			return (0.9f - modelScale.body.scaleY) * 0.75f + getLegsY();
		if(legParts.type == 3)
			return (0.5f - modelScale.body.scaleY) * 0.75f + getLegsY();
		return (1 - modelScale.body.scaleY) * 0.75f + getLegsY();
	}

	public float getLegsY() {
		if(legParts.type == 3)
			return (0.87f - modelScale.legs.scaleY);
		return (1 - modelScale.legs.scaleY) * 0.75f;
	}

	// Hide Body Parts
	// [0: Head, 1: Body, 2: Arms, 3: Legs]
	// [0: None, 1: Both, 2: Right, 3: Left]
	public void hidePart(int part, byte hide) {
		part = ValueUtil.clamp(part,0,3);
		hide = part > 1 ? ValueUtil.clamp(hide,(byte) 0,(byte) 3) : ValueUtil.clamp(hide,(byte) 0,(byte) 1);
		switch (part) {
			case 0:
				this.hideHead = hide;
				break;
			case 1:
				this.hideBody = hide;
				break;
			case 2:
				this.hideArms = hide;
				break;
			case 3:
				this.hideLegs = hide;
				break;
		}
	}

	public int hidden(int part) {
		part = ValueUtil.clamp(part,0,3);
		switch (part) {
			case 0:
				return this.hideHead;
			case 1:
				return this.hideBody;
			case 2:
				return this.hideArms;
			case 3:
				return this.hideLegs;
		}
		return 0;
	}

	public void enableRotation(boolean enableRotation) {
		this.enableRotation = enableRotation;
	}

	public boolean enableRotation() {
		return this.enableRotation;
	}

	public IModelRotate getRotation() {
		return this.rotation;
	}

	public IModelScale getScale() {
		return this.modelScale;
	}

	public void setEntity(String string) {
		entityClass = null;
		entity = null;
		try {
			Class<?> cls = Class.forName(string);
			if (EntityLivingBase.class.isAssignableFrom(cls))
				entityClass = cls.asSubclass(EntityLivingBase.class);

		} catch (ClassNotFoundException ignored) {}
	}

	public String getEntity() {
		return entityClass == null ? null : entityClass.getName();
	}
}
