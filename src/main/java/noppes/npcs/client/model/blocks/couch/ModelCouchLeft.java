package noppes.npcs.client.model.blocks.couch;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCouchLeft extends ModelBase {
	private final ModelRenderer CouchBack;
	private final ModelRenderer Cussion;

	public ModelCouchLeft() {
		textureWidth = 64;
		textureHeight = 64;

		CouchBack = new ModelRenderer(this);
		CouchBack.setRotationPoint(0.0F, 24.0F, 0.0F);
		CouchBack.cubeList.add(new ModelBox(CouchBack, 0, 0, -8.0F, -1.0F, -8.0F, 2, 1, 2, 0.0F));
		CouchBack.cubeList.add(new ModelBox(CouchBack, 0, 0, 6.0F, -1.0F, -8.0F, 2, 1, 2, 0.0F));
		CouchBack.cubeList.add(new ModelBox(CouchBack, 0, 0, -8.0F, -1.0F, 6.0F, 2, 1, 2, 0.0F));
		CouchBack.cubeList.add(new ModelBox(CouchBack, 0, 0, 6.0F, -1.0F, 6.0F, 2, 1, 2, 0.0F));
		CouchBack.cubeList.add(new ModelBox(CouchBack, 0, 18, -6.0F, -2.0F, -8.0F, 14, 1, 15, 0.0F));
		CouchBack.cubeList.add(new ModelBox(CouchBack, 4, 46, -6.0F, -10.0F, 7.0F, 14, 9, 1, 0.0F));
		CouchBack.cubeList.add(new ModelBox(CouchBack, 13, 14, -8.0F, -5.0F, -8.0F, 2, 4, 16, 0.0F));
		CouchBack.cubeList.add(new ModelBox(CouchBack, 1, 47, -8.0F, -10.0F, 4.0F, 2, 5, 4, 0.0F));
		CouchBack.cubeList.add(new ModelBox(CouchBack, 15, 19, -8.0F, -10.0F, -8.0F, 2, 2, 12, 0.0F));
		CouchBack.cubeList.add(new ModelBox(CouchBack, 44, 51, -7.0F, -8.0F, -6.0F, 0, 3, 10, 0.0F));
		CouchBack.cubeList.add(new ModelBox(CouchBack, 15, 18, -8.0F, -8.0F, -8.0F, 2, 3, 2, 0.0F));

		Cussion = new ModelRenderer(this);
		Cussion.setRotationPoint(0.0F, 24.0F, 0.0F);
		Cussion.cubeList.add(new ModelBox(Cussion, 1, 0, -6.0F, -5.0F, -8.0F, 14, 3, 15, 0.0F));
		Cussion.cubeList.add(new ModelBox(Cussion, 1, 34, -6.0F, -14.0F, 4.0F, 14, 9, 3, 0.0F));
		Cussion.cubeList.add(new ModelBox(Cussion, 3, 56, -6.0F, -14.0F, 7.0F, 14, 4, 1, 0.0F));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		CouchBack.render(f5);
		Cussion.render(f5);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
