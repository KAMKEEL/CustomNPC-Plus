package noppes.npcs.controllers.data;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.handler.data.IMagic;
import noppes.npcs.controllers.MagicController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Magic implements IMagic {
    public String name = "";
    public String displayName = "";
    public int color = Integer.parseInt("FF00", 16);
    public int id = -1;

    public ItemStack iconItem = null;
    public String iconTexture = "";
    public Map<Integer, Float> interactions = new HashMap<>();
    public HashSet<Integer> cycles = new HashSet<>();

    public Magic() {}

    public Magic(int id, String name, int color) {
        this.name = name;
        this.displayName = name;
        this.color = color;
        this.id = id;
    }

    public static String formatName(String name) {
        name = name.toLowerCase().trim();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public void readNBT(NBTTagCompound compound) {
        name = compound.getString("Name");
        displayName = compound.getString("DisplayName");
        color = compound.getInteger("Color");
        id = compound.getInteger("Slot");
        // Read icon item.
        if(compound.hasKey("IconItem")){
            NBTTagCompound itemTag = compound.getCompoundTag("IconItem");
            iconItem = ItemStack.loadItemStackFromNBT(itemTag);
        }
        iconTexture = compound.getString("IconTexture");

        interactions.clear();
        if(compound.hasKey("Interactions")) {
            NBTTagList interactionsList = compound.getTagList("Interactions", 10);
            for (int i = 0; i < interactionsList.tagCount(); i++){
                NBTTagCompound interactionTag = interactionsList.getCompoundTagAt(i);
                int magicId = interactionTag.getInteger("MagicID");
                float percentage = interactionTag.getFloat("Percentage");
                interactions.put(magicId, percentage);
            }
        }

        cycles.clear();
        if(compound.hasKey("Cycles")) {
            NBTTagList cyclesList = compound.getTagList("Cycles", 10);
            for (int i = 0; i < cyclesList.tagCount(); i++){
                NBTTagCompound cycleTag = cyclesList.getCompoundTagAt(i);
                cycles.add(cycleTag.getInteger("Cycle"));
            }
        }
    }

    public void writeNBT(NBTTagCompound compound) {
        compound.setInteger("Slot", id);
        compound.setString("Name", name);
        compound.setString("DisplayName", displayName);
        compound.setInteger("Color", color);
        // Write icon item.
        if(iconItem != null) {
            NBTTagCompound itemTag = new NBTTagCompound();
            iconItem.writeToNBT(itemTag);
            compound.setTag("IconItem", itemTag);
        }
        if(iconTexture != null && !iconTexture.isEmpty()){
            compound.setString("IconTexture", iconTexture);
        }

        NBTTagList interactionsList = new NBTTagList();
        for (Map.Entry<Integer, Float> entry : interactions.entrySet()){
            NBTTagCompound interactionTag = new NBTTagCompound();
            interactionTag.setInteger("MagicID", entry.getKey());
            interactionTag.setFloat("Percentage", entry.getValue());
            interactionsList.appendTag(interactionTag);
        }
        compound.setTag("Interactions", interactionsList);

        NBTTagList cyclesList = new NBTTagList();
        for (Integer cycle : cycles) {
            NBTTagCompound cycleTag = new NBTTagCompound();
            cycleTag.setInteger("Cycle", cycle);
            cyclesList.appendTag(cycleTag);
        }
        compound.setTag("Cycles", cyclesList);
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public void setInteractions(Map<Integer, Float> interactions) {
        this.interactions = interactions;
    }

    public Map<Integer, Float> getInteractions() {
        return this.interactions;
    }
}
