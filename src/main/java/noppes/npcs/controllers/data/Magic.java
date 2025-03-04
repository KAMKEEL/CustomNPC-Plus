package noppes.npcs.controllers.data;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.handler.data.IMagic;
import noppes.npcs.constants.EnumTextureType;
import noppes.npcs.controllers.MagicController;

import java.util.HashMap;
import java.util.Map;

public class Magic implements IMagic {
    public String name = "";
    public String displayName = "";
    public int color = Integer.parseInt("FF00", 16);
    public int id = -1;

    public ItemStack item = null;

    public EnumTextureType type = EnumTextureType.BASE;
    public String iconTexture = "";
    public Map<Integer, Float> interactions = new HashMap<>();

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

        type = EnumTextureType.values()[compound.getInteger("Type")];
        iconTexture = compound.getString("IconTexture");
        if(type == EnumTextureType.ITEM && !iconTexture.isEmpty()){
            String[] parts = iconTexture.split(":");
            if (parts.length == 2) {
                String modID = parts[0];
                String itemName = parts[1];
                Item item = GameRegistry.findItem(modID, itemName);
                this.item = (item != null) ? new ItemStack(item) : null;
            }
        }

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
    }

    public void writeNBT(NBTTagCompound compound) {
        compound.setInteger("Slot", id);
        compound.setString("Name", name);
        compound.setString("DisplayName", displayName);
        compound.setInteger("Color", color);
        compound.setString("IconTexture", iconTexture);
        compound.setInteger("Type", type.ordinal());
        NBTTagList interactionsList = new NBTTagList();
        for (Map.Entry<Integer, Float> entry : interactions.entrySet()){
            NBTTagCompound interactionTag = new NBTTagCompound();
            interactionTag.setInteger("MagicID", entry.getKey());
            interactionTag.setFloat("Percentage", entry.getValue());
            interactionsList.appendTag(interactionTag);
        }
        compound.setTag("Interactions", interactionsList);

        if(type == EnumTextureType.ITEM && !iconTexture.isEmpty()){
            String[] parts = iconTexture.split(":");
            if (parts.length == 2) {
                String modID = parts[0];
                String itemName = parts[1];
                Item item = GameRegistry.findItem(modID, itemName);
                this.item = (item != null) ? new ItemStack(item) : null;
            }
        }
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

    public void setItem(ItemStack item) {
        GameRegistry.UniqueIdentifier identifier = GameRegistry.findUniqueIdentifierFor(item.getItem());
        if(identifier != null){
            this.item = item;
            this.type = EnumTextureType.ITEM;
            this.iconTexture = identifier.modId + ":" + identifier.name;
        }
    }

    public ItemStack getItem() {
        return this.item;
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
