package noppes.npcs.client.gui.util.script.autocomplete.weighter;

import noppes.npcs.client.gui.util.script.autocomplete.UsageTracker;

/**
 * Immutable context for a single scoring pass.
 * Created once when suggestions are requested, shared by all weighers.
 */
public class ScoringContext {

    public final String prefix;
    public final boolean isMemberAccess;
    public final boolean isStaticContext;
    public final String ownerFullName;
    public final UsageTracker usageTracker;
    public final boolean requirePrefix;

    public ScoringContext(String prefix, boolean isMemberAccess, boolean isStaticContext,
                          String ownerFullName, UsageTracker usageTracker, boolean requirePrefix) {
        this.prefix = prefix;
        this.isMemberAccess = isMemberAccess;
        this.isStaticContext = isStaticContext;
        this.ownerFullName = ownerFullName;
        this.usageTracker = usageTracker;
        this.requirePrefix = requirePrefix;
    }
}
