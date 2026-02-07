package kamkeel.npcs.controllers.data.ability;

import java.util.function.Consumer;

/**
 * Represents a variant/template for an ability type.
 * When a user creates an ability that has registered variants,
 * they are presented with a selection dialog before the ability is created.
 *
 * Each variant has a display name (lang key) and a configurator that
 * applies preset values to the newly created ability.
 */
public class AbilityVariant {

    private final String displayKey;
    private final Consumer<Ability> configurator;

    /**
     * @param displayKey   Lang key for the variant display name (e.g., "ability.variant.dual")
     * @param configurator Lambda that configures the ability with preset values
     */
    public AbilityVariant(String displayKey, Consumer<Ability> configurator) {
        this.displayKey = displayKey;
        this.configurator = configurator;
    }

    public String getDisplayKey() {
        return displayKey;
    }

    /**
     * Apply this variant's preset configuration to the given ability.
     */
    public void apply(Ability ability) {
        if (configurator != null) {
            configurator.accept(ability);
        }
    }
}
