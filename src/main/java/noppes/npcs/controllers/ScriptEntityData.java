package noppes.npcs.controllers;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import noppes.npcs.scripted.ScriptEntity;

public class ScriptEntityData implements IExtendedEntityProperties{
	public ScriptEntity base;
	public ScriptEntityData(ScriptEntity base){
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
