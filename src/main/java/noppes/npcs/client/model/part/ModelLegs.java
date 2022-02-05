package noppes.npcs.client.model.part;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.model.ModelMPM;
import noppes.npcs.client.model.part.legs.ModelDigitigradeLegs;
import noppes.npcs.client.model.part.legs.ModelMermaidLegs;
import noppes.npcs.client.model.part.legs.ModelNagaLegs;
import noppes.npcs.client.model.util.ModelScaleRenderer;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.entity.EntityCustomNpc;

import org.lwjgl.opengl.GL11;

public class ModelLegs extends ModelScaleRenderer{
	private EntityCustomNpc entity;
	private ModelScaleRenderer leg1, leg2;
	private ModelRenderer spider;
	private ModelRenderer horse;
	private ModelNagaLegs naga;
	private ModelDigitigradeLegs digitigrade;
	private ModelMermaidLegs mermaid;

	private ModelRenderer spiderLeg1;
    private ModelRenderer spiderLeg2;
    private ModelRenderer spiderLeg3;
    private ModelRenderer spiderLeg4;
    private ModelRenderer spiderLeg5;
    private ModelRenderer spiderLeg6;
    private ModelRenderer spiderLeg7;
    private ModelRenderer spiderLeg8;
    private ModelRenderer spiderBody;
    private ModelRenderer spiderNeck;
  
    private ModelRenderer backLeftLeg;
    private ModelRenderer backLeftShin;
    private ModelRenderer backLeftHoof;
    
    private ModelRenderer backRightLeg;
    private ModelRenderer backRightShin;
    private ModelRenderer backRightHoof;
    
    private ModelRenderer frontLeftLeg;
    private ModelRenderer frontLeftShin;
    private ModelRenderer frontLeftHoof;
    
    private ModelRenderer frontRightLeg;
    private ModelRenderer frontRightShin;
    private ModelRenderer frontRightHoof;
            
    private ModelMPM base;
	
	public ModelLegs(ModelMPM base, ModelScaleRenderer leg1, ModelScaleRenderer leg2) {
		super(base);
		this.base = base;
		this.leg1 = leg1;
		this.leg2 = leg2;

		if(base.isArmor)
			return;
		spider = new ModelRenderer(base);
        this.addChild(spider);

        float var1 = 0.0F;
        byte var2 = 15;
        spiderNeck = new ModelRenderer(base, 0, 0);
        spiderNeck.addBox(-3.0F, -3.0F, -3.0F, 6, 6, 6, var1);
        spiderNeck.setRotationPoint(0.0F, (float)var2, 2.0F);
        spider.addChild(spiderNeck);
        
        spiderBody = new ModelRenderer(base, 0, 12);
        spiderBody.addBox(-5.0F, -4.0F, -6.0F, 10, 8, 12, var1);
        spiderBody.setRotationPoint(0.0F, (float)var2, 11.0F);
        spider.addChild(spiderBody);
        
        this.spiderLeg1 = new ModelRenderer(base, 18, 0);
        this.spiderLeg1.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, var1);
        this.spiderLeg1.setRotationPoint(-4.0F, (float)var2, 4.0F);
        spider.addChild(spiderLeg1);
        
