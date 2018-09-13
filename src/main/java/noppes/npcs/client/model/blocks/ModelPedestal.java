package noppes.npcs.client.model.blocks;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelPedestal extends ModelBase {
	ModelRenderer Main_Block;
	ModelRenderer Front;

	public ModelPedestal() {
		Main_Block = new ModelRenderer(this, 1, 0);
		Main_Block.addBox(-7F, 0F, -8F, 14, 3, 16);
		Main_Block.setRotationPoint(0, 16F, 0F);
		
		Front = new ModelRenderer(this, 16, 8);
		Front.addBox(-8F, 0F, -8F, 16, 5, 16);
		Front.setRotationPoint(0F, 19F, 0F);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3,
			float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		GL11.glPushMatrix();
		GL11.glScalef(1, 1, 0.5f);
		Main_Block.render(f5);
		GL11.glScalef(1, 1, 1.25f);
		Front.render(f5);
		GL11.glPopMatrix();
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}


}
