package noppes.npcs.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.*;
import noppes.npcs.client.gui.customoverlay.OverlayCustom;
import noppes.npcs.client.renderer.RenderCNPCPlayer;
import noppes.npcs.controllers.data.PlayerModelData;
import noppes.npcs.roles.JobPuppet;
import org.lwjgl.opengl.GL11;

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

    public void setInterpolatedAngles(PlayerModelData modelData) {
        float pi = (float) Math.PI * (modelData.fullAngles ? 2 : 1);
        if (!modelData.animate) {
            modelData.modelRotations[0] = modelData.rotationX * pi;
            modelData.modelRotations[1] = modelData.rotationY * pi;
            modelData.modelRotations[2] = modelData.rotationZ * pi;
        } else if (modelData.modelRotPartialTicks != ClientEventHandler.partialRenderTick) {
            modelData.modelRotPartialTicks = ClientEventHandler.partialRenderTick;
            if (modelData.rotationX - modelData.modelRotations[0] != 0 && modelData.rotationEnabledX) {
                modelData.modelRotations[0] = (modelData.rotationX - modelData.modelRotations[0]) * modelData.animRate / 10f + modelData.modelRotations[0];
            } else {
                modelData.modelRotations[0] = modelData.rotationX;
            }

            if (modelData.rotationY - modelData.modelRotations[1] != 0 && modelData.rotationEnabledY) {
                modelData.modelRotations[1] = (modelData.rotationY - modelData.modelRotations[1]) * modelData.animRate / 10f + modelData.modelRotations[1];
            } else {
                modelData.modelRotations[1] = modelData.rotationY;
            }

            if (modelData.rotationZ - modelData.modelRotations[2] != 0 && modelData.rotationEnabledZ) {
                modelData.modelRotations[2] = (modelData.rotationZ - modelData.modelRotations[2]) * modelData.animRate /10f + modelData.modelRotations[2];
            } else {
                modelData.modelRotations[2] = modelData.rotationZ;
            }
        }
    }

    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Pre event) {
        EntityPlayer player = event.entityPlayer;
        ClientEventHandler.renderingPlayer = player;
        ClientEventHandler.renderer = event.renderer;
        ClientEventHandler.partialRenderTick = event.partialRenderTick;

        if (Client.playerModelData.containsKey(player.getUniqueID())) {
            PlayerModelData data = Client.playerModelData.get(player.getUniqueID());
            if (data.enabled()) {
                GL11.glPushMatrix();
                this.setInterpolatedAngles(data);
                if (data.rotationEnabledX) {
                    GL11.glRotatef(data.modelRotations[0], 1, 0, 0);
                }
                if (data.rotationEnabledY) {
                    GL11.glRotatef(data.modelRotations[1], 0, 1, 0);
                }
                if (data.rotationEnabledZ) {
                    GL11.glRotatef(data.modelRotations[2], 0, 0, 1);
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Post event) {
        EntityPlayer player = event.entityPlayer;
        if (Client.playerModelData.containsKey(player.getUniqueID())) {
            PlayerModelData data = Client.playerModelData.get(player.getUniqueID());
            if (data.enabled()) {
                GL11.glPopMatrix();
            }
        }
        ClientEventHandler.renderingPlayer = null;

        if (hasOverlays(player)) {
            try {
                Class<?> renderPlayerJBRA = Class.forName("JinRyuu.JBRA.RenderPlayerJBRA");
                if (renderPlayerJBRA.isInstance(event.renderer))
                    return;
            } catch (ClassNotFoundException ignored) {}

            if (!(event.renderer instanceof RenderCNPCPlayer)) {
                renderCNPCPlayer.tempRenderPartialTicks = event.partialRenderTick;
                double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) event.partialRenderTick - RenderManager.renderPosX;
                double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) event.partialRenderTick - RenderManager.renderPosY;
                double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) event.partialRenderTick - RenderManager.renderPosZ;
                float f1 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * event.partialRenderTick;

                if (Minecraft.getMinecraft().thePlayer.equals(player)) {
                    d0 = 0;
                    d1 = 0;
                    d2 = 0;
                }

                renderCNPCPlayer.doRender(player, d0, d1, d2, f1, event.partialRenderTick);
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
