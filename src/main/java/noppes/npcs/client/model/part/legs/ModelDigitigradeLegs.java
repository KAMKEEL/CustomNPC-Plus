package noppes.npcs.client.model.part.legs;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import noppes.npcs.client.model.ModelMPM;

public class ModelDigitigradeLegs extends ModelRenderer {

	private ModelRenderer rightleg;
	private ModelRenderer rightleg2;
	private ModelRenderer rightleglow;
	private ModelRenderer rightfoot;
	private ModelRenderer leftleg;
	private ModelRenderer leftleg2;
	private ModelRenderer leftleglow;
	private ModelRenderer leftfoot;

	public boolean isRiding = false;
	public boolean isSneaking = false;
	public boolean isSleeping = false;
	public boolean isCrawling = false;

	private ModelMPM base;

	public ModelDigitigradeLegs(ModelMPM base) {
		super(base);
		this.base = base;
		rightleg = new ModelRenderer(base, 0, 16);
		rightleg.addBox(-2F, 0F, -2F, 4, 6, 4);
		rightleg.setRotationPoint(-2.1F, 11F, 0F);
		setRotation(rightleg, -0.3F, 0F, 0F);
		this.addChild(rightleg);

		rightleg2 = new ModelRenderer(base, 0, 20);
		rightleg2.addBox(-1.5F, -1F, -2F, 3, 7, 3);
		rightleg2.setRotationPoint(0F, 4.1F, 0F);
		setRotation(rightleg2, 1.1f, 0F, 0F);
		rightleg.addChild(rightleg2);

		rightleglow = new ModelRenderer(base, 0, 24);
		rightleglow.addBox(-1.5F, 0F, -1F, 3, 5, 2);
		rightleglow.setRotationPoint(0F, 5F, 0F);
		setRotation(rightleglow, -1.35F, 0F, 0F);
		rightleg2.addChild(rightleglow);

		rightfoot = new ModelRenderer(base, 1, 26);
		rightfoot.addBox(-1.5F, 0F, -5F, 3, 2, 4);
		rightfoot.setRotationPoint(0F, 3.7F, 1.2F);
		setRotation(rightfoot, 0.55F, 0F, 0F);
		rightleglow.addChild(rightfoot);

		leftleg = new ModelRenderer(base, 0, 16);
		leftleg.mirror = true;
		leftleg.addBox(-2F, 0F, -2F, 4, 6, 4);
		leftleg.setRotationPoint(2.1F, 11F, 0F);
		setRotation(leftleg, -0.3F, 0F, 0F);
		this.addChild(leftleg);

		leftleg2 = new ModelRenderer(base, 0, 20);
		leftleg2.mirror = true;
		leftleg2.addBox(-1.5F, -1F, -2F, 3, 7, 3);
		leftleg2.setRotationPoint(0F, 4.1F, 0F);
		setRotation(leftleg2, 1.1f, 0F, 0F);
		leftleg.addChild(leftleg2);

		leftleglow = new ModelRenderer(base, 0, 24);
		leftleglow.mirror = true;
		leftleglow.addBox(-1.5F, 0F, -1F, 3, 5, 2);
		leftleglow.setRotationPoint(0F, 5F, 0F);
		setRotation(leftleglow, -1.35F, 0F, 0F);
		leftleg2.addChild(leftleglow);

		leftfoot = new ModelRenderer(base, 1, 26);
		leftfoot.mirror = true;
		leftfoot.addBox(-1.5F, 0F, -5F, 3, 2, 4);
		leftfoot.setRotationPoint(0F, 3.7F, 1.2F);
		setRotation(leftfoot, 0.55F, 0F, 0F);
		leftleglow.addChild(leftfoot);
	}

	public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity entity)
    {
    	rightleg.rotateAngleX = base.bipedRightLeg.rotateAngleX - 0.3f;
    	leftleg.rotateAngleX = base.bipedLeftLeg.rotateAngleX - 0.3f;
    	rightleg.rotationPointY = base.bipedRightLeg.rotationPointY;
    	leftleg.rotationPointY = base.bipedLeftLeg.rotationPointY;
    	rightleg.rotationPointZ = base.bipedRightLeg.rotationPointZ;
    	leftleg.rotationPointZ = base.bipedLeftLeg.rotationPointZ;
    	if(!base.isSneak){
    		leftleg.rotationPointY--;
    		rightleg.rotationPointY--;
    	}
    		
    }

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
