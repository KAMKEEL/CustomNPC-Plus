package noppes.npcs.attribute;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides helper functions to write/read item attributes to/from NBT.
 * Non–magic attributes are stored under a compound tag (TAG_ROOT),
 * and magic attributes are stored as a compound mapping (by magic ID) under specific keys.
 */
public class CNPCItemAttributeHelper {
    public static final String TAG_ROOT = "CNPCAttributes";

    public static void applyAttribute(ItemStack item, String attributeKey, double value) {
        if (item == null) return;
        if (item.stackTagCompound == null) {
            item.stackTagCompound = new NBTTagCompound();
        }
        NBTTagCompound root = item.stackTagCompound;
        NBTTagCompound attrTag;
        if (root.hasKey(TAG_ROOT)) {
            attrTag = root.getCompoundTag(TAG_ROOT);
        } else {
            attrTag = new NBTTagCompound();
        }
        attrTag.setDouble(attributeKey, value);
        root.setTag(TAG_ROOT, attrTag);
    }

    public static Map<String, Double> readAttributes(ItemStack item) {
        Map<String, Double> map = new HashMap<>();
        if (item == null || item.stackTagCompound == null) return map;
        NBTTagCompound root = item.stackTagCompound;
        if (root.hasKey(TAG_ROOT)) {
            NBTTagCompound attrTag = root.getCompoundTag(TAG_ROOT);
            Set<String> keys = attrTag.func_150296_c();
            for (String key : keys) {
                map.put(key, attrTag.getDouble(key));
            }
        }
        return map;
    }

    public static Map<Integer, Double> readMagicAttributeMap(ItemStack item, String attributeTag) {
        Map<Integer, Double> map = new HashMap<>();
        if (item == null || item.stackTagCompound == null) return map;
        if (item.stackTagCompound.hasKey(attributeTag)) {
            NBTTagCompound magicMap = item.stackTagCompound.getCompoundTag(attributeTag);
            Set<String> keys = magicMap.func_150296_c();
            for (String key : keys) {
                try {
                    int magicId = Integer.parseInt(key);
                    map.put(magicId, magicMap.getDouble(key));
                } catch (NumberFormatException e) {
                    // Skip invalid key.
                }
            }
        }
        return map;
    }

    public static void writeMagicAttribute(ItemStack item, String attributeTag, int magicId, double value) {
        if (item == null) return;
        if (item.stackTagCompound == null)
            item.stackTagCompound = new NBTTagCompound();
        NBTTagCompound magicMap;
        if (item.stackTagCompound.hasKey(attributeTag)) {
            magicMap = item.stackTagCompound.getCompoundTag(attributeTag);
        } else {
            magicMap = new NBTTagCompound();
        }
        magicMap.setDouble(String.valueOf(magicId), value);
        item.stackTagCompound.setTag(attributeTag, magicMap);
    }
}
