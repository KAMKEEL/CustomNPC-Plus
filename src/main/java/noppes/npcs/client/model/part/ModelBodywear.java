package noppes.npcs.client.model.part;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import noppes.npcs.client.model.util.Model2DRenderer;
import noppes.npcs.client.model.util.ModelScaleRenderer;

public class ModelBodywear extends ModelScaleRenderer{

	public ModelBodywear(ModelBase base) {
		super(base);

		float thick = 0.50f;

		Model2DRenderer front = new Model2DRenderer(base, 20, 36, 8, 12, 64, 64);
		front.setRotationPoint(-4.53f, 12.535f, -2.52f);
		front.setScale(0.85f,0.8175f);
		front.setThickness(thick);
		setRotation(front, 0, 0, 0);
		this.addChild(front);

		Model2DRenderer back = new Model2DRenderer(base, 32, 36, 8, 12, 64, 64);
		back.setRotationPoint(4.53f, 12.535f, 2.52f);
		back.setScale(0.85f,0.8175f);
		back.setThickness(thick);
		setRotation(back, 0, (float)(Math.PI), 0);
		this.addChild(back);

		Model2DRenderer right = new Model2DRenderer(base, 28, 36, 4, 12, 64, 64);
		right.setRotationPoint(4.03f, 12.55f, 2.55f);
		right.setScale(0.96f,0.82f);
		right.setThickness(thick);
		setRotation(right, 0, (float)(Math.PI/2f), 0);
		this.addChild(right);

		Model2DRenderer left = new Model2DRenderer(base, 16, 36, 4, 12, 64, 64);
		left.setRotationPoint(-4.03f, 12.55f, -2.55f);
		left.setScale(0.96f,0.82f);
		left.setThickness(thick);
		setRotation(left, 0, (float)(Math.PI/-2f), 0);
		this.addChild(left);


		Model2DRenderer top = new Model2DRenderer(base, 20, 32, 8, 4, 64, 64);
		top.setRotationPoint(-4.5f, -0.6f, -2.525f);
		top.setScale(0.2815f,0.315f);
		top.setThickness(thick);
		setRotation(top, (float)(Math.PI / -2), 0, 0);
		this.addChild(top);

		Model2DRenderer bottom = new Model2DRenderer(base, 28, 32, 8, 4, 64, 64);
		bottom.setRotationPoint(-4.5f, 12.11f, -2.525f);
		bottom.setScale(0.2815f,0.315f);
		bottom.setThickness(thick);
		setRotation(bottom, (float)(Math.PI / -2), 0, 0);
		this.addChild(bottom);
	}

	public void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

}
