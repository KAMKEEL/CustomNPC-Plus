package noppes.npcs.attribute;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.ClientProxy;
import org.lwjgl.input.Keyboard;

import java.util.*;

import static noppes.npcs.attribute.AttributeDefinition.AttributeSection.INFO;
import static noppes.npcs.attribute.AttributeDefinition.AttributeSection.MODIFIER;

/**
 * Provides helper functions to write/read item attributes to/from NBT.
 * Non–magic attributes are stored under a compound tag (TAG_ROOT),
 * and magic attributes are stored as a compound mapping (by magic ID) under specific keys.
 */
public class ItemAttributeHelper {
    public static final String RPGItemAttributes = "RPGCoreAttributes";

    /**
     * Applies a non–magic attribute to an item.
     */
    public static void applyAttribute(ItemStack item, String attributeKey, double value) {
        if (item == null) return;
        if (item.stackTagCompound == null) {
            item.stackTagCompound = new NBTTagCompound();
        }
        NBTTagCompound root = item.stackTagCompound;
        NBTTagCompound attrTag;
        if (root.hasKey(RPGItemAttributes)) {
            attrTag = root.getCompoundTag(RPGItemAttributes);
        } else {
            attrTag = new NBTTagCompound();
        }
        attrTag.setDouble(attributeKey, value);
        root.setTag(RPGItemAttributes, attrTag);
    }

    /**
     * Removes a non–magic attribute from an item.
     */
    public static void removeAttribute(ItemStack item, String attributeKey) {
        if (item == null || item.stackTagCompound == null) return;
        NBTTagCompound root = item.stackTagCompound;
        if (root.hasKey(RPGItemAttributes)) {
            NBTTagCompound attrTag = root.getCompoundTag(RPGItemAttributes);
            attrTag.removeTag(attributeKey);
            // If no attributes remain, remove the root tag.
            if (attrTag.func_150296_c().isEmpty()) {
                root.removeTag(RPGItemAttributes);
            }
        }
    }

    /**
     * Reads non–magic attributes from an item.
     */
    public static Map<String, Double> readAttributes(ItemStack item) {
        Map<String, Double> map = new HashMap<>();
        if (item == null || item.stackTagCompound == null) return map;
        NBTTagCompound root = item.stackTagCompound;
        if (root.hasKey(RPGItemAttributes)) {
            NBTTagCompound attrTag = root.getCompoundTag(RPGItemAttributes);
            Set<String> keys = attrTag.func_150296_c();
            for (String key : keys) {
                map.put(key, attrTag.getDouble(key));
            }
        }
        return map;
    }

    /**
     * Reads a magic attribute map from an item.
     * The given attributeTag is the key under which the compound is stored (e.g., MAGIC_DAMAGE_FLAT).
     */
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

    /**
     * Applies (writes) a magic attribute to an item.
     * This is essentially the same as writeMagicAttribute.
     */
    public static void applyMagicAttribute(ItemStack item, String attributeTag, int magicId, double value) {
        writeMagicAttribute(item, attributeTag, magicId, value);
    }

    /**
     * Writes a magic attribute value to the given attributeTag.
     */
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

    /**
     * Removes a magic attribute value from the given attributeTag.
     */
    public static void removeMagicAttribute(ItemStack item, String attributeTag, int magicId) {
        if (item == null || item.stackTagCompound == null)
            return;
        NBTTagCompound root = item.stackTagCompound;
        if (root.hasKey(attributeTag)) {
            NBTTagCompound magicMap = root.getCompoundTag(attributeTag);
            magicMap.removeTag(String.valueOf(magicId));
            // If the magic map is empty, remove the tag.
            if (magicMap.func_150296_c().isEmpty())
                root.removeTag(attributeTag);
        }
    }

    @SideOnly(Side.CLIENT)
    public static List<String> getToolTip(List<String> original, NBTTagCompound compound) {
        List<String> tooltip = original;
        NBTTagCompound attrTag = compound.getCompoundTag(RPGItemAttributes);
        if (Keyboard.isKeyDown(ClientProxy.NPCButton.getKeyCode())) {
            List<String> newTooltips = new ArrayList<>();
            if (!tooltip.isEmpty()) {
                newTooltips.add((String) tooltip.get(0));
                newTooltips.add("");
            }

            // Create lists for each section
            List<String> baseList = new ArrayList<>();
            List<String> modifierList = new ArrayList<>();
            List<String> infoList = new ArrayList<>();
            List<String> extraList = new ArrayList<>();

            Iterator iterator = attrTag.func_150296_c().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                double value = attrTag.getDouble(key);
                AttributeDefinition def = AttributeController.getAttribute(key);
                // Default section is EXTRA if not defined
                AttributeDefinition.AttributeSection section = def != null ? def.getSection() : AttributeDefinition.AttributeSection.EXTRA;
                String line;
                String translation = null;
                if (key.contains(":")) {
                    if (StatCollector.canTranslate(key)) {
                        translation = StatCollector.translateToLocal(key);
                    }
                } else {
                    if (StatCollector.canTranslate("rpgcore:attribute." + key)) {
                        translation = StatCollector.translateToLocal("rpgcore:attribute." + key);
                    }
                }
                if (translation == null && def != null) {
                    translation = def.getDisplayName();
                } else if (translation == null) {
                    translation = key;
                }
                translation = EnumChatFormatting.AQUA + translation;
                String formattedValue = formatDouble(value);
                if (section == MODIFIER || section == INFO) {
                    String sign = (value >= 0) ? "+" : "";
                    String valueColor = (value >= 0) ? EnumChatFormatting.GREEN + "" : EnumChatFormatting.RED + "";
                    String valueString = valueColor + sign + formattedValue + EnumChatFormatting.GRAY;
                    switch (section) {
                        case MODIFIER:
                            line = valueString + " " + translation;
                            modifierList.add(line);
                            break;
                        case INFO:
                            line = valueString + " " + translation;
                            infoList.add(line);
                            break;
                        default:
                            line = translation + ": " + formattedValue;
                            break;
                    }
                } else {
                    formattedValue = EnumChatFormatting.GRAY + formattedValue;
                    switch (section) {
                        case BASE:
                            line = translation + ": " + formattedValue;
                            baseList.add(line);
                            break;
                        case EXTRA:
                            line = translation + ": " + formattedValue;
                            extraList.add(line);
                            break;
                        default:
                            line = translation + ": " + formattedValue;
                            break;
                    }
                }
            }

            if (!baseList.isEmpty()) {
                newTooltips.add("");
                newTooltips.addAll(baseList);
            }
            if (!modifierList.isEmpty()) {
                newTooltips.add("");
                newTooltips.addAll(modifierList);
            }
            if (!infoList.isEmpty()) {
                newTooltips.add("");
                newTooltips.addAll(infoList);
            }
            if (!extraList.isEmpty()) {
                newTooltips.add("");
                newTooltips.addAll(extraList);
            }
            tooltip = newTooltips;
        } else {
            // Inform the user to hold the NPCButton key for attribute details
            String keyName = Keyboard.getKeyName(ClientProxy.NPCButton.getKeyCode());
            tooltip.add(EnumChatFormatting.YELLOW + "" + EnumChatFormatting.ITALIC +
                StatCollector.translateToLocal("rpgcore:tooltip").replace("%key%", keyName));
        }
        return tooltip;
    }

    /**
     * Helper to format a double by removing trailing zeros.
     */
    private static String formatDouble(double value) {
        return new java.math.BigDecimal(Double.toString(value)).stripTrailingZeros().toPlainString();
    }
}
