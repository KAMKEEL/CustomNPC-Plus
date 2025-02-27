package noppes.npcs.attribute;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.controllers.MagicController;
import noppes.npcs.controllers.data.Magic;
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
        List<String> tooltip = new ArrayList<>(original);
        NBTTagCompound attrTag = compound.getCompoundTag(RPGItemAttributes);
        if (Keyboard.isKeyDown(ClientProxy.NPCButton.getKeyCode())) {
            List<String> newTooltips = new ArrayList<>();
            if (!tooltip.isEmpty()) {
                newTooltips.add(tooltip.get(0));
                newTooltips.add("");
            }

            // Lists for non–magic attributes
            List<String> baseList = new ArrayList<>();
            List<String> modifierList = new ArrayList<>();
            List<String> infoList = new ArrayList<>();
            List<String> extraList = new ArrayList<>();

            // Process non–magic attributes (order maintained)
            Iterator<String> iter = attrTag.func_150296_c().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                // Skip magic attribute keys; they are handled separately.
                if(key.equals(ModAttributes.MAGIC_DAMAGE_KEY) || key.equals(ModAttributes.MAGIC_BOOST_KEY)
                    || key.equals(ModAttributes.MAGIC_DEFENSE_KEY) || key.equals(ModAttributes.MAGIC_RESISTANCE_KEY)) {
                    continue;
                }
                double value = attrTag.getDouble(key);
                AttributeDefinition def = AttributeController.getAttribute(key);
                AttributeDefinition.AttributeSection section = def != null ? def.getSection() : AttributeDefinition.AttributeSection.EXTRA;
                String displayName = getTranslatedAttributeName(key, def);
                String line = formatAttributeLine(def, section, value, displayName);
                switch (section) {
                    case BASE:
                        baseList.add(line);
                        break;
                    case MODIFIER:
                        modifierList.add(line);
                        break;
                    case INFO:
                        infoList.add(line);
                        break;
                    default:
                        extraList.add(line);
                        break;
                }
            }

            // Process magic attributes and merge them into the proper lists.
            processMagicAttributes(compound, baseList, modifierList, infoList, extraList);

            // Append sections (adds an empty line before each if not empty)
            newTooltips.addAll(buildSection(baseList));
            newTooltips.addAll(buildSection(modifierList));
            newTooltips.addAll(buildSection(infoList));
            newTooltips.addAll(buildSection(extraList));

            tooltip = newTooltips;
        } else {
            String keyName = Keyboard.getKeyName(ClientProxy.NPCButton.getKeyCode());
            tooltip.add(EnumChatFormatting.YELLOW + "" + EnumChatFormatting.ITALIC +
                StatCollector.translateToLocal("rpgcore:tooltip").replace("%key%", keyName));
        }
        return tooltip;
    }

    /**
     * Processes magic attributes from the compound.
     * For each magic key (from ModAttributes), it retrieves the stored magic attributes,
     * formats each line (using the magic's display name) and adds it to the proper section list.
     */
    private static void processMagicAttributes(NBTTagCompound compound, List<String> baseList, List<String> modifierList,
                                               List<String> infoList, List<String> extraList) {
        String[] magicKeys = {
            ModAttributes.MAGIC_DAMAGE_KEY,
            ModAttributes.MAGIC_BOOST_KEY,
            ModAttributes.MAGIC_DEFENSE_KEY,
            ModAttributes.MAGIC_RESISTANCE_KEY
        };
        for (String magicKey : magicKeys) {
            if (compound.hasKey(magicKey)) {
                NBTTagCompound magicTag = compound.getCompoundTag(magicKey);
                // Use the attribute definition for this magic key to determine its section.
                AttributeDefinition def = AttributeController.getAttribute(magicKey);
                AttributeDefinition.AttributeSection section = def != null ? def.getSection() : AttributeDefinition.AttributeSection.EXTRA;
                Iterator<String> magicIter = magicTag.func_150296_c().iterator();
                while (magicIter.hasNext()) {
                    String key = magicIter.next();
                    try {
                        int magicId = Integer.parseInt(key);
                        double value = magicTag.getDouble(key);
                        Magic magic = MagicController.getInstance().getMagic(magicId);
                        if (magic != null) {
                            String magicDisplayName = magic.getDisplayName();
                            // Format using the magic attribute's definition and the magic's display name.
                            String line = formatAttributeLine(def, section, value, magicDisplayName);
                            switch (section) {
                                case BASE:
                                    baseList.add(line);
                                    break;
                                case MODIFIER:
                                    modifierList.add(line);
                                    break;
                                case INFO:
                                    infoList.add(line);
                                    break;
                                default:
                                    extraList.add(line);
                                    break;
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Skip invalid key.
                    }
                }
            }
        }
    }

    /**
     * Returns a translated attribute name.
     */
    private static String getTranslatedAttributeName(String key, AttributeDefinition def) {
        String translation = null;
        if (key.contains(":")) {
            if (StatCollector.canTranslate(key))
                translation = StatCollector.translateToLocal(key);
        } else {
            if (StatCollector.canTranslate("rpgcore:attribute." + key))
                translation = StatCollector.translateToLocal("rpgcore:attribute." + key);
        }
        if (translation == null && def != null)
            translation = def.getDisplayName();
        else if (translation == null)
            translation = key;
        return translation;
    }

    /**
     * Formats the attribute line based on its section and value type.
     */
    private static String formatAttributeLine(AttributeDefinition def, AttributeDefinition.AttributeSection section,
                                              double value, String displayName) {
        String formattedValue = formatDouble(value);
        if (section == AttributeDefinition.AttributeSection.MODIFIER || section == AttributeDefinition.AttributeSection.INFO) {
            String sign = value >= 0 ? "+" : "";
            String color = value >= 0 ? EnumChatFormatting.GREEN.toString() : EnumChatFormatting.RED.toString();
            String valueString = color + sign + formattedValue + EnumChatFormatting.GRAY;
            if (def != null && def.getValueType() == AttributeValueType.PERCENT)
                valueString += "%";
            return valueString + " " + displayName;
        } else {
            if (def != null)
                displayName = "\u00A7" + def.getColorCode() + displayName;
            else
                displayName = EnumChatFormatting.AQUA + displayName;
            formattedValue = EnumChatFormatting.GRAY + formattedValue;
            return displayName + "\u00A77: " + formattedValue;
        }
    }

    /**
     * Builds a section from a list of tooltip lines.
     * Adds an empty line before the section if it contains lines.
     */
    private static List<String> buildSection(List<String> lines) {
        List<String> section = new ArrayList<>();
        if (!lines.isEmpty()) {
            section.add("");
            section.addAll(lines);
        }
        return section;
    }

    /**
     * Helper to format a double by removing trailing zeros.
     */
    private static String formatDouble(double value) {
        return new java.math.BigDecimal(Double.toString(value)).stripTrailingZeros().toPlainString();
    }
}
