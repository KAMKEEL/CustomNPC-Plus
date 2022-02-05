package noppes.npcs.client.model;

import java.util.Random;

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
import noppes.npcs.client.model.part.ModelBeard;
import noppes.npcs.client.model.part.ModelBreasts;
import noppes.npcs.client.model.part.ModelClaws;
import noppes.npcs.client.model.part.ModelEars;
import noppes.npcs.client.model.part.ModelFin;
import noppes.npcs.client.model.part.ModelHair;
import noppes.npcs.client.model.part.ModelHeadwear;
import noppes.npcs.client.model.part.ModelLegs;
import noppes.npcs.client.model.part.ModelMohawk;
import noppes.npcs.client.model.part.ModelSnout;
import noppes.npcs.client.model.part.ModelTail;
import noppes.npcs.client.model.part.ModelWings;
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

	private ModelPartInterface clawsR;
	private ModelPartInterface clawsL;
	
	private ModelLegs legs;
	private ModelScaleRenderer headwear;
	private ModelTail tail;
	public ModelBase entityModel;
	public EntityLivingBase entity;
	
	public boolean currentlyPlayerTexture;
	
	public boolean isArmor;
	
	public float alpha = 1;

	public ModelMPM(float par1) {
		super(par1);
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
		
		headwear = new ModelHeadwear(this);
		legs = new ModelLegs(this, (ModelScaleRenderer)bipedRightLeg, (ModelScaleRenderer)bipedLeftLeg);
		
		breasts = new ModelBreasts(this);
		this.bipedBody.addChild(breasts);
		if(!isArmor){
			ears = new ModelEars(this);
			this.bipedHead.addChild(ears);
			
			mohawk = new ModelMohawk(this);
			this.bipedHead.addChild(mohawk);
			
			hair = new ModelHair(this);
			this.bipedHead.addChild(hair);
			
			beard = new ModelBeard(this);
			this.bipedHead.addChild(beard);
	
			snout = new ModelSnout(this);
			this.bipedHead.addChild(snout);
			
			tail = new ModelTail(this);
			
			wings = new ModelWings(this);
			this.bipedBody.addChild(wings);
			
			fin = new ModelFin(this);
			this.bipedBody.addChild(fin);
			
			clawsL = new ModelClaws(this, false);
			this.bipedLeftArm.addChild(clawsL);

			clawsR = new ModelClaws(this, true);
			this.bipedRightArm.addChild(clawsR);
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
		}
		breasts.setData(entity.modelData, entity);
		legs.setData(entity);
	}
	
	
    @Override
    public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7){
    	EntityCustomNpc npc = (EntityCustomNpc) par1Entity;

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
			alpha = npc.isInvisible() && !npc.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer)?0.15f:1;
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
    	this.bipedBody.rotationPointX = 0;
    	this.bipedBody.rotationPointY = 0;
    	this.bipedBody.rotationPointZ = 0;
		this.bipedBody.rotateAngleX = 0;
		this.bipedBody.rotateAngleY = 0;
		this.bipedBody.rotateAngleZ = 0;
		this.bipedHead.rotateAngleZ = 0;
		this.bipedHeadwear.rotateAngleZ = 0;
		this.bipedLeftLeg.rotateAngleX = 0;
		this.bipedLeftLeg.rotateAngleY = 0;
		this.bipedLeftLeg.rotateAngleZ = 0;
		this.bipedRightLeg.rotateAngleX = 0;
		this.bipedRightLeg.rotateAngleY = 0;
		this.bipedRightLeg.rotateAngleZ = 0;
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
    	else if(isSneak)
            this.bipedBody.rotateAngleX = 0.5F / npc.modelData.body.scaleY;
    	
    	
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
