package noppes.npcs.controllers.data;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.handler.data.IMagic;
import noppes.npcs.controllers.MagicController;

import java.util.HashMap;
import java.util.Map;

public class Magic implements IMagic {
    public String name = "";
    public int color = Integer.parseInt("FF00", 16);
    public int id = -1;

    public int index;
    public int priority;

    // Icon as an ItemStack or as a texture file
    public ItemStack iconItem = null;
    public String iconTexture = "";

    // Weaknesses mapping: key = magic ID this magic is weak against,
    // value = extra damage percentage (as a fraction, e.g. 0.2 for 20% extra damage)
    public Map<Integer, Float> weaknesses = new HashMap<Integer, Float>();

    public Magic() {}

    public Magic(int id, String name, int color) {
        this.name = name;
        this.color = color;
        this.id = id;
    }

    public Magic(int id, String name, int color, int index, int priority) {
        this.name = name;
        this.color = color;
        this.id = id;
        this.index = index;
        this.priority = priority;
    }

    public static String formatName(String name) {
        name = name.toLowerCase().trim();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public void readNBT(NBTTagCompound compound) {
        name = compound.getString("Name");
        color = compound.getInteger("Color");
        id = compound.getInteger("Slot");
        index = compound.getInteger("Index");
        priority = compound.getInteger("Priority");

        if(compound.hasKey("IconItem")){
            NBTTagCompound itemTag = compound.getCompoundTag("IconItem");
            iconItem = ItemStack.loadItemStackFromNBT(itemTag);
        }
        iconTexture = compound.getString("IconTexture");

        // Read the weaknesses mapping from NBT.
        weaknesses.clear();
        if(compound.hasKey("Weaknesses")) {
            NBTTagList weaknessList = compound.getTagList("Weaknesses", 10); // compound tags
            for(int i = 0; i < weaknessList.tagCount(); i++){
                NBTTagCompound weaknessTag = weaknessList.getCompoundTagAt(i);
                int weakMagicId = weaknessTag.getInteger("MagicID");
                float percentage = weaknessTag.getFloat("Percentage");
                weaknesses.put(weakMagicId, percentage);
            }
        }
    }

    public void writeNBT(NBTTagCompound compound) {
        compound.setInteger("Slot", id);
        compound.setString("Name", name);
        compound.setInteger("Color", color);
        compound.setInteger("Index", index);
        compound.setInteger("Priority", priority);

        if(iconItem != null) {
            NBTTagCompound itemTag = new NBTTagCompound();
            iconItem.writeToNBT(itemTag);
            compound.setTag("IconItem", itemTag);
        }
        if(iconTexture != null && !iconTexture.isEmpty()){
            compound.setString("IconTexture", iconTexture);
        }

        // Write the weaknesses mapping.
        NBTTagList weaknessList = new NBTTagList();
        for(Map.Entry<Integer, Float> entry : weaknesses.entrySet()){
            NBTTagCompound weaknessTag = new NBTTagCompound();
            weaknessTag.setInteger("MagicID", entry.getKey());
            weaknessTag.setFloat("Percentage", entry.getValue());
            weaknessList.appendTag(weaknessTag);
        }
        compound.setTag("Weaknesses", weaknessList);
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return this.color;
    }

    public void save() {
        MagicController.getInstance().saveMagic(this);
    }

    public void setIconItem(ItemStack item) {
        this.iconItem = item;
    }

    public ItemStack getIconItem() {
        return this.iconItem;
    }

    public void setIconTexture(String texture) {
        this.iconTexture = texture;
    }

    public String getIconTexture() {
        return this.iconTexture;
    }

    public void setWeaknesses(Map<Integer, Float> weaknesses) {
        this.weaknesses = weaknesses;
    }

    public Map<Integer, Float> getWeaknesses() {
        return this.weaknesses;
    }
}
