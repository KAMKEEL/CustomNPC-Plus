package noppes.npcs.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.*;
import noppes.npcs.client.gui.customoverlay.OverlayCustom;
import noppes.npcs.client.renderer.RenderCNPCPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClientEventHandler {
    public static final RenderCNPCPlayer renderCNPCPlayer = new RenderCNPCPlayer();
    public static HashMap<Integer,Long> disabledButtonTimes = new HashMap<>();
    public static float partialHandTicks;

    public static float partialRenderTick;
    public static EntityPlayer renderingPlayer;
    public static RenderPlayer renderer;

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (Minecraft.getMinecraft().currentScreen != null)
            return;

        ArrayList<Integer> removeList = new ArrayList<>();
        for (Map.Entry<Integer,Long> entry : disabledButtonTimes.entrySet()) {
            if (entry.getValue() > 0) {
                if (entry.getKey() == event.button || entry.getKey() == -1) {
                    event.setCanceled(true);
                }
                disabledButtonTimes.put(entry.getKey(), entry.getValue() - 1);
            } else {
                removeList.add(entry.getKey());
            }
        }

        for (int i : removeList) {
            disabledButtonTimes.remove(i);
        }
    }

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
    public void onRenderPlayer(RenderPlayerEvent.Pre event) {
        ClientEventHandler.renderingPlayer = event.entityPlayer;
        ClientEventHandler.renderer = event.renderer;
        ClientEventHandler.partialRenderTick = event.partialRenderTick;
    }

    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Post event) {
        ClientEventHandler.renderingPlayer = null;

        if (hasOverlays(event.entityPlayer)) {
            try {
                Class<?> renderPlayerJBRA = Class.forName("JinRyuu.JBRA.RenderPlayerJBRA");
                if (renderPlayerJBRA.isInstance(event.renderer))
                    return;
            } catch (ClassNotFoundException ignored) {}

            if (!(event.renderer instanceof RenderCNPCPlayer)) {
                renderCNPCPlayer.tempRenderPartialTicks = event.partialRenderTick;
                double d0 = event.entityPlayer.lastTickPosX + (event.entityPlayer.posX - event.entityPlayer.lastTickPosX) * (double) event.partialRenderTick - RenderManager.renderPosX;
                double d1 = event.entityPlayer.lastTickPosY + (event.entityPlayer.posY - event.entityPlayer.lastTickPosY) * (double) event.partialRenderTick - RenderManager.renderPosY;
                double d2 = event.entityPlayer.lastTickPosZ + (event.entityPlayer.posZ - event.entityPlayer.lastTickPosZ) * (double) event.partialRenderTick - RenderManager.renderPosZ;
                float f1 = event.entityPlayer.prevRotationYaw + (event.entityPlayer.rotationYaw - event.entityPlayer.prevRotationYaw) * event.partialRenderTick;

                if (Minecraft.getMinecraft().thePlayer.equals(event.entityPlayer)) {
                    d0 = 0;
                    d1 = 0;
                    d2 = 0;
                }

                renderCNPCPlayer.doRender(event.entityPlayer, d0, d1, d2, f1, event.partialRenderTick);
            }
        }
    }

    @SubscribeEvent
    public void cancelSpecials(RenderPlayerEvent.Specials.Pre event) {
        if (event.renderer instanceof RenderCNPCPlayer) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void tryRenderDBC(RenderPlayerEvent.Specials.Post event) {
        if (hasOverlays(event.entityPlayer)) {
            try {
                Class<?> renderPlayerJBRA = Class.forName("JinRyuu.JBRA.RenderPlayerJBRA");
                if (!renderPlayerJBRA.isInstance(event.renderer))
                    return;
            } catch (ClassNotFoundException ignored) {
                return;
            }

            renderCNPCPlayer.renderDBCModel(event);
        }
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        partialHandTicks = event.partialTicks;
    }

    public static boolean hasOverlays(EntityPlayer player) {
        return Client.skinOverlays.containsKey(player.getUniqueID()) && Client.skinOverlays.get(player.getUniqueID()).values().size() > 0;
    }
}
