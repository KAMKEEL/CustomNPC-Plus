package noppes.npcs;

import kamkeel.npcs.addon.DBCAddon;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.ClientEventHandler;
import noppes.npcs.constants.EnumAnimationPart;
import noppes.npcs.controllers.data.AnimationData;
import noppes.npcs.controllers.data.Frame;
import noppes.npcs.controllers.data.FramePart;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public class AnimationMixinFunctions {

    public static boolean applyValues(ModelRenderer modelRenderer) {
        if (ClientEventHandler.renderingPlayer == null && ClientEventHandler.renderingNpc == null) {
            return false;
        }

        if (ClientEventHandler.renderingPlayer != null) {
            ClientEventHandler.playerModel = (modelRenderer).baseModel;
            if (ClientCacheHandler.playerAnimations.containsKey(ClientEventHandler.renderingPlayer.getUniqueID())) {
                AnimationData animData = ClientCacheHandler.playerAnimations.get(ClientEventHandler.renderingPlayer.getUniqueID());
                EnumAnimationPart mainPartType = getPlayerPartType(modelRenderer);
                EnumAnimationPart partType = mainPartType != null ? mainPartType : pivotEqualPart(modelRenderer);
                if (partType != null && animData != null && animData.animation != null && animData.isActive()) {
                    if (!ClientEventHandler.originalValues.containsKey(modelRenderer)) {
                        FramePart part = new FramePart();
                        part.pivot = new float[]{modelRenderer.rotationPointX, modelRenderer.rotationPointY, modelRenderer.rotationPointZ};
                        part.rotation = new float[]{modelRenderer.rotateAngleX, modelRenderer.rotateAngleY, modelRenderer.rotateAngleZ};
                        ClientEventHandler.originalValues.put(modelRenderer, part);
                    }
                    FramePart originalPart = ClientEventHandler.originalValues.get(modelRenderer);
                    Frame frame = (Frame) animData.animation.currentFrame();
                    if (frame != null && frame.frameParts.containsKey(partType)) {
                        FramePart part = frame.frameParts.get(partType);
                        if (partType == mainPartType) {
                            part.interpolateAngles();
                            part.interpolateOffset();
                            modelRenderer.rotationPointX = originalPart.pivot[0] + part.prevPivots[0];
                            modelRenderer.rotationPointY = originalPart.pivot[1] + part.prevPivots[1];
                            modelRenderer.rotationPointZ = originalPart.pivot[2] + part.prevPivots[2];
                            modelRenderer.rotateAngleX = part.prevRotations[0];
                            modelRenderer.rotateAngleY = part.prevRotations[1];
                            modelRenderer.rotateAngleZ = part.prevRotations[2];
                        } else {
                            modelRenderer.rotateAngleZ += part.prevRotations[2];
                            return true;
                        }
                    }
                }
            }
        } else if (ClientEventHandler.renderingNpc.display.animationData.isActive()) {
            AnimationData animData = ClientEventHandler.renderingNpc.display.animationData;
            EnumAnimationPart partType = getPartType(modelRenderer);
            if (partType != null && animData != null) {
                Frame frame = (Frame) animData.animation.currentFrame();
                if (frame.frameParts.containsKey(partType)) {
                    FramePart part = frame.frameParts.get(partType);
                    part.interpolateOffset();
                    part.interpolateAngles();
                    modelRenderer.rotationPointX += part.prevPivots[0];
                    modelRenderer.rotationPointY += part.prevPivots[1];
                    modelRenderer.rotationPointZ += part.prevPivots[2];
                    modelRenderer.rotateAngleX = part.prevRotations[0];
                    modelRenderer.rotateAngleY = part.prevRotations[1];
                    modelRenderer.rotateAngleZ = part.prevRotations[2];
                    return true;
                }
            }
        }
        return false;
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
        return getPartType(renderer);
    }

    private static EnumAnimationPart pivotEqualPart(ModelRenderer renderer) {
        if(renderer.baseModel instanceof ModelBiped){
            ModelRenderer head = ((ModelBiped) renderer.baseModel).bipedHead;
            ModelRenderer body = ((ModelBiped) renderer.baseModel).bipedBody;
            ModelRenderer larm = ((ModelBiped) renderer.baseModel).bipedLeftArm;
            ModelRenderer rarm = ((ModelBiped) renderer.baseModel).bipedRightArm;
            ModelRenderer lleg = ((ModelBiped) renderer.baseModel).bipedLeftLeg;
            ModelRenderer rleg = ((ModelBiped) renderer.baseModel).bipedRightLeg;

            if (pivotsEqual(renderer,head)) {
                return EnumAnimationPart.HEAD;
            }
            if (pivotsEqual(renderer,body)) {
                return EnumAnimationPart.BODY;
            }
            if (pivotsEqual(renderer,rarm)) {
                return EnumAnimationPart.RIGHT_ARM;
            }
            if (pivotsEqual(renderer,larm)) {
                return EnumAnimationPart.LEFT_ARM;
            }
            if (pivotsEqual(renderer,rleg)) {
                return EnumAnimationPart.RIGHT_LEG;
            }
            if (pivotsEqual(renderer,lleg)) {
                return EnumAnimationPart.LEFT_LEG;
            }
        }

        return null;
    }

    private static boolean pivotsEqual(ModelRenderer m1, ModelRenderer m2) {
        return m1.rotationPointX == m2.rotationPointX && m1.rotationPointY == m2.rotationPointY && m1.rotationPointZ == m2.rotationPointZ;
    }

    private static EnumAnimationPart getPartType(ModelRenderer renderer) {
        Class<?> RenderClass = renderer.baseModel.getClass();
        Object model = renderer.baseModel;

        Set<Map.Entry<EnumAnimationPart, String[]>> entrySet = ClientEventHandler.partNames.entrySet();
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
            RenderClass = RenderClass.getSuperclass();
        }

        return null;
    }

    public static void playerFullModel_head(Entity p_78088_1_, CallbackInfo callbackInfo) {
        if (!DBCAddon.IsAvailable() && ClientCacheHandler.playerAnimations.containsKey(p_78088_1_.getUniqueID())) {
            AnimationData animData = ClientCacheHandler.playerAnimations.get(p_78088_1_.getUniqueID());
            if (animData != null && animData.isActive()) {
                Frame frame = (Frame) animData.animation.currentFrame();
                if (frame.frameParts.containsKey(EnumAnimationPart.FULL_MODEL)) {
                    FramePart part = frame.frameParts.get(EnumAnimationPart.FULL_MODEL);
                    part.interpolateOffset();
                    part.interpolateAngles();
                    float pi = 180 / (float) Math.PI;
                    GL11.glTranslatef(part.prevPivots[0], part.prevPivots[1], part.prevPivots[2]);
                    GL11.glRotatef(part.prevRotations[0] * pi, 1, 0, 0);
                    GL11.glRotatef(part.prevRotations[1] * pi, 0, 1, 0);
                    GL11.glRotatef(part.prevRotations[2] * pi, 0, 0, 1);
                }
            }
        }
    }
}
