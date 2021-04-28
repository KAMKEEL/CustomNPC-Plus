package noppes.npcs.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.opengl.GL11;

// DELETE IF NOT NEEDED

public class ModelNPCMale64 extends ModelNPCMale
{
    public boolean isDancing;
    public boolean isSleeping;

    public float animationTick;
    public float dancingTicks;

    public ModelNPCMale64(float f)
    {
    	super(f);
    }

    @Override
    public void init(float f, float f1)
    {
        heldItemLeft = 0;
        heldItemRight = 0;
        isSneak = false;
        aimedBow = false;
        bipedCloak = new ModelRenderer(this, 0, 0);
        bipedCloak.textureHeight = 32;
        bipedCloak.addBox(-5F, 0.0F, -1F, 10, 16, 1, f);
        
        bipedEars = new ModelRenderer(this, 24, 0);
        bipedEars.addBox(-3F, -6F, -1F, 6, 6, 1, f);
        
        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8, f);
        bipedHead.setRotationPoint(0.0F, 0.0F + f1, 0.0F);
        
        bipedHeadwear = new ModelRenderer(this, 32, 0);
        bipedHeadwear.addBox(-4F, -8F, -4F, 8, 8, 8, f + 0.5F);
        bipedHeadwear.setRotationPoint(0.0F, 0.0F + f1, 0.0F);
        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addBox(-4F, 0.0F, -2F, 8, 12, 4, f);
        bipedBody.setRotationPoint(0.0F, 0.0F + f1, 0.0F);
        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedRightArm.addBox(-3F, -2F, -2F, 4, 12, 4, f);
        bipedRightArm.setRotationPoint(-5F, 2.0F + f1, 0.0F);
        bipedLeftArm = new ModelRenderer(this, 32, 48);
        bipedLeftArm.mirror = false;
        bipedLeftArm.addBox(-1F, -2F, -2F, 4, 12, 4, f);
        bipedLeftArm.setRotationPoint(5F, 2.0F + f1, 0.0F);
        bipedRightLeg = new ModelRenderer(this, 0, 16);
        bipedRightLeg.addBox(-2F, 0.0F, -2F, 4, 12, 4, f);
        bipedRightLeg.setRotationPoint(-2F, 12F + f1, 0.0F);
        bipedLeftLeg = new ModelRenderer(this, 16, 48);
        bipedLeftLeg.mirror = false;
        bipedLeftLeg.addBox(-2F, 0.0F, -2F, 4, 12, 4, f);
        bipedLeftLeg.setRotationPoint(2.0F, 12F + f1, 0.0F);
        
    }
}
