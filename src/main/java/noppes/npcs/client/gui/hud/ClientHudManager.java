package noppes.npcs.client.gui.hud;

import java.util.HashMap;

public class ClientHudManager {
    private static ClientHudManager instance;
    private HashMap<EnumHudComponent, HudComponent> hudComponents = new HashMap<>();

    private ClientHudManager() {
    }

    public static ClientHudManager getInstance() {
        if (instance == null)
            instance = new ClientHudManager();
        return instance;
    }

    public void registerHud(EnumHudComponent enumHudComponent, HudComponent hud) {
        hudComponents.put(enumHudComponent, hud);
    }

    public HashMap<EnumHudComponent, HudComponent> getHudComponents() {
        return hudComponents;
    }

    /**
     * Renders all enabled HUD components (normal mode).
     */
    public void renderAllHUDs(float partialTicks) {
        for (HudComponent hud : hudComponents.values()) {
            if (hud.enabled)
                hud.renderOnScreen(partialTicks);
        }
    }
}
