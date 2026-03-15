package noppes.npcs.client.gui.util.script.autocomplete.weighter;

import noppes.npcs.client.gui.util.script.autocomplete.AutocompleteItem;

/**
 * Demote unimported classes below imported ones.
 *
 * Fixes the bug where the unimported class penalty (-500) was applied at
 * creation time and overwritten by calculateMatchScore(). In the chain model,
 * import status is a separate dimension that can never be overwritten.
 */
public class ImportStatusWeigher extends CompletionWeigher {

    public enum ImportStatus {
        IMPORTED,
        UNIMPORTED
    }

    public ImportStatusWeigher() {
        super("importStatus");
    }

    @Override
    public Comparable<?> weigh(AutocompleteItem item, ScoringContext context) {
        return item.requiresImport() ? ImportStatus.UNIMPORTED : ImportStatus.IMPORTED;
    }
}
