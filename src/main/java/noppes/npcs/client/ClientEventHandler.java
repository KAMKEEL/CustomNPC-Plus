package noppes.npcs.client;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.living.LivingEvent;
import noppes.npcs.AnimationData;
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
import java.util.Set;

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

    public static HashMap<EnumAnimationPart,FramePart> originalValues = new HashMap<>();
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
        AnimationData data = null;
        if (event.entity instanceof EntityNPCInterface) {
            data = ClientEventHandler.renderingNpc.display.animationData;
        } else if (event.entity instanceof EntityPlayer) {
            if (Client.playerAnimations.containsKey(event.entity.getUniqueID())) {
                data = Client.playerAnimations.get(event.entity.getUniqueID());
            }

            Class<?> RenderClass = playerModel.getClass();
            Object model = playerModel;

            while (RenderClass != Object.class) {
                Field[] declared;
                if (ClientEventHandler.declaredFieldCache.containsKey(RenderClass)) {
                    declared = ClientEventHandler.declaredFieldCache.get(RenderClass);
                } else {
                    declared = RenderClass.getDeclaredFields();
                    ClientEventHandler.declaredFieldCache.put(RenderClass,declared);
                }
                for (Field f : declared) {
                    f.setAccessible(true);
                    try {
                        ModelRenderer fieldValue = (ModelRenderer) f.get(model);
                        EnumAnimationPart enumPart = this.getPlayerPartType(fieldValue);
                        if (originalValues.containsKey(enumPart)) {
                            FramePart part = originalValues.get(enumPart);
                            fieldValue.rotationPointX = part.pivot[0];
                            fieldValue.rotationPointY = part.pivot[1];
                            fieldValue.rotationPointZ = part.pivot[2];
                            fieldValue.rotateAngleX = part.rotation[0];
                            fieldValue.rotateAngleY = part.rotation[1];
                            fieldValue.rotateAngleZ = part.rotation[2];
                        }
                    } catch (Exception ignored) {}
                }
                RenderClass = RenderClass.getSuperclass();
            }

            originalValues.clear();
            ClientEventHandler.playerModel = null;
        }

        if (data != null && data.isActive()) {
            Animation animation = data.animation;
            if (data.isActive() && animation.currentFrame().useRenderTicks()) {
                animation.increaseTime();
            }
        }
        ClientEventHandler.renderingNpc = null;
    }

    public EnumAnimationPart getPlayerPartType(ModelRenderer renderer) {
        if (renderer.baseModel instanceof ModelBiped) {
            if (renderer == ((ModelBiped) renderer.baseModel).bipedHead
                    || renderer == ((ModelBiped) renderer.baseModel).bipedHeadwear) {
                return EnumAnimationPart.HEAD;
            }
            if (renderer == ((ModelBiped) renderer.baseModel).bipedBody) {
                return EnumAnimationPart.BODY;
            }
            if (renderer == ((ModelBiped) renderer.baseModel).bipedRightArm) {
                return EnumAnimationPart.RIGHT_ARM;
            }
            if (renderer == ((ModelBiped) renderer.baseModel).bipedLeftArm) {
                return EnumAnimationPart.LEFT_ARM;
            }
            if (renderer == ((ModelBiped) renderer.baseModel).bipedRightLeg) {
                return EnumAnimationPart.RIGHT_LEG;
            }
            if (renderer == ((ModelBiped) renderer.baseModel).bipedLeftLeg) {
                return EnumAnimationPart.LEFT_LEG;
            }
        }

        try {
            Class<?> ModelBipedBody = Class.forName("JinRyuu.JRMCore.entity.ModelBipedBody");
            Object model = renderer.baseModel;
            Field[] declared;
            if (ClientEventHandler.declaredFieldCache.containsKey(ModelBipedBody)) {
                declared = ClientEventHandler.declaredFieldCache.get(ModelBipedBody);
            } else {
                declared = ModelBipedBody.getDeclaredFields();
                ClientEventHandler.declaredFieldCache.put(ModelBipedBody,declared);
            }
            Set<Map.Entry<EnumAnimationPart, String[]>> entrySet = ClientEventHandler.partNames.entrySet();
            for (Field f : declared) {
                f.setAccessible(true);
                for (Map.Entry<EnumAnimationPart, String[]> entry : entrySet) {
                    String[] names = entry.getValue();
                    for (String partName : names) {
                        try {
                            if (partName.equals(f.getName()) && renderer == f.get(model)) {
                                return entry.getKey();
                            }
                        } catch (IllegalAccessException ignored) {}
                    }
                }
            }
        } catch (ClassNotFoundException ignored) {}

        return null;
    }

    @SubscribeEvent
    public void onUpdateEntity(LivingEvent.LivingUpdateEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            AnimationData data = null;
            if (event.entity instanceof EntityNPCInterface) {
                data = ((EntityNPCInterface) event.entity).display.animationData;
            } else if (event.entity instanceof EntityPlayer && Client.playerAnimations.containsKey(event.entity.getUniqueID())) {
                data = Client.playerAnimations.get(event.entity.getUniqueID());
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
        return Client.skinOverlays.containsKey(player.getUniqueID()) && Client.skinOverlays.get(player.getUniqueID()).values().size() > 0;
    }
}
