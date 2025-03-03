package noppes.npcs.client.model.blocks;// Made with Blockbench 4.12.2
// Exported for Minecraft version 1.7 - 1.12
// Paste this class into your mod and generate all required imports


import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelMailbox extends ModelBase {
	private final ModelRenderer mailbox;

	public ModelMailbox() {
		textureWidth = 64;
		textureHeight = 64;

		mailbox = new ModelRenderer(this);
		mailbox.setRotationPoint(0.0F, 24.0F, 0.0F);
		mailbox.cubeList.add(new ModelBox(mailbox, 0, 0, -8.0F, -24.0F, -8.0F, 16, 20, 16, 0.0F));
		mailbox.cubeList.add(new ModelBox(mailbox, 0, 36, -8.0F, -4.0F, 6.0F, 2, 4, 2, 0.0F));
		mailbox.cubeList.add(new ModelBox(mailbox, 0, 36, 6.0F, -4.0F, 6.0F, 2, 4, 2, 0.0F));
		mailbox.cubeList.add(new ModelBox(mailbox, 0, 36, 6.0F, -4.0F, -8.0F, 2, 4, 2, 0.0F));
		mailbox.cubeList.add(new ModelBox(mailbox, 0, 36, -8.0F, -4.0F, -8.0F, 2, 4, 2, 0.0F));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		mailbox.render(f5);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
