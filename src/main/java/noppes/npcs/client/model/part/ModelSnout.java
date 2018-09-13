package noppes.npcs.client.model.part;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.ModelData;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.model.ModelMPM;
import noppes.npcs.client.model.util.ModelPartInterface;

public class ModelSnout extends ModelPartInterface {
	private ModelRenderer small;
	private ModelRenderer medium;
	private ModelRenderer large;
	private ModelRenderer bunny;
	
	public ModelSnout(ModelMPM base) {
		super(base);
		
		small = new ModelRenderer(base, 24, 0);
		small.addBox(0F, 0F, 0F, 4, 3, 1);
		small.setRotationPoint(-2F, -3F, -5F);
		this.addChild(small);

		medium = new ModelRenderer(base, 24, 0);
		medium.addBox(0F, 0F, 0F, 4, 3, 2);
		medium.setRotationPoint(-2F, -3F, -6F);
		this.addChild(medium);

		large = new ModelRenderer(base, 24, 0);
		large.addBox(0F, 0F, 0F, 4, 3, 3);
		large.setRotationPoint(-2F, -3F, -7F);
		this.addChild(large);
		
		bunny = new ModelRenderer(base, 24, 0);
		bunny.addBox(1F, 1F, 0F, 4, 2, 1);
		bunny.setRotationPoint(-3F, -4F, -5F);
		this.addChild(bunny);
		
		ModelRenderer tooth = new ModelRenderer(base, 24, 3);
		tooth.addBox(2F, 3f, 0F, 2, 1, 1);
		tooth.setRotationPoint(0F, 0F, 0F);
		bunny.addChild(tooth);
	}

	@Override
	public void setRotationAngles(float par1, float par2, float par3,
			float par4, float par5, float par6, Entity entity) {

	}

	@Override
	public void initData(ModelData data) {
		ModelPartData config = data.getPartData("snout");
		if(config == null)
		{
			isHidden = true;
			return;
		}

		color = config.color;
		isHidden = false;
		small.isHidden = config.type != 0;
		medium.isHidden = config.type != 1;
		large.isHidden = config.type != 2;
		bunny.isHidden = config.type != 3;
		
		if(!config.playerTexture){
			location = config.getResource();
		}
		else
			location = null;
	}
}
