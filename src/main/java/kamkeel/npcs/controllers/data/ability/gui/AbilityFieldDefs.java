package kamkeel.npcs.controllers.data.ability.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.AbilityCustomEffect;
import kamkeel.npcs.controllers.data.ability.AbilityEffectActionEntry;
import kamkeel.npcs.controllers.data.ability.AbilityPotionEffect;
import noppes.npcs.client.gui.builder.FieldDef;
import noppes.npcs.client.gui.builder.FieldType;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Ability-specific FieldDef factories. These create FieldDef instances
 * with types that require ability-specific rendering (e.g. EFFECTS_LIST).
 */
@SideOnly(Side.CLIENT)
public class AbilityFieldDefs {

    @SuppressWarnings("unchecked")
    public static FieldDef effectsListField(String label, Supplier<List<AbilityPotionEffect>> getter, Consumer<List<AbilityPotionEffect>> setter) {
        return FieldDef.custom(label, FieldType.EFFECTS_LIST,
            () -> getter.get(),
            v -> setter.accept((List<AbilityPotionEffect>) v));
    }

    @SuppressWarnings("unchecked")
    public static FieldDef customEffectsListField(String label, Supplier<List<AbilityCustomEffect>> getter, Consumer<List<AbilityCustomEffect>> setter) {
        return FieldDef.custom(label, FieldType.CUSTOM_EFFECTS_LIST,
            () -> getter.get(),
            v -> setter.accept((List<AbilityCustomEffect>) v));
    }

    @SuppressWarnings("unchecked")
    public static FieldDef effectActionsListField(String label, Supplier<List<AbilityEffectActionEntry>> getter, Consumer<List<AbilityEffectActionEntry>> setter) {
        return FieldDef.custom(label, FieldType.EFFECT_ACTIONS_LIST,
            () -> getter.get(),
            v -> setter.accept((List<AbilityEffectActionEntry>) v));
    }
}
