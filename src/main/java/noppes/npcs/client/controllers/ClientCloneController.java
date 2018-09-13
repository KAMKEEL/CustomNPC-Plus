package noppes.npcs.client.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.controllers.ServerCloneController;

public class ClientCloneController extends ServerCloneController{
	public static ClientCloneController Instance;

	@Override
	public File getDir(){
		File dir = new File(CustomNpcs.Dir,"clones");
		if(!dir.exists())
			dir.mkdir();
		return dir;
	}
}
