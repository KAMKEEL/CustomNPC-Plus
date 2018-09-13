package noppes.npcs.client.model.part;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.ModelData;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.model.ModelMPM;
import noppes.npcs.client.model.util.Model2DRenderer;
import noppes.npcs.client.model.util.ModelPartInterface;

public class ModelEars extends ModelPartInterface {
	private ModelRenderer ears;
	private ModelRenderer bunny;
	public ModelEars(ModelMPM par1ModelBase) {
		super(par1ModelBase);
		
		ears = new ModelRenderer(base);
		this.addChild(ears);

		Model2DRenderer right = new Model2DRenderer(base, 56, 0, 8, 4, 64, 32);
		right.setRotationPoint(-7.44f, -7.3f, -0.0f);
		right.setScale(0.234f, 0.234f);
		right.setThickness(1.16f);
		ears.addChild(right);

		Model2DRenderer left = new Model2DRenderer(base, 56, 0, 8, 4, 64, 32);
		left.setRotationPoint(7.44f, -7.3f, 1.15f);
		left.setScale(0.234f, 0.234f);
        setRotation(left, 0, (float)(Math.PI), 0);
        left.setThickness(1.16f);
        ears.addChild(left);

		Model2DRenderer right2 = new Model2DRenderer(base, 56, 4, 8, 4, 64, 32);
		right2.setRotationPoint(-7.44f, -7.3f, 1.14f);
		right2.setScale(0.234f, 0.234f);
		right2.setThickness(1.16f);
		ears.addChild(right2);

		Model2DRenderer left2 = new Model2DRenderer(base, 56, 4, 8, 4, 64, 32);
		left2.setRotationPoint(7.44f, -7.3f, 2.31f);
		left2.setScale(0.234f, 0.234f);
        setRotation(left2, 0, (float)(Math.PI), 0);
        left2.setThickness(1.16f);
        ears.addChild(left2);

		
		bunny = new ModelRenderer(base);
		this.addChild(bunny);
		
		ModelRenderer earleft = new ModelRenderer(base, 56, 0);
		earleft.mirror = true;
		earleft.addBox(-1.466667F, -4F, 0F, 3, 7, 1);
		earleft.setRotationPoint(2.533333F, -11F, 0F);
		bunny.addChild(earleft);

		ModelRenderer earright = new ModelRenderer(base, 56, 0);
		earright.addBox(-1.5F, -4F, 0F, 3, 7, 1);
		earright.setRotationPoint(-2.466667F, -11F, 0F);
		bunny.addChild(earright);
	}

	@Override
	public void initData(ModelData data) {
		ModelPartData config = data.getPartData("ears");
		if(config == null)
		{
			isHidden = true;
			return;
		}
		isHidden = false;
		this.color = config.color;
		
		ears.isHidden = config.type != 0;
		bunny.isHidden = config.type != 1;

		if(!config.playerTexture){
			location = config.getResource();
		}
		else
			location = null;
	}

}
