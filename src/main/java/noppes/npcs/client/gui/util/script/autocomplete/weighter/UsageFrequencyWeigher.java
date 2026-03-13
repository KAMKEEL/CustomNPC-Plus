package noppes.npcs.client.gui.util.script.autocomplete.weighter;

import noppes.npcs.client.gui.util.script.autocomplete.AutocompleteItem;
import noppes.npcs.client.gui.util.script.autocomplete.UsageTracker;

/**
 * Boost items the user has selected before.
 * Uses logarithmic scaling with a cap via UsageTracker.calculateUsageBoost().
 * Negated: higher usage count = higher priority.
 */
public class UsageFrequencyWeigher extends CompletionWeigher {

    public UsageFrequencyWeigher() {
        super("usageFrequency", true);
    }

    @Override
    public Comparable<?> weigh(AutocompleteItem item, ScoringContext context) {
        if (context.usageTracker == null) {
            return 0;
        }
        int usageCount = context.usageTracker.getUsageCount(item, context.ownerFullName);
        return UsageTracker.calculateUsageBoost(usageCount);
    }
}
