package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.constants.EnumDiagramLayout;

import java.util.HashMap;

/**
 * Holds category information for magic.
 * Each category can have many magic associations.
 */
public class MagicCycle {
    public int id = -1;
    public String name = "";
    public String displayName = "";
    public EnumDiagramLayout layout = EnumDiagramLayout.CIRCULAR;
    public HashMap<Integer, MagicAssociation> associations = new HashMap<>();
    public MagicCycle() {}

    public void readNBT(NBTTagCompound compound) {
        id = compound.getInteger("CategoryID");
        name = compound.getString("Name");
        displayName = compound.getString("DisplayName");
        layout = EnumDiagramLayout.values()[compound.getInteger("Layout")];
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
        compound.setString("Title", name);
        compound.setString("DisplayName", displayName);
        compound.setInteger("Layout", layout.ordinal());
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

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public EnumDiagramLayout getLayout() {
        return layout;
    }

    public void setLayout(EnumDiagramLayout layout) {
        this.layout = layout;
    }

    public HashMap<Integer, MagicAssociation> getAssociations() {
        return associations;
    }

    public void setAssociations(HashMap<Integer, MagicAssociation> associations) {
        this.associations = associations;
    }
}
