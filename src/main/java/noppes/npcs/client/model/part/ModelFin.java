package noppes.npcs.client.model.part;

import net.minecraft.util.ResourceLocation;
import noppes.npcs.ModelData;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.model.ModelMPM;
import noppes.npcs.client.model.util.Model2DRenderer;
import noppes.npcs.client.model.util.ModelPartInterface;

public class ModelFin extends ModelPartInterface {

	private Model2DRenderer model;
	
	public ModelFin(ModelMPM base) {
		super(base);
		model = new Model2DRenderer(base, 56, 20, 8, 12, 64, 32);
		model.setRotationPoint(-0.5F, 12, 10);
		model.setScale(0.74f);
		model.rotateAngleY = (float)Math.PI / 2;
		this.addChild(model);
	}

	@Override
	public void initData(ModelData data) {
		ModelPartData config = data.getPartData("fin");
		if(config == null)
		{
			isHidden = true;
			return;
		}
		color = config.color;
		isHidden = false;
		
		if(!config.playerTexture){
			location = config.getResource();
		}
		else
			location = null;
	}

}
