package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelStool extends ModelBase {
	private final ModelRenderer stool;

	public ModelStool() {
        textureWidth = 64;
        textureHeight = 32;

        stool = new ModelRenderer(this);
        stool.setRotationPoint(0.0F, 24.0F, 0.0F);
        stool.cubeList.add(new ModelBox(stool, 0, 0, -5.0F, -9.0F, -5.0F, 10, 2, 10, 0.0F));
        stool.cubeList.add(new ModelBox(stool, 0, 12, 2.0F, -7.0F, -4.0F, 2, 7, 2, 0.0F));
        stool.cubeList.add(new ModelBox(stool, 8, 8, 3.0F, -5.0F, -2.0F, 0, 2, 4, 0.0F));
        stool.cubeList.add(new ModelBox(stool, 8, 12, -2.0F, -5.0F, -3.0F, 4, 2, 0, 0.0F));
        stool.cubeList.add(new ModelBox(stool, 0, 12, -4.0F, -7.0F, -4.0F, 2, 7, 2, 0.0F));
        stool.cubeList.add(new ModelBox(stool, 0, 12, -4.0F, -7.0F, 2.0F, 2, 7, 2, 0.0F));
        stool.cubeList.add(new ModelBox(stool, 8, 8, -3.0F, -5.0F, -2.0F, 0, 2, 4, 0.0F));
        stool.cubeList.add(new ModelBox(stool, 0, 12, 2.0F, -7.0F, 2.0F, 2, 7, 2, 0.0F));
        stool.cubeList.add(new ModelBox(stool, 8, 12, -2.0F, -5.0F, 3.0F, 4, 2, 0, 0.0F));	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		stool.render(f5);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
