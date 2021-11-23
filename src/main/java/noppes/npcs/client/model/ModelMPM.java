package noppes.npcs.client.model;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import noppes.npcs.CustomItems;
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

public class ModelMPM extends ModelNPCMale{
	private ModelPartInterface wings;
	private ModelPartInterface mohawk;
	private ModelPartInterface hair;
	private ModelPartInterface beard;
	private ModelPartInterface breasts;
	private ModelPartInterface snout;
	private ModelPartInterface ears;
	private ModelPartInterface fin;
	private ModelPartInterface skirt;
	private ModelPartInterface horns;

	private ModelPartInterface clawsR;
	private ModelPartInterface clawsL;

	// New
	public ModelRenderer bipedBodywear;
	public ModelRenderer bipedRightArmwear;
	public ModelRenderer bipedLeftArmwear;
	public ModelRenderer bipedRightLegWear;
	public ModelRenderer bipedLeftLegWear;

	private ModelLegs legs;
	private ModelScaleRenderer headwear;

	// private ModelScaleRenderer bodywear;
	// private ModelScaleRenderer armwear;
	// private ModelScaleRenderer legwear;

	private ModelTail tail;
	public ModelBase entityModel;
	public EntityLivingBase entity;

	public boolean currentlyPlayerTexture;

	public boolean isArmor;
	public float alpha = 1;

	// Check Legacy Code for Old Version

	// Updated False = 64x32 Skin
	// Steve 64x64 and Alex 64x64
	public ModelMPM(float par1, boolean arms) {

		super(par1, arms);
		isArmor = par1 > 0;
		float par2 = 0;

		this.bipedCloak = new ModelRenderer(this, 0, 0);
		this.bipedCloak.setTextureSize(64,32);
		this.bipedCloak.addBox(-5.0F, 0.0F, -1.0F, 10, 16, 1, par1);

		this.bipedEars = new ModelRenderer(this, 24, 0);
		this.bipedEars.addBox(-3.0F, -6.0F, -1.0F, 6, 6, 1, par1);

		this.bipedHead = (new ModelScaleRenderer(this, 0, 0));
		this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, par1);
		this.bipedHead.setRotationPoint(0.0F, 0.0F + par2, 0.0F);

