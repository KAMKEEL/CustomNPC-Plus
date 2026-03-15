package noppes.npcs.client.controllers;

import noppes.npcs.CustomNpcs;
import noppes.npcs.controllers.ServerTagMapController;

import java.io.File;

public class ClientTagMapController extends ServerTagMapController {
    public static ClientTagMapController Instance;

    @Override
    public File getDir() {
        File dir = new File(CustomNpcs.Dir, "clones");
        if (!dir.exists())
            dir.mkdir();
        return dir;
    }
}
