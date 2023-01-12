package noppes.npcs.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.*;
import noppes.npcs.client.gui.customoverlay.OverlayCustom;
import noppes.npcs.client.renderer.RenderCNPCPlayer;
import noppes.npcs.controllers.data.PlayerModelData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobPuppet;
import noppes.npcs.roles.PartConfig;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClientEventHandler {
    public static final RenderCNPCPlayer renderCNPCPlayer = new RenderCNPCPlayer();
    public static HashMap<Integer,Long> disabledButtonTimes = new HashMap<>();
    public static float partialHandTicks;

    public static float partialRenderTick;
    public static EntityNPCInterface renderingNpc;
    public static EntityPlayer renderingPlayer;
    public static RendererLivingEntity renderer;

    public static boolean isPart(HashMap<PartConfig, String> modelNameMap, PartConfig puppetPart, String partName) {
        return !puppetPart.disabled && partName.equals(modelNameMap.get(puppetPart));
    }

    public static String getPartName(ModelRenderer renderer, HashMap<String,String[]> partNames) {
        Class<?> RenderClass = renderer.baseModel.getClass();
        Object model = renderer.baseModel;
        String returnName = "";

        while (returnName.isEmpty()) {
            for (Field f : RenderClass.getDeclaredFields()) {
                f.setAccessible(true);
                try {
                    if (renderer == f.get(model)) {
                        int i = 0;
                        break;
                    }
                } catch (Exception ignored) {
                }
            }

            for (Map.Entry<String, String[]> entry : partNames.entrySet()) {
                String[] names = entry.getValue();
                for (String partName : names) {
                    try {
                        Field field = RenderClass.getDeclaredField(partName);
                        field.setAccessible(true);
                        if (renderer == field.get(model)) {
                            returnName = entry.getKey();
                            break;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            if (RenderClass == ModelBase.class || RenderClass.getSuperclass() == null) {
                break;
            }
            RenderClass = RenderClass.getSuperclass();
        }

        return returnName;
    }

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
    public void onRenderEntity(RenderLivingEvent.Pre event) {
        if (event.entity instanceof EntityNPCInterface) {
            ClientEventHandler.renderingNpc = (EntityNPCInterface) event.entity;
        }
        ClientEventHandler.renderer = event.renderer;
        ClientEventHandler.partialRenderTick = Minecraft.getMinecraft().timer.renderPartialTicks;
    }

    @SubscribeEvent
    public void onRenderEntity(RenderLivingEvent.Post event) {
        if (event.entity instanceof EntityNPCInterface) {
            ClientEventHandler.renderingNpc = null;
        }
    }

    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Pre event) {
        ClientEventHandler.renderingPlayer = event.entityPlayer;
    }

    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Post event) {
        EntityPlayer player = event.entityPlayer;
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
