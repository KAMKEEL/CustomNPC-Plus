package noppes.npcs.client.gui.util.script.autocomplete.weighter;

import noppes.npcs.client.gui.util.script.autocomplete.AutocompleteItem;

public class DeprecatedWeigher extends CompletionWeigher {

    public DeprecatedWeigher() {
        super("deprecated");
    }

    @Override
    public Comparable<?> weigh(AutocompleteItem item, ScoringContext context) {
        return item.isDeprecated();
    }
}
