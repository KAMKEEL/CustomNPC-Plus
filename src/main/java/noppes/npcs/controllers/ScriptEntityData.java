package noppes.npcs.controllers;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import noppes.npcs.api.entity.IEntity;

public class ScriptEntityData implements IExtendedEntityProperties{
	public IEntity base;
	public ScriptEntityData(IEntity base){
		this.base = base;
	}

	@Override
	public void saveNBTData(NBTTagCompound compound) {
	}

	@Override
	public void loadNBTData(NBTTagCompound compound) {		
	}

	@Override
	public void init(Entity entity, World world) {
	}
}
