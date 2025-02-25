package noppes.npcs.mixin;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import noppes.npcs.AnimationMixinFunctions;
import noppes.npcs.client.ClientEventHandler;
import noppes.npcs.constants.EnumAnimationPart;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

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

                boolean changedAngles = false;
                try {
                    changedAngles = AnimationMixinFunctions.applyValues((ModelRenderer) (Object) this);
                } catch (Exception ignored) {
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

                if (changedAngles) {
                    this.rotationPointX = prevPointX;
                    this.rotationPointY = prevPointY;
                    this.rotationPointZ = prevPointZ;
                    this.rotateAngleX = prevAngleX;
                    this.rotateAngleY = prevAngleY;
                    this.rotateAngleZ = prevAngleZ;
                }
            }
        }
    }
}
