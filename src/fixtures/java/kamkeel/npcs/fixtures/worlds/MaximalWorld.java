package kamkeel.npcs.fixtures.worlds;

import kamkeel.npcs.fixtures.Case;
import kamkeel.npcs.fixtures.FixtureWorld;
import kamkeel.npcs.fixtures.WorldWriter;
import kamkeel.npcs.fixtures.content.DialogFixtures;
import kamkeel.npcs.fixtures.content.FactionFixtures;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.Faction;

import java.util.List;

/**
 * Every field CustomNPC+ can store, moved off its default at least once.
 *
 * <p>The world a reader is measured against. If a field is written by CustomNPC+ and absent from
 * here, that is a gap in the fixtures rather than a decision.
 */
public final class MaximalWorld implements FixtureWorld {

    public String name() {
        return "maximal";
    }

    public String purpose() {
        return "every storable field moved off its default at least once, with each enum walked to "
            + "all of its constants. The world a reader is measured against.";
    }

    public void build(WorldWriter out) {
        List<Faction> factions = FactionFixtures.maximal();
        out.dat("faction", "factions.dat", FactionFixtures.document(factions, 17),
            factions.size() + " factions: the three CustomNPC+ defaults, each boolean flag alone "
                + "and all together, a hostility graph, inverted thresholds and negative points");

        for (Case<Dialog> c : DialogFixtures.maximal()) {
            out.dialog(c.category, c.value, c.note);
        }
    }
}
