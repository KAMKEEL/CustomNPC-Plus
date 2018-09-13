package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelMailboxWow extends ModelBase {
	// fields
	ModelRenderer Shape4;
	ModelRenderer Shape1;
	ModelRenderer Shape2;
	ModelRenderer Shape3;

	public ModelMailboxWow() {
		textureWidth = 128;
		textureHeight = 64;

		Shape4 = new ModelRenderer(this, 59, 0);
		Shape4.addBox(0F, 0F, 0F, 8, 6, 0);
		Shape4.setRotationPoint(-4F, -4F, 0F);
		
		Shape1 = new ModelRenderer(this, 0, 39);
		Shape1.addBox(0F, 0F, 0F, 8, 5, 8);
		Shape1.setRotationPoint(-4F, 19F, -4F);
		
		Shape2 = new ModelRenderer(this, 0, 21);
		Shape2.addBox(0F, 0F, 0F, 6, 9, 6);
		Shape2.setRotationPoint(-3F, 10F, -3F);
		
		Shape3 = new ModelRenderer(this, 0, 0);
		Shape3.addBox(0F, 0F, 0F, 12, 8, 12);
		Shape3.setRotationPoint(-6F, 2F, -6F);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3,
			float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		Shape4.render(f5);
		Shape1.render(f5);
		Shape2.render(f5);
		Shape3.render(f5);
	}
}
