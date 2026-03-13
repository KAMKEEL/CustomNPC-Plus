package noppes.npcs.client.gui.util.script.autocomplete.weighter;

import noppes.npcs.client.gui.util.script.autocomplete.AutocompleteItem;

/**
 * Prefer members defined closer in the inheritance tree.
 * Child class members rank above parent class members.
 *
 * Only meaningful for JS/.d.ts types where inheritance depth is tracked.
 * For items without inheritance tracking (depth < 0), this weigher abstains.
 */
public class InheritanceDepthWeigher extends CompletionWeigher {

    public InheritanceDepthWeigher() {
        super("inheritanceDepth");
    }

    @Override
    public Comparable<?> weigh(AutocompleteItem item, ScoringContext context) {
        int depth = item.getInheritanceDepth();
        if (depth < 0) {
            return null;
        }
        return depth;
    }
}
