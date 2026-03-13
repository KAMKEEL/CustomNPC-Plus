package noppes.npcs.client.gui.util.script.autocomplete.weighter;

import noppes.npcs.client.gui.util.script.autocomplete.AutocompleteItem;

import java.util.*;

/**
 * An ordered list of weighers that produces a Comparator for sorting autocomplete items.
 *
 * Items are compared dimension-by-dimension through the chain.
 * The first weigher to produce a non-equal comparison determines the ordering.
 * If all weighers return equal, items are considered equivalent (stable sort preserves
 * insertion order).
 *
 * Simplified from IntelliJ's CompletionSorterImpl — uses Comparator instead of a
 * classifier tree since we sort all items in a single batch.
 */
public class WeigherChain {

    private final List<CompletionWeigher> weighers;

    public WeigherChain(List<CompletionWeigher> weighers) {
        this.weighers = Collections.unmodifiableList(new ArrayList<>(weighers));
    }

    /**
     * Build a Comparator that evaluates items through the weigher chain.
     *
     * Weigher results are cached per item via IdentityHashMap to avoid
     * recomputing weights during O(N log N) comparisons.
     */
    public Comparator<AutocompleteItem> buildComparator(ScoringContext context) {
        final Map<AutocompleteItem, Comparable<?>[]> cache = new IdentityHashMap<>();
        final int chainLength = weighers.size();

        return (a, b) -> {
            Comparable<?>[] weightsA = cache.computeIfAbsent(a, k -> computeWeights(k, context));
            Comparable<?>[] weightsB = cache.computeIfAbsent(b, k -> computeWeights(k, context));

            for (int i = 0; i < chainLength; i++) {
                Comparable<?> wA = weightsA[i];
                Comparable<?> wB = weightsB[i];

                if (wA == null && wB == null) continue;
                if (wA == null) return 1;
                if (wB == null) return -1;

                @SuppressWarnings("unchecked")
                int cmp = ((Comparable<Object>) wA).compareTo(wB);

                if (cmp != 0) {
                    return weighers.get(i).isNegated() ? -cmp : cmp;
                }
            }
            return 0;
        };
    }

    private Comparable<?>[] computeWeights(AutocompleteItem item, ScoringContext context) {
        Comparable<?>[] weights = new Comparable<?>[weighers.size()];
        for (int i = 0; i < weighers.size(); i++) {
            weights[i] = weighers.get(i).weigh(item, context);
        }
        return weights;
    }

    /**
     * Get all weigher results for an item (for debugging/testing).
     */
    public Map<String, Comparable<?>> debugWeights(AutocompleteItem item, ScoringContext context) {
        Map<String, Comparable<?>> result = new LinkedHashMap<>();
        for (CompletionWeigher weigher : weighers) {
            result.put(weigher.getId(), weigher.weigh(item, context));
        }
        return result;
    }

    public List<CompletionWeigher> getWeighers() {
        return weighers;
    }
}
