package noppes.npcs.controllers.data;

import noppes.npcs.core.NBT;
import noppes.npcs.constants.EnumTextureType;
import noppes.npcs.controllers.MagicController;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;

import java.util.HashMap;
import java.util.Map;

public class Magic {
    public String name = "";
    public String displayName = "";
    public int color = Integer.parseInt("FF00", 16);
    public int id = -1;

    // TODO: mc1710 version has: public ItemStack item = null;
    // OLD: import net.minecraft.item.ItemStack;

    public EnumTextureType type = EnumTextureType.BASE;
    public String iconTexture = "";
    public Map<Integer, Float> interactions = new HashMap<>();

    public Magic() {
    }

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

    public void readNBT(INbt compound) {
        name = compound.getString("Name");
        displayName = compound.getString("DisplayName");
        color = compound.getInteger("Color");
        id = compound.getInteger("Slot");

        type = EnumTextureType.values()[compound.getInteger("Type")];
        iconTexture = compound.getString("IconTexture");
        // TODO: mc1710 version resolves ItemStack from GameRegistry here:
        // OLD: if (type == EnumTextureType.ITEM && !iconTexture.isEmpty()) {
        // OLD:     String[] parts = iconTexture.split(":");
        // OLD:     Item item = GameRegistry.findItem(parts[0], parts[1]);
        // OLD:     this.item = (item != null) ? new ItemStack(item) : null;
        // OLD: }

        interactions.clear();
        if (compound.hasKey("Interactions")) {
            INbtList interactionsList = compound.getTagList("Interactions", 10);
            for (int i = 0; i < interactionsList.size(); i++) {
                INbt interactionTag = interactionsList.getCompound(i);
                int magicId = interactionTag.getInteger("MagicID");
                float percentage = interactionTag.getFloat("Percentage");
                interactions.put(magicId, percentage);
            }
        }
    }

    public void writeNBT(INbt compound) {
        compound.setInteger("Slot", id);
        compound.setString("Name", name);
        compound.setString("DisplayName", displayName);
        compound.setInteger("Color", color);
        compound.setString("IconTexture", iconTexture);
        compound.setInteger("Type", type.ordinal());
        INbtList interactionsList = NBT.list();
        for (Map.Entry<Integer, Float> entry : interactions.entrySet()) {
            INbt interactionTag = NBT.compound();
            interactionTag.setInteger("MagicID", entry.getKey());
            interactionTag.setFloat("Percentage", entry.getValue());
            interactionsList.addCompound(interactionTag);
        }
        compound.setTagList("Interactions", interactionsList);

        // TODO: mc1710 version resolves ItemStack from GameRegistry here:
        // OLD: if (type == EnumTextureType.ITEM && !iconTexture.isEmpty()) {
        // OLD:     Item item = GameRegistry.findItem(parts[0], parts[1]);
        // OLD:     this.item = (item != null) ? new ItemStack(item) : null;
        // OLD: }
    }

    // TODO: mc1710 version implements IMagic and adds:
    // OLD: public void setItem(ItemStack item) — uses GameRegistry.findUniqueIdentifierFor
    // OLD: public ItemStack getItem()

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

    public boolean hasInteraction(int magicID) {
        return interactions.containsKey(magicID);
    }

    public void setInteraction(int magicID, float value) {
        interactions.put(magicID, value);
    }

    public float getInteraction(int magicID, float value) {
        if (hasInteraction(magicID))
            return interactions.get(magicID);
        return 0.0f;
    }
}
