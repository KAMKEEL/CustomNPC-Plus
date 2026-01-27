package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.AbilityEffect;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * SubGui for editing ability effects (potion effects that apply to targets).
 * Allows adding/removing effects and configuring type, duration, and amplifier.
 */
public class SubGuiAbilityEffects extends SubGuiInterface implements ITextfieldListener {

    private static final int MAX_EFFECTS = 5;

    // Effect type names for display
    private static final String[] EFFECT_TYPE_NAMES = {
        "gui.none",
        "potion.moveSlowdown",
        "potion.weakness",
        "potion.poison",
        "potion.wither",
        "potion.blindness",
        "potion.confusion",
        "potion.hunger",
        "potion.digSlowDown"
    };

    // Working copy of effects
    private List<AbilityEffect> effects;
    private boolean cancelled = false;

    public SubGuiAbilityEffects(List<AbilityEffect> existingEffects) {
        // Create working copies of effects
        this.effects = new ArrayList<>();
        if (existingEffects != null) {
            for (AbilityEffect effect : existingEffects) {
                this.effects.add(effect.copy());
            }
        }

        setBackground("menubg.png");
        xSize = 280;
        ySize = 180;
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 8;
        int labelX = guiLeft + 8;

        // Title
        addLabel(new GuiNpcLabel(0, "ability.effects", labelX, y));
        y += 16;

        // Column headers
        addLabel(new GuiNpcLabel(1, "gui.type", guiLeft + 10, y));
        addLabel(new GuiNpcLabel(2, "gui.duration", guiLeft + 115, y));
        addLabel(new GuiNpcLabel(3, "gui.amp", guiLeft + 175, y));
        y += 14;

        // Effect rows
        for (int i = 0; i < effects.size() && i < MAX_EFFECTS; i++) {
            AbilityEffect effect = effects.get(i);
            int baseId = 1 + i * 10;

            // Type selector button
            int typeIndex = effect.getType().ordinal();
            addButton(new GuiNpcButton(baseId, guiLeft + 8, y, 100, 20, EFFECT_TYPE_NAMES, typeIndex));

            // Duration text field
            GuiNpcTextField durField = new GuiNpcTextField(baseId + 1, this, fontRendererObj,
                guiLeft + 112, y, 50, 20, String.valueOf(effect.getDurationTicks()));
            durField.setIntegersOnly();
            durField.setMinMaxDefault(1, 12000, 60);
            addTextField(durField);

            // Amplifier selector (0-10)
            String[] ampValues = new String[11];
            for (int a = 0; a <= 10; a++) {
                ampValues[a] = String.valueOf(a);
            }
            addButton(new GuiNpcButton(baseId + 2, guiLeft + 168, y, 40, 20, ampValues, effect.getAmplifier()));

            // Delete button
            addButton(new GuiNpcButton(baseId + 3, guiLeft + 215, y, 20, 20, "X"));

            y += 24;
        }

        // Add button (if space available)
        if (effects.size() < MAX_EFFECTS) {
            addButton(new GuiNpcButton(100, guiLeft + 8, y, 50, 20, "gui.add"));
        }

        // Bottom buttons
        addButton(new GuiNpcButton(65, guiLeft + 8, guiTop + ySize - 28, 60, 20, "gui.cancel"));
        addButton(new GuiNpcButton(66, guiLeft + xSize - 68, guiTop + ySize - 28, 60, 20, "gui.done"));
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;

        // Add button
        if (id == 100) {
            if (effects.size() < MAX_EFFECTS) {
                // Add new effect with default values (Slowness, 60 ticks, amp 0)
                effects.add(new AbilityEffect(AbilityEffect.EffectType.SLOWNESS, 60, 0));
                initGui();
            }
            return;
        }

        // Cancel button
        if (id == 65) {
            cancelled = true;
            close();
            return;
        }

        // Done button
        if (id == 66) {
            // Remove any NONE effects before saving
            effects.removeIf(e -> !e.isValid());
            cancelled = false;
            close();
            return;
        }

        // Effect row buttons (id >= 1 and < 100)
        if (id >= 1 && id < 100) {
            int effectIndex = (id - 1) / 10;
            int action = (id - 1) % 10;

            if (effectIndex >= 0 && effectIndex < effects.size()) {
                AbilityEffect effect = effects.get(effectIndex);

                switch (action) {
                    case 0: // Type selector
                        int typeIndex = ((GuiNpcButton) guibutton).getValue();
                        effect.setType(AbilityEffect.EffectType.fromOrdinal(typeIndex));
                        break;
                    case 2: // Amplifier selector
                        int amp = ((GuiNpcButton) guibutton).getValue();
                        effect.setAmplifier(amp);
                        break;
                    case 3: // Delete button
                        effects.remove(effectIndex);
                        initGui();
                        break;
                }
            }
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textField) {
        int id = textField.id;

        // Duration text fields (baseId + 1)
        if (id >= 2 && id < 100) {
            int effectIndex = (id - 1) / 10;
            int action = (id - 1) % 10;

            if (action == 1 && effectIndex >= 0 && effectIndex < effects.size()) {
                effects.get(effectIndex).setDurationTicks(textField.getInteger());
            }
        }
    }

    /**
     * Returns the resulting effects list after the dialog closes.
     * Returns null if cancelled.
     */
    public List<AbilityEffect> getResult() {
        if (cancelled) {
            return null;
        }
        return effects;
    }

    /**
     * Returns true if the dialog was cancelled.
     */
    public boolean wasCancelled() {
        return cancelled;
    }
}
