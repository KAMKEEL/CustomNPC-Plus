package noppes.npcs.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemArmor;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import noppes.npcs.client.gui.OverlayQuestTracking;
import noppes.npcs.client.gui.customoverlay.OverlayCustom;
import noppes.npcs.client.renderer.RenderCNPCPlayer;
import org.lwjgl.opengl.GL11;

public class ClientEventHandler {
    RenderCNPCPlayer renderCNPCPlayer = new RenderCNPCPlayer();

    @SubscribeEvent
    public void onOverlayRender(RenderGameOverlayEvent.Post event){
        if(event.type == RenderGameOverlayEvent.ElementType.ALL) {
            for (OverlayCustom overlayCustom : Client.customOverlays.values()) {
                overlayCustom.renderGameOverlay(event.partialTicks);
            }

            if (Client.questTrackingOverlay != null) {
                Client.questTrackingOverlay.renderGameOverlay(event.partialTicks);
            }
        }
    }

    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Post event) {
        if (!(event.renderer instanceof RenderCNPCPlayer)) {
            renderCNPCPlayer.tempRenderPartialTicks = event.partialRenderTick;
            renderCNPCPlayer.doRender(event.entityPlayer, 0, 0, 0, 0.0F, event.partialRenderTick);
        }
    }

    @SubscribeEvent
    public void cancelSpecials(RenderPlayerEvent.Specials event) {
        if (event.renderer instanceof RenderCNPCPlayer)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void cancelSpecials(RenderPlayerEvent.SetArmorModel event) {
        if (event.renderer instanceof RenderCNPCPlayer && event.stack != null && event.stack.getItem() instanceof ItemArmor)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null && mc.theWorld != null && !mc.isGamePaused() && event.phase == TickEvent.Phase.END) {
            renderCNPCPlayer.itemRenderer.updateEquippedItem();
            renderCNPCPlayer.updateFovModifierHand();
        }
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        if (Client.skinOverlays.containsKey(Minecraft.getMinecraft().thePlayer.getUniqueID()) && Client.skinOverlays.get(Minecraft.getMinecraft().thePlayer.getUniqueID()).values().size() > 0) {
            GL11.glPushMatrix();
                event.setCanceled(true);
                GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
                renderCNPCPlayer.renderHand(event.partialTicks, event.renderPass);
            GL11.glPopMatrix();
        } else {
            event.setCanceled(false);
        }
    }
}
