package kamkeel.npcs.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.AttributeController;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import kamkeel.npcs.controllers.data.attribute.AttributeDefinition;
import kamkeel.npcs.controllers.data.attribute.AttributeValueType;
import kamkeel.npcs.CustomAttributes;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.controllers.MagicController;
import noppes.npcs.controllers.data.Magic;
import org.lwjgl.input.Keyboard;

import java.util.*;

/**
 * Provides helper functions to write/read item attributes to/from NBT.
 * Non–magic attributes are stored under a compound tag (TAG_ROOT),
 * and magic attributes are stored as a compound mapping (by magic ID) under specific keys.
 */
public class AttributeItemUtil {
    public static final String RPGItemAttributes = "RPGCoreAttributes";

    /**
     * Applies a non–magic attribute to an item.
     */
    public static void applyAttribute(ItemStack item, String attributeKey, float value) {
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
        attrTag.setFloat(attributeKey, value);
        root.setTag(RPGItemAttributes, attrTag);
    }

    public static void applyAttribute(ItemStack item, AttributeDefinition definition, float value) {
        applyAttribute(item, definition.getKey(), value);
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
    public static Map<String, Float> readAttributes(ItemStack item) {
        Map<String, Float> map = new HashMap<>();
        if (item == null || item.stackTagCompound == null) return map;
        NBTTagCompound root = item.stackTagCompound;
        if (root.hasKey(RPGItemAttributes)) {
            NBTTagCompound attrTag = root.getCompoundTag(RPGItemAttributes);
            Set<String> keys = attrTag.func_150296_c();
            for (String key : keys) {
                map.put(key, attrTag.getFloat(key));
            }
        }
        return map;
    }

    /**
     * Reads a magic attribute map from an item.
     * The given attributeTag is the key under which the compound is stored (e.g., MAGIC_DAMAGE_FLAT).
     */
    public static Map<Integer, Float> readMagicAttributeMap(ItemStack item, String attributeTag) {
        Map<Integer, Float> map = new HashMap<>();
        if (item == null || item.stackTagCompound == null) return map;
        if (item.stackTagCompound.hasKey(attributeTag)) {
            NBTTagCompound magicMap = item.stackTagCompound.getCompoundTag(attributeTag);
            Set<String> keys = magicMap.func_150296_c();
            for (String key : keys) {
                try {
                    int magicId = Integer.parseInt(key);
                    map.put(magicId, magicMap.getFloat(key));
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
    public static void applyMagicAttribute(ItemStack item, String attributeTag, int magicId, float value) {
        writeMagicAttribute(item, attributeTag, magicId, value);
    }

    /**
     * Writes a magic attribute value to the given attributeTag.
     */
    public static void writeMagicAttribute(ItemStack item, String attributeTag, int magicId, float value) {
        if (item == null) return;
        if (item.stackTagCompound == null)
            item.stackTagCompound = new NBTTagCompound();
        NBTTagCompound magicMap;
        if (item.stackTagCompound.hasKey(attributeTag)) {
            magicMap = item.stackTagCompound.getCompoundTag(attributeTag);
        } else {
            magicMap = new NBTTagCompound();
        }
        magicMap.setFloat(String.valueOf(magicId), value);
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
            }

            // Lists for non–magic attributes
            List<String> baseList = new ArrayList<>();
            List<String> modifierList = new ArrayList<>();
            List<String> statsList = new ArrayList<>();
            List<String> infoList = new ArrayList<>();
            List<String> extraList = new ArrayList<>();

            // Process non–magic attributes (order maintained)
            Iterator<String> iter = attrTag.func_150296_c().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                // Skip magic attribute keys; they are handled separately.
                if(key.equals(CustomAttributes.MAGIC_DAMAGE_KEY) || key.equals(CustomAttributes.MAGIC_BOOST_KEY)
                    || key.equals(CustomAttributes.MAGIC_DEFENSE_KEY) || key.equals(CustomAttributes.MAGIC_RESISTANCE_KEY)) {
                    continue;
                }
                Float value = attrTag.getFloat(key);
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
                    case STATS:
                        statsList.add(line);
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
            newTooltips.addAll(buildSection(statsList));
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
            CustomAttributes.MAGIC_DAMAGE_KEY,
            CustomAttributes.MAGIC_BOOST_KEY,
            CustomAttributes.MAGIC_DEFENSE_KEY,
            CustomAttributes.MAGIC_RESISTANCE_KEY
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
                        Float value = magicTag.getFloat(key);
                        Magic magic = MagicController.getInstance().getMagic(magicId);
                        if (magic != null) {
                            String magicDisplayName = magic.getDisplayName() + " " + EnumChatFormatting.GRAY + getMagicAppendix(magicKey);
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

    @SideOnly(Side.CLIENT)
    private static String getMagicAppendix(String type){
        switch(type){
            case CustomAttributes.MAGIC_DEFENSE_KEY:
                return StatCollector.translateToLocal("rpgcore:attribute.defense");
            case CustomAttributes.MAGIC_RESISTANCE_KEY:
                return StatCollector.translateToLocal("rpgcore:attribute.resistance");
            default:
                return StatCollector.translateToLocal("rpgcore:attribute.damage");
        }
    }

    /**
     * Returns a translated attribute name.
     */
    @SideOnly(Side.CLIENT)
    private static String getTranslatedAttributeName(String key, AttributeDefinition def) {
        key = def != null ? def.getTranslationKey() : key;

        String translation = null;
        if (StatCollector.canTranslate(key))
            translation = StatCollector.translateToLocal(key);

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
                                              Float value, String displayName) {
        String formattedValue = formatFloat(value);
        if (section == AttributeDefinition.AttributeSection.STATS) {
            String sign = value >= 0 ? "+" : "";
            String color = value >= 0 ? EnumChatFormatting.GREEN.toString() : EnumChatFormatting.RED.toString();
            String valueString = color + sign + formattedValue;
            if (def != null && def.getValueType() == AttributeValueType.PERCENT)
                valueString += "%";
            valueString += EnumChatFormatting.GRAY;
            if (def != null)
                displayName = "\u00A7" + def.getColorCode() + displayName;
            else
                displayName = EnumChatFormatting.AQUA + displayName;
            return valueString + " " + displayName;
        } else if (section == AttributeDefinition.AttributeSection.MODIFIER || section == AttributeDefinition.AttributeSection.INFO) {
            String sign = value >= 0 ? "+" : "";
            String color = value >= 0 ? EnumChatFormatting.GREEN.toString() : EnumChatFormatting.RED.toString();
            String valueString = color + sign + formattedValue;
            if (def != null && def.getValueType() == AttributeValueType.PERCENT)
                valueString += "%";
            valueString += EnumChatFormatting.GRAY;
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
     * Helper to format a Float by removing trailing zeros.
     */
    private static String formatFloat(Float value) {
        return new java.math.BigDecimal(Float.toString(value)).stripTrailingZeros().toPlainString();
    }
}
