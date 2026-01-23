package noppes.npcs.config;

import java.io.File;

public class LoadConfiguration {
    public static File mainConfigFile;
    public static File itemConfigFile;
    public static File scriptConfigFile;
    public static File debugConfigFile;
    public static File clientConfigFile;
    public static File experimentalConfigFile;
    public static File marketConfigFile;

    public static void init(String configpath) {
        mainConfigFile = new File(configpath + "main.cfg");
        itemConfigFile = new File(configpath + "item.cfg");
        clientConfigFile = new File(configpath + "client.cfg");
        scriptConfigFile = new File(configpath + "script.cfg");
        debugConfigFile = new File(configpath + "debug.cfg");
        experimentalConfigFile = new File(configpath + "experimental.cfg");
        marketConfigFile = new File(configpath + "market.cfg");

        ConfigMain.init(mainConfigFile);
        ConfigItem.init(itemConfigFile);
        ConfigClient.init(clientConfigFile);
        ConfigScript.init(scriptConfigFile);
        ConfigDebug.init(debugConfigFile);
        ConfigExperimental.init(experimentalConfigFile);
        ConfigMarket.init(marketConfigFile);
    }
}
