package noppes.npcs.client.model.part;

import net.minecraft.util.ResourceLocation;
import noppes.npcs.ModelData;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.model.ModelMPM;
import noppes.npcs.client.model.util.Model2DRenderer;
import noppes.npcs.client.model.util.ModelPartInterface;
import noppes.npcs.entity.EntityCustomNpc;

public class ModelClaws extends ModelPartInterface {

	private Model2DRenderer model;
	private boolean isRight = false;
	
	public ModelClaws(ModelMPM base, boolean isRight) {
		super(base);
		this.isRight = isRight;
		model = new Model2DRenderer(base, 0, 16, 4, 4, 64, 32);
		if(isRight)
			model.setRotationPoint(-2F, 14f, -2);
		else
			model.setRotationPoint(3F, 14f, -2);
		model.rotateAngleY = (float) (Math.PI / -2);
		model.setScale(0.25f);
		this.addChild(model);
	}

	@Override
	public void initData(ModelData data) {
		ModelPartData config = data.getPartData("claws");
		if(config == null || isRight && config.type == 1 || !isRight && config.type == 2)
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
