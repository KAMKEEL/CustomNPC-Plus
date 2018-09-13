package noppes.npcs.client.model.part;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.ModelData;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.model.ModelMPM;
import noppes.npcs.client.model.util.Model2DRenderer;
import noppes.npcs.client.model.util.ModelPartInterface;

public class ModelMohawk extends ModelPartInterface {
	private Model2DRenderer model;
	public ModelMohawk(ModelMPM base) {
		super(base);

		model = new Model2DRenderer(base, 0, 0, 13 , 13);
		model.setRotationPoint(-0.5F, 0f, 9F);
        setRotation(model, 0, (float)(Math.PI/2f), 0);
        model.setScale(0.825f);
        this.addChild(model);
	}

	@Override
	public void setRotationAngles(float par1, float par2, float par3,
			float par4, float par5, float par6, Entity entity) {
		
	}


	@Override
	public void initData(ModelData data) {
		ModelPartData config = data.getPartData("mohawk");
		if(config == null)
		{
			isHidden = true;
			return;
		}
		color = config.color;
		isHidden = false;
		location = config.getResource();
		
	}

}
