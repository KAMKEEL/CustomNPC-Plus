package kamkeel.npcs.controllers.data.ability.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.AbilityCustomEffect;
import kamkeel.npcs.controllers.data.ability.AbilityEffectActionEntry;
import kamkeel.npcs.controllers.data.ability.AbilityPotionEffect;
import kamkeel.npcs.controllers.data.ability.IEffectAction;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.advanced.SubGuiCustomEffectSelect;
import noppes.npcs.client.gui.builder.FieldDef;
import noppes.npcs.client.gui.builder.FieldType;
import noppes.npcs.client.gui.builder.GuiFieldBuilder;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.constants.EnumPotionType;
import noppes.npcs.controllers.CustomEffectController;
import noppes.npcs.controllers.data.CustomEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Ability-specific extension of {@link GuiFieldBuilder} that handles
 * the EFFECTS_LIST, CUSTOM_EFFECTS_LIST, and EFFECT_ACTIONS_LIST field types.
 */
@SideOnly(Side.CLIENT)
public class AbilityFieldBuilder extends GuiFieldBuilder {

    // Unified metadata: widgetId -> [index, action]
    // EFFECTS_LIST actions:        0=type, 1=duration(text), 2=amp, 3=delete, 4=add, 5=manualId(text)
    // CUSTOM_EFFECTS_LIST actions:  0=effectName, 1=duration(text), 2=level, 3=delete, 4=add
    // EFFECT_ACTIONS_LIST actions:  0=actionSelector, 3=delete, 4=add
    private final Map<Integer, int[]> effectWidgetMeta = new HashMap<>();

    // Cached sorted custom effect data for GUI selectors
    private int[] sortedCustomEffectIds;
    private String[] sortedCustomEffectNames;

    // Cached effect action data for GUI selectors
    private String[] effectActionIds;
    private String[] effectActionDisplayNames;

    public AbilityFieldBuilder(GuiNPCInterface parent, FontRenderer fontRenderer) {
        super(parent, fontRenderer);
    }

    @Override
    protected int build(List<FieldDef> fields) {
        effectWidgetMeta.clear();
        sortedCustomEffectIds = null;
        sortedCustomEffectNames = null;
        effectActionIds = null;
        effectActionDisplayNames = null;
        return super.build(fields);
    }

    @Override
    protected int buildField(FieldDef def, int y, List<FieldDef> fields, int index) {
        if (def.getType() == FieldType.EFFECTS_LIST) {
            return renderEffectsList(def, y);
        }
        if (def.getType() == FieldType.CUSTOM_EFFECTS_LIST) {
            return renderCustomEffectsList(def, y);
        }
        if (def.getType() == FieldType.EFFECT_ACTIONS_LIST) {
            return renderEffectActionsList(def, y);
        }
        return -1; // not handled
    }

    // ═══════════════════════════════════════════════════════════════════
    // POTION EFFECTS LIST (existing)
    // ═══════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private int renderEffectsList(FieldDef def, int y) {
        y += 3;
        sw.addLabel(new GuiNpcLabel(labelId++, def.getLabel(), colLLabel, y + 2, 0xFFFF55));
        y += 15;

        List<AbilityPotionEffect> effects = (List<AbilityPotionEffect>) def.getValue();
        if (effects == null) effects = new ArrayList<>();

        String[] typeNames = EnumPotionType.getLangKeysNoNone();
        String[] ampValues = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

        for (int e = 0; e < effects.size() && e < 5; e++) {
            AbilityPotionEffect effect = effects.get(e);

            int typeIdx = effect.getType().ordinal() - 1;
            GuiNpcButton typeBtn = new GuiNpcButton(widgetId, colLLabel, y, 100, 20, typeNames, Math.max(0, typeIdx));
            sw.addButton(typeBtn);
            buttonFieldMap.put(widgetId, def);
            effectWidgetMeta.put(widgetId, new int[]{e, 0});
            widgetId++;

            GuiNpcTextField durField = new GuiNpcTextField(widgetId, parent, fontRenderer,
                colLLabel + 104, y, 50, 20, String.valueOf(effect.getDurationTicks()));
            durField.setIntegersOnly();
            durField.setMinMaxDefault(1, 12000, 60);
            sw.addTextField(durField);
            textFieldMap.put(widgetId, def);
            effectWidgetMeta.put(widgetId, new int[]{e, 1});
            widgetId++;

            if (effect.getType() != EnumPotionType.Fire) {
                GuiNpcButton ampBtn = new GuiNpcButton(widgetId, colLLabel + 158, y, 40, 20, ampValues, effect.getAmplifier());
                sw.addButton(ampBtn);
                buttonFieldMap.put(widgetId, def);
                effectWidgetMeta.put(widgetId, new int[]{e, 2});
                widgetId++;
            } else {
                widgetId++;
            }

            GuiNpcButton delBtn = new GuiNpcButton(clearId, colLLabel + 202, y, 20, 20, "X");
            sw.addButton(delBtn);
            clearFieldMap.put(clearId, def);
            effectWidgetMeta.put(clearId, new int[]{e, 3});
            clearId++;

            y += rowHeight;

            if (effect.getType() == EnumPotionType.Manual) {
                sw.addLabel(new GuiNpcLabel(labelId++, "effect.potionid", colLLabel, y + 5, 0xFFFFFF));
                GuiNpcTextField idField = new GuiNpcTextField(widgetId, parent, fontRenderer,
                    colLLabel + 80, y, 50, 20, String.valueOf(effect.getManualPotionId()));
                idField.setIntegersOnly();
                idField.setMinMaxDefault(0, Integer.MAX_VALUE, 0);
                sw.addTextField(idField);
                textFieldMap.put(widgetId, def);
                effectWidgetMeta.put(widgetId, new int[]{e, 5});
                widgetId++;
                y += rowHeight;
            }
        }

