package noppes.npcs.client.model.part.tails;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelMonkeyTail extends ModelRenderer {
	public boolean isAnimated = true;

	// Monkey Tail
	public ModelRenderer monkey;
	public ModelRenderer m1;
	public ModelRenderer m2;
	public ModelRenderer m3;
	public ModelRenderer m4;
	public ModelRenderer m5;
	public ModelRenderer m6;

	// Monkey Tail (Belt)
	public ModelRenderer monkey_wrapped;
	public ModelRenderer mw1;
	public ModelRenderer mw2;
	public ModelRenderer mw3;
	public ModelRenderer mw4;

	// Monkey Tail (Large)
	public ModelRenderer monkey_large;
	public ModelRenderer ml1;
	public ModelRenderer ml2;
	public ModelRenderer ml3;
	public ModelRenderer ml4;
	public ModelRenderer ml5;

	public ModelMonkeyTail(ModelBiped base) {
		super(base);
		float heightFactor = -9.0f;

		(this.monkey_large = new ModelRenderer(base, 38, 54)).addBox(-2.0f, -2.0f, 0.0f, 4, 4, 6);
		this.setRotation(this.monkey_large, -0.5235988f, 0.0f, 0.0f);
		(this.ml1 = new ModelRenderer(base, 38, 54)).addBox(-2.0f, -2.0f, 0.0f, 4, 4, 6);
		this.setRotation(this.ml1, 0.5235988f, 8.727E-4f, 0.0f);
		(this.ml2 = new ModelRenderer(base, 38, 54)).addBox(-2.0f, -2.0f, 0.0f, 4, 4, 6);
		this.setRotation(this.ml2, 0.0f, 0.0f, 0.0f);
		(this.ml3 = new ModelRenderer(base, 38, 54)).addBox(-2.0f, -2.0f, 0.0f, 4, 4, 6);
		this.setRotation(this.ml3, 0.0f, 0.0f, 0.0f);
		(this.ml4 = new ModelRenderer(base, 38, 54)).addBox(-2.0f, -2.0f, 0.0f, 4, 4, 6);
		this.setRotation(this.ml4, 0.0f, 0.0f, 0.0f);
		(this.ml5 = new ModelRenderer(base, 38, 54)).addBox(-2.0f, -2.0f, 0.0f, 4, 4, 6);
		this.setRotation(this.ml5, 0.0f, 0.0f, 0.0f);

		this.monkey_large.rotateAngleX = 0.65f ;
		this.monkey_large.rotationPointY = -2.0f ;
		this.ml1.rotationPointZ = 5.0f;
		this.ml2.rotationPointZ = 5.0f;
		this.ml3.rotationPointZ = 5.0f;
		this.ml4.rotationPointZ = 5.0f;
		this.ml5.rotationPointZ = 5.0f;

		this.ml4.addChild(this.ml5);
		this.ml3.addChild(this.ml4);
		this.ml2.addChild(this.ml3);
		this.ml1.addChild(this.ml2);
		this.monkey_large.addChild(this.ml1);

		(this.monkey = new ModelRenderer(base, 0, 0)).addBox(-0.0f, -0.0f, -0.0f, 0, 0, 0, 0.02f);
		this.m1 = new ModelRenderer(base, 32, 48).addBox(-1.0f, -1.0f, 0.0f, 2, 2, 4);
		this.setRotation(this.m1, -0.5235988f, 0.0f, 0.0f);
		this.m2 = new ModelRenderer(base, 32, 48).addBox(-1.0f, -1.0f, 0.0f, 2, 2, 4);
		this.setRotation(this.m2, 0.5235988f, 8.727E-4f, 0.0f);
		this.m3 = new ModelRenderer(base, 32, 48).addBox(-1.0f, -1.0f, 0.0f, 2, 2, 4);
		this.setRotation(this.m3, 0.0f, 0.0f, 0.0f);
		this.m4 = new ModelRenderer(base, 32, 48).addBox(-1.0f, -1.0f, 0.0f, 2, 2, 4);
		this.setRotation(this.m4, 0.0f, 0.0f, 0.0f);
		this.m5 = new ModelRenderer(base, 32, 48).addBox(-1.0f, -1.0f, 0.0f, 2, 2, 4);
		this.setRotation(this.m5, 0.0f, 0.0f, 0.0f);
		this.m6 = new ModelRenderer(base, 32, 48).addBox(-1.0f, -1.0f, 0.0f, 2, 2, 4);
		this.setRotation(this.m6, 0.0f, 0.0f, 0.0f);

		this.m5.addChild(this.m6);
		this.m4.addChild(this.m5);
		this.m3.addChild(this.m4);
		this.m2.addChild(this.m3);
		this.m1.addChild(this.m2);
		this.monkey.addChild(this.m1);

		// Init Rotation Points
		this.monkey.rotationPointX = 1.0f;
		this.monkey.rotationPointY = 10.0f + heightFactor;
		this.monkey.rotationPointZ = 2.0f;
		this.m1.rotationPointX = -1.0f;
		this.m1.rotationPointY = -1.0f;
		this.m2.rotationPointZ = 4.0f;
		this.m3.rotationPointZ = 4.0f;
		this.m4.rotationPointZ = 4.0f;
		this.m5.rotationPointZ = 4.0f;
		this.m6.rotationPointZ = 4.0f;

		(this.monkey_wrapped = new ModelRenderer(base, 0, 0)).addBox(-0.0f, -0.0f, -0.0f, 0, 0, 0, 0.02f);
		this.monkey_wrapped.setRotationPoint(0.0f, 0.0f + heightFactor, 0.0f);
		this.mw1 = new ModelRenderer(base, 32, 48).addBox(3.5f, 8.0f, -2.5f, 1, 2, 5);
		this.mw1.setRotationPoint(0.0f, 0.0f, 0.0f);
		this.setRotation(this.mw1, 0.0f, 0.0f, 0.0f);
		this.mw2 = new ModelRenderer(base, 32, 48).addBox(-4.433333f, 8.0f, -2.5f, 1, 2, 5);
		this.mw2.setRotationPoint(0.0f, 0.0f, 0.0f);
		this.setRotation(this.mw2, 0.0f, 0.0f, 0.0f);
		this.mw3 = new ModelRenderer(base, 32, 48).addBox(-3.433333f, 8.0f, 1.5f, 7, 2, 1);
		this.mw3.setRotationPoint(0.0f, 0.0f, 0.0f);
		this.setRotation(this.mw3, 0.0f, 0.0f, 0.0f);
		this.mw4 = new ModelRenderer(base, 32, 48).addBox(-3.433333f, 8.0f, -2.5f, 7, 2, 1);
		this.mw4.setRotationPoint(0.0f, 0.0f, 0.0f);
		this.setRotation(this.mw4, 0.0f, 0.0f, 0.0f);

		this.monkey_wrapped.rotationPointZ = -0.5f;

		this.monkey_wrapped.addChild(this.mw1);
		this.monkey_wrapped.addChild(this.mw2);
		this.monkey_wrapped.addChild(this.mw3);
		this.monkey_wrapped.addChild(this.mw4);

		this.addChild(monkey);
		this.addChild(monkey_wrapped);
		this.addChild(monkey_large);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
		// Scale down the motion values to control the effect
		float scale = 0.18f; // Adjust the scale factor as needed

		// Define a damping factor for smoother motion
		float damping = 0.6f; // Adjust the damping factor as needed

		double motionX = entity.motionX;
		double motionY = -entity.motionY;

		if(!monkey_large.isHidden){
			this.monkey_large.rotateAngleY = 0.2f;
			this.monkey_large.rotateAngleX = -0.3f;
			this.ml1.rotateAngleY = 0.2f;
			this.ml1.rotateAngleX = 0.4f;
			this.ml2.rotateAngleY = 0.1f;
			this.ml2.rotateAngleX = 0.6f;
			this.ml3.rotateAngleY = 0.1f;
			this.ml3.rotateAngleX = 0.3f;
			this.ml4.rotateAngleY = 0.2f;
			this.ml4.rotateAngleX = -0.2f;
			this.ml5.rotateAngleY = 0.2f;
			this.ml5.rotateAngleX = -0.4f;

			if(isAnimated){
				float angleOffset = 0.01f;
				final float r = MathHelper.sin(f2 * 0.02f) * angleOffset;
				final float r2 = MathHelper.cos(f2 * 0.02f) * angleOffset;
				final float r3 = MathHelper.cos(f2 * 0.14f) * angleOffset;
				final float angleSpeed = 0.05f;
				float xMotionReducer = 0.2f;
				float yMotionReducer = 0.4f;

				monkey_large.rotateAngleY += yMotionReducer * (MathHelper.cos(f2 * angleSpeed) * 0.2f - 0.2f + r);
				ml1.rotateAngleY += yMotionReducer * (MathHelper.cos(f2 * angleSpeed) * 0.2f - 0.2f + r2 + r3);
				ml2.rotateAngleY += yMotionReducer * (MathHelper.cos(f2 * angleSpeed) * 0.1f - 0.1f + r + r3);
				ml2.rotateAngleX += xMotionReducer * (MathHelper.sin(f2 * angleSpeed) * 0.4f + 0.3f);
				ml3.rotateAngleY += yMotionReducer * (MathHelper.cos(f2 * angleSpeed) * 0.4f - 0.1f + r2);
				ml3.rotateAngleX += xMotionReducer * (MathHelper.sin(f2 * angleSpeed) * 0.1f - 0.2f);
				ml4.rotateAngleY += yMotionReducer * (MathHelper.cos(f2 * angleSpeed) * 0.4f - 0.2f + r + r3);
				ml4.rotateAngleX += xMotionReducer * (MathHelper.sin(f2 * angleSpeed) * 0.1f - 0.3f);
				ml5.rotateAngleY += yMotionReducer * (MathHelper.cos(f2 * angleSpeed) * 0.4f - 0.2f + r2 + r3);
				ml5.rotateAngleX += xMotionReducer * (MathHelper.sin(f2 * angleSpeed) * 0.4f - 0.4f);
			}

			// Calculate the difference between the current rotation angle and the motion influence
			double deltaY = motionY * scale - monkey.rotateAngleX;
			double deltaX = motionX * scale - monkey.rotateAngleY;

			// Apply damping to smooth out the motion

			monkey_large.rotateAngleX += deltaY * damping;
			ml1.rotateAngleX += deltaY * damping;
			ml2.rotateAngleX += deltaY * damping;
			ml3.rotateAngleX += deltaY * damping;
			ml4.rotateAngleX += deltaY * damping;
			ml5.rotateAngleX += deltaY * damping;

			monkey_large.rotateAngleY += deltaX * damping;
			ml1.rotateAngleY += deltaX * damping;
			ml2.rotateAngleY += deltaX * damping;
			ml3.rotateAngleY += deltaX * damping;
			ml4.rotateAngleY += deltaX * damping;
			ml5.rotateAngleY += deltaX * damping;

		} else if (!monkey.isHidden){
			this.m1.rotateAngleX = -0.3f;
			this.m1.rotateAngleY = 0.2f;
			this.m2.rotateAngleX = 0.4f;
			this.m2.rotateAngleY = 0.2f;
			this.m3.rotateAngleX = 0.6f;
			this.m3.rotateAngleY = 0.1f;
			this.m4.rotateAngleX = 0.3f;
			this.m4.rotateAngleY = 0.1f;
			this.m5.rotateAngleX = -0.2f;
			this.m5.rotateAngleY = 0.2f;
			this.m6.rotateAngleX = -0.4f;
			this.m6.rotateAngleY = 0.2f;

			if(isAnimated){
				float angleOffset = 0.01f;
				final float r4 = MathHelper.sin(f2 * 0.02f) * angleOffset;
				final float r5 = MathHelper.cos(f2 * 0.02f) * angleOffset;
				final float r6 = MathHelper.cos(f2 * 0.14f) * angleOffset;
				final float angleSpeed = 0.05f;
				float xMotionReducer = 0.4f;
				float yMotionReducer = 0.4f;

				this.m1.rotateAngleY += yMotionReducer * (MathHelper.cos(f2 * angleSpeed) * 0.2f - 0.2f + r4);
				this.m2.rotateAngleY += yMotionReducer * (MathHelper.cos(f2 * angleSpeed) * 0.2f - 0.2f + r5 + r6);

				this.m3.rotateAngleX += xMotionReducer * (MathHelper.sin(f2 * angleSpeed) * 0.4f + 0.1f);
				this.m3.rotateAngleY += yMotionReducer * (MathHelper.cos(f2 * angleSpeed) * 0.1f - 0.1f + r4 + r6);

				this.m4.rotateAngleX += xMotionReducer * (MathHelper.sin(f2 * angleSpeed) * 0.1f - 0.2f);
				this.m4.rotateAngleY += yMotionReducer * (MathHelper.cos(f2 * angleSpeed) * 0.4f - 0.1f + r5);

				this.m5.rotateAngleX += xMotionReducer * (MathHelper.sin(f2 * angleSpeed) * 0.1f - 0.1f);
				this.m5.rotateAngleY += yMotionReducer * (MathHelper.cos(f2 * angleSpeed) * 0.4f - 0.2f + r4 + r6);

				this.m6.rotateAngleX += xMotionReducer * (MathHelper.sin(f2 * angleSpeed) * 0.4f - 0.4f);
				this.m6.rotateAngleY += yMotionReducer * (MathHelper.cos(f2 * angleSpeed) * 0.4f - 0.2f + r5 + r6);
			}

			// Calculate the difference between the current rotation angle and the motion influence
			double deltaY = motionY * scale - monkey.rotateAngleX;
			double deltaX = motionX * scale - monkey.rotateAngleY;

			// Apply damping to smooth out the motion
			m1.rotateAngleX += deltaY * damping;
			m2.rotateAngleX += deltaY * damping;
			m3.rotateAngleX += deltaY * damping;
			m4.rotateAngleX += deltaY * damping;
			m5.rotateAngleX += deltaY * damping;
			m6.rotateAngleX += deltaY * damping;

			m1.rotateAngleY += deltaX * damping;
			m2.rotateAngleY += deltaX * damping;
			m3.rotateAngleY += deltaX * damping;
			m4.rotateAngleY += deltaX * damping;
			m5.rotateAngleY += deltaX * damping;
			m6.rotateAngleY += deltaX * damping;
		}
	}
}
