package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelTable extends ModelBase {
	public ModelRenderer Shape1;
	public ModelRenderer Table;
	public ModelRenderer Shape3;
	public ModelRenderer Shape4;
	public ModelRenderer Shape5;

	public ModelTable() {
		Shape1 = new ModelRenderer(this, 0, 0);
		Shape1.mirror = true;
		Shape1.addBox(-1F, 0F, -1F, 2, 14, 2);
		Shape1.setRotationPoint(-6F, 10F, 6F);
		
		Table = new ModelRenderer(this, 0, 0);
		Table.addBox(0F, -2F, 0F, 16, 2, 16);
		Table.setRotationPoint(-8F, 10F, -8F);
		
		Shape3 = new ModelRenderer(this, 0, 0);
		Shape3.addBox(-1F, 0F, -1F, 2, 14, 2);
		Shape3.setRotationPoint(6F, 10F, -6F);
		
		Shape4 = new ModelRenderer(this, 0, 0);
		Shape4.addBox(-1F, 0F, -1F, 2, 14, 2);
		Shape4.setRotationPoint(6F, 10F, 6F);
		
		Shape5 = new ModelRenderer(this, 0, 0);
		Shape5.mirror = true;
		Shape5.addBox(-1F, 0F, -1F, 2, 14, 2);
		Shape5.setRotationPoint(-6F, 10F, -6F);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3,
			float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		Shape1.render(f5);
		Shape3.render(f5);
		Shape4.render(f5);
		Shape5.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

}