		this.bipedHeadwear = (new ModelScaleRenderer(this, 32, 0));
		this.bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, par1 + 0.5F);
		this.bipedHeadwear.setRotationPoint(0.0F, 0.0F + par2, 0.0F);

		this.bipedBody = (new ModelScaleRenderer(this, 16, 16));
		this.bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, par1);
		this.bipedBody.setRotationPoint(0.0F, 0.0F + par2, 0.0F);

		// New Extension
		this.bipedBodywear = (new ModelScaleRenderer(this, 16, 32));
		this.bipedBodywear.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, par1 + 0.5F);
		this.bipedBody.addChild(this.bipedBodywear);
		// this.bipedBodywear.setRotationPoint(0.0F, 0.0F + par2, 0.0F);

		// Steve 64x64 Model or Alex 64x64 Model
		if (arms){
			// Alex Version
			this.bipedRightArm = (new ModelScaleRenderer(this, 40, 16));
			this.bipedRightArm.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, par1);
			// this.bipedRightArm.setRotationPoint(-5.0F, 2.5F + par2, 0.0F);

			this.bipedLeftArm = new ModelScaleRenderer(this, 32, 48);
			this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, par1);
			this.bipedLeftArm.setRotationPoint(5.0F, 2.5F + par2, 0.0F);

			this.bipedRightArmwear = (new ModelScaleRenderer(this, 40, 32));
			this.bipedRightArmwear.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, par1 + 0.25F);
			this.bipedRightArm.addChild(this.bipedRightArmwear);
			// this.bipedRightArmWear.setRotationPoint(-5.0F, 2.5F + par2, 0.0F);

			this.bipedLeftArmwear = new ModelScaleRenderer(this, 48, 48);
			this.bipedLeftArmwear.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, par1 + 0.25F);
			this.bipedLeftArm.addChild(this.bipedLeftArmwear);
			// this.bipedLeftArmWear.setRotationPoint(5.0F, 2.5F + par2, 0.0F);
		}
		else{
			// Steve Version
			this.bipedRightArm = (new ModelScaleRenderer(this, 40, 16));
			this.bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, par1);
			this.bipedRightArm.setRotationPoint(-5.0F, 2.0F + par2, 0.0F);

			this.bipedLeftArm = new ModelScaleRenderer(this, 32, 48);
			this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, par1);
			this.bipedLeftArm.setRotationPoint(5.0F, 2.0F + par2, 0.0F);

			this.bipedRightArmwear = (new ModelScaleRenderer(this, 40, 32));
			this.bipedRightArmwear.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, par1 + 0.25F);
			this.bipedRightArm.addChild(this.bipedRightArmwear);
			//this.bipedRightArmWear.setRotationPoint(-5.0F, 2.0F + par2, 0.0F);

			this.bipedLeftArmwear = new ModelScaleRenderer(this, 48, 48);
			this.bipedLeftArmwear.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, par1 + 0.25F);
			this.bipedLeftArm.addChild(this.bipedLeftArmwear);
			//this.bipedLeftArmWear.setRotationPoint(5.0F, 2.0F + par2, 0.0F);
		}

		this.bipedRightLeg = (new ModelScaleRenderer(this, 0, 16));
		this.bipedRightLeg.addBox(-2.08F, 0.0F, -2.0F, 4, 12, 4, par1);
		this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F + par2, 0.0F);

		this.bipedLeftLeg = new ModelScaleRenderer(this, 16, 48);
		this.bipedLeftLeg.addBox(-1.92F, 0.0F, -2.0F, 4, 12, 4, par1);
		this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F + par2, 0.0F);

		this.bipedRightLegWear = (new ModelScaleRenderer(this, 0, 32));
		this.bipedRightLegWear.addBox(-2.08F, 0.0F, -2.0F, 4, 12, 4, par1 + 0.25F);
		this.bipedRightLeg.addChild(this.bipedRightLegWear);
		// this.bipedRightLegWear.setRotationPoint(-1.9F, 12.0F + par2, 0.0F);

		this.bipedLeftLegWear = new ModelScaleRenderer(this, 0, 48);
		this.bipedLeftLegWear.addBox(-1.92F, 0.0F, -2.0F, 4, 12, 4, par1 + 0.25F);
		this.bipedLeftLeg.addChild(this.bipedLeftLegWear);

		headwear = new ModelHeadwear(this);
		legs = new ModelLegs(this, (ModelScaleRenderer)bipedRightLeg, (ModelScaleRenderer)bipedLeftLeg, 64, 64);
		// bodywear = new ModelBodywear(this);

		this.bipedBody.addChild(breasts = new ModelBreasts(this, 64, 64));
		if(!isArmor){
			this.bipedHead.addChild(ears = new ModelEars(this));
			this.bipedHead.addChild(mohawk = new ModelMohawk(this));
			this.bipedHead.addChild(hair = new ModelHair(this));
			this.bipedHead.addChild(beard = new ModelBeard(this));

			// Completed
			this.bipedHead.addChild(snout = new ModelSnout(this));
			this.bipedHead.addChild(horns = new ModelHorns(this));

			// Completed
			tail = new ModelTail(this);

			this.bipedBody.addChild(wings = new ModelWings(this));
			this.bipedBody.addChild(fin = new ModelFin(this));

			// Fix This
			this.bipedBody.addChild(skirt = new ModelSkirt(this));
			this.bipedLeftArm.addChild(clawsL = new ModelClaws(this, false));
			this.bipedRightArm.addChild(clawsR = new ModelClaws(this, true));
		}
	}

	// Steve 64x32
	public ModelMPM(float par1) {

		super(par1);
		isArmor = par1 > 0;
		float par2 = 0;

		this.bipedCloak = new ModelRenderer(this, 0, 0);
		this.bipedCloak.setTextureSize(64,32);
		this.bipedCloak.addBox(-5.0F, 0.0F, -1.0F, 10, 16, 1, par1);

		this.bipedEars = new ModelRenderer(this, 24, 0);
		this.bipedEars.addBox(-3.0F, -6.0F, -1.0F, 6, 6, 1, par1);

		this.bipedHead = (new ModelScaleRenderer(this, 0, 0));
		this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, par1);
		this.bipedHead.setRotationPoint(0.0F, 0.0F + par2, 0.0F);

		this.bipedHeadwear = (new ModelScaleRenderer(this, 32, 0));
		this.bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, par1 + 0.5F);
		this.bipedHeadwear.setRotationPoint(0.0F, 0.0F + par2, 0.0F);

		this.bipedBody = (new ModelScaleRenderer(this, 16, 16));
		this.bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, par1);
		this.bipedBody.setRotationPoint(0.0F, 0.0F + par2, 0.0F);

		this.bipedRightArm = new ModelScaleRenderer(this, 40, 16);
		this.bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, par1);
		this.bipedRightArm.setRotationPoint(-5.0F, 2.0F + par2, 0.0F);

		this.bipedLeftArm = new ModelScaleRenderer(this, 40, 16);
		this.bipedLeftArm.mirror = true;
		this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, par1);
		this.bipedLeftArm.setRotationPoint(5.0F, 2.0F + par2, 0.0F);

		this.bipedRightLeg = new ModelScaleRenderer(this, 0, 16);
		this.bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, par1);
		this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F + par2, 0.0F);

		this.bipedLeftLeg = new ModelScaleRenderer(this, 0, 16);
		this.bipedLeftLeg.mirror = true;
		this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, par1);
		this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F + par2, 0.0F);

		// Body
		this.bipedBodywear = (new ModelScaleRenderer(this, 0, 0));
		this.bipedBody.addChild(this.bipedBodywear);

		// Arms
		this.bipedRightArmwear = (new ModelScaleRenderer(this, 0, 0));
		this.bipedRightArm.addChild(this.bipedRightArmwear);
		this.bipedLeftArmwear = new ModelScaleRenderer(this, 0, 0);
		this.bipedLeftArm.addChild(this.bipedLeftArmwear);

		// Legs
		this.bipedRightLegWear = (new ModelScaleRenderer(this, 0, 0));
		this.bipedRightLeg.addChild(this.bipedRightLegWear);
		this.bipedLeftLegWear = new ModelScaleRenderer(this, 0, 0);
		this.bipedLeftLeg.addChild(this.bipedLeftLegWear);

		headwear = new ModelHeadwear(this, true);
		legs = new ModelLegs(this, (ModelScaleRenderer)bipedRightLeg, (ModelScaleRenderer)bipedLeftLeg, 64, 32);

		this.bipedBody.addChild(breasts = new ModelBreasts(this, 64, 32));
		if(!isArmor){
			this.bipedHead.addChild(ears = new ModelEars(this));
			this.bipedHead.addChild(mohawk = new ModelMohawk(this));
			this.bipedHead.addChild(hair = new ModelHair(this));
			this.bipedHead.addChild(beard = new ModelBeard(this));

			// Completed
			this.bipedHead.addChild(snout = new ModelSnout(this));
			this.bipedHead.addChild(horns = new ModelHorns(this));

			// Completed
			tail = new ModelTail(this);

			this.bipedBody.addChild(wings = new ModelWings(this));
			this.bipedBody.addChild(fin = new ModelFin(this));

			// Fix This
			this.bipedBody.addChild(skirt = new ModelSkirt(this));
			this.bipedLeftArm.addChild(clawsL = new ModelClaws(this, false));
			this.bipedRightArm.addChild(clawsR = new ModelClaws(this, true));
		}
	}

	private void setPlayerData(EntityCustomNpc entity){
		if(!isArmor){
			mohawk.setData(entity.modelData, entity);
			beard.setData(entity.modelData, entity);
			hair.setData(entity.modelData, entity);
			snout.setData(entity.modelData, entity);
			tail.setData(entity);
			fin.setData(entity.modelData, entity);
			wings.setData(entity.modelData, entity);
			ears.setData(entity.modelData, entity);
			clawsL.setData(entity.modelData, entity);
			clawsR.setData(entity.modelData, entity);
			skirt.setData(entity.modelData, entity);
			horns.setData(entity.modelData, entity);
		}
		breasts.setData(entity.modelData, entity);
		legs.setData(entity);
	}


	@Override
	public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7){
		EntityCustomNpc npc = (EntityCustomNpc) par1Entity;
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if(npc.scriptInvisibleToPlayer(Minecraft.getMinecraft().thePlayer) && (player.getHeldItem() == null || player.getHeldItem().getItem() != CustomItems.wand))
			return;

		if(entityModel != null){
			if(!isArmor){
				entityModel.isChild = entity.isChild();
				entityModel.onGround = onGround;
				entityModel.isRiding = isRiding;
				if(entityModel instanceof ModelBiped){
					ModelBiped biped = (ModelBiped) entityModel;
					biped.aimedBow = aimedBow;
					biped.heldItemLeft = heldItemLeft;
					biped.heldItemRight = heldItemRight;
					biped.isSneak = isSneak;
				}
				entityModel.render(entity, par2, par3, par4, par5, par6, par7);
			}
		}
		else{
			alpha = npc.isInvisible() && !npc.isInvisibleToPlayer(player) ? 0.15f : 1;

			setPlayerData(npc);
			currentlyPlayerTexture = true;
			setRotationAngles(par2, par3, par4, par5, par6, par7, par1Entity);
			if(npc.advanced.job == EnumJobType.Puppet){
				JobPuppet job = (JobPuppet) npc.jobInterface;
				if(job.isActive()){
					float pi = (float) Math.PI;

					if(!job.head.disabled){
						bipedHeadwear.rotateAngleX = bipedHead.rotateAngleX = job.head.rotationX * pi;
						bipedHeadwear.rotateAngleY = bipedHead.rotateAngleY = job.head.rotationY * pi;
						bipedHeadwear.rotateAngleZ = bipedHead.rotateAngleZ = job.head.rotationZ * pi;
					}

					if(!job.body.disabled){
						bipedBody.rotateAngleX = job.body.rotationX * pi;
						bipedBody.rotateAngleY = job.body.rotationY * pi;
						bipedBody.rotateAngleZ = job.body.rotationZ * pi;
					}

					if(!job.larm.disabled){
						bipedLeftArm.rotateAngleX = job.larm.rotationX * pi;
						bipedLeftArm.rotateAngleY = job.larm.rotationY * pi;
						bipedLeftArm.rotateAngleZ = job.larm.rotationZ * pi;

						if(!npc.display.disableLivingAnimation){
							this.bipedLeftArm.rotateAngleZ -= MathHelper.cos(par3 * 0.09F) * 0.05F + 0.05F;
							this.bipedLeftArm.rotateAngleX -= MathHelper.sin(par3 * 0.067F) * 0.05F;
						}
					}

					if(!job.rarm.disabled){
						bipedRightArm.rotateAngleX = job.rarm.rotationX * pi;
						bipedRightArm.rotateAngleY = job.rarm.rotationY * pi;
						bipedRightArm.rotateAngleZ = job.rarm.rotationZ * pi;

						if(!npc.display.disableLivingAnimation){
							this.bipedRightArm.rotateAngleZ += MathHelper.cos(par3 * 0.09F) * 0.05F + 0.05F;
							this.bipedRightArm.rotateAngleX += MathHelper.sin(par3 * 0.067F) * 0.05F;
						}
					}

					if(!job.rleg.disabled){
						bipedRightLeg.rotateAngleX = job.rleg.rotationX * pi;
						bipedRightLeg.rotateAngleY = job.rleg.rotationY * pi;
						bipedRightLeg.rotateAngleZ = job.rleg.rotationZ * pi;
					}

					if(!job.lleg.disabled){
						bipedLeftLeg.rotateAngleX = job.lleg.rotationX * pi;
						bipedLeftLeg.rotateAngleY = job.lleg.rotationY * pi;
						bipedLeftLeg.rotateAngleZ = job.lleg.rotationZ * pi;
					}
				}
			}
			renderHead(npc, par7);
			renderArms(npc, par7,false);
			renderBody(npc, par7);
			renderLegs(npc, par7);
		}

	}
	@Override
	public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity entity)
	{
		EntityCustomNpc npc = (EntityCustomNpc) entity;
		isRiding = npc.isRiding();
		if(isSneak && (npc.currentAnimation == EnumAnimation.CRAWLING || npc.currentAnimation == EnumAnimation.LYING))
			isSneak = false;

		// Body Rotations
		this.bipedBody.rotationPointX = 0;
		this.bipedBody.rotationPointY = 0;
		this.bipedBody.rotationPointZ = 0;
		this.bipedBody.rotateAngleX = 0;
		this.bipedBody.rotateAngleY = 0;
		this.bipedBody.rotateAngleZ = 0;

		this.bipedBodywear.rotationPointX = 0;
		this.bipedBodywear.rotationPointY = 0;
		this.bipedBodywear.rotationPointZ = 0;
		this.bipedBodywear.rotateAngleX = 0;
		this.bipedBodywear.rotateAngleY = 0;
		this.bipedBodywear.rotateAngleZ = 0;

		// Head Rotations
		this.bipedHead.rotateAngleZ = 0;
		this.bipedHeadwear.rotateAngleZ = 0;

		// Leg Rotations
		this.bipedLeftLeg.rotateAngleX = 0;
		this.bipedLeftLeg.rotateAngleY = 0;
		this.bipedLeftLeg.rotateAngleZ = 0;

		this.bipedRightLeg.rotateAngleX = 0;
		this.bipedRightLeg.rotateAngleY = 0;
		this.bipedRightLeg.rotateAngleZ = 0;

		// Arm Rotations
		this.bipedLeftArm.rotationPointY = 2;
		this.bipedLeftArm.rotationPointZ = 0;

		this.bipedRightArm.rotationPointY = 2;
		this.bipedRightArm.rotationPointZ = 0;

		super.setRotationAngles(par1, par2, par3, par4, par5, par6, entity);

		if(!isArmor){
			hair.setRotationAngles(par1, par2, par3, par4, par5, par6, entity);
			beard.setRotationAngles(par1, par2, par3, par4, par5, par6, entity);
			wings.setRotationAngles(par1, par2, par3, par4, par5, par6, entity);
			tail.setRotationAngles(par1, par2, par3, par4, par5, par6, entity);
			skirt.setRotationAngles(par1, par2, par3, par4, par5, par6, entity);
		}
		legs.setRotationAngles(par1, par2, par3, par4, par5, par6, entity);

		if(isSleeping(entity)){
			if(bipedHead.rotateAngleX < 0){
				bipedHead.rotateAngleX = 0;
				bipedHeadwear.rotateAngleX = 0;
			}
		}
		else if(npc.currentAnimation == EnumAnimation.CRY)
			bipedHeadwear.rotateAngleX = bipedHead.rotateAngleX = 0.7f;

		else if(npc.currentAnimation == EnumAnimation.HUG){
			AniHug.setRotationAngles(par1, par2, par3, par4, par5, par6, entity, this);
		}

		else if(npc.currentAnimation == EnumAnimation.CRAWLING)
			AniCrawling.setRotationAngles(par1, par2, par3, par4, par5, par6, entity, this);

		else if(npc.currentAnimation == EnumAnimation.WAVING){
			bipedRightArm.rotateAngleX = -0.1f;
			bipedRightArm.rotateAngleY = 0;
			bipedRightArm.rotateAngleZ = (float) (Math.PI - 1f  - Math.sin(entity.ticksExisted * 0.27f) * 0.5f );
		}
		else if(isSneak){
			this.bipedBody.rotateAngleX = 0.5F / npc.modelData.body.scaleY;
		}

	}

	public void setLivingAnimations(EntityLivingBase par1EntityLivingBase, float par2, float par3, float par4)
	{
		if(entityModel != null){
			entityModel.setLivingAnimations(entity, par2, par3, par4);
		}
		else{
			EntityCustomNpc npc = (EntityCustomNpc) par1EntityLivingBase;
			if(!isArmor){
				ModelPartData partData = npc.modelData.getPartData("tail");
				if(partData != null)
					tail.setLivingAnimations(partData, par1EntityLivingBase, par2, par3, par4);
			}
		}
	}

	public void loadPlayerTexture(EntityCustomNpc npc){
		if(!isArmor && !currentlyPlayerTexture){
			ClientProxy.bindTexture(npc.textureLocation);
			currentlyPlayerTexture = true;
		}
	}

	private void renderHead(EntityCustomNpc entity, float f) {
		loadPlayerTexture(entity);
		float x = 0;
		float y = entity.modelData.getBodyY();
		float z = 0;

		GL11.glPushMatrix();
		if(entity.currentAnimation == EnumAnimation.DANCING){
			float dancing = entity.ticksExisted / 4f;
			GL11.glTranslatef((float)Math.sin(dancing) * 0.075F, (float)Math.abs(Math.cos(dancing)) * 0.125F - 0.02F, (float)(-Math.abs(Math.cos(dancing))) * 0.075F);
		}
		ModelPartConfig head = entity.modelData.head;

		// Hide Head
		((ModelScaleRenderer)this.bipedHead).isHidden = entity.modelData.hideHead == 1;

		if(bipedHeadwear.showModel && !bipedHeadwear.isHidden){
			if(entity.modelData.headwear == 1 || isArmor){
				((ModelScaleRenderer)this.bipedHeadwear).setConfig(head,x,y,z);
				((ModelScaleRenderer)this.bipedHeadwear).render(f);
			}
			else if(entity.modelData.headwear == 2){
				this.headwear.rotateAngleX = bipedHeadwear.rotateAngleX;
				this.headwear.rotateAngleY = bipedHeadwear.rotateAngleY;
				this.headwear.rotateAngleZ = bipedHeadwear.rotateAngleZ;
				this.headwear.rotationPointX = bipedHeadwear.rotationPointX;
				this.headwear.rotationPointY = bipedHeadwear.rotationPointY;
				this.headwear.rotationPointZ = bipedHeadwear.rotationPointZ;
				this.headwear.setConfig(head,x,y,z);
				this.headwear.render(f);
			}
		}
		((ModelScaleRenderer)this.bipedHead).setConfig(head,x,y,z);
		((ModelScaleRenderer)this.bipedHead).render(f);

		GL11.glPopMatrix();
	}

	private void renderBody(EntityCustomNpc entity, float f) {
		loadPlayerTexture(entity);
		float x = 0;
		float y = entity.modelData.getBodyY();
		float z = 0;
		GL11.glPushMatrix();

		if(entity.currentAnimation == EnumAnimation.DANCING){
			float dancing = entity.ticksExisted / 4f;
			GL11.glTranslatef((float)Math.sin(dancing) * 0.015F, 0.0F, 0.0F);
		}

		ModelPartConfig body = entity.modelData.body;

		// Hide Body
		((ModelScaleRenderer)this.bipedBody).isHidden = entity.modelData.hideBody == 1;

		// Hide Bodywear
		((ModelScaleRenderer)this.bipedBodywear).isHidden = entity.modelData.bodywear != 1;

		((ModelScaleRenderer)this.bipedBody).setConfig(body,x,y,z);
		((ModelScaleRenderer)this.bipedBody).render(f);
		GL11.glPopMatrix();

	}
	public void renderArms(EntityCustomNpc entity, float f, boolean bo){
		loadPlayerTexture(entity);
		ModelPartConfig arms = entity.modelData.arms;

		float x = (1 - entity.modelData.body.scaleX) * 0.25f + (1 - arms.scaleX) * 0.075f;
		float y = entity.modelData.getBodyY() + (1 - arms.scaleY) * -0.1f;
		float z = 0;

		GL11.glPushMatrix();

		if(entity.currentAnimation == EnumAnimation.DANCING){
			float dancing = entity.ticksExisted / 4f;
			GL11.glTranslatef((float)Math.sin(dancing) * 0.025F, (float)Math.abs(Math.cos(dancing)) * 0.125F - 0.02F, 0.0F);
		}

		// Hide Arms
		if(entity.modelData.hideArms == 1){
			((ModelScaleRenderer)this.bipedRightArm).isHidden = true;
			((ModelScaleRenderer)this.bipedLeftArm).isHidden = true;
		}
		else if(entity.modelData.hideArms == 2){
			((ModelScaleRenderer)this.bipedRightArm).isHidden = true;
			((ModelScaleRenderer)this.bipedLeftArm).isHidden = false;
		}
		else if(entity.modelData.hideArms == 3){
			((ModelScaleRenderer)this.bipedRightArm).isHidden = false;
			((ModelScaleRenderer)this.bipedLeftArm).isHidden = true;
		}
		else{
			((ModelScaleRenderer)this.bipedRightArm).isHidden = false;
			((ModelScaleRenderer)this.bipedLeftArm).isHidden = false;
		}

		// Hide Armwear
		if(entity.modelData.armwear == 1){
			((ModelScaleRenderer)this.bipedRightArmwear).isHidden = false;
			((ModelScaleRenderer)this.bipedLeftArmwear).isHidden = false;
		}
		else if(entity.modelData.armwear == 2){
			((ModelScaleRenderer)this.bipedRightArmwear).isHidden = true;
			((ModelScaleRenderer)this.bipedLeftArmwear).isHidden = false;
		}
		else if(entity.modelData.armwear == 3){
			((ModelScaleRenderer)this.bipedRightArmwear).isHidden = false;
			((ModelScaleRenderer)this.bipedLeftArmwear).isHidden = true;
		}
		else{
			((ModelScaleRenderer)this.bipedRightArmwear).isHidden = true;
			((ModelScaleRenderer)this.bipedLeftArmwear).isHidden = true;
		}

		if(!bo){
			((ModelScaleRenderer)this.bipedLeftArm).setConfig(arms,-x,y,z);
			((ModelScaleRenderer)this.bipedLeftArm).render(f);
			((ModelScaleRenderer)this.bipedRightArm).setConfig(arms,x,y,z);
			((ModelScaleRenderer)this.bipedRightArm).render(f);
		}
		else{
			((ModelScaleRenderer)this.bipedRightArm).setConfig(arms,0,0,0);
			((ModelScaleRenderer)this.bipedRightArm).render(f);
		}

		GL11.glPopMatrix();
	}
	private void renderLegs(EntityCustomNpc entity, float f) {
		loadPlayerTexture(entity);
		ModelPartConfig legs = entity.modelData.legs;

		float x = (1 - legs.scaleX) * 0.125f;
		float y = entity.modelData.getLegsY();
		float z = 0;

		GL11.glPushMatrix();


		// Hide Legs
		if(entity.modelData.hideLegs == 1){
			((ModelScaleRenderer)this.bipedRightLeg).isHidden = true;
			((ModelScaleRenderer)this.bipedLeftLeg).isHidden = true;
		}
		else if(entity.modelData.hideLegs == 2){
			((ModelScaleRenderer)this.bipedRightLeg).isHidden = true;
			((ModelScaleRenderer)this.bipedLeftLeg).isHidden = false;
		}
		else if(entity.modelData.hideLegs == 3){
			((ModelScaleRenderer)this.bipedRightLeg).isHidden = false;
			((ModelScaleRenderer)this.bipedLeftLeg).isHidden = true;
		}
		else{
			((ModelScaleRenderer)this.bipedRightLeg).isHidden = false;
			((ModelScaleRenderer)this.bipedLeftLeg).isHidden = false;
		}

		// Hide Legwear
		if(entity.modelData.legwear == 1){
			((ModelScaleRenderer)this.bipedRightLegWear).isHidden = false;
			((ModelScaleRenderer)this.bipedLeftLegWear).isHidden = false;
		}
		else if(entity.modelData.legwear == 2){
			((ModelScaleRenderer)this.bipedRightLegWear).isHidden = true;
			((ModelScaleRenderer)this.bipedLeftLegWear).isHidden = false;
		}
		else if(entity.modelData.legwear == 3){
			((ModelScaleRenderer)this.bipedRightLegWear).isHidden = false;
			((ModelScaleRenderer)this.bipedLeftLegWear).isHidden = true;
		}
		else{
			((ModelScaleRenderer)this.bipedRightLegWear).isHidden = true;
			((ModelScaleRenderer)this.bipedLeftLegWear).isHidden = true;
		}

		this.legs.setConfig(legs,x,y,z);
		this.legs.render(f);
		if(!isArmor){
			this.tail.setConfig(legs, 0, y, z);
			this.tail.render(f);
		}
		GL11.glPopMatrix();
	}
	@Override
	public ModelRenderer getRandomModelBox(Random par1Random)
	{
		int random = par1Random.nextInt(5);
		switch(random){
			case 0:
				return bipedRightLeg;
			case 1:
				return bipedHead;
			case 2:
				return bipedLeftArm;
			case 3:
				return bipedRightArm;
			case 4:
				return bipedLeftLeg;
		}

		return bipedBody;
	}
}
