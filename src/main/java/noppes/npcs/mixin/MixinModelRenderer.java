package noppes.npcs.mixin;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.AnimationData;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.ClientEventHandler;
import noppes.npcs.constants.EnumAnimationPart;
import noppes.npcs.controllers.data.Frame;
import noppes.npcs.controllers.data.FramePart;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.reflect.Field;
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

    @Shadow public ModelBase baseModel;

    /**
     * @author Someone
     * @reason Because we need it
     */
    @Overwrite
    @SideOnly(Side.CLIENT)
    public void render(float p_78785_1_)
    {
        if (ClientEventHandler.partNames.isEmpty()) {
            String[] headNames = new String[]{"field_78116_c","bipedHead","bipedHeadwear","head","Head","headwear",
                    "bipedHeadg","bipedHeadt","bipedHeadgh","bipedHeadv","bipedHeadb","bipedHeadt2"};
            String[] bodyNames = new String[]{"field_78115_e","bipedBody","B1","body","Body","UpperBody","Body1","BodyBase"};
            String[] larmNames = new String[]{"field_78113_g","bipedLeftArm","LA","leftarm","ArmL","Arm1L","ArmL1"};
            String[] rarmNames = new String[]{"field_78112_f","bipedRightArm","RA","rightarm","ArmR","Arm1R","ArmR1"};
            String[] llegNames = new String[]{"field_78124_i","bipedLeftLeg","LL","leftleg","LegL","Leg1L","LegL1"};
            String[] rlegNames = new String[]{"field_78123_h","bipedRightLeg","RL","rightleg","LegR","Leg1R","LegR1"};

            ClientEventHandler.partNames.put(EnumAnimationPart.HEAD, headNames);
            ClientEventHandler.partNames.put(EnumAnimationPart.BODY, bodyNames);
            ClientEventHandler.partNames.put(EnumAnimationPart.LEFT_ARM, larmNames);
            ClientEventHandler.partNames.put(EnumAnimationPart.RIGHT_ARM, rarmNames);
            ClientEventHandler.partNames.put(EnumAnimationPart.LEFT_LEG, llegNames);
            ClientEventHandler.partNames.put(EnumAnimationPart.RIGHT_LEG, rlegNames);
        }
        if (!this.isHidden)
        {
            if (this.showModel)
            {
                if (!this.compiled)
                {
                    this.compileDisplayList(p_78785_1_);
                }

                float prevPointX = this.rotationPointX;
                float prevPointY = this.rotationPointY;
                float prevPointZ = this.rotationPointZ;
                float prevAngleX = this.rotateAngleX;
                float prevAngleY = this.rotateAngleY;
                float prevAngleZ = this.rotateAngleZ;

                this.setAnimationValues(p_78785_1_);

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
                                if(this.childModels.get(i) instanceof ModelRenderer){
                                    ((ModelRenderer)this.childModels.get(i)).render(p_78785_1_);
                                }
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
                                if(this.childModels.get(i) instanceof ModelRenderer){
                                    ((ModelRenderer)this.childModels.get(i)).render(p_78785_1_);
                                }
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
                            if(this.childModels.get(i) instanceof ModelRenderer){
                                ((ModelRenderer)this.childModels.get(i)).render(p_78785_1_);
                            }
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

    private void setAnimationValues(float p_78785_1_) {
        float prevPointX = this.rotationPointX;
        float prevPointY = this.rotationPointY;
        float prevPointZ = this.rotationPointZ;
        float prevAngleX = this.rotateAngleX;
        float prevAngleY = this.rotateAngleY;
        float prevAngleZ = this.rotateAngleZ;

        if (ClientEventHandler.renderingNpc != null && ClientEventHandler.renderingNpc.display.animationData.isActive()) {
            AnimationData animData = ClientEventHandler.renderingNpc.display.animationData;
            EnumAnimationPart partType = this.getPartType((ModelRenderer) (Object) this);

            if (partType != null && animData != null) {
                Frame frame = (Frame) animData.animation.currentFrame();
                if (frame.frameParts.containsKey(partType)) {
                    FramePart currentPart = frame.frameParts.get(partType);
                    currentPart.interpolateOffset();
                    currentPart.interpolateAngles();

                    this.rotationPointX += currentPart.prevPivots[0];
                    this.rotationPointY += currentPart.prevPivots[1];
                    this.rotationPointZ += currentPart.prevPivots[2];
                    this.rotateAngleX = currentPart.prevRotations[0];
                    this.rotateAngleY = currentPart.prevRotations[1];
                    this.rotateAngleZ = currentPart.prevRotations[2];
                }
            }
        }

        if (ClientEventHandler.renderingPlayer != null && ClientCacheHandler.playerAnimations.containsKey(ClientEventHandler.renderingPlayer.getUniqueID())) {
            ClientEventHandler.playerModel = ((ModelRenderer) (Object) this).baseModel;
            AnimationData animData = ClientCacheHandler.playerAnimations.get(ClientEventHandler.renderingPlayer.getUniqueID());

            EnumAnimationPart mainPartType = this.getPlayerPartType((ModelRenderer) (Object) this);
            EnumAnimationPart partType = mainPartType != null ? mainPartType : this.pivotEqualPart((ModelRenderer) (Object) this);

            if (partType != null && animData != null && animData.animation != null) {
                boolean animDataActive = animData.isActive();

                if (!ClientEventHandler.originalValues.containsKey((ModelRenderer) (Object) this)) {
                    FramePart part = new FramePart();
                    part.pivot = new float[]{prevPointX, prevPointY, prevPointZ};
                    part.rotation = new float[]{prevAngleX, prevAngleY, prevAngleZ};
                    ClientEventHandler.originalValues.put((ModelRenderer) (Object) this, part);
                }

                FramePart originalPart = ClientEventHandler.originalValues.get((ModelRenderer) (Object) this);
                Frame frame = (Frame) animData.animation.currentFrame();
                if (!animDataActive && animData.finishedFrame >= 0 && animData.finishedFrame < animData.animation.frames.size()) {
                    frame = animData.animation.frames.get(animData.finishedFrame);
                }

                if (frame != null && frame.frameParts.containsKey(partType)) {
                    FramePart part = frame.frameParts.get(partType);
                    if (partType == mainPartType) {
                        if (animDataActive) {
                            part.interpolateAngles();
                            part.interpolateOffset();
                        } else {
                            float speed = 0.4F;
                            part.prevPivots[0] = part.prevPivots[0] * (1.0F - speed);
                            part.prevPivots[1] = part.prevPivots[1] * (1.0F - speed);
                            part.prevPivots[2] = part.prevPivots[2] * (1.0F - speed);
                            part.prevRotations[0] = part.prevRotations[0] * (1.0F - speed) + (this.rotateAngleX * speed);
                            part.prevRotations[1] = part.prevRotations[1] * (1.0F - speed) + (this.rotateAngleY * speed);
                            part.prevRotations[2] = part.prevRotations[2] * (1.0F - speed) + (this.rotateAngleZ * speed);
                        }

                        if (animDataActive || (partType != EnumAnimationPart.HEAD
                                        && ClientEventHandler.renderingPlayer.getAge() - animData.finishedTime < 20)) {
                            this.rotationPointX = originalPart.pivot[0] + part.prevPivots[0];
                            this.rotationPointY = originalPart.pivot[1] + part.prevPivots[1];
                            this.rotationPointZ = originalPart.pivot[2] + part.prevPivots[2];
                            this.rotateAngleX = part.prevRotations[0];
                            this.rotateAngleY = part.prevRotations[1];
                            this.rotateAngleZ = part.prevRotations[2];
                        }
                    } else {
                        this.rotateAngleZ += part.prevRotations[2];
                    }
                }
            }
        }
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

    public EnumAnimationPart pivotEqualPart(ModelRenderer renderer) {
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

    public boolean pivotsEqual(ModelRenderer m1, ModelRenderer m2) {
        return m1.rotationPointX == m2.rotationPointX && m1.rotationPointY == m2.rotationPointY && m1.rotationPointZ == m2.rotationPointZ;
    }

    public EnumAnimationPart getPartType(ModelRenderer renderer) {
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
}
