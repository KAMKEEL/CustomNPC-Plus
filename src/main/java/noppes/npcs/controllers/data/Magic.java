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

    public ItemStack iconItem = null;
    public String iconTexture = "";
    public Map<Integer, Float> weaknesses = new HashMap<>();

    public Magic() {}

    public Magic(int id, String name, int color) {
        this.name = name;
        this.color = color;
        this.id = id;
    }

    public static String formatName(String name) {
        name = name.toLowerCase().trim();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public void readNBT(NBTTagCompound compound) {
        name = compound.getString("Name");
        color = compound.getInteger("Color");
        id = compound.getInteger("Slot");
        // No longer reading index/priority here.
        if(compound.hasKey("IconItem")){
            NBTTagCompound itemTag = compound.getCompoundTag("IconItem");
            iconItem = ItemStack.loadItemStackFromNBT(itemTag);
        }
        iconTexture = compound.getString("IconTexture");
        weaknesses.clear();
        if(compound.hasKey("Weaknesses")) {
            NBTTagList weaknessList = compound.getTagList("Weaknesses", 10);
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
        // No longer writing index/priority here.
        if(iconItem != null) {
            NBTTagCompound itemTag = new NBTTagCompound();
            iconItem.writeToNBT(itemTag);
            compound.setTag("IconItem", itemTag);
        }
        if(iconTexture != null && !iconTexture.isEmpty()){
            compound.setString("IconTexture", iconTexture);
        }
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

    public void setColor(int c) {
        this.color = c;
    }

    @Override
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
