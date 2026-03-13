package noppes.npcs.client.gui.util.script.autocomplete.weighter;

import noppes.npcs.client.gui.util.script.autocomplete.AutocompleteItem;

public class AlphabeticalWeigher extends CompletionWeigher {

    public AlphabeticalWeigher() {
        super("alphabetical");
    }

    @Override
    public Comparable<?> weigh(AutocompleteItem item, ScoringContext context) {
        return item.getName().toLowerCase();
    }
}
