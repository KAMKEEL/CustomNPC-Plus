package noppes.npcs.client.model.part;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import noppes.npcs.client.model.util.Model2DRenderer;
import noppes.npcs.client.model.util.ModelScaleRenderer;

public class ModelHeadwear extends ModelScaleRenderer{
	
	public ModelHeadwear(ModelBase base) {
		super(base);		
		Model2DRenderer right = new Model2DRenderer(base, 32, 8, 8, 8, 64, 32);
		right.setRotationPoint(-4.641F, .8f, 4.64f);
		right.setScale(0.58f);
		right.setThickness(0.65f);
        setRotation(right, 0, (float)(Math.PI/2f), 0);
		this.addChild(right);
		
		Model2DRenderer left = new Model2DRenderer(base, 48, 8, 8, 8, 64, 32);
		left.setRotationPoint(4.639F, .8f, -4.64f);
		left.setScale(0.58f);
		left.setThickness(0.65f);
        setRotation(left, 0, (float)(Math.PI/-2f), 0);
		this.addChild(left);
		
		Model2DRenderer front = new Model2DRenderer(base, 40, 8, 8, 8, 64, 32);
		front.setRotationPoint(-4.64F, .801f, -4.641f);
		front.setScale(0.58f);
		front.setThickness(0.65f);
        setRotation(front, 0, 0, 0);
		this.addChild(front);
		
		Model2DRenderer back = new Model2DRenderer(base, 56, 8, 8, 8, 64, 32);
		back.setRotationPoint(4.64F, .801f, 4.639f);
		back.setScale(0.58f);
		back.setThickness(0.65f);
        setRotation(back, 0, (float)(Math.PI), 0);
		this.addChild(back);
		
		Model2DRenderer top = new Model2DRenderer(base, 40, 0, 8, 8, 64, 32);
		top.setRotationPoint(-4.64F, -8.5f, -4.64f);
		top.setScale(0.5799f);
		top.setThickness(0.65f);
        setRotation(top, (float)(Math.PI / -2), 0, 0);
		this.addChild(top);
		
		Model2DRenderer bottom = new Model2DRenderer(base, 48, 0, 8, 8, 64, 32);
		bottom.setRotationPoint(-4.64F, 0f, -4.64f);
		bottom.setScale(0.5799f);
		bottom.setThickness(0.65f);
        setRotation(bottom, (float)(Math.PI / -2), 0, 0);
		this.addChild(bottom);
	}

	public void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}   
	
}
