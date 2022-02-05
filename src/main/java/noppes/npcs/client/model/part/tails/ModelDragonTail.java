package noppes.npcs.client.model.part.tails;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import noppes.npcs.client.model.ModelMPM;
import noppes.npcs.client.model.util.ModelPlaneRenderer;

public class ModelDragonTail extends ModelRenderer {

	public ModelDragonTail(ModelMPM base) {
		super(base);
		
		int x = 52;
		int y = 16;

		ModelRenderer dragon = new ModelRenderer(base, x, y);
        dragon.setRotationPoint(0F, 0F, 3F);
        this.addChild(dragon);
		
        ModelRenderer DragonTail2 = new ModelRenderer(base, x, y);
		DragonTail2.setRotationPoint(0F, 2F, 2F);

		ModelRenderer DragonTail3 = new ModelRenderer(base, x, y);
		DragonTail3.setRotationPoint(0F, 4.5F, 4F);
		
		ModelRenderer DragonTail4 = new ModelRenderer(base, x, y);
		DragonTail4.setRotationPoint(0F, 7F, 5.75F);
		
		ModelRenderer DragonTail5 = new ModelRenderer(base, x, y);
		DragonTail5.setRotationPoint(0F, 9F, 8F);

		ModelPlaneRenderer planeLeft = new ModelPlaneRenderer(base, x, y);
		planeLeft.addSidePlane(-1.5F, -1.5F, -1.5F, 3, 3);
		ModelPlaneRenderer planeRight = new ModelPlaneRenderer(base, x, y);
		planeRight.addSidePlane(-1.5F, -1.5F, -1.5F, 3, 3);
		setRotation(planeRight,  (float)Math.PI, (float)Math.PI ,0);

		ModelPlaneRenderer planeTop = new ModelPlaneRenderer(base, x, y);
		planeTop.addTopPlane(-1.5F, -1.5F, -1.5F, 3, 3);
		setRotation(planeTop,   0 ,-(float)Math.PI / 2,0);
		ModelPlaneRenderer planeBottom = new ModelPlaneRenderer(base, x, y);
		planeBottom.addTopPlane(-1.5F, -1.5F, -1.5F, 3, 3);
		setRotation(planeBottom,   0 ,-(float)Math.PI / 2, (float)Math.PI);

		ModelPlaneRenderer planeBack = new ModelPlaneRenderer(base, x, y);
		planeBack.addBackPlane(-1.5F, -1.5F, -1.5F, 3, 3);
		setRotation(planeBack,  0, 0 ,(float)Math.PI / 2);
		ModelPlaneRenderer planeFront = new ModelPlaneRenderer(base, x, y);
		planeFront.addBackPlane(-1.5F, -1.5F, -1.5F, 3, 3);
		setRotation(planeFront,  0, (float)Math.PI ,-(float)Math.PI / 2);

		dragon.addChild(planeLeft);
		dragon.addChild(planeRight);
		dragon.addChild(planeTop);
		dragon.addChild(planeBottom);
		dragon.addChild(planeFront);
		dragon.addChild(planeBack);

		DragonTail2.addChild(planeLeft);
		DragonTail2.addChild(planeRight);
		DragonTail2.addChild(planeTop);
		DragonTail2.addChild(planeBottom);
		DragonTail2.addChild(planeFront);
		DragonTail2.addChild(planeBack);

		DragonTail3.addChild(planeLeft);
		DragonTail3.addChild(planeRight);
		DragonTail3.addChild(planeTop);
		DragonTail3.addChild(planeBottom);
		DragonTail3.addChild(planeFront);
		DragonTail3.addChild(planeBack);

		DragonTail4.addChild(planeLeft);
		DragonTail4.addChild(planeRight);
		DragonTail4.addChild(planeTop);
		DragonTail4.addChild(planeBottom);
		DragonTail4.addChild(planeFront);
		DragonTail4.addChild(planeBack);

		dragon.addChild(DragonTail2);
		dragon.addChild(DragonTail3);
		dragon.addChild(DragonTail4);
	}

	public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity entity)
    {
		
    }

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
