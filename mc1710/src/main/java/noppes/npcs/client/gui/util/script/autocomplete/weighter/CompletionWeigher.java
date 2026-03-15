package noppes.npcs.client.gui.util.script.autocomplete.weighter;

import noppes.npcs.client.gui.util.script.autocomplete.AutocompleteItem;

/**
 * A single scoring dimension for autocomplete ranking.
 *
 * Weighers are evaluated in chain order. For each pair of items being compared,
 * the first weigher to return non-equal values determines the ordering.
 *
 * Contract:
 * - weigh() MUST be a pure function (no side effects, no mutations)
 * - weigh() MUST return consistent results for the same item+context
 * - weigh() MAY return null to abstain (treated as equal for this weigher)
 * - Lower Comparable values = higher priority (shown first) unless negated
 *
 * Modeled after IntelliJ's LookupElementWeigher (Apache 2.0).
 */
public abstract class CompletionWeigher {

    private final String id;
    private final boolean negated;

    protected CompletionWeigher(String id, boolean negated) {
        this.id = id;
        this.negated = negated;
    }

    protected CompletionWeigher(String id) {
        this(id, false);
    }

    public abstract Comparable<?> weigh(AutocompleteItem item, ScoringContext context);

    public String getId() { return id; }
    public boolean isNegated() { return negated; }

    @Override
    public String toString() { return id; }
}
