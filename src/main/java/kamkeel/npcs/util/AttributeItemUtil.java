package kamkeel.npcs.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.CustomAttributes;
import kamkeel.npcs.controllers.AttributeController;
import kamkeel.npcs.controllers.data.attribute.AttributeDefinition;
import kamkeel.npcs.controllers.data.attribute.AttributeValueType;
import kamkeel.npcs.controllers.data.attribute.requirement.IRequirementChecker;
import kamkeel.npcs.controllers.data.attribute.requirement.RequirementCheckerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.controllers.MagicController;
import noppes.npcs.controllers.data.Magic;
import org.lwjgl.input.Keyboard;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Provides helper functions to write/read item attributes to/from NBT.
 * All data is now stored under a hierarchical structure:
 * <p>
 * "RPGCore"
 * ├─ "Attributes"
 * ├─ "Magic"
 * └─ "Requirements"
 */
public class AttributeItemUtil {
    // New hierarchical keys.
    public static final String TAG_RPGCORE = "RPGCore";
    public static final String TAG_ATTRIBUTES = "Attributes";
    public static final String TAG_MAGIC = "Magic";
    public static final String TAG_REQUIREMENTS = "Requirements";

    /**
     * Applies a non–magic attribute to an item.
     */
    public static void applyAttribute(ItemStack item, String attributeKey, float value) {
        if (item == null) return;
        if (item.stackTagCompound == null) {
            item.stackTagCompound = new NBTTagCompound();
        }
        NBTTagCompound root = item.stackTagCompound;
        // Get (or create) the RPGCore compound.
        NBTTagCompound rpgCore = root.hasKey(TAG_RPGCORE) ? root.getCompoundTag(TAG_RPGCORE) : new NBTTagCompound();
        // Get (or create) the Attributes compound.
        NBTTagCompound attrTag = rpgCore.hasKey(TAG_ATTRIBUTES) ? rpgCore.getCompoundTag(TAG_ATTRIBUTES) : new NBTTagCompound();
        attrTag.setFloat(attributeKey, value);
        rpgCore.setTag(TAG_ATTRIBUTES, attrTag);
        root.setTag(TAG_RPGCORE, rpgCore);
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
        if (root.hasKey(TAG_RPGCORE)) {
            NBTTagCompound rpgCore = root.getCompoundTag(TAG_RPGCORE);
            if (rpgCore.hasKey(TAG_ATTRIBUTES)) {
                NBTTagCompound attrTag = rpgCore.getCompoundTag(TAG_ATTRIBUTES);
                attrTag.removeTag(attributeKey);
                if (attrTag.func_150296_c().isEmpty()) {
                    rpgCore.removeTag(TAG_ATTRIBUTES);
                } else {
                    rpgCore.setTag(TAG_ATTRIBUTES, attrTag);
                }
                root.setTag(TAG_RPGCORE, rpgCore);
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
        if (root.hasKey(TAG_RPGCORE)) {
            NBTTagCompound rpgCore = root.getCompoundTag(TAG_RPGCORE);
            if (rpgCore.hasKey(TAG_ATTRIBUTES)) {
                NBTTagCompound attrTag = rpgCore.getCompoundTag(TAG_ATTRIBUTES);
                Set<String> keys = attrTag.func_150296_c();
                for (String key : keys) {
                    map.put(key, attrTag.getFloat(key));
                }
            }
        }
        return map;
    }

    /**
     * Reads a magic attribute map from an item.
     * The given attributeTag is the key under which the compound is stored (e.g., MAGIC_DAMAGE_KEY).
     */
    public static Map<Integer, Float> readMagicAttributeMap(ItemStack item, String attributeTag) {
        Map<Integer, Float> map = new HashMap<>();
        if (item == null || item.stackTagCompound == null) return map;
        NBTTagCompound root = item.stackTagCompound;
        if (root.hasKey(TAG_RPGCORE)) {
            NBTTagCompound rpgCore = root.getCompoundTag(TAG_RPGCORE);
            if (rpgCore.hasKey(TAG_MAGIC)) {
                NBTTagCompound magicCompound = rpgCore.getCompoundTag(TAG_MAGIC);
                if (magicCompound.hasKey(attributeTag)) {
                    NBTTagCompound magicMap = magicCompound.getCompoundTag(attributeTag);
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
            }
        }
        return map;
    }

    /**
     * Applies (writes) a magic attribute to an item.
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
        NBTTagCompound root = item.stackTagCompound;
        // Get or create RPGCore compound.
        NBTTagCompound rpgCore = root.hasKey(TAG_RPGCORE) ? root.getCompoundTag(TAG_RPGCORE) : new NBTTagCompound();
        // Get or create Magic compound.
        NBTTagCompound magicCompound = rpgCore.hasKey(TAG_MAGIC) ? rpgCore.getCompoundTag(TAG_MAGIC) : new NBTTagCompound();
        // Get or create the specific magic map.
        NBTTagCompound magicMap = magicCompound.hasKey(attributeTag) ? magicCompound.getCompoundTag(attributeTag) : new NBTTagCompound();
        magicMap.setFloat(String.valueOf(magicId), value);
        magicCompound.setTag(attributeTag, magicMap);
        rpgCore.setTag(TAG_MAGIC, magicCompound);
        root.setTag(TAG_RPGCORE, rpgCore);
    }

    /**
     * Removes a magic attribute value from the given attributeTag.
     */
    public static void removeMagicAttribute(ItemStack item, String attributeTag, int magicId) {
        if (item == null || item.stackTagCompound == null)
            return;
        NBTTagCompound root = item.stackTagCompound;
        if (root.hasKey(TAG_RPGCORE)) {
            NBTTagCompound rpgCore = root.getCompoundTag(TAG_RPGCORE);
            if (rpgCore.hasKey(TAG_MAGIC)) {
                NBTTagCompound magicCompound = rpgCore.getCompoundTag(TAG_MAGIC);
                if (magicCompound.hasKey(attributeTag)) {
                    NBTTagCompound magicMap = magicCompound.getCompoundTag(attributeTag);
                    magicMap.removeTag(String.valueOf(magicId));
                    if (magicMap.func_150296_c().isEmpty())
                        magicCompound.removeTag(attributeTag);
                    else
                        magicCompound.setTag(attributeTag, magicMap);
                    rpgCore.setTag(TAG_MAGIC, magicCompound);
                    root.setTag(TAG_RPGCORE, rpgCore);
                }
            }
        }
    }


    public static void applyRequirement(ItemStack item, String reqKey, Object value) {
        if (item == null) return;
        if (item.stackTagCompound == null)
            item.stackTagCompound = new NBTTagCompound();
        NBTTagCompound root = item.stackTagCompound;
        // Get (or create) the RPGCore compound.
        NBTTagCompound rpgCore = root.hasKey(TAG_RPGCORE) ? root.getCompoundTag(TAG_RPGCORE) : new NBTTagCompound();
        // Get (or create) the Requirements compound.
        NBTTagCompound reqTag = rpgCore.hasKey(TAG_REQUIREMENTS) ? rpgCore.getCompoundTag(TAG_REQUIREMENTS) : new NBTTagCompound();
        // Retrieve the checker from the registry.
        IRequirementChecker checker = RequirementCheckerRegistry.getChecker(reqKey);
        if (checker != null) {
            checker.apply(reqTag, value);
        }
        rpgCore.setTag(TAG_REQUIREMENTS, reqTag);
        root.setTag(TAG_RPGCORE, rpgCore);
    }

    public static void removeRequirement(ItemStack item, String reqKey) {
        if (item == null || item.stackTagCompound == null) return;
        NBTTagCompound root = item.stackTagCompound;
        if (root.hasKey(TAG_RPGCORE)) {
            NBTTagCompound rpgCore = root.getCompoundTag(TAG_RPGCORE);
            if (rpgCore.hasKey(TAG_REQUIREMENTS)) {
                NBTTagCompound reqTag = rpgCore.getCompoundTag(TAG_REQUIREMENTS);
                reqTag.removeTag(reqKey);
                if (reqTag.func_150296_c().isEmpty()) {
                    rpgCore.removeTag(TAG_REQUIREMENTS);
                } else {
                    rpgCore.setTag(TAG_REQUIREMENTS, reqTag);
                }
                root.setTag(TAG_RPGCORE, rpgCore);
            }
        }
    }

    // ----------------- Tooltip Generation with Custom Sorting -------------------

    /**
     * Returns the item tooltip.
     */
    @SideOnly(Side.CLIENT)
    public static List<String> getToolTip(List<String> original, NBTTagCompound compound) {
        List<String> tooltip = new ArrayList<>(original);
        // Get the RPGCore compound and then the Attributes compound.
        NBTTagCompound rpgCore = compound.hasKey(TAG_RPGCORE) ? compound.getCompoundTag(TAG_RPGCORE) : new NBTTagCompound();
        NBTTagCompound attrTag = rpgCore.hasKey(TAG_ATTRIBUTES) ? rpgCore.getCompoundTag(TAG_ATTRIBUTES) : new NBTTagCompound();
        if (Keyboard.isKeyDown(ClientProxy.NPCButton.getKeyCode())) {
            List<String> newTooltips = new ArrayList<>();
            if (!tooltip.isEmpty()) {
                newTooltips.add(tooltip.get(0));
            }

            // Instead of storing plain strings, we wrap each line in a TooltipEntry
            List<TooltipEntry> baseList = new ArrayList<>();
            List<TooltipEntry> modifierList = new ArrayList<>();
            List<TooltipEntry> statsList = new ArrayList<>();
            List<TooltipEntry> infoList = new ArrayList<>();
            List<TooltipEntry> extraList = new ArrayList<>();

            // Process non–magic attributes.
            Set<String> keys = attrTag.func_150296_c();
            for (String key : keys) {
                // For non–magic we include everything.
                Float value = attrTag.getFloat(key);
                AttributeDefinition def = AttributeController.getAttribute(key);
                if(def == null)
                    continue;

                AttributeDefinition.AttributeSection section = def != null ? def.getSection() : AttributeDefinition.AttributeSection.EXTRA;
                String plainName = getTranslatedAttributeName(key, def); // unformatted name
                String formattedLine = formatAttributeLine(def, section, value, plainName);
                TooltipEntry entry = new TooltipEntry(plainName, formattedLine);
                switch (section) {
                    case BASE:
                        baseList.add(entry);
                        break;
                    case MODIFIER:
                        modifierList.add(entry);
                        break;
                    case STATS:
                        statsList.add(entry);
                        break;
                    case INFO:
                        infoList.add(entry);
                        break;
                    default:
                        extraList.add(entry);
                        break;
                }
            }

            // Process magic attributes.
            processMagicAttributes(compound, baseList, modifierList, infoList, extraList);

            // Define custom order maps for Base and Modifier sections.
            // For Base: Health, Main Attack Damage, Neutral Damage come first.
            Map<String, Integer> baseOrder = new HashMap<>();
            baseOrder.put("Health", 1);
            baseOrder.put("Main Attack Damage", 2);
            baseOrder.put("Neutral Damage", 3);

            // For Modifier: Main Attack Damage then Neutral Damage.
            Map<String, Integer> modOrder = new HashMap<>();
            modOrder.put("Health Boost", 1);
            modOrder.put("Main Attack Damage", 2);
            modOrder.put("Neutral Damage", 3);
            modOrder.put("Movement Speed", 4);
            modOrder.put("Knockback Resistance", 5);

            // Build sections using our custom sorting.
            newTooltips.addAll(buildSection(baseList, baseOrder));
            newTooltips.addAll(buildSection(modifierList, modOrder));
            newTooltips.addAll(buildSection(statsList)); // alphabetical
            newTooltips.addAll(buildSection(infoList));  // alphabetical
            newTooltips.addAll(buildSection(extraList)); // alphabetical

            if (rpgCore.hasKey(TAG_REQUIREMENTS)) {
                NBTTagCompound reqTag = rpgCore.getCompoundTag(TAG_REQUIREMENTS);
                List<TooltipEntry> reqEntries = new ArrayList<>();

                Minecraft mc = Minecraft.getMinecraft();
                EntityPlayer clientPlayer = mc.thePlayer;
                Set<String> requirements = reqTag.func_150296_c();
                for (String reqKey : requirements) {
                    IRequirementChecker checker = RequirementCheckerRegistry.getChecker(reqKey);
                    if (checker != null) {
                        boolean met = clientPlayer != null && checker.check(clientPlayer, reqTag);
                        String tooltipValue = checker.getTooltipValue(reqTag);
                        String color = met ? EnumChatFormatting.GRAY.toString() : EnumChatFormatting.RED.toString();
                        String line = EnumChatFormatting.GRAY + StatCollector.translateToLocal(checker.getTranslation()) + ": " + color + tooltipValue;
                        reqEntries.add(new TooltipEntry(stripFormatting(StatCollector.translateToLocal(checker.getTranslation())), line));
                    }
                }
                newTooltips.addAll(buildSection(reqEntries));
            }

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
     * For each magic key, it retrieves the stored magic attributes from the "Magic" compound,
     * formats each line and adds it to the proper section list.
     */
    private static void processMagicAttributes(NBTTagCompound compound, List<TooltipEntry> baseList, List<TooltipEntry> modifierList,
                                               List<TooltipEntry> infoList, List<TooltipEntry> extraList) {
        if (!compound.hasKey(TAG_RPGCORE)) return;
        NBTTagCompound rpgCore = compound.getCompoundTag(TAG_RPGCORE);
        if (!rpgCore.hasKey(TAG_MAGIC)) return;
        NBTTagCompound magicCompound = rpgCore.getCompoundTag(TAG_MAGIC);
        String[] magicKeys = {
            CustomAttributes.MAGIC_DAMAGE_KEY,
            CustomAttributes.MAGIC_BOOST_KEY,
            CustomAttributes.MAGIC_DEFENSE_KEY,
            CustomAttributes.MAGIC_RESISTANCE_KEY
        };
        for (String magicKey : magicKeys) {
            if (magicCompound.hasKey(magicKey)) {
                NBTTagCompound magicTag = magicCompound.getCompoundTag(magicKey);
                AttributeDefinition def = AttributeController.getAttribute(magicKey);
                AttributeDefinition.AttributeSection section = def != null ? def.getSection() : AttributeDefinition.AttributeSection.EXTRA;
                Set<String> keys = magicTag.func_150296_c();
                for (String key : keys) {
                    try {
                        int magicId = Integer.parseInt(key);
                        Float value = magicTag.getFloat(key);
                        Magic magic = MagicController.getInstance().getMagic(magicId);
                        if (magic != null) {
                            // Build the magic display name without formatting for sorting.
                            String rawMagicName = magic.getDisplayName().replace("&", "\u00A7") + " \u00A77" + getMagicAppendix(magicKey);
                            String plainName = stripFormatting(rawMagicName);
                            String formattedLine = formatAttributeLine(def, section, value, rawMagicName);
                            TooltipEntry entry = new TooltipEntry(plainName, formattedLine);
                            switch (section) {
                                case BASE:
                                    baseList.add(entry);
                                    break;
                                case MODIFIER:
                                    modifierList.add(entry);
                                    break;
                                case INFO:
                                    infoList.add(entry);
                                    break;
                                default:
                                    extraList.add(entry);
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
    private static String getMagicAppendix(String type) {
        switch (type) {
            case CustomAttributes.MAGIC_DEFENSE_KEY:
                return StatCollector.translateToLocal("rpgcore:attribute.defense");
            case CustomAttributes.MAGIC_RESISTANCE_KEY:
                return StatCollector.translateToLocal("rpgcore:attribute.resistance");
            default:
                return StatCollector.translateToLocal("rpgcore:attribute.damage");
        }
    }

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
            if (def != null && (def.getValueType() == AttributeValueType.PERCENT || def.getValueType() == AttributeValueType.MAGIC))
                valueString += "%";
            valueString += EnumChatFormatting.GRAY;
            return valueString + " " + displayName;
        } else {
            if (def != null)
                displayName = "\u00A7" + def.getColorCode() + displayName;
            else
                displayName = EnumChatFormatting.AQUA + displayName;

            String sign = value >= 0 ? "+" : "";
            String color = value >= 0 ? EnumChatFormatting.GREEN.toString() : EnumChatFormatting.RED.toString();
            String valueString = color + sign + formattedValue;
            return displayName + "\u00A77: " + valueString;
        }
    }

    private static String formatFloat(Float value) {
        return new java.math.BigDecimal(Float.toString(value)).stripTrailingZeros().toPlainString();
    }

    // ---------------- Helper Methods for Sorting Tooltip Entries ----------------

    // A small container to hold a tooltip line and its plain sort key.
    private static class TooltipEntry {
        public String sortKey;
        public String line;

        public TooltipEntry(String sortKey, String line) {
            this.sortKey = sortKey;
            this.line = line;
        }
    }

    // Removes formatting codes (e.g., '§') from a string.
    private static String stripFormatting(String input) {
        return input == null ? "" : Pattern.compile("(?i)§[0-9A-FK-OR]").matcher(input).replaceAll("");
    }

    // Build a section with default alphabetical order.
    private static List<String> buildSection(List<TooltipEntry> entries) {
        Collections.sort(entries, (a, b) -> a.sortKey.compareToIgnoreCase(b.sortKey));
        List<String> section = new ArrayList<>();
        if (!entries.isEmpty()) {
            section.add("");
            for (TooltipEntry entry : entries) {
                section.add(entry.line);
            }
        }
        return section;
    }

    // Build a section using a custom order map.
    private static List<String> buildSection(List<TooltipEntry> entries, Map<String, Integer> orderMap) {
        Collections.sort(entries, (a, b) -> {
            int pa = orderMap.containsKey(a.sortKey) ? orderMap.get(a.sortKey) : Integer.MAX_VALUE;
            int pb = orderMap.containsKey(b.sortKey) ? orderMap.get(b.sortKey) : Integer.MAX_VALUE;
            if (pa != pb) return Integer.compare(pa, pb);
            return a.sortKey.compareToIgnoreCase(b.sortKey);
        });
        List<String> section = new ArrayList<>();
        if (!entries.isEmpty()) {
            section.add("");
            for (TooltipEntry entry : entries) {
                section.add(entry.line);
            }
        }
        return section;
    }
}
