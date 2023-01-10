package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.handler.data.ITag;
import noppes.npcs.controllers.TagController;

import java.util.UUID;

import static java.util.UUID.randomUUID;

public class Tag implements ITag {
	public String name = "";
	public int color = Integer.parseInt("FF00", 16);
	public int id = -1;
	public UUID uuid = randomUUID();
	public boolean hideTag = false;

	public Tag(){}

	public Tag(int id, String name, int color){
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
        hideTag = compound.getBoolean("HideTag");
		uuid = UUID.fromString(compound.getString("Uuid"));
  }
	public void writeNBT(NBTTagCompound compound){
		compound.setInteger("Slot", id);
		compound.setString("Name", name);
		compound.setInteger("Color", color);
		compound.setString("Uuid", uuid.toString());
		compound.setBoolean("HideTag", hideTag);
	}

	public int getId() {
		return this.id;
	}

	public String getUuid() {
		return this.uuid.toString();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) { this.name = name; }

	public void setColor(int color) { this.color = color; }

	public int getColor() {
		return this.color;
	}

	public boolean getIsHidden() {
		return this.hideTag;
	}

	public void setIsHidden(boolean bo) {
		this.hideTag = bo;
	}

	public void save() {
		TagController.getInstance().saveTag(this);
	}
}
