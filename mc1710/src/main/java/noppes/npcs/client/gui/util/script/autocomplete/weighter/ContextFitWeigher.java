package noppes.npcs.client.gui.util.script.autocomplete.weighter;

import noppes.npcs.client.gui.util.script.autocomplete.AutocompleteItem;

/**
 * How well does this item fit the current access context?
 *
 * Combines what were previously applyStaticPenalty() and applyObjectMethodPenalty().
 * Uses ordered enum categories, eliminating all magic threshold numbers.
 */
public class ContextFitWeigher extends CompletionWeigher {

    public enum ContextFit {
        /** Item naturally belongs in this context. */
        NATURAL,
        /** Static member accessed through an instance — legal but bad style. */
        WRONG_STATIC_CONTEXT,
        /** Inherited Object method (wait, notify, hashCode, getClass, etc.) */
        OBJECT_METHOD
    }

    public ContextFitWeigher() {
        super("contextFit");
    }

    @Override
    public Comparable<?> weigh(AutocompleteItem item, ScoringContext context) {
        if (item.isInheritedObjectMethod()) {
            return ContextFit.OBJECT_METHOD;
        }

        if (context.isMemberAccess && !context.isStaticContext && item.isStatic()) {
            return ContextFit.WRONG_STATIC_CONTEXT;
        }

        return ContextFit.NATURAL;
    }
}
