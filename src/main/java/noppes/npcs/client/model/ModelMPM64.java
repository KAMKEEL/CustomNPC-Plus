package noppes.npcs.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import noppes.npcs.ModelPartConfig;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.model.animation.AniCrawling;
import noppes.npcs.client.model.animation.AniHug;
import noppes.npcs.client.model.part.*;
import noppes.npcs.client.model.util.ModelPartInterface;
import noppes.npcs.client.model.util.ModelScaleRenderer;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.constants.EnumJobType;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.roles.JobPuppet;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class ModelMPM64 extends ModelMPM{

	public EntityLivingBase entity;
	public boolean isArmor;

	public ModelMPM64(float par1) {
		super(par1);
		this.textureHeight = 64;
		this.textureWidth = 64;

		isArmor = par1 > 0;
		float par2 = 0;
		this.bipedCloak = new ModelRenderer(this, 0, 0);
		this.bipedCloak.addBox(-5.0F, 0.0F, -1.0F, 10, 16, 1, par1);
		this.bipedEars = new ModelRenderer(this, 24, 0);
		this.bipedEars.addBox(-3.0F, -6.0F, -1.0F, 6, 6, 1, par1);
		this.bipedHead = new ModelScaleRenderer(this, 0, 0);
		this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, par1);
		this.bipedHead.setRotationPoint(0.0F, 0.0F + par2, 0.0F);
		this.bipedHeadwear = new ModelScaleRenderer(this, 32, 0);
		this.bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, par1 + 0.5F);
		this.bipedHeadwear.setRotationPoint(0.0F, 0.0F + par2, 0.0F);
		this.bipedBody = new ModelScaleRenderer(this, 16, 16);
		this.bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, par1);
		this.bipedBody.setRotationPoint(0.0F, 0.0F + par2, 0.0F);

		this.bipedRightArm = new ModelScaleRenderer(this, 40, 16);
		this.bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, par1);
		this.bipedRightArm.setRotationPoint(-5.0F, 2.0F + par2, 0.0F);

		this.bipedLeftArm = new ModelScaleRenderer(this, 32, 48);
		this.bipedLeftArm.mirror = false;
		this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, par1);
		this.bipedLeftArm.setRotationPoint(5.0F, 2.0F + par2, 0.0F);

		this.bipedRightLeg = new ModelScaleRenderer(this, 0, 16);
		this.bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, par1);
		this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F + par2, 0.0F);


		this.bipedLeftLeg = new ModelScaleRenderer(this, 16, 48);
		this.bipedLeftLeg.mirror = false;
		this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, par1);
		this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F + par2, 0.0F);

	}
}

