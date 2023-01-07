package noppes.npcs.config;

import noppes.npcs.CustomNpcs;
import noppes.npcs.config.legacy.ConfigProp;
import noppes.npcs.config.legacy.LegacyConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedList;

public class LoadConfiguration {
    public static File mainConfigFile;
    public static File scriptConfigFile;
    public static File debugConfigFile;
    public static File clientConfigFile;

    public static void init(String configpath)
    {
        mainConfigFile = new File(configpath + "main.cfg");
        clientConfigFile = new File(configpath + "client.cfg");
        scriptConfigFile = new File(configpath + "script.cfg");
        debugConfigFile = new File(configpath + "debug.cfg");

        ConfigMain.init(mainConfigFile);
        ConfigClient.init(clientConfigFile);
        ConfigScript.init(scriptConfigFile);
        ConfigDebug.init(debugConfigFile);
    }
}
