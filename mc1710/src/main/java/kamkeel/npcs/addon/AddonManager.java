package kamkeel.npcs.addon;

import kamkeel.npcs.addon.client.DBCClient;
import kamkeel.npcs.addon.client.GeckoAddonClient;

public class AddonManager {
    public static AddonManager Instance;

    // Manager class to easily disable mixin manipulated
    // addons for debugging and testing

    public AddonManager() {
        Instance = this;
        load();
    }

    public void load() {
        // Gecko Addon Initialization
        new GeckoAddon();
        new GeckoAddonClient();

        // DBC Addon
        new DBCAddon();
        new DBCClient();
    }
}
