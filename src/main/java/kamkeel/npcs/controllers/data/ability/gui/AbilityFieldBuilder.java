package kamkeel.npcs.controllers.data.ability.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.AbilityEffect;
import net.minecraft.client.gui.FontRenderer;
import noppes.npcs.client.gui.builder.FieldDef;
import noppes.npcs.client.gui.builder.FieldType;
import noppes.npcs.client.gui.builder.GuiFieldBuilder;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ability-specific extension of {@link GuiFieldBuilder} that handles
 * the EFFECTS_LIST field type.
 */
@SideOnly(Side.CLIENT)
public class AbilityFieldBuilder extends GuiFieldBuilder {

    // Effects list metadata: widgetId -> [effectIndex, action]
    // action: 0=type, 1=duration(text), 2=amp, 3=delete, 4=add
    private final Map<Integer, int[]> effectWidgetMeta = new HashMap<>();

    public AbilityFieldBuilder(GuiNPCInterface parent, FontRenderer fontRenderer) {
        super(parent, fontRenderer);
    }

    @Override
    protected int build(List<FieldDef> fields) {
        effectWidgetMeta.clear();
        return super.build(fields);
    }

    @Override
    protected int buildField(FieldDef def, int y, List<FieldDef> fields, int index) {
        if (def.getType() == FieldType.EFFECTS_LIST) {
            return renderEffectsList(def, y);
        }
        return -1; // not handled
    }

    @SuppressWarnings("unchecked")
    private int renderEffectsList(FieldDef def, int y) {
        // Section header
        y += 3;
        sw.addLabel(new GuiNpcLabel(labelId++, def.getLabel(), colLLabel, y + 2, 0xFFFF55));
        y += 15;

        List<AbilityEffect> effects = (List<AbilityEffect>) def.getValue();
        if (effects == null) effects = new ArrayList<>();

        String[] typeNames = AbilityEffect.EffectType.getLangKeys();
        String[] ampValues = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

        for (int e = 0; e < effects.size() && e < 5; e++) {
            AbilityEffect effect = effects.get(e);

            // Type selector button
            int typeIdx = effect.getType().ordinal();
            GuiNpcButton typeBtn = new GuiNpcButton(widgetId, colLLabel, y, 100, 20, typeNames, typeIdx);
            sw.addButton(typeBtn);
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

            // Amplifier selector (0-10)
            GuiNpcButton ampBtn = new GuiNpcButton(widgetId, colLLabel + 158, y, 40, 20, ampValues, effect.getAmplifier());
            sw.addButton(ampBtn);
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

        // Add button (if < 5 effects)
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

    public Map<Integer, int[]> getEffectWidgetMeta() { return effectWidgetMeta; }
}
