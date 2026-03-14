package noppes.npcs.controllers.data;

import noppes.npcs.api.INbt;
import noppes.npcs.core.NBT;

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

    public void readNBT(INbt compound) {
        id = compound.getInteger("CatID");
        title = compound.getString("CatTitle");
    }

    public INbt writeNBT(INbt compound) {
        compound.setInteger("CatID", id);
        compound.setString("CatTitle", title);
        return compound;
    }
}
