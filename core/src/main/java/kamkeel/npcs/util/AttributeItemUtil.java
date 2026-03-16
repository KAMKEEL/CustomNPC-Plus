package kamkeel.npcs.util;

import noppes.npcs.constants.ClientOnly;
import kamkeel.npcs.util.IVector3;
import kamkeel.npcs.controllers.data.ability.preview.PreviewEntityHandler;
import kamkeel.npcs.controllers.data.ability.gui.SubGuiAbilityConfig;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityConfigCallback;
import kamkeel.npcs.controllers.data.ability.gui.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.IChainedAbilityFieldProvider;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityFieldProvider;
import noppes.npcs.entity.EntityNPCInterface;
import kamkeel.npcs.entity.EntityEnergyDome;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import kamkeel.npcs.entity.EntityEnergyPanel;
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.entity.EntityAbilityLaser;
import kamkeel.npcs.entity.EntityAbilityDisc;
import kamkeel.npcs.entity.EntityAbilityBeam;
import kamkeel.npcs.entity.EntityEnergyProjectile;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import kamkeel.npcs.CustomAttributes;
import kamkeel.npcs.controllers.AttributeController;
import kamkeel.npcs.controllers.data.attribute.AttributeDefinition;
import kamkeel.npcs.controllers.data.attribute.AttributeValueType;
import kamkeel.npcs.controllers.data.attribute.requirement.IRequirementChecker;
import kamkeel.npcs.controllers.data.attribute.requirement.RequirementCheckerRegistry;
import noppes.npcs.controllers.MagicController;
import noppes.npcs.controllers.data.Magic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public static void applyAttribute(IItemStack item, String attributeKey, float value) {
        if (item == null) return;
        if (item.stackTagCompound == null) {
            item.stackTagCompound = new INbt();
        }
        INbt root = item.stackTagCompound;
        // Get (or create) the RPGCore compound.
        INbt rpgCore = root.hasKey(TAG_RPGCORE) ? root.getCompoundTag(TAG_RPGCORE) : new INbt();
        // Get (or create) the Attributes compound.
        INbt attrTag = rpgCore.hasKey(TAG_ATTRIBUTES) ? rpgCore.getCompoundTag(TAG_ATTRIBUTES) : new INbt();
        attrTag.setFloat(attributeKey, value);
        rpgCore.setTag(TAG_ATTRIBUTES, attrTag);
        root.setTag(TAG_RPGCORE, rpgCore);
    }

    public static void applyAttribute(IItemStack item, AttributeDefinition definition, float value) {
        applyAttribute(item, definition.getKey(), value);
    }

    /**
     * Removes a non–magic attribute from an item.
     */
    public static void removeAttribute(IItemStack item, String attributeKey) {
        if (item == null || item.stackTagCompound == null) return;
        INbt root = item.stackTagCompound;
        if (root.hasKey(TAG_RPGCORE)) {
            INbt rpgCore = root.getCompoundTag(TAG_RPGCORE);
            if (rpgCore.hasKey(TAG_ATTRIBUTES)) {
                INbt attrTag = rpgCore.getCompoundTag(TAG_ATTRIBUTES);
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
    public static Map<String, Float> readAttributes(IItemStack item) {
        Map<String, Float> map = new HashMap<>();
        if (item == null || item.stackTagCompound == null) return map;
        INbt root = item.stackTagCompound;
        if (root.hasKey(TAG_RPGCORE)) {
            INbt rpgCore = root.getCompoundTag(TAG_RPGCORE);
            if (rpgCore.hasKey(TAG_ATTRIBUTES)) {
                INbt attrTag = rpgCore.getCompoundTag(TAG_ATTRIBUTES);
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
    public static Map<Integer, Float> readMagicAttributeMap(IItemStack item, String attributeTag) {
        Map<Integer, Float> map = new HashMap<>();
        if (item == null || item.stackTagCompound == null) return map;
        INbt root = item.stackTagCompound;
        if (root.hasKey(TAG_RPGCORE)) {
            INbt rpgCore = root.getCompoundTag(TAG_RPGCORE);
            if (rpgCore.hasKey(TAG_MAGIC)) {
                INbt magicCompound = rpgCore.getCompoundTag(TAG_MAGIC);
                if (magicCompound.hasKey(attributeTag)) {
                    INbt magicMap = magicCompound.getCompoundTag(attributeTag);
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
    public static void applyMagicAttribute(IItemStack item, String attributeTag, int magicId, float value) {
        writeMagicAttribute(item, attributeTag, magicId, value);
    }

    /**
     * Writes a magic attribute value to the given attributeTag.
     */
    public static void writeMagicAttribute(IItemStack item, String attributeTag, int magicId, float value) {
        if (item == null) return;
        if (item.stackTagCompound == null)
            item.stackTagCompound = new INbt();
        INbt root = item.stackTagCompound;
        // Get or create RPGCore compound.
        INbt rpgCore = root.hasKey(TAG_RPGCORE) ? root.getCompoundTag(TAG_RPGCORE) : new INbt();
        // Get or create Magic compound.
        INbt magicCompound = rpgCore.hasKey(TAG_MAGIC) ? rpgCore.getCompoundTag(TAG_MAGIC) : new INbt();
        // Get or create the specific magic map.
        INbt magicMap = magicCompound.hasKey(attributeTag) ? magicCompound.getCompoundTag(attributeTag) : new INbt();
        magicMap.setFloat(String.valueOf(magicId), value);
        magicCompound.setTag(attributeTag, magicMap);
        rpgCore.setTag(TAG_MAGIC, magicCompound);
        root.setTag(TAG_RPGCORE, rpgCore);
    }

    /**
     * Removes a magic attribute value from the given attributeTag.
     */
    public static void removeMagicAttribute(IItemStack item, String attributeTag, int magicId) {
        if (item == null || item.stackTagCompound == null)
            return;
        INbt root = item.stackTagCompound;
        if (root.hasKey(TAG_RPGCORE)) {
            INbt rpgCore = root.getCompoundTag(TAG_RPGCORE);
            if (rpgCore.hasKey(TAG_MAGIC)) {
                INbt magicCompound = rpgCore.getCompoundTag(TAG_MAGIC);
                if (magicCompound.hasKey(attributeTag)) {
                    INbt magicMap = magicCompound.getCompoundTag(attributeTag);
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


    public static void applyRequirement(IItemStack item, String reqKey, Object value) {
        if (item == null) return;
        if (item.stackTagCompound == null)
            item.stackTagCompound = new INbt();
        INbt root = item.stackTagCompound;
        // Get (or create) the RPGCore compound.
        INbt rpgCore = root.hasKey(TAG_RPGCORE) ? root.getCompoundTag(TAG_RPGCORE) : new INbt();
        // Get (or create) the Requirements compound.
        INbt reqTag = rpgCore.hasKey(TAG_REQUIREMENTS) ? rpgCore.getCompoundTag(TAG_REQUIREMENTS) : new INbt();
        // Retrieve the checker from the registry.
        IRequirementChecker checker = RequirementCheckerRegistry.getChecker(reqKey);
        if (checker != null) {
            checker.apply(reqTag, value);
        }
        rpgCore.setTag(TAG_REQUIREMENTS, reqTag);
        root.setTag(TAG_RPGCORE, rpgCore);
    }

    public static void removeRequirement(IItemStack item, String reqKey) {
        if (item == null || item.stackTagCompound == null) return;
        INbt root = item.stackTagCompound;
        if (root.hasKey(TAG_RPGCORE)) {
            INbt rpgCore = root.getCompoundTag(TAG_RPGCORE);
            if (rpgCore.hasKey(TAG_REQUIREMENTS)) {
                INbt reqTag = rpgCore.getCompoundTag(TAG_REQUIREMENTS);
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
    @ClientOnly
    public static List<String> getToolTip(List<String> original, INbt compound, boolean override) {
        List<String> tooltip = new ArrayList<>(original);
        // Get the RPGCore compound and then the Attributes compound.
        INbt rpgCore = compound.hasKey(TAG_RPGCORE) ? compound.getCompoundTag(TAG_RPGCORE) : new INbt();
        INbt attrTag = rpgCore.hasKey(TAG_ATTRIBUTES) ? rpgCore.getCompoundTag(TAG_ATTRIBUTES) : new INbt();
        if (KeyPressHandler.isKeyBindDown(ClientProxy.NPCButton) || override) {
            List<String> newTooltips = new ArrayList<>();
            if (!tooltip.isEmpty() && !override) {
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
                if (def == null)
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
                INbt reqTag = rpgCore.getCompoundTag(TAG_REQUIREMENTS);
                List<TooltipEntry> reqEntries = new ArrayList<>();

                Minecraft mc = Minecraft.getMinecraft();
                IPlayer clientPlayer = mc.thePlayer;
                Set<String> requirements = reqTag.func_150296_c();
                for (String reqKey : requirements) {
                    IRequirementChecker checker = RequirementCheckerRegistry.getChecker(reqKey);
                    if (checker != null) {
                        boolean met = clientPlayer != null && checker.check(clientPlayer, reqTag);
                        String tooltipValue = checker.getTooltipValue(reqTag);
                        String color = met ? TextFormatting.GRAY.toString() : TextFormatting.RED.toString();
                        String line = TextFormatting.GRAY + PlatformServiceHolder.get().translateToLocal(checker.getTranslation()) + ": " + color + tooltipValue;
                        reqEntries.add(new TooltipEntry(stripFormatting(PlatformServiceHolder.get().translateToLocal(checker.getTranslation())), line));
                    }
                }
                newTooltips.addAll(buildSection(reqEntries));
            }

            if(override){
                tooltip.addAll(newTooltips);
            } else {
                tooltip = newTooltips;
            }
        } else {
            String keyName = GameSettings.getKeyDisplayString(ClientProxy.NPCButton.getKeyCode());
            tooltip.add(TextFormatting.YELLOW + "" + TextFormatting.ITALIC +
                PlatformServiceHolder.get().translateToLocal("rpgcore:tooltip").replace("%key%", keyName));
        }
        return tooltip;
    }

    /**
     * Processes magic attributes from the compound.
     * For each magic key, it retrieves the stored magic attributes from the "Magic" compound,
     * formats each line and adds it to the proper section list.
     */
    private static void processMagicAttributes(INbt compound, List<TooltipEntry> baseList, List<TooltipEntry> modifierList,
                                               List<TooltipEntry> infoList, List<TooltipEntry> extraList) {
        if (!compound.hasKey(TAG_RPGCORE)) return;
        INbt rpgCore = compound.getCompoundTag(TAG_RPGCORE);
        if (!rpgCore.hasKey(TAG_MAGIC)) return;
        INbt magicCompound = rpgCore.getCompoundTag(TAG_MAGIC);
        String[] magicKeys = {
            CustomAttributes.MAGIC_DAMAGE_KEY,
            CustomAttributes.MAGIC_BOOST_KEY,
            CustomAttributes.MAGIC_DEFENSE_KEY,
            CustomAttributes.MAGIC_RESISTANCE_KEY
        };
        for (String magicKey : magicKeys) {
            if (magicCompound.hasKey(magicKey)) {
                INbt magicTag = magicCompound.getCompoundTag(magicKey);
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

    @ClientOnly
    private static String getMagicAppendix(String type) {
        switch (type) {
            case CustomAttributes.MAGIC_DEFENSE_KEY:
                return PlatformServiceHolder.get().translateToLocal("rpgcore:attribute.defense");
            case CustomAttributes.MAGIC_RESISTANCE_KEY:
                return PlatformServiceHolder.get().translateToLocal("rpgcore:attribute.resistance");
            default:
                return PlatformServiceHolder.get().translateToLocal("rpgcore:attribute.damage");
        }
    }

    @ClientOnly
    private static String getTranslatedAttributeName(String key, AttributeDefinition def) {
        key = def != null ? def.getTranslationKey() : key;
        String translation = null;
        if (StatCollector.canTranslate(key))
            translation = PlatformServiceHolder.get().translateToLocal(key);
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
            String color = value >= 0 ? TextFormatting.GREEN.toString() : TextFormatting.RED.toString();
            String valueString = color + sign + formattedValue;
            if (def != null && def.getValueType() == AttributeValueType.PERCENT)
                valueString += "%";
            valueString += TextFormatting.GRAY;
            if (def != null)
                displayName = "\u00A7" + def.getColorCode() + displayName;
            else
                displayName = TextFormatting.AQUA + displayName;
            return valueString + " " + displayName;
        } else if (section == AttributeDefinition.AttributeSection.MODIFIER || section == AttributeDefinition.AttributeSection.INFO) {
            String sign = value >= 0 ? "+" : "";
            String color = value >= 0 ? TextFormatting.GREEN.toString() : TextFormatting.RED.toString();
            String valueString = color + sign + formattedValue;
            if (def != null && (def.getValueType() == AttributeValueType.PERCENT || def.getValueType() == AttributeValueType.MAGIC))
                valueString += "%";
            valueString += TextFormatting.GRAY;
            return valueString + " " + displayName;
        } else {
            if (def != null)
                displayName = "\u00A7" + def.getColorCode() + displayName;
            else
                displayName = TextFormatting.AQUA + displayName;

            String sign = value >= 0 ? "+" : "";
            String color = value >= 0 ? TextFormatting.GREEN.toString() : TextFormatting.RED.toString();
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
