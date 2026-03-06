package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Simple category data class for the generic category system.
 * Used by CategoryManager to organize items into named groups.
 */
public class Category {
    public int id = -1;
    public String title = "";

    public Category() {
    }

    public Category(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public void readNBT(NBTTagCompound compound) {
        id = compound.getInteger("CatID");
        title = compound.getString("CatTitle");
    }

    public NBTTagCompound writeNBT(NBTTagCompound compound) {
        compound.setInteger("CatID", id);
        compound.setString("CatTitle", title);
        return compound;
    }
}
