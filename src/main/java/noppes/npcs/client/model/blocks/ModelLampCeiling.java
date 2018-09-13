package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelLampCeiling extends ModelBase {
	ModelRenderer Base;
	ModelRenderer Top1;
	ModelRenderer Top2;
	ModelRenderer Top3;
	ModelRenderer Chain8;
	ModelRenderer Chain1;
	ModelRenderer Chain2;
	ModelRenderer Chain3;
	ModelRenderer Chain4;
	ModelRenderer Chain5;
	ModelRenderer Chain6;
	ModelRenderer Chain7;
	ModelRenderer TippyTop1;
	ModelRenderer TippyTop2;
	ModelRenderer Shape3;
	ModelRenderer Shape1;
	ModelRenderer Shape2;

	public ModelLampCeiling() {
		Base = new ModelRenderer(this, 0, 6);
		Base.addBox(0F, 0F, 0F, 4, 7, 4);
		Base.setRotationPoint(-2F, 17F, -2F);
		
		Top1 = new ModelRenderer(this, 0, 0);
		Top1.addBox(0F, 0F, 0F, 5, 1, 5);
		Top1.setRotationPoint(-2.5F, 17F, -2.5F);
		
		Top2 = new ModelRenderer(this, 0, 0);
		Top2.addBox(0F, 0F, 0F, 4, 1, 4);
		Top2.setRotationPoint(-2F, 16.5F, -2F);
		
		Top3 = new ModelRenderer(this, 0, 0);
		Top3.addBox(0F, 0F, 0F, 3, 1, 3);
		Top3.setRotationPoint(-1.5F, 16F, -1.5F);
		
		Chain8 = new ModelRenderer(this, 0, 0);
		Chain8.addBox(0F, 0F, 0F, 1, 2, 1);
		Chain8.setRotationPoint(-0.5F, 14F, -1.5F);
		
		Chain1 = new ModelRenderer(this, 0, 0);
		Chain1.addBox(0F, 0F, 0F, 1, 3, 1);
		Chain1.setRotationPoint(0.5F, 8F, -0.5F);
		
		Chain2 = new ModelRenderer(this, 0, 0);
		Chain2.addBox(0F, 0F, 0F, 1, 3, 1);
		Chain2.setRotationPoint(-1.5F, 8F, -0.5F);
		
		Chain3 = new ModelRenderer(this, 0, 0);
		Chain3.addBox(0F, 0F, 0F, 1, 3, 1);
		Chain3.setRotationPoint(-0.5F, 10F, 0.5F);
		
		Chain4 = new ModelRenderer(this, 0, 0);
		Chain4.addBox(0F, 0F, 0F, 1, 3, 1);
		Chain4.setRotationPoint(-0.5F, 10F, -1.5F);
		
		Chain5 = new ModelRenderer(this, 0, 0);
		Chain5.addBox(0F, 0F, 0F, 1, 3, 1);
		Chain5.setRotationPoint(-1.5F, 12F, -0.5F);
		
		Chain6 = new ModelRenderer(this, 0, 0);
		Chain6.addBox(0F, 0F, 0F, 1, 3, 1);
		Chain6.setRotationPoint(0.5F, 12F, -0.5F);
		
		Chain7 = new ModelRenderer(this, 0, 0);
		Chain7.addBox(0F, 0F, 0F, 1, 2, 1);
		Chain7.setRotationPoint(-0.5F, 14F, 0.5F);
		
		TippyTop1 = new ModelRenderer(this, 0, 0);
		TippyTop1.addBox(0F, 0F, 0F, 4, 1, 4);
		TippyTop1.setRotationPoint(-2.8F, 8F, 0F);
		setRotation(TippyTop1, 0F, 0.7853982F, 0F);
		
		TippyTop2 = new ModelRenderer(this, 0, 0);
		TippyTop2.addBox(0F, 0F, 0F, 3, 1, 3);
		TippyTop2.setRotationPoint(-2.1F, 8.5F, 0F);
		setRotation(TippyTop2, 0F, 0.7853982F, 0F);
		
		Shape3 = new ModelRenderer(this, 0, 0);
		Shape3.addBox(0F, 0F, 0F, 1, 1, 1);
		Shape3.setRotationPoint(-0.5F, 14F, -0.5F);
		
		Shape1 = new ModelRenderer(this, 0, 0);
		Shape1.addBox(0F, 0F, 0F, 1, 1, 1);
		Shape1.setRotationPoint(-0.5F, 10F, -0.5F);
		
		Shape2 = new ModelRenderer(this, 0, 0);
		Shape2.addBox(0F, 0F, 0F, 1, 1, 1);
		Shape2.setRotationPoint(-0.5F, 12F, -0.5F);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3,
			float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		Base.render(f5);
		Top1.render(f5);
		Top2.render(f5);
		Top3.render(f5);
		Chain8.render(f5);
		Chain1.render(f5);
		Chain2.render(f5);
		Chain3.render(f5);
		Chain4.render(f5);
		Chain5.render(f5);
		Chain6.render(f5);
		Chain7.render(f5);
		TippyTop1.render(f5);
		TippyTop2.render(f5);
		Shape3.render(f5);
		Shape1.render(f5);
		Shape2.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

}
