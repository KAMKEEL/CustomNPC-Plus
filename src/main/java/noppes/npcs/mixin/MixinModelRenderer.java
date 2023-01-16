package noppes.npcs.mixin;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
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

    HashMap<EnumAnimationPart,String[]> partNames = new HashMap<>();

    /**
     * @author Someone
     * @reason Because we need it
     */
    @Overwrite
    @SideOnly(Side.CLIENT)
    public void render(float p_78785_1_)
    {
        FramePart currentPart = null;

        if (partNames.isEmpty()) {
            String[] headNames = new String[]{"field_78116_c","bipedHead","bipedHeadwear","head","Head","bipedHeadAll",
                    "bipedHeadg","bipedHeadt","bipedHeadgh","bipedHeadv","bipedHeadb","bipedHeadt2"};
            String[] bodyNames = new String[]{"field_78115_e","bipedBody","B1","body","UpperBody","Body1","BodyBase"};
            String[] larmNames = new String[]{"field_78113_g","bipedLeftArm","LA","leftarm","ArmL","Arm1L","ArmL1"};
            String[] rarmNames = new String[]{"field_78112_f","bipedRightArm","RA","rightarm","ArmR","Arm1R","ArmR1"};
            String[] llegNames = new String[]{"field_78124_i","bipedLeftLeg","LL","leftleg","LegL","Leg1L","LegL1"};
            String[] rlegNames = new String[]{"field_78123_h","bipedRightLeg","RL","rightleg","LegR","Leg1R","LegR1"};

            partNames.put(EnumAnimationPart.HEAD, headNames);
            partNames.put(EnumAnimationPart.BODY, bodyNames);
            partNames.put(EnumAnimationPart.LEFT_ARM, larmNames);
            partNames.put(EnumAnimationPart.RIGHT_ARM, rarmNames);
            partNames.put(EnumAnimationPart.LEFT_LEG, llegNames);
            partNames.put(EnumAnimationPart.RIGHT_LEG, rlegNames);
        }

        AnimationData animData = null;
        if (ClientEventHandler.renderingPlayer != null && Client.playerAnimations.containsKey(ClientEventHandler.renderingPlayer.getUniqueID())) {
            animData = Client.playerAnimations.get(ClientEventHandler.renderingPlayer.getUniqueID());
        } else if (ClientEventHandler.renderingNpc != null) {
            animData = ClientEventHandler.renderingNpc.display.animationData;
        }
        if (animData != null && animData.isActive()) {
            EnumAnimationPart partType;
            Frame frame = (Frame) animData.animation.currentFrame();
            partType = this.getPartName((ModelRenderer) (Object) this, this.partNames);
            if (frame.frameParts.containsKey(partType)) {
                currentPart = frame.frameParts.get(partType);
                currentPart.interpolateOffset();
                currentPart.interpolateAngles();
            }
        }

        if (!this.isHidden)
        {
            if (this.showModel)
            {
                if (!this.compiled)
                {
                    this.compileDisplayList(p_78785_1_);
                }

                float[] prevPivots = new float[]{this.rotationPointX,this.rotationPointY,this.rotationPointZ};
                float[] prevRotations = new float[]{this.rotateAngleX,this.rotateAngleY,this.rotateAngleZ};

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

                if (currentPart != null) {
                    this.rotationPointX = prevPivots[0];
                    this.rotationPointY = prevPivots[1];
                    this.rotationPointZ = prevPivots[2];
                    this.rotateAngleX = prevRotations[0];
                    this.rotateAngleY = prevRotations[1];
                    this.rotateAngleZ = prevRotations[2];
                }
            }
        }
    }

    public EnumAnimationPart getPartName(ModelRenderer renderer, HashMap<EnumAnimationPart,String[]> partNames) {
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
