package noppes.npcs.client;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.*;
import noppes.npcs.client.gui.customoverlay.OverlayCustom;
import noppes.npcs.client.gui.hud.ClientHudManager;
import noppes.npcs.client.gui.hud.CompassHudComponent;
import noppes.npcs.client.gui.hud.EnumHudComponent;
import noppes.npcs.client.gui.hud.QuestTrackingComponent;
import noppes.npcs.client.renderer.MarkRenderer;
import noppes.npcs.client.renderer.RenderCNPCPlayer;
import noppes.npcs.constants.EnumAnimationPart;
import noppes.npcs.constants.MarkType;
import noppes.npcs.controllers.data.FramePart;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;

import java.lang.reflect.Field;
import java.util.*;

public class ClientEventHandler {
    public static final RenderCNPCPlayer renderCNPCSelf = new RenderCNPCPlayer();
    public static final RenderCNPCPlayer renderCNPCPlayer = new RenderCNPCPlayer();
    public static HashMap<Integer, Long> disabledButtonTimes = new HashMap<>();
    public static float partialHandTicks;
    public static boolean firstPersonAnimation;
    public static ModelBiped firstPersonModel;

    public static final ResourceLocation steveTextures = new ResourceLocation("textures/entity/steve.png");
    public static final ResourceLocation fem = new ResourceLocation("jinryuufamilyc:fem.png");

    public static float partialRenderTick;
    public static RendererLivingEntity renderer;

    public static EntityNPCInterface renderingNpc;
    public static EntityPlayer renderingPlayer;
    public static HashMap<EnumAnimationPart, String[]> partNames = new HashMap<>();
    public static HashMap<Class<?>, Field[]> declaredFieldCache = new HashMap<>();

    private static final HashSet<Render> processedPlayerRenderers = new HashSet<>();
    public static final HashMap<ModelRenderer, FramePart> originalValues = new HashMap<>();
    public static ModelBase playerModel;

    private Class<?> renderPlayerJBRA;

    public ClientEventHandler() {
        try {
            renderPlayerJBRA = Class.forName("JinRyuu.JBRA.RenderPlayerJBRA");
        } catch (ClassNotFoundException e) {
            renderPlayerJBRA = null;
        }
    }

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (Minecraft.getMinecraft().currentScreen != null)
            return;

