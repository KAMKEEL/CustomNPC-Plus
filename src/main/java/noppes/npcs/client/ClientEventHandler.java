package noppes.npcs.client;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.living.LivingEvent;
import noppes.npcs.controllers.data.AnimationData;
import noppes.npcs.client.gui.customoverlay.OverlayCustom;
import noppes.npcs.client.renderer.RenderCNPCPlayer;
import noppes.npcs.constants.EnumAnimationPart;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.FramePart;
import noppes.npcs.entity.EntityNPCInterface;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClientEventHandler {
    public static final RenderCNPCPlayer renderCNPCPlayer = new RenderCNPCPlayer();
    public static HashMap<Integer,Long> disabledButtonTimes = new HashMap<>();
    public static float partialHandTicks;

    public static float partialRenderTick;
    public static RendererLivingEntity renderer;

    public static EntityNPCInterface renderingNpc;
    public static EntityPlayer renderingPlayer;
    public static HashMap<EnumAnimationPart,String[]> partNames = new HashMap<>();
    public static HashMap<Class<?>,Field[]> declaredFieldCache = new HashMap<>();

    public static HashMap<ModelRenderer,FramePart> originalValues = new HashMap<>();
    public static ModelBase playerModel;

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
            for (OverlayCustom overlayCustom : ClientCacheHandler.customOverlays.values()) {
                overlayCustom.renderGameOverlay(event.partialTicks);
            }

            if (ClientCacheHandler.questTrackingOverlay != null) {
                ClientCacheHandler.questTrackingOverlay.renderGameOverlay(event.partialTicks);
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
        AnimationData data = null;
        if (event.entity instanceof EntityNPCInterface) {
            data = ClientEventHandler.renderingNpc.display.animationData;
        } else if (event.entity instanceof EntityPlayer) {
            if (ClientCacheHandler.playerAnimations.containsKey(event.entity.getUniqueID())) {
                data = ClientCacheHandler.playerAnimations.get(event.entity.getUniqueID());
            }

            for (Map.Entry<ModelRenderer,FramePart> entry : ClientEventHandler.originalValues.entrySet()) {
                ModelRenderer renderer = entry.getKey();
                FramePart part = entry.getValue();
                renderer.rotateAngleX = part.rotation[0];
                renderer.rotateAngleY = part.rotation[1];
                renderer.rotateAngleZ = part.rotation[2];
                renderer.rotationPointX = part.pivot[0];
                renderer.rotationPointY = part.pivot[1];
                renderer.rotationPointZ = part.pivot[2];
            }

            ClientEventHandler.playerModel = null;
        }

        if (data != null && data.isActive() && !Minecraft.getMinecraft().isGamePaused()) {
            Animation animation = data.animation;
            if (data.isActive() && animation.currentFrame().useRenderTicks()) {
                animation.increaseTime();
            }
        }
        ClientEventHandler.renderingNpc = null;
    }

    @SubscribeEvent
    public void onUpdateEntity(LivingEvent.LivingUpdateEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            AnimationData data = null;
            if (event.entity instanceof EntityNPCInterface) {
                data = ((EntityNPCInterface) event.entity).display.animationData;
            } else if (event.entity instanceof EntityPlayer && ClientCacheHandler.playerAnimations.containsKey(event.entity.getUniqueID())) {
                data = ClientCacheHandler.playerAnimations.get(event.entity.getUniqueID());
            }

            if (data != null && data.isActive()) {
                Animation animation = data.animation;
                if (data.isActive() && !animation.currentFrame().useRenderTicks()) {
                    animation.increaseTime();
                }
            }
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
        return ClientCacheHandler.skinOverlays.containsKey(player.getUniqueID()) && ClientCacheHandler.skinOverlays.get(player.getUniqueID()).values().size() > 0;
    }
}
