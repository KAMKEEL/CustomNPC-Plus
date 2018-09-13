package noppes.npcs.client.model.part;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.ModelData;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.model.ModelMPM;
import noppes.npcs.client.model.util.Model2DRenderer;
import noppes.npcs.client.model.util.ModelPartInterface;

public class ModelHair extends ModelPartInterface {
	private Model2DRenderer model;
	
	public ModelHair(ModelMPM base) {
		super(base);
		model = new Model2DRenderer(base, 56, 20, 8, 12, 64, 32);
		model.setRotationPoint(-4F, 12, 3);
		model.setScale(0.75f);
		addChild(model);
	}

	@Override
	public void setRotationAngles(float par1, float par2, float par3,
			float par4, float par5, float par6, Entity entity) {
		ModelRenderer parent = this.base.bipedHead;
		if(parent.rotateAngleX < 0){
			rotateAngleX = -parent.rotateAngleX * 1.2f;
			if(parent.rotateAngleX > -1){
    			rotationPointY = -parent.rotateAngleX * 1.5f;
    			rotationPointZ = -parent.rotateAngleX * 1.5f;
			}
		}
		else{
			rotateAngleX = 0;
			rotationPointY = 0;
			rotationPointZ = 0;
		}
	}

	@Override
	public void initData(ModelData data) {
		ModelPartData config = data.getPartData("hair");
		if(config == null)
		{
			isHidden = true;
			return;
		}
		this.color = config.color;
		isHidden = false;
		if(!config.playerTexture){
			location = config.getResource();
		}
		else
			location = null;
	}

}
