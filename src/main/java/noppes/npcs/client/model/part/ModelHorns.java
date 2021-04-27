package noppes.npcs.client.model.part;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.ModelPartData;
import noppes.npcs.ModelData;
import noppes.npcs.client.model.ModelMPM;
import noppes.npcs.client.model.util.ModelPartInterface;
import noppes.npcs.client.model.part.horns.ModelAntennasBack;
import noppes.npcs.client.model.part.horns.ModelAntennasFront;
import noppes.npcs.client.model.part.horns.ModelAntlerHorns;
import noppes.npcs.client.model.part.horns.ModelBullHorns;

public class ModelHorns extends ModelPartInterface {
	private ModelRenderer bull;
	private ModelRenderer antlers;
	private ModelRenderer antennasBack;
	private ModelRenderer antennasFront;
	
	public ModelHorns(ModelMPM base) {
		super(base);

		this.addChild(bull = new ModelBullHorns(base));
		this.addChild(antlers = new ModelAntlerHorns(base));
		this.addChild(antennasBack = new ModelAntennasBack(base));
		this.addChild(antennasFront = new ModelAntennasFront(base));
	}

	@Override
	public void setRotationAngles(float par1, float par2, float par3,
			float par4, float par5, float par6, Entity entity) {

	}

	@Override
	public void initData(ModelData data) {
		ModelPartData config = data.getPartData("horns");
		if(config == null)
		{
			isHidden = true;
			return;
		}
		color = config.color;
		isHidden = false;
		
		bull.isHidden = config.type != 0;
		antlers.isHidden = config.type != 1;
		antennasBack.isHidden = config.type != 2;
		antennasFront.isHidden = config.type != 3;
		
		if(!config.playerTexture){
			location = config.getResource();
		}
		else
			location = null;
	}
}
