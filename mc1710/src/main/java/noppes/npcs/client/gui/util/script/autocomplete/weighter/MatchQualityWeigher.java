package noppes.npcs.client.gui.util.script.autocomplete.weighter;

import noppes.npcs.client.gui.util.script.autocomplete.AutocompleteItem;

/**
 * Text match quality against the user's prefix.
 * PRIMARY sort dimension — how well the item name matches what was typed.
 *
 * Returns the existing calculateMatchScore() result, negated so that
 * higher scores = higher priority.
 */
public class MatchQualityWeigher extends CompletionWeigher {

    public MatchQualityWeigher() {
        super("matchQuality", true);
    }

    @Override
    public Comparable<?> weigh(AutocompleteItem item, ScoringContext context) {
        return item.getMatchScore();
    }
}
