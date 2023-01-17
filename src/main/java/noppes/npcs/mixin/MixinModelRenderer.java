package noppes.npcs.mixin;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import noppes.npcs.AnimationData;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientEventHandler;
import noppes.npcs.constants.EnumAnimationPart;
import noppes.npcs.controllers.data.Frame;
import noppes.npcs.controllers.data.FramePart;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(value = ModelRenderer.class)
public abstract class MixinModelRenderer {
    @Shadow public float rotationPointX;
    @Shadow public float rotationPointY;
    @Shadow public float rotationPointZ;
    @Shadow public float rotateAngleX;
    @Shadow public float rotateAngleY;
    @Shadow public float rotateAngleZ;
    @Shadow public float offsetX;
    @Shadow public float offsetY;
    @Shadow public float offsetZ;
    @Shadow public boolean isHidden;
    @Shadow public boolean showModel;
    @Shadow public boolean compiled;
    @Shadow private int displayList;
    @Shadow public List childModels;
    @Shadow abstract void compileDisplayList(float p_78788_1_);

    /**
     * @author Someone
     * @reason Because we need it
     */
    @Overwrite
    @SideOnly(Side.CLIENT)
    public void render(float p_78785_1_)
    {
        if (!this.isHidden)
        {
            if (this.showModel)
            {
                if (!this.compiled)
                {
                    this.compileDisplayList(p_78785_1_);
                }

                if (ClientEventHandler.renderingPlayer != null && Client.playerAnimations.containsKey(ClientEventHandler.renderingPlayer.getUniqueID())) {
                    AnimationData animData = Client.playerAnimations.get(ClientEventHandler.renderingPlayer.getUniqueID());
                    EnumAnimationPart partType = this.getPlayerPartType((ModelRenderer) (Object) this);
                    if (partType != null && animData != null) {
                        if (animData.isActive()) {
                            Frame frame = (Frame) animData.animation.currentFrame();
                            if (frame.frameParts.containsKey(partType)) {
                                FramePart part = frame.frameParts.get(partType);
                                if (!animData.originalPivots.containsKey(part.part)) {
                                    FramePart originalPart = new FramePart(part.part);
                                    originalPart.originalRotations = new float[]{this.rotateAngleX, this.rotateAngleY, this.rotateAngleZ};
                                    originalPart.originalPivots = new float[]{this.rotationPointX, this.rotationPointY, this.rotationPointZ};
                                    animData.originalPivots.put(part.part, originalPart);
                                } else {
                                    this.rotateAngleX = animData.originalPivots.get(part.part).prevRotations[0];
                                    this.rotateAngleY = animData.originalPivots.get(part.part).prevRotations[1];
                                    this.rotateAngleZ = animData.originalPivots.get(part.part).prevRotations[2];
                                }
                                FramePart originalPart = animData.originalPivots.get(part.part);
                                part.interpolateAngles();
                                part.interpolateOffset();
                                this.rotationPointX = originalPart.originalPivots[0] + part.prevPivots[0];
                                this.rotationPointY = originalPart.originalPivots[1] + part.prevPivots[1];
                                this.rotationPointZ = originalPart.originalPivots[2] + part.prevPivots[2];
                                this.rotateAngleX = part.prevRotations[0];
                                this.rotateAngleY = part.prevRotations[1];
                                this.rotateAngleZ = part.prevRotations[2];
                                animData.originalPivots.get(part.part).prevRotations = new float[]{this.rotateAngleX, this.rotateAngleY, this.rotateAngleZ};
                            }
                        } else if (animData.originalPivots.containsKey(partType)) {
                            this.rotateAngleX = animData.originalPivots.get(partType).originalRotations[0];
                            this.rotateAngleY = animData.originalPivots.get(partType).originalRotations[1];
                            this.rotateAngleZ = animData.originalPivots.get(partType).originalRotations[2];
                            this.rotationPointX = animData.originalPivots.get(partType).originalPivots[0];
                            this.rotationPointY = animData.originalPivots.get(partType).originalPivots[1];
                            this.rotationPointZ = animData.originalPivots.get(partType).originalPivots[2];
                            animData.originalPivots.remove(partType);
                        }
                    }
                }

                float prevPointX = this.rotationPointX;
                float prevPointY = this.rotationPointY;
                float prevPointZ = this.rotationPointZ;
                float prevAngleX = this.rotateAngleX;
                float prevAngleY = this.rotateAngleY;
                float prevAngleZ = this.rotateAngleZ;

                FramePart currentPart = null;
                AnimationData animData = null;
                EnumAnimationPart partType = null;
                if (ClientEventHandler.renderingNpc != null) {
                    animData = ClientEventHandler.renderingNpc.display.animationData;
                    partType = this.getPartType((ModelRenderer) (Object) this, ClientEventHandler.partNames);
                }
                if (partType != null && animData != null && animData.isActive()) {
                    Frame frame = (Frame) animData.animation.currentFrame();
                    if (frame.frameParts.containsKey(partType)) {
                        currentPart = frame.frameParts.get(partType);
                        currentPart.interpolateOffset();
                        currentPart.interpolateAngles();
                    }
                }

                if (currentPart != null) {
                    this.rotationPointX += currentPart.prevPivots[0];
                    this.rotationPointY += currentPart.prevPivots[1];
                    this.rotationPointZ += currentPart.prevPivots[2];
                    this.rotateAngleX = currentPart.prevRotations[0];
                    this.rotateAngleY = currentPart.prevRotations[1];
                    this.rotateAngleZ = currentPart.prevRotations[2];
                }
                GL11.glTranslatef(this.offsetX, this.offsetY, this.offsetZ);
                int i;

                if (this.rotateAngleX == 0.0F && this.rotateAngleY == 0.0F && this.rotateAngleZ == 0.0F)
                {
                    if (this.rotationPointX == 0.0F && this.rotationPointY == 0.0F && this.rotationPointZ == 0.0F)
                    {
                        GL11.glCallList(this.displayList);

                        if (this.childModels != null)
                        {
                            for (i = 0; i < this.childModels.size(); ++i)
                            {
                                ((ModelRenderer)this.childModels.get(i)).render(p_78785_1_);
                            }
                        }
                    }
                    else
                    {
                        GL11.glTranslatef(this.rotationPointX * p_78785_1_, this.rotationPointY * p_78785_1_, this.rotationPointZ * p_78785_1_);
                        GL11.glCallList(this.displayList);

                        if (this.childModels != null)
                        {
                            for (i = 0; i < this.childModels.size(); ++i)
                            {
                                ((ModelRenderer)this.childModels.get(i)).render(p_78785_1_);
                            }
                        }

                        GL11.glTranslatef(-this.rotationPointX * p_78785_1_, -this.rotationPointY * p_78785_1_, -this.rotationPointZ * p_78785_1_);
                    }
                }
                else
                {
                    GL11.glPushMatrix();
                    GL11.glTranslatef(this.rotationPointX * p_78785_1_, this.rotationPointY * p_78785_1_, this.rotationPointZ * p_78785_1_);

                    if (this.rotateAngleZ != 0.0F)
                    {
                        GL11.glRotatef(this.rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
                    }

                    if (this.rotateAngleY != 0.0F)
                    {
                        GL11.glRotatef(this.rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
                    }

                    if (this.rotateAngleX != 0.0F)
                    {
                        GL11.glRotatef(this.rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
                    }

                    GL11.glCallList(this.displayList);

                    if (this.childModels != null)
                    {
                        for (i = 0; i < this.childModels.size(); ++i)
                        {
                            ((ModelRenderer)this.childModels.get(i)).render(p_78785_1_);
                        }
                    }

                    GL11.glPopMatrix();
                }

                this.rotationPointX = prevPointX;
                this.rotationPointY = prevPointY;
                this.rotationPointZ = prevPointZ;
                this.rotateAngleX = prevAngleX;
                this.rotateAngleY = prevAngleY;
                this.rotateAngleZ = prevAngleZ;
            }
        }
    }

    public EnumAnimationPart getPlayerPartType(ModelRenderer renderer) {
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

        try {
            Class<?> ModelBipedBody = Class.forName("JinRyuu.JRMCore.entity.ModelBipedBody");
            Object m = renderer.baseModel;
            Set<Map.Entry<EnumAnimationPart,String[]>> entrySet = ClientEventHandler.partNames.entrySet();
            for (Map.Entry<EnumAnimationPart,String[]> entry : entrySet) {
                for (String s : entry.getValue()) {
                    try {
                        if (renderer == ModelBipedBody.getField(s).get(m)) {
                            return entry.getKey();
                        }
                    } catch (NoSuchFieldException | SecurityException | IllegalAccessException ignored) {}
                }
            }
        } catch (ClassNotFoundException ignored) {}

        return null;
    }

    public EnumAnimationPart getPartType(ModelRenderer renderer, HashMap<EnumAnimationPart,String[]> partNames) {
        Class<?> RenderClass = renderer.baseModel.getClass();
        Object model = renderer.baseModel;
        EnumAnimationPart type = null;

        while (type == null) {
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

            for (Map.Entry<EnumAnimationPart, String[]> entry : partNames.entrySet()) {
                String[] names = entry.getValue();
                for (String partName : names) {
                    try {
                        Field field = RenderClass.getDeclaredField(partName);
                        field.setAccessible(true);
                        if (renderer == field.get(model)) {
                            type = entry.getKey();
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

        return type;
    }
}
