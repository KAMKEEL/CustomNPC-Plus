package noppes.npcs.client.model.part.legs;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import noppes.npcs.client.model.util.ModelPlaneRenderer;

import org.lwjgl.opengl.GL11;

public class ModelNagaLegs extends ModelRenderer{

    private ModelRenderer nagaPart1;
    private ModelRenderer nagaPart2;
    private ModelRenderer nagaPart3;
    private ModelRenderer nagaPart4;
    private ModelRenderer nagaPart5;

    public boolean isRiding = false;
    public boolean isSneaking = false;
    public boolean isSleeping = false;
    public boolean isCrawling = false;
    
	public ModelNagaLegs(ModelBase base) {
		super(base);
		
		nagaPart1 = new ModelRenderer(base,0,0);
		
		ModelRenderer legPart = new ModelRenderer(base,0,16);
		legPart.addBox(0, -2, -2, 4, 4, 4);
		legPart.setRotationPoint(-4, 0, 0);
		nagaPart1.addChild(legPart);
		legPart = new ModelRenderer(base,0,16);
		legPart.mirror = true;
		legPart.addBox(0, -2, -2, 4, 4, 4);
		nagaPart1.addChild(legPart);

		nagaPart2 = new ModelRenderer(base,0,0);
		nagaPart2.childModels = nagaPart1.childModels;

		nagaPart3 = new ModelRenderer(base,0,0);
		
		ModelPlaneRenderer plane = new ModelPlaneRenderer(base, 4, 24);
		plane.addBackPlane(0,-2, 0, 4, 4);
		plane.setRotationPoint(-4, 0, 0);
		nagaPart3.addChild(plane);
		plane = new ModelPlaneRenderer(base, 4, 24);
		plane.mirror = true;
		plane.addBackPlane(0,-2, 0, 4, 4);
		nagaPart3.addChild(plane);
		
		plane = new ModelPlaneRenderer(base, 8, 24);
		plane.addBackPlane(0,-2, 6, 4, 4);
		plane.setRotationPoint(-4, 0, 0);
		nagaPart3.addChild(plane);
		plane = new ModelPlaneRenderer(base, 8, 24);
		plane.mirror = true;
		plane.addBackPlane(0,-2, 6, 4, 4);
		nagaPart3.addChild(plane);

		plane = new ModelPlaneRenderer(base, 4, 26);
		plane.addTopPlane(0,-2, -6, 4, 6);
		plane.setRotationPoint(-4, 0, 0);
		plane.rotateAngleX = (float) (Math.PI);
		nagaPart3.addChild(plane);
		plane = new ModelPlaneRenderer(base, 4, 26);
		plane.mirror = true;
		plane.addTopPlane(0,-2, -6, 4, 6);
		plane.rotateAngleX = (float) (Math.PI);
		nagaPart3.addChild(plane);

		plane = new ModelPlaneRenderer(base, 8, 26);
		plane.addTopPlane(0,-2, 0, 4, 6);
		plane.setRotationPoint(-4, 0, 0);
		nagaPart3.addChild(plane);
		plane = new ModelPlaneRenderer(base, 8, 26);
		plane.mirror = true;
		plane.addTopPlane(0,-2, 0, 4, 6);
		nagaPart3.addChild(plane);;

		plane = new ModelPlaneRenderer(base, 0, 26);
		plane.rotateAngleX = (float) (Math.PI / 2);
		plane.addSidePlane(0,0, -2, 6, 4);
		plane.setRotationPoint(-4, 0, 0);
		nagaPart3.addChild(plane);
		plane = new ModelPlaneRenderer(base, 0, 26);
		plane.rotateAngleX = (float) (Math.PI / 2);
		plane.addSidePlane(4,0, -2, 6, 4);
		nagaPart3.addChild(plane);

		nagaPart4 = new ModelRenderer(base,0,0);
		nagaPart4.childModels = nagaPart3.childModels;
		
		nagaPart5 = new ModelRenderer(base,0,0);
		
		legPart = new ModelRenderer(base,56,20);
		legPart.addBox(0, 0, -2, 2, 5, 2);
		legPart.setRotationPoint(-2, 0, 0);
		legPart.rotateAngleX = (float) (Math.PI/2);
		nagaPart5.addChild(legPart);
		legPart = new ModelRenderer(base,56,20);
		legPart.mirror = true;
		legPart.addBox(0, 0, -2, 2, 5, 2);
		legPart.rotateAngleX = (float) (Math.PI/2);
		nagaPart5.addChild(legPart);

		this.addChild(nagaPart1);
		this.addChild(nagaPart2);
		this.addChild(nagaPart3);
		this.addChild(nagaPart4);
		this.addChild(nagaPart5);

        nagaPart1.setRotationPoint(0F, 14.0F, 0.0F);
        nagaPart2.setRotationPoint(0, 18.0F, 0.6F);
        nagaPart3.setRotationPoint(0F, 22.0F, -0.3F);
        nagaPart4.setRotationPoint(0F, 22.0F, 5F);
        nagaPart5.setRotationPoint(0F, 22.0F, 10F);
	}
    public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity entity)
    {
		this.nagaPart1.rotateAngleY = MathHelper.cos(par1 * 0.6662F) * 0.26F * par2;
		this.nagaPart2.rotateAngleY = MathHelper.cos(par1 * 0.6662F) * 0.5F * par2;
		this.nagaPart3.rotateAngleY = MathHelper.cos(par1 * 0.6662F) * 0.26F * par2;
		this.nagaPart4.rotateAngleY = -MathHelper.cos(par1 * 0.6662F) * 0.16F * par2;
		this.nagaPart5.rotateAngleY = -MathHelper.cos(par1 * 0.6662F) * 0.3F * par2;

		nagaPart1.setRotationPoint(0F, 14.0F, 0.0F);
		nagaPart2.setRotationPoint(0, 18.0F, 0.6F);
		nagaPart3.setRotationPoint(0F, 22.0F, -0.3F);
		nagaPart4.setRotationPoint(0F, 22.0F, 5F);
		nagaPart5.setRotationPoint(0F, 22.0F, 10F);

		nagaPart1.rotateAngleX = 0;
		nagaPart2.rotateAngleX = 0;
		nagaPart3.rotateAngleX = 0;
		nagaPart4.rotateAngleX = 0;
		nagaPart5.rotateAngleX = 0;
		
		if(isSleeping || isCrawling){
			nagaPart3.rotateAngleX = (float) -(Math.PI/2);
			nagaPart4.rotateAngleX = (float) -(Math.PI/2);
			nagaPart5.rotateAngleX = (float) -(Math.PI/2);

			nagaPart3.rotationPointY -= 2;
        	nagaPart3.rotationPointZ = 0.9f;

        	nagaPart4.rotationPointY += 4;
        	nagaPart4.rotationPointZ = 0.9f;
        	
        	nagaPart5.rotationPointY += 7;
        	nagaPart5.rotationPointZ = 2.9f;
		}
		if(this.isRiding){
			nagaPart1.rotationPointY-= 1;
			nagaPart1.rotateAngleX = (float) -(Math.PI/16f);
			nagaPart1.rotationPointZ = -1;
        	
			nagaPart2.rotationPointY-= 4;
			nagaPart2.rotationPointZ = -1;
			
			nagaPart3.rotationPointY-= 9;
        	nagaPart3.rotationPointZ -= 1;
        	nagaPart4.rotationPointY-= 13;
        	nagaPart4.rotationPointZ -= 1;
        	nagaPart5.rotationPointY-= 9;
        	nagaPart5.rotationPointZ -= 1;
        	if (this.isSneaking){
        		nagaPart1.rotationPointZ += 5;
        		nagaPart3.rotationPointZ += 5;
        		nagaPart4.rotationPointZ += 5;
        		nagaPart5.rotationPointZ += 4;
            	nagaPart1.rotationPointY--;
            	nagaPart2.rotationPointY--;
            	nagaPart3.rotationPointY--;
            	nagaPart4.rotationPointY--;
            	nagaPart5.rotationPointY--;
        	}
		}
		else if (this.isSneaking){
			nagaPart1.rotationPointY--;
			nagaPart2.rotationPointY--;
			nagaPart3.rotationPointY--;
			nagaPart4.rotationPointY--;
			nagaPart5.rotationPointY--;

        	nagaPart1.rotationPointZ = 5;
        	nagaPart2.rotationPointZ = 3;
        }
    }
    
    @Override
    public void render(float par7)
    {
    	if(isHidden || !showModel)
    		return;
    	
    	nagaPart1.render(par7);
    	nagaPart3.render(par7);

		if(!this.isRiding)
			nagaPart2.render(par7);

        GL11.glPushMatrix();
        GL11.glScalef(0.74f, 0.7f,0.85f);
        GL11.glTranslatef(nagaPart3.rotateAngleY, 0.66f, 0.06F);
        nagaPart4.render(par7);
    	GL11.glPopMatrix();
    	
        GL11.glPushMatrix();
        GL11.glTranslatef(nagaPart3.rotateAngleY + nagaPart4.rotateAngleY, 0, 0);
        nagaPart5.render(par7);
    	GL11.glPopMatrix();
    }
}
