package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import java.util.HashMap;

/**
 * Holds category information for magic.
 * Each category can have many magic associations.
 */
public class MagicCycle {
    public int id = -1;
    public String title = "";
    public HashMap<Integer, MagicAssociation> associations = new HashMap<>();

    public MagicCycle() {}

    public void readNBT(NBTTagCompound compound) {
        id = compound.getInteger("CategoryID");
        title = compound.getString("Title");
        associations.clear();
        NBTTagList list = compound.getTagList("Associations", 10); // compound tags
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound assocTag = list.getCompoundTagAt(i);
            MagicAssociation assoc = new MagicAssociation();
            assoc.magicId = assocTag.getInteger("MagicID");
            assoc.index = assocTag.getInteger("Index");
            assoc.priority = assocTag.getInteger("Priority");
            associations.put(assoc.magicId, assoc);
        }
    }

    public void writeNBT(NBTTagCompound compound) {
        compound.setInteger("CategoryID", id);
        compound.setString("Title", title);
        NBTTagList list = new NBTTagList();
        for (MagicAssociation assoc : associations.values()) {
            NBTTagCompound assocTag = new NBTTagCompound();
            assocTag.setInteger("MagicID", assoc.magicId);
            assocTag.setInteger("Index", assoc.index);
            assocTag.setInteger("Priority", assoc.priority);
            list.appendTag(assocTag);
        }
        compound.setTag("Associations", list);
    }
}