        ArrayList<Integer> removeList = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : disabledButtonTimes.entrySet()) {
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
    public void onOverlayRender(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.ALL) {
            for (OverlayCustom overlayCustom : ClientCacheHandler.customOverlays.values()) {
                overlayCustom.renderGameOverlay(event.partialTicks);
            }

            if (ClientHudManager.getInstance().getHudComponents().isEmpty()) {
                ClientHudManager.getInstance().registerHud(EnumHudComponent.QuestTracker, new QuestTrackingComponent(Minecraft.getMinecraft()));
                ClientHudManager.getInstance().registerHud(EnumHudComponent.QuestCompass, new CompassHudComponent(Minecraft.getMinecraft()));
            }

            ClientHudManager.getInstance().renderAllHUDs(event.partialTicks);
        }
    }

    @SubscribeEvent
    public void onRenderEntity(RenderLivingEvent.Pre event) {
        if (event.entity instanceof EntityNPCInterface) {
            ClientEventHandler.renderingPlayer = null;
            ClientEventHandler.renderingNpc = (EntityNPCInterface) event.entity;
        }
        ClientEventHandler.renderer = event.renderer;
        ClientEventHandler.partialRenderTick = Minecraft.getMinecraft().timer.renderPartialTicks;

        this.setOriginalPlayerParts(event.entity);
    }

    @SubscribeEvent
    public void renderHand(RenderHandEvent event) {
        this.setOriginalPlayerParts(Minecraft.getMinecraft().thePlayer);
    }

    private void setOriginalPlayerParts(Entity entity) {
        if (entity instanceof EntityClientPlayerMP) {
            Render render = RenderManager.instance.getEntityClassRenderObject(entity.getClass());
            if (!processedPlayerRenderers.contains(render)) {
                processedPlayerRenderers.add(render);
                Collection<ModelRenderer> modelRenderers = getAllModelRenderers(render);
                for (ModelRenderer modelRenderer : modelRenderers) {
                    if (!ClientEventHandler.originalValues.containsKey(modelRenderer)) {
                        FramePart part = new FramePart();
                        part.pivot = new float[]{modelRenderer.rotationPointX, modelRenderer.rotationPointY, modelRenderer.rotationPointZ};
                        part.rotation = new float[]{modelRenderer.rotateAngleX, modelRenderer.rotateAngleY, modelRenderer.rotateAngleZ};
                        ClientEventHandler.originalValues.put(modelRenderer, part);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderEntity(RenderLivingEvent.Post event) {
        if (event.entity instanceof EntityNPCInterface) {
            MarkData markData = MarkData.get((EntityNPCInterface) event.entity);
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            if (PlayerData.get(player) != null) {
                for (MarkData.Mark m : markData.marks) {
                    if (m.getType() != MarkType.NONE && m.availability.isAvailable(player)) {
                        MarkRenderer.render(event.entity, event.x, event.y, event.z, m);
                        break;
                    }
                }
            }
        } else if (event.entity instanceof EntityPlayer) {
            for (Map.Entry<ModelRenderer, FramePart> entry : ClientEventHandler.originalValues.entrySet()) {
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

        ClientEventHandler.renderingNpc = null;
    }

    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Pre event) {
        ClientEventHandler.renderingNpc = null;
        ClientEventHandler.renderingPlayer = event.entityPlayer;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void pre(RenderPlayerEvent.Pre event) {
        if (!(event.entity instanceof AbstractClientPlayer))
            return;
    }

    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Post event) {
        EntityPlayer player = event.entityPlayer;
        ClientEventHandler.renderingPlayer = null;

        if (hasOverlays(player)) {
            if (renderPlayerJBRA != null && renderPlayerJBRA.isInstance(event.renderer))
                return;

            if (!(event.renderer instanceof RenderCNPCPlayer)) {
                renderCNPCPlayer.mainModel = event.renderer.mainModel;
                renderCNPCPlayer.modelBipedMain = event.renderer.modelBipedMain;
                renderCNPCPlayer.modelArmor = event.renderer.modelArmor;
                renderCNPCPlayer.modelArmorChestplate = event.renderer.modelArmorChestplate;
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
        if (!hasOverlays(event.entityPlayer)) {
            return;
        }

        if (renderPlayerJBRA == null && !renderPlayerJBRA.isInstance(event.renderer)) {
            return;
        }

        renderCNPCPlayer.renderDBCModel(event);
    }

    public static boolean hasOverlays(EntityPlayer player) {
        return ClientCacheHandler.skinOverlays.containsKey(player.getUniqueID()) && !ClientCacheHandler.skinOverlays.get(player.getUniqueID()).values().isEmpty();
    }

    // Function to recursively get all ModelRenderer objects from a given object
    public static Collection<ModelRenderer> getAllModelRenderers(Object object) {
        HashSet<ModelRenderer> modelRenderers = new HashSet<>();

        HashSet<ModelBase> modelBases = getAllModelBases(object);
        for (ModelBase modelBase : modelBases) {
            modelRenderers.addAll(getModelRenderersFromModelBase(modelBase));
        }

        return modelRenderers;
    }

    // Recursively find all ModelBase objects in a given object
    private static HashSet<ModelBase> getAllModelBases(Object object) {
        HashSet<ModelBase> modelBases = new HashSet<>();

        if (object == null) {
            return modelBases;
        }

        // Get all fields from the object's class and superclasses
        Class<?> clazz = object.getClass();
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);

                try {
                    Object value = field.get(object);
                    if (value instanceof ModelBase) {
                        modelBases.add((ModelBase) value);
                    }
                } catch (IllegalAccessException ignored) {
                }
            }

            clazz = clazz.getSuperclass();
        }

        return modelBases;
    }

    // Recursively find all ModelRenderer objects within a ModelBase
    private static HashSet<ModelRenderer> getModelRenderersFromModelBase(ModelBase modelBase) {
        HashSet<ModelRenderer> modelRenderers = new HashSet<>();

        Class<?> clazz = modelBase.getClass();
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);

                try {
                    Object value = field.get(modelBase);

                    if (value instanceof ModelRenderer && getPlayerPartType((ModelRenderer) value) != null) {
                        modelRenderers.add((ModelRenderer) value);
                    }
                } catch (IllegalAccessException ignored) {
                }
            }

            clazz = clazz.getSuperclass();
        }

        return modelRenderers;
    }

    private static EnumAnimationPart getPlayerPartType(ModelRenderer renderer) {
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
            if (!(model instanceof ModelBiped))
                return null;

            Field[] declared;
            if (ClientEventHandler.declaredFieldCache.containsKey(ModelBipedBody)) {
                declared = ClientEventHandler.declaredFieldCache.get(ModelBipedBody);
            } else {
                declared = ModelBipedBody.getDeclaredFields();
                ClientEventHandler.declaredFieldCache.put(ModelBipedBody, declared);
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
                        } catch (IllegalAccessException ignored) {
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }
}
