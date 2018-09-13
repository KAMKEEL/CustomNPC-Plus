package noppes.npcs;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class ModelPartData {
	public int color = 0xFFFFFF;
	public String texture;
	public byte type = 0;
	public boolean playerTexture;
	
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
		if(texture != null && !texture.isEmpty())
			compound.setString("Texture", texture);
		compound.setBoolean("PlayerTexture", playerTexture);
		return compound;
	}
	
	public void readFromNBT(NBTTagCompound compound){
		type = compound.getByte("Type");
		color = compound.getInteger("Color");
		texture = compound.getString("Texture");
		playerTexture = compound.getBoolean("PlayerTexture");
		location = null;
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
			this.texture = "moreplayermodels:textures/"+ texture + ".png";
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
}
