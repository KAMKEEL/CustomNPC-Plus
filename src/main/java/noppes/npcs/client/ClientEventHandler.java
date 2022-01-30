package noppes.npcs.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import noppes.npcs.client.gui.customoverlay.OverlayCustom;

public class ClientEventHandler {
    @SubscribeEvent
    public void onOverlayRender(RenderGameOverlayEvent.Pre event){
        for(OverlayCustom overlayCustom : Client.customOverlays.values()){
            overlayCustom.renderGameOverlay(event.partialTicks);
        }
    }
}
