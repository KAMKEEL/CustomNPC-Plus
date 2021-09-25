package noppes.npcs.client.controllers;

import noppes.npcs.CustomNpcs;
import noppes.npcs.controllers.ServerCloneController;

import java.io.File;

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
