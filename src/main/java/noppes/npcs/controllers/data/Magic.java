package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.handler.data.IMagic;
import noppes.npcs.controllers.MagicController;

public class Magic implements IMagic {
	public String name = "";
	public int color = Integer.parseInt("FF00", 16);

	public int id = -1;

    public Magic(){}

	public Magic(int id, String name, int color){
		this.name = name;
		this.color = color;
		this.id = id;
	}
	public static String formatName(String name){
		name = name.toLowerCase().trim();
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}
	public void readNBT(NBTTagCompound compound){
        name = compound.getString("Name");
        color = compound.getInteger("Color");
        id = compound.getInteger("Slot");
	}
	public void writeNBT(NBTTagCompound compound){
		compound.setInteger("Slot", id);
		compound.setString("Name", name);
		compound.setInteger("Color", color);
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) { this.name = name; }

	public void setColor(int color) { this.color = color; }

	public int getColor() {
		return this.color;
	}

	public void save() {
		MagicController.getInstance().saveMagic(this);
	}
}
