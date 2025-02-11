package noppes.npcs.client.model.blocks.chair;


import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelChairSpoof extends ModelBase {

    private final ModelRenderer chairBase;

	public ModelChairSpoof() {
		textureWidth = 64;
		textureHeight = 32;

		chairBase = new ModelRenderer(this);
		chairBase.setRotationPoint(0.0F, 24.0F, 0.0F);
		chairBase.cubeList.add(new ModelBox(chairBase, 0, 0, 4.0F, -6.0F, 4.0F, 2, 6, 2, 0.0F));
		chairBase.cubeList.add(new ModelBox(chairBase, 0, 0, -6.0F, -6.0F, 4.0F, 2, 6, 2, 0.0F));
		chairBase.cubeList.add(new ModelBox(chairBase, 0, 0, -6.0F, -6.0F, -6.0F, 2, 6, 2, 0.0F));
		chairBase.cubeList.add(new ModelBox(chairBase, 0, 0, 4.0F, -6.0F, -6.0F, 2, 6, 2, 0.0F));
		chairBase.cubeList.add(new ModelBox(chairBase, 0, 0, -6.0F, -8.0F, -6.0F, 12, 2, 12, 0.0F));
		chairBase.cubeList.add(new ModelBox(chairBase, 12, 0, 4.0F, -20.0F, 4.0F, 2, 12, 2, 0.0F));
		chairBase.cubeList.add(new ModelBox(chairBase, 30, 0, -6.0F, -20.0F, 4.0F, 2, 12, 2, 0.0F));
		chairBase.cubeList.add(new ModelBox(chairBase, 0, 2, -4.0F, -19.0F, 5.0F, 8, 3, 0, 0.0F));
		chairBase.cubeList.add(new ModelBox(chairBase, -5, -6, 5.0F, -4.0F, -4.0F, 0, 2, 8, 0.0F));
		chairBase.cubeList.add(new ModelBox(chairBase, 1, 2, -4.0F, -4.0F, -5.0F, 8, 2, 0, 0.0F));
		chairBase.cubeList.add(new ModelBox(chairBase, -5, -6, -5.0F, -4.0F, -4.0F, 0, 2, 8, 0.0F));
		chairBase.cubeList.add(new ModelBox(chairBase, -3, 2, -4.0F, -4.0F, 5.0F, 8, 2, 0, 0.0F));
		chairBase.cubeList.add(new ModelBox(chairBase, 0, 2, -4.0F, -13.0F, 5.0F, 8, 3, 0, 0.0F));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		chairBase.render(f5);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
