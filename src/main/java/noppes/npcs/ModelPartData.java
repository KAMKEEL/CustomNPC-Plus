package noppes.npcs;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class ModelPartData {
	public int color = 0xFFFFFF;
	public String texture;
	public byte type = 0;
	public boolean playerTexture;

	public int amount, maxAge, scaleRateStart, alphaRateStart;
	public float scale1, scale2, scaleRate, gravity, alpha1, alpha2, alphaRate;
	public double x, y, z, motionX, motionY, motionZ;

	private ResourceLocation location;

	public ModelPartData(){
		playerTexture = true;
	}

	public ModelPartData(String texture) {
		this.texture = texture;
		playerTexture = false;
	}

	public NBTTagCompound writeToNBT(){
		NBTTagCompound compound = new NBTTagCompound();
		compound.setByte("Type", type);
		compound.setInteger("Color", color);

		compound.setInteger("Amount", amount);
		compound.setInteger("MaxAge", maxAge);

		compound.setFloat("Scale1", scale1);
		compound.setFloat("Scale2", scale2);
		compound.setFloat("ScaleRate", scaleRate);
		compound.setInteger("ScaleRateStart",scaleRateStart);

		compound.setFloat("Alpha1", alpha1);
		compound.setFloat("Alpha2", alpha2);
		compound.setFloat("AlphaRate", alphaRate);
		compound.setInteger("AlphaRateStart",alphaRateStart);

		compound.setDouble("X",x);
		compound.setDouble("Y",y);
		compound.setDouble("Z",z);
		compound.setDouble("MotionX",motionX);
		compound.setDouble("MotionY",motionY);
		compound.setDouble("MotionZ",motionZ);
		compound.setFloat("Gravity",gravity);

		if(texture != null && !texture.isEmpty())
			compound.setString("Texture", texture);
		compound.setBoolean("PlayerTexture", playerTexture);
		return compound;
	}

	public void readFromNBT(NBTTagCompound compound){
		type = compound.getByte("Type");
		color = compound.getInteger("Color");
		texture = compound.getString("Texture");

		amount = compound.getInteger("Amount");
		maxAge = compound.getInteger("MaxAge");

		scale1 = compound.getFloat("Scale1");
		scale2 = compound.getFloat("Scale2");
		scaleRate = compound.getFloat("ScaleRate");
		scaleRateStart = compound.getInteger("ScaleRateStart");

		alpha1 = compound.getFloat("Alpha1");
		alpha2 = compound.getFloat("Alpha2");
		alphaRate = compound.getFloat("AlphaRate");
		alphaRateStart = compound.getInteger("AlphaRateStart");

		x = compound.getDouble("X");
		y = compound.getDouble("Y");
		z = compound.getDouble("Z");
		motionX = compound.getDouble("MotionX");
		motionY = compound.getDouble("MotionY");
		motionZ = compound.getDouble("MotionZ");
		gravity = compound.getFloat("Gravity");

		playerTexture = compound.getBoolean("PlayerTexture");
		location = null;
	}

	public void setAttributes(String texture, int HEXcolor, int amount, int maxAge,
							  double x, double y, double z,
							  double motionX, double motionY, double motionZ, float gravity,
							  float scale1, float scale2, float scaleRate, int scaleRateStart,
							  float alpha1, float alpha2, float alphaRate, int alphaRateStart) {
		this.location = null;
		if(texture.isEmpty()){
			playerTexture = true;
			this.texture = texture;
		}
		else{
			this.texture = texture;
			this.color = HEXcolor;
			this.amount = amount;

			this.scale1 = scale1;
			this.scale2 = scale2;
			this.scaleRate = scaleRate;
			this.scaleRateStart = scaleRateStart;

			this.alpha1 = alpha1;
			this.alpha2 = alpha2;
			this.alphaRate = alphaRate;
			this.alphaRateStart = alphaRateStart;

			this.maxAge = maxAge;
			this.x = x;
			this.y = y;
			this.z = z;
			this.motionX = motionX;
			this.motionY = motionY;
			this.motionZ = motionZ;
			this.gravity = gravity;

			playerTexture = false;
		}
	}

	public ResourceLocation getResource(){
		if(texture.isEmpty())
			return null;
		if(location != null)
			return location;
		location = new ResourceLocation(texture);
		return location;
	}

	public void setTexture(String texture, int type) {
		this.type = (byte) type;
		this.location = null;
		if(texture.isEmpty()){
			playerTexture = true;
			this.texture = texture;
		}
		else{
			this.texture = "customnpcs:textures/parts/"+ texture + ".png";
			playerTexture = false;
		}
	}

	public String toString(){
		return "Color: " + color + " Type: " + type;
	}

	public String getColor() {
		String str = Integer.toHexString(color);

		while(str.length() < 6)
			str = "0" + str;

		return str;
	}

	public int getAmount() {
		return amount;
	}

	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
	public double getZ() {
		return z;
	}
	public double getMotionX() {
		return motionX;
	}
	public double getMotionY() {
		return motionY;
	}
	public double getMotionZ() {
		return motionZ;
	}

	public float getScale1() {
		return scale1;
	}
	public float getScale2() {return scale2;}
	public float getScaleRate() {
		return scaleRate;
	}
	public int getScaleRateStart() {return scaleRateStart;}

	public float getAlpha1() {return alpha1;}
	public float getAlpha2() {return alpha2;}
	public float getAlphaRate() {
		return alphaRate;
	}
	public int getAlphaRateStart() {return alphaRateStart;}

	public int getMaxAge() {
		return maxAge;
	}

	public float getGravity() {
		return gravity;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}
}