        this.spiderLeg2 = new ModelRenderer(base, 18, 0);
        this.spiderLeg2.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, var1);
        this.spiderLeg2.setRotationPoint(4.0F, (float)var2, 4.0F);
        spider.addChild(spiderLeg2);
        
        this.spiderLeg3 = new ModelRenderer(base, 18, 0);
        this.spiderLeg3.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, var1);
        this.spiderLeg3.setRotationPoint(-4.0F, (float)var2, 3.0F);
        spider.addChild(spiderLeg3);
        
        this.spiderLeg4 = new ModelRenderer(base, 18, 0);
        this.spiderLeg4.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, var1);
        this.spiderLeg4.setRotationPoint(4.0F, (float)var2, 3.0F);
        spider.addChild(spiderLeg4);
        
        this.spiderLeg5 = new ModelRenderer(base, 18, 0);
        this.spiderLeg5.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, var1);
        this.spiderLeg5.setRotationPoint(-4.0F, (float)var2, 2.0F);
        spider.addChild(spiderLeg5);
        
        this.spiderLeg6 = new ModelRenderer(base, 18, 0);
        this.spiderLeg6.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, var1);
        this.spiderLeg6.setRotationPoint(4.0F, (float)var2, 2.0F);
        spider.addChild(spiderLeg6);
        
        this.spiderLeg7 = new ModelRenderer(base, 18, 0);
        this.spiderLeg7.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, var1);
        this.spiderLeg7.setRotationPoint(-4.0F, (float)var2, 1.0F);
        spider.addChild(spiderLeg7);
        
        this.spiderLeg8 = new ModelRenderer(base, 18, 0);
        this.spiderLeg8.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, var1);
        this.spiderLeg8.setRotationPoint(4.0F, (float)var2, 1.0F);
        spider.addChild(spiderLeg8);

        
        int zOffset = 10;
        float yOffset = 7f;
        horse = new ModelRenderer(base);
        this.addChild(horse);

        ModelRenderer body = new ModelRenderer(base, 0, 34);
        body.setTextureSize(128, 128);
        body.addBox(-5.0F, -8.0F, -19.0F, 10, 10, 24);
        body.setRotationPoint(0.0F, 11.0F + yOffset, 9.0F + zOffset);
        horse.addChild(body);
        
        this.backLeftLeg = new ModelRenderer(base, 78, 29);
        this.backLeftLeg.setTextureSize(128, 128);
        this.backLeftLeg.addBox(-2.5F, -2.0F, -2.5F, 4, 9, 5);
        this.backLeftLeg.setRotationPoint(4.0F, 9.0F + yOffset, 11.0F + zOffset);
        horse.addChild(backLeftLeg);
        this.backLeftShin = new ModelRenderer(base, 78, 43);
        this.backLeftShin.setTextureSize(128, 128);
        this.backLeftShin.addBox(-2.0F, 0.0F, -1.5F, 3, 5, 3);
        this.backLeftShin.setRotationPoint(0F, 7.0F, 0);
        backLeftLeg.addChild(backLeftShin);
        this.backLeftHoof = new ModelRenderer(base, 78, 51);
        this.backLeftHoof.setTextureSize(128, 128);
        this.backLeftHoof.addBox(-2.5F, 5.1F, -2.0F, 4, 3, 4);
        this.backLeftHoof.setRotationPoint(0F, 7.0F, 0);
        backLeftLeg.addChild(backLeftHoof);
        
        this.backRightLeg = new ModelRenderer(base, 96, 29);
        this.backRightLeg.setTextureSize(128, 128);
        this.backRightLeg.addBox(-1.5F, -2.0F, -2.5F, 4, 9, 5);
        this.backRightLeg.setRotationPoint(-4.0F, 9.0F + yOffset, 11.0F + zOffset);
        horse.addChild(backRightLeg);
        this.backRightShin = new ModelRenderer(base, 96, 43);
        this.backRightShin.setTextureSize(128, 128);
        this.backRightShin.addBox(-1.0F, 0.0F, -1.5F, 3, 5, 3);
        this.backRightShin.setRotationPoint(0F, 7, 0);
        backRightLeg.addChild(backRightShin);
        this.backRightHoof = new ModelRenderer(base, 96, 51);
        this.backRightHoof.setTextureSize(128, 128);
        this.backRightHoof.addBox(-1.5F, 5.1F, -2.0F, 4, 3, 4);
        this.backRightHoof.setRotationPoint(0F, 7, 0);
        backRightLeg.addChild(backRightHoof);
        
        this.frontLeftLeg = new ModelRenderer(base, 44, 29);
        this.frontLeftLeg.setTextureSize(128, 128);
        this.frontLeftLeg.addBox(-1.9F, -1.0F, -2.1F, 3, 8, 4);
        this.frontLeftLeg.setRotationPoint(4.0F, 9.0F + yOffset, -8.0F + zOffset);
        horse.addChild(frontLeftLeg);
        this.frontLeftShin = new ModelRenderer(base, 44, 41);
        this.frontLeftShin.setTextureSize(128, 128);
        this.frontLeftShin.addBox(-1.9F, 0.0F, -1.6F, 3, 5, 3);
        this.frontLeftShin.setRotationPoint(0F, 7.0F, 0F);
        frontLeftLeg.addChild(frontLeftShin);
        this.frontLeftHoof = new ModelRenderer(base, 44, 51);
        this.frontLeftHoof.setTextureSize(128, 128);
        this.frontLeftHoof.addBox(-2.4F, 5.1F, -2.1F, 4, 3, 4);
        this.frontLeftHoof.setRotationPoint(.0F, 7.0F, 0F);
        frontLeftLeg.addChild(frontLeftHoof);
        
        this.frontRightLeg = new ModelRenderer(base, 60, 29);
        this.frontRightLeg.setTextureSize(128, 128);
        this.frontRightLeg.addBox(-1.1F, -1.0F, -2.1F, 3, 8, 4);
        this.frontRightLeg.setRotationPoint(-4.0F, 9.0F + yOffset, -8.0F + zOffset);
        horse.addChild(frontRightLeg);
        this.frontRightShin = new ModelRenderer(base, 60, 41);
        this.frontRightShin.setTextureSize(128, 128);
        this.frontRightShin.addBox(-1.1F, 0.0F, -1.6F, 3, 5, 3);
        this.frontRightShin.setRotationPoint(0F, 7, 0);
        frontRightLeg.addChild(frontRightShin);
        this.frontRightHoof = new ModelRenderer(base, 60, 51);
        this.frontRightHoof.setTextureSize(128, 128);
        this.frontRightHoof.addBox(-1.6F, 5.1F, -2.1F, 4, 3, 4);
        this.frontRightHoof.setRotationPoint(0F, 7, 0);
        frontRightLeg.addChild(frontRightHoof);

		naga = new ModelNagaLegs(base);
        this.addChild(naga);

		mermaid = new ModelMermaidLegs(base);
        this.addChild(mermaid);
        
        digitigrade = new ModelDigitigradeLegs(base);
        this.addChild(digitigrade);
	}
	public void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
    public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity entity)
    {
		ModelPartData part = this.entity.modelData.legParts;
    	rotationPointZ = 0;
    	rotationPointY = 0;
		
		if(base.isArmor)
			return;
		if(part.type == 2){
			this.rotateAngleX = 0;
			spiderBody.rotationPointY = 15;
			spiderBody.rotationPointZ = 11;
			spiderNeck.rotateAngleX = 0;
			
	        float var8 = ((float)Math.PI / 4F);
	        this.spiderLeg1.rotateAngleZ = -var8;
	        this.spiderLeg2.rotateAngleZ = var8;
	        this.spiderLeg3.rotateAngleZ = -var8 * 0.74F;
	        this.spiderLeg4.rotateAngleZ = var8 * 0.74F;
	        this.spiderLeg5.rotateAngleZ = -var8 * 0.74F;
	        this.spiderLeg6.rotateAngleZ = var8 * 0.74F;
	        this.spiderLeg7.rotateAngleZ = -var8;
	        this.spiderLeg8.rotateAngleZ = var8;
	        float var9 = -0.0F;
	        float var10 = 0.3926991F;
	        this.spiderLeg1.rotateAngleY = var10 * 2.0F + var9;
	        this.spiderLeg2.rotateAngleY = -var10 * 2.0F - var9;
	        this.spiderLeg3.rotateAngleY = var10 * 1.0F + var9;
	        this.spiderLeg4.rotateAngleY = -var10 * 1.0F - var9;
	        this.spiderLeg5.rotateAngleY = -var10 * 1.0F + var9;
	        this.spiderLeg6.rotateAngleY = var10 * 1.0F - var9;
	        this.spiderLeg7.rotateAngleY = -var10 * 2.0F + var9;
	        this.spiderLeg8.rotateAngleY = var10 * 2.0F - var9;
	        float var11 = -(MathHelper.cos(par1 * 0.6662F * 2.0F + 0.0F) * 0.4F) * par2;
	        float var12 = -(MathHelper.cos(par1 * 0.6662F * 2.0F + (float)Math.PI) * 0.4F) * par2;
	        float var13 = -(MathHelper.cos(par1 * 0.6662F * 2.0F + ((float)Math.PI / 2F)) * 0.4F) * par2;
	        float var14 = -(MathHelper.cos(par1 * 0.6662F * 2.0F + ((float)Math.PI * 3F / 2F)) * 0.4F) * par2;
	        float var15 = Math.abs(MathHelper.sin(par1 * 0.6662F + 0.0F) * 0.4F) * par2;
	        float var16 = Math.abs(MathHelper.sin(par1 * 0.6662F + (float)Math.PI) * 0.4F) * par2;
	        float var17 = Math.abs(MathHelper.sin(par1 * 0.6662F + ((float)Math.PI / 2F)) * 0.4F) * par2;
	        float var18 = Math.abs(MathHelper.sin(par1 * 0.6662F + ((float)Math.PI * 3F / 2F)) * 0.4F) * par2;
	        this.spiderLeg1.rotateAngleY += var11;
	        this.spiderLeg2.rotateAngleY += -var11;
	        this.spiderLeg3.rotateAngleY += var12;
	        this.spiderLeg4.rotateAngleY += -var12;
	        this.spiderLeg5.rotateAngleY += var13;
	        this.spiderLeg6.rotateAngleY += -var13;
	        this.spiderLeg7.rotateAngleY += var14;
	        this.spiderLeg8.rotateAngleY += -var14;
	        this.spiderLeg1.rotateAngleZ += var15;
	        this.spiderLeg2.rotateAngleZ += -var15;
	        this.spiderLeg3.rotateAngleZ += var16;
	        this.spiderLeg4.rotateAngleZ += -var16;
	        this.spiderLeg5.rotateAngleZ += var17;
	        this.spiderLeg6.rotateAngleZ += -var17;
	        this.spiderLeg7.rotateAngleZ += var18;
	        this.spiderLeg8.rotateAngleZ += -var18;

	        
	        if(base.isSneak){
	        	rotationPointZ = 5;
	        	rotationPointY = -1;
				spiderBody.rotationPointY = 16;
				spiderBody.rotationPointZ = 10;
				spiderNeck.rotateAngleX = (float) (Math.PI / -8);
	        }
	        if(base.isSleeping(entity) || this.entity.currentAnimation == EnumAnimation.CRAWLING){
	        	rotationPointY = 12 * this.entity.modelData.legs.scaleY;
	        	rotationPointZ = 15 * this.entity.modelData.legs.scaleY;

				this.rotateAngleX = (float) (Math.PI / -2);
	        }
		}
		else if(part.type == 3){
            this.frontLeftLeg.rotateAngleX = MathHelper.cos(par1 * 0.6662F) * .4F * par2;            
            this.frontRightLeg.rotateAngleX = MathHelper.cos(par1 * 0.6662F + (float)Math.PI) * .4F * par2;
            this.backLeftLeg.rotateAngleX = MathHelper.cos(par1 * 0.6662F + (float)Math.PI) * .4F * par2;
            this.backRightLeg.rotateAngleX = MathHelper.cos(par1 * 0.6662F) * .4F * par2;
		}
		else if(part.type == 1){
			naga.isRiding = base.isRiding;
			naga.isSleeping = base.isSleeping(entity);
			naga.isCrawling = this.entity.currentAnimation == EnumAnimation.CRAWLING;
			naga.isSneaking = base.isSneak;
			naga.setRotationAngles(par1, par2, par3, par4, par5, par6, entity);
		}
		else if(part.type == 4){
			mermaid.setRotationAngles(par1, par2, par3, par4, par5, par6, entity);
		}
		else if(part.type == 5){
			digitigrade.setRotationAngles(par1, par2, par3, par4, par5, par6, entity);
		}
    }
	
	@Override
    public void render(float par1)
    {
		if(!showModel || isHidden)
			return;
		ModelPartData part = entity.modelData.legParts;
		if(part.type < 0)
			return;
		GL11.glPushMatrix();
		if(part.type == 4)
			part.playerTexture = !entity.isInWater();
		if(!base.isArmor){
			if(!part.playerTexture){
				ClientProxy.bindTexture(part.getResource());
				base.currentlyPlayerTexture = false;
			}
			else if(!base.currentlyPlayerTexture){
				ClientProxy.bindTexture(entity.textureLocation);
	            base.currentlyPlayerTexture = true;
			}
		}
		if(part.type == 0 || part.type == 4 && !entity.isInWater()){
			leg1.setConfig(config, x, y, z);
			leg1.render(par1);
			leg2.setConfig(config, -x, y, z);
			leg2.render(par1);
		}

		if(!base.isArmor){
			naga.isHidden = part.type != 1;
			spider.isHidden = part.type != 2;
			horse.isHidden = part.type != 3;
			mermaid.isHidden = part.type != 4 || !entity.isInWater();
			digitigrade.isHidden = part.type != 5;
	
			if(!horse.isHidden){
				x = 0;
				y *= 1.8f;
				GL11.glScalef(0.9f, 0.9f, 0.9f);
			}
			else if(!spider.isHidden){
				x = 0;
				y *= 2f;
			}
			else if(!naga.isHidden){
				x = 0;
				y *= 2f;
			}
			else if(!mermaid.isHidden || !digitigrade.isHidden){
				x = 0;
				y *= 2f;
			}
		}
		boolean bo = entity.hurtTime <= 0 && entity.deathTime <= 0 && !base.isArmor;
    	if(bo){
	    	float red = (entity.modelData.legParts.color >> 16 & 255) / 255f;
	    	float green = (entity.modelData.legParts.color >> 8  & 255) / 255f;
	    	float blue = (entity.modelData.legParts.color & 255) / 255f;
	    	GL11.glColor4f(red, green, blue, base.alpha);
    	}
		super.render(par1);
		if(bo){
			GL11.glColor4f(1, 1, 1, base.alpha);
		}
		GL11.glPopMatrix();
		
    }
	public void setData(EntityCustomNpc entity) {
		this.entity = entity;
	}
}
