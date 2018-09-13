package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCandleCeiling extends ModelBase {
	ModelRenderer Wax1;
	ModelRenderer Wax2;
	ModelRenderer Wax3;
	ModelRenderer Wax4;
	ModelRenderer TippyTop1;
	ModelRenderer TippyTop2;
	ModelRenderer Middle;
	ModelRenderer BottomBar1;
	ModelRenderer Rod1;
	ModelRenderer Rod2;
	ModelRenderer Rod3;
	ModelRenderer Rod4;
	ModelRenderer Base1;
	ModelRenderer Base2;
	ModelRenderer Base3;
	ModelRenderer Base4;
	ModelRenderer BottomBar3;
	ModelRenderer BottomBar2;
	ModelRenderer BottomBar4;

	public ModelCandleCeiling() {
		Wax1 = new ModelRenderer(this, 16, 0);
		Wax1.addBox(0F, 0F, 0F, 2, 4, 2);
		Wax1.setRotationPoint(-1F, 15.5F, 5F);
		
		Wax2 = new ModelRenderer(this, 16, 0);
		Wax2.addBox(0F, 0F, 0F, 2, 4, 2);
		Wax2.setRotationPoint(7F, 15.5F, 1F);
		setRotation(Wax2, 0F, 3.141593F, 0F);
		
		Wax3 = new ModelRenderer(this, 16, 0);
		Wax3.addBox(0F, 0F, 0F, 2, 4, 2);
		Wax3.setRotationPoint(-7F, 15.5F, -1F);
		
		Wax4 = new ModelRenderer(this, 16, 0);
		Wax4.addBox(0F, 0F, 0F, 2, 4, 2);
		Wax4.setRotationPoint(1F, 15.5F, -5F);
		setRotation(Wax4, 0F, 3.141593F, 0F);
		
		TippyTop1 = new ModelRenderer(this, 0, 0);
		TippyTop1.addBox(0F, 0F, 0F, 4, 1, 4);
		TippyTop1.setRotationPoint(-2.8F, 7.5F, 0F);
		setRotation(TippyTop1, 0F, 0.7853982F, 0F);
		
		TippyTop2 = new ModelRenderer(this, 0, 0);
		TippyTop2.addBox(0F, 0F, 0F, 3, 1, 3);
		TippyTop2.setRotationPoint(-2.1F, 8F, 0F);
		setRotation(TippyTop2, 0F, 0.7853982F, 0F);
		
		Middle = new ModelRenderer(this, 0, 0);
		Middle.addBox(0F, 0F, 0F, 1, 13, 1);
		Middle.setRotationPoint(-0.5F, 9F, -0.5F);
		
		BottomBar1 = new ModelRenderer(this, 0, 4);
		BottomBar1.addBox(0F, 0F, 0F, 1, 1, 5);
		BottomBar1.setRotationPoint(-0.5F, 21F, 0.5F);
		
		Rod1 = new ModelRenderer(this, 0, 0);
		Rod1.addBox(0F, 0F, 0F, 1, 2, 1);
		Rod1.setRotationPoint(-0.5F, 20F, 5.5F);
		
		Rod2 = new ModelRenderer(this, 0, 0);
		Rod2.addBox(0F, 0F, 0F, 1, 2, 1);
		Rod2.setRotationPoint(5.5F, 20F, -0.5F);
		
		Rod3 = new ModelRenderer(this, 0, 0);
		Rod3.addBox(0F, 0F, 0F, 1, 2, 1);
		Rod3.setRotationPoint(-6.5F, 20F, -0.5F);
		
		Rod4 = new ModelRenderer(this, 0, 0);
		Rod4.addBox(0F, 0F, 0F, 1, 2, 1);
		Rod4.setRotationPoint(-0.5F, 20F, -6.5F);
		
		Base1 = new ModelRenderer(this, 0, 0);
		Base1.addBox(0F, 0F, 0F, 4, 1, 4);
		Base1.setRotationPoint(-2F, 19F, 4F);
		
		Base2 = new ModelRenderer(this, 0, 0);
		Base2.addBox(0F, 0F, 0F, 4, 1, 4);
		Base2.setRotationPoint(4F, 19F, -2F);
		
		Base3 = new ModelRenderer(this, 0, 0);
		Base3.addBox(0F, 0F, 0F, 4, 1, 4);
		Base3.setRotationPoint(-8F, 19F, -2F);
		
		Base4 = new ModelRenderer(this, 0, 0);
		Base4.addBox(0F, 0F, 0F, 4, 1, 4);
		Base4.setRotationPoint(-2F, 19F, -8F);
		
		BottomBar3 = new ModelRenderer(this, 0, 4);
		BottomBar3.addBox(0F, 0F, 0F, 1, 1, 5);
		BottomBar3.setRotationPoint(-0.5F, 21F, -0.5F);
		setRotation(BottomBar3, 0F, -1.570796F, 0F);
		
		BottomBar2 = new ModelRenderer(this, 0, 4);
		BottomBar2.addBox(0F, 0F, 0F, 1, 1, 5);
		BottomBar2.setRotationPoint(0.5F, 21F, 0.5F);
		setRotation(BottomBar2, 0F, 1.570796F, 0F);
		
		BottomBar4 = new ModelRenderer(this, 0, 4);
		BottomBar4.addBox(0F, 0F, 0F, 1, 1, 5);
		BottomBar4.setRotationPoint(0.4F, 21F, -0.5F);
		setRotation(BottomBar4, 0F, 3.141593F, 0F);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3,
			float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		Wax1.render(f5);
		Wax2.render(f5);
		Wax3.render(f5);
		Wax4.render(f5);
		TippyTop1.render(f5);
		TippyTop2.render(f5);
		Middle.render(f5);
		BottomBar1.render(f5);
		Rod1.render(f5);
		Rod2.render(f5);
		Rod3.render(f5);
		Rod4.render(f5);
		Base1.render(f5);
		Base2.render(f5);
		Base3.render(f5);
		Base4.render(f5);
		BottomBar3.render(f5);
		BottomBar2.render(f5);
		BottomBar4.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

}