        if (effects.size() < 5) {
            GuiNpcButton addBtn = new GuiNpcButton(widgetId, colLLabel, y, 50, 20, "gui.add");
            sw.addButton(addBtn);
            buttonFieldMap.put(widgetId, def);
            effectWidgetMeta.put(widgetId, new int[]{effects.size(), 4});
            widgetId++;
            clearId++;
            y += rowHeight;
        }

        return y;
    }

    // ═══════════════════════════════════════════════════════════════════
    // CUSTOM EFFECTS LIST
    // ═══════════════════════════════════════════════════════════════════

    private void ensureCustomEffectCache() {
        if (sortedCustomEffectIds != null) return;

        HashMap<Integer, CustomEffect> allEffects = CustomEffectController.getInstance().getCustomEffects();
        // Sort by name for consistent ordering
        TreeMap<String, Integer> nameToId = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (CustomEffect ce : allEffects.values()) {
            if (ce.getName() != null && !ce.getName().isEmpty()) {
                nameToId.put(ce.getName(), ce.id);
            }
        }

        sortedCustomEffectIds = new int[nameToId.size()];
        sortedCustomEffectNames = new String[nameToId.size()];
        int i = 0;
        for (Map.Entry<String, Integer> entry : nameToId.entrySet()) {
            sortedCustomEffectIds[i] = entry.getValue();
            sortedCustomEffectNames[i] = entry.getKey();
            i++;
        }
    }

    private String getCustomEffectName(int effectId, int index) {
        HashMap<Integer, CustomEffect> allEffects = CustomEffectController.getInstance().getEffectMap(index);
        if (allEffects != null) {
            CustomEffect ce = allEffects.get(effectId);
            if (ce != null && ce.getName() != null && !ce.getName().isEmpty()) {
                return ce.getName();
            }
        }
        // Fallback: check index 0
        if (index != 0) {
            return getCustomEffectName(effectId, 0);
        }
        return "?";
    }

    private boolean hasAnyEffects() {
        for (HashMap<Integer, CustomEffect> map : CustomEffectController.getInstance().indexMapper.values()) {
            if (map != null && !map.isEmpty()) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private int renderCustomEffectsList(FieldDef def, int y) {
        ensureCustomEffectCache();

        y += 3;
        sw.addLabel(new GuiNpcLabel(labelId++, def.getLabel(), colLLabel, y + 2, 0xFFFF55));
        y += 15;

        if (!hasAnyEffects()) {
            sw.addLabel(new GuiNpcLabel(labelId++, "ability.noCustomEffects", colLLabel, y + 5, 0x888888));
            y += rowHeight;
            return y;
        }

        List<AbilityCustomEffect> effects = (List<AbilityCustomEffect>) def.getValue();
        if (effects == null) effects = new ArrayList<>();

        String[] levelValues = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

        for (int e = 0; e < effects.size() && e < 5; e++) {
            AbilityCustomEffect effect = effects.get(e);

            // Effect name button — opens SubGui picker
            String effectName = getCustomEffectName(effect.getEffectId(), effect.getIndex());
            GuiNpcButton nameBtn = new GuiNpcButton(widgetId, colLLabel, y, 100, 20, effectName);
            sw.addButton(nameBtn);
            buttonFieldMap.put(widgetId, def);
            effectWidgetMeta.put(widgetId, new int[]{e, 0});
            widgetId++;

            // Duration text field
            GuiNpcTextField durField = new GuiNpcTextField(widgetId, parent, fontRenderer,
                colLLabel + 104, y, 50, 20, String.valueOf(effect.getDurationTicks()));
            durField.setIntegersOnly();
            durField.setMinMaxDefault(1, 12000, 60);
            sw.addTextField(durField);
            textFieldMap.put(widgetId, def);
            effectWidgetMeta.put(widgetId, new int[]{e, 1});
            widgetId++;

            // Level selector (0-10)
            GuiNpcButton lvlBtn = new GuiNpcButton(widgetId, colLLabel + 158, y, 40, 20, levelValues, effect.getLevel());
            sw.addButton(lvlBtn);
            buttonFieldMap.put(widgetId, def);
            effectWidgetMeta.put(widgetId, new int[]{e, 2});
            widgetId++;

            // Delete button
            GuiNpcButton delBtn = new GuiNpcButton(clearId, colLLabel + 202, y, 20, 20, "X");
            sw.addButton(delBtn);
            clearFieldMap.put(clearId, def);
            effectWidgetMeta.put(clearId, new int[]{e, 3});
            clearId++;

            y += rowHeight;
        }

        if (effects.size() < 5) {
            GuiNpcButton addBtn = new GuiNpcButton(widgetId, colLLabel, y, 50, 20, "gui.add");
            sw.addButton(addBtn);
            buttonFieldMap.put(widgetId, def);
            effectWidgetMeta.put(widgetId, new int[]{effects.size(), 4});
            widgetId++;
            clearId++;
            y += rowHeight;
        }

        return y;
    }

    // ═══════════════════════════════════════════════════════════════════
    // EFFECT ACTIONS LIST
    // ═══════════════════════════════════════════════════════════════════

    private void ensureEffectActionCache() {
        if (effectActionIds != null) return;

        String[] ids = AbilityController.Instance.getEffectActionIds();
        effectActionIds = ids;
        effectActionDisplayNames = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            IEffectAction action = AbilityController.Instance.getEffectAction(ids[i]);
            effectActionDisplayNames[i] = action != null ? action.getDisplayName() : ids[i];
        }
    }

    private int findEffectActionIndex(String actionId) {
        ensureEffectActionCache();
        for (int i = 0; i < effectActionIds.length; i++) {
            if (effectActionIds[i].equals(actionId)) return i;
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    private int renderEffectActionsList(FieldDef def, int y) {
        ensureEffectActionCache();

        if (effectActionIds.length == 0) return y;

        y += 3;
        sw.addLabel(new GuiNpcLabel(labelId++, def.getLabel(), colLLabel, y + 2, 0xFFFF55));
        y += 15;

        List<AbilityEffectActionEntry> entries = (List<AbilityEffectActionEntry>) def.getValue();
        if (entries == null) entries = new ArrayList<>();

        for (int e = 0; e < entries.size() && e < 5; e++) {
            AbilityEffectActionEntry entry = entries.get(e);

            // Action selector
            int selectedIdx = Math.max(0, findEffectActionIndex(entry.getActionId()));
            GuiNpcButton actionBtn = new GuiNpcButton(widgetId, colLLabel, y, 180, 20, effectActionDisplayNames, selectedIdx);
            sw.addButton(actionBtn);
            buttonFieldMap.put(widgetId, def);
            effectWidgetMeta.put(widgetId, new int[]{e, 0});
            widgetId++;

            // Delete button
            GuiNpcButton delBtn = new GuiNpcButton(clearId, colLLabel + 202, y, 20, 20, "X");
            sw.addButton(delBtn);
            clearFieldMap.put(clearId, def);
            effectWidgetMeta.put(clearId, new int[]{e, 3});
            clearId++;

            y += rowHeight;
        }

        if (entries.size() < 5) {
            GuiNpcButton addBtn = new GuiNpcButton(widgetId, colLLabel, y, 50, 20, "gui.add");
            sw.addButton(addBtn);
            buttonFieldMap.put(widgetId, def);
            effectWidgetMeta.put(widgetId, new int[]{entries.size(), 4});
            widgetId++;
            clearId++;
            y += rowHeight;
        }

        return y;
    }

    // ═══════════════════════════════════════════════════════════════════
    // EVENT HANDLING OVERRIDES
    // ═══════════════════════════════════════════════════════════════════

    @Override
    @SuppressWarnings("unchecked")
    public boolean handleButtonEvent(int buttonId, GuiButton button) {
        int[] meta = effectWidgetMeta.get(buttonId);
        if (meta != null) {
            FieldDef def = buttonFieldMap.get(buttonId);
            if (def == null) def = clearFieldMap.get(buttonId);
            if (def == null) return false;

            int idx = meta[0];
            int action = meta[1];

            // ── Potion Effects ──
            if (def.getType() == FieldType.EFFECTS_LIST) {
                List<AbilityPotionEffect> effects = (List<AbilityPotionEffect>) def.getValue();
                if (effects == null) return false;
                switch (action) {
                    case 0:
                        if (idx < effects.size())
                            effects.get(idx).setType(EnumPotionType.fromIndexNoNone(((GuiNpcButton) button).getValue()));
                        return true;
                    case 2:
                        if (idx < effects.size())
                            effects.get(idx).setAmplifier(((GuiNpcButton) button).getValue());
                        return true;
                    case 3:
                        if (idx < effects.size()) effects.remove(idx);
                        return true;
                    case 4:
                        if (effects.size() < 5) effects.add(new AbilityPotionEffect(EnumPotionType.Slowness, 60, 0));
                        return true;
                }
            }

            // ── Custom Effects ──
            if (def.getType() == FieldType.CUSTOM_EFFECTS_LIST) {
                List<AbilityCustomEffect> effects = (List<AbilityCustomEffect>) def.getValue();
                if (effects == null) return false;
                ensureCustomEffectCache();
                switch (action) {
                    case 0: // Open effect picker SubGui
                        if (idx < effects.size()) {
                            int currentId = effects.get(idx).getEffectId();
                            int currentIndex = effects.get(idx).getIndex();
                            parent.setSubGuiWithResult(new SubGuiCustomEffectSelect(currentId, currentIndex), sub -> {
                                SubGuiCustomEffectSelect select = (SubGuiCustomEffectSelect) sub;
                                int selectedId = select.getSelectedEffectId();
                                if (selectedId >= 0 && idx < effects.size()) {
                                    effects.get(idx).setEffectId(selectedId);
                                    effects.get(idx).setIndex(select.getSelectedIndex());
                                }
                            });
                        }
                        return true;
                    case 2: // Level changed
                        if (idx < effects.size())
                            effects.get(idx).setLevel((byte) ((GuiNpcButton) button).getValue());
                        return true;
                    case 3: // Delete
                        if (idx < effects.size()) effects.remove(idx);
                        return true;
                    case 4: // Add
                        if (effects.size() < 5) {
                            parent.setSubGuiWithResult(new SubGuiCustomEffectSelect(-1), sub -> {
                                SubGuiCustomEffectSelect select = (SubGuiCustomEffectSelect) sub;
                                int selectedId = select.getSelectedEffectId();
                                if (selectedId >= 0 && effects.size() < 5) {
                                    effects.add(new AbilityCustomEffect(selectedId, 60, (byte) 0, select.getSelectedIndex()));
                                }
                            });
                        }
                        return true;
                }
            }

            // ── Effect Actions ──
            if (def.getType() == FieldType.EFFECT_ACTIONS_LIST) {
                List<AbilityEffectActionEntry> entries = (List<AbilityEffectActionEntry>) def.getValue();
                if (entries == null) return false;
                ensureEffectActionCache();
                switch (action) {
                    case 0: // Action selector changed
                        if (idx < entries.size()) {
                            int btnVal = ((GuiNpcButton) button).getValue();
                            if (btnVal >= 0 && btnVal < effectActionIds.length) {
                                entries.get(idx).setActionId(effectActionIds[btnVal]);
                                IEffectAction ea = AbilityController.Instance.getEffectAction(effectActionIds[btnVal]);
                                if (ea != null) entries.get(idx).setConfig(ea.createDefaultConfig());
                            }
                        }
                        return true;
                    case 3: // Delete
                        if (idx < entries.size()) entries.remove(idx);
                        return true;
                    case 4: // Add
                        if (entries.size() < 5 && effectActionIds.length > 0) {
                            entries.add(new AbilityEffectActionEntry(effectActionIds[0]));
                        }
                        return true;
                }
            }
        }

        return super.handleButtonEvent(buttonId, button);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean handleTextFieldEvent(int textFieldId, GuiNpcTextField field) {
        int[] meta = effectWidgetMeta.get(textFieldId);
        if (meta != null) {
            FieldDef def = textFieldMap.get(textFieldId);
            if (def == null) return false;

            int idx = meta[0];
            int action = meta[1];

            // ── Potion Effects ──
            if (def.getType() == FieldType.EFFECTS_LIST) {
                List<AbilityPotionEffect> effects = (List<AbilityPotionEffect>) def.getValue();
                if (effects != null && idx < effects.size()) {
                    if (action == 1) effects.get(idx).setDurationTicks(field.getInteger());
                    else if (action == 5) effects.get(idx).setManualPotionId(field.getInteger());
                }
                return true;
            }

            // ── Custom Effects ──
            if (def.getType() == FieldType.CUSTOM_EFFECTS_LIST) {
                List<AbilityCustomEffect> effects = (List<AbilityCustomEffect>) def.getValue();
                if (effects != null && idx < effects.size()) {
                    if (action == 1) effects.get(idx).setDurationTicks(field.getInteger());
                }
                return true;
            }
        }

        return super.handleTextFieldEvent(textFieldId, field);
    }

    public Map<Integer, int[]> getEffectWidgetMeta() {
        return effectWidgetMeta;
    }
}
