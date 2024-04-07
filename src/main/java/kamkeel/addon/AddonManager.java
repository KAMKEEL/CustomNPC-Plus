package kamkeel.addon;

import kamkeel.addon.client.DBCClient;
import kamkeel.addon.client.GeckoAddonClient;

public class AddonManager {
    public static AddonManager Instance;

    // Manager class to easily disable mixin manipulated
    // addons for debugging and testing

    public AddonManager(){
        Instance = this;
        load();
    }

    public void load(){
        // Gecko Addon Initialization
        new GeckoAddon();
        new GeckoAddonClient();

        // DBC Addon
        new DBCAddon();
        new DBCClient();
    }
}
