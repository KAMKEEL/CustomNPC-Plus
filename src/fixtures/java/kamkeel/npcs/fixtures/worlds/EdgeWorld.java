package kamkeel.npcs.fixtures.worlds;

import kamkeel.npcs.fixtures.FixtureWorld;
import kamkeel.npcs.fixtures.WorldWriter;
import kamkeel.npcs.fixtures.content.FactionFixtures;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.Faction;

import java.util.List;

/**
 * Legal content that readers get wrong.
 *
 * <p>Nothing here is corrupt. Every file is something CustomNPC+ will write and load: empty
 * strings, integer extremes, ids that resolve nowhere, names outside Latin-1, and the same id
 * living in two categories. The last is the one worth having -- it is what makes "a dialog's
 * identity is its id" wrong on its own.
 */
public final class EdgeWorld implements FixtureWorld {

    public String name() {
        return "edge";
    }

    public String purpose() {
        return "legal but awkward content: empty strings, integer extremes, dangling ids, non-Latin "
            + "names, and one dialog id occupying two categories at once.";
    }

    public void build(WorldWriter out) {
        List<Faction> factions = FactionFixtures.edge();
        out.dat("faction", "factions.dat", FactionFixtures.document(factions, 4096),
            "empty and non-Latin names, Integer.MIN_VALUE/MAX_VALUE thresholds, colour -1, "
                + "attack lists naming factions that are not in the file, and a sparse id of 4096");

        // The same id in two categories. DialogController loads categories directory by
        // directory, and on the second occurrence renames the file and reassigns the id -- so
        // this world does not survive a load unchanged, which is exactly the behaviour worth
        // pinning. Whichever category is read second is decided by File.listFiles order.
        Dialog first = new Dialog();
        first.id = 1;
        first.title = "Start";
        first.text = "I am dialog 1 in category A.";

        Dialog second = new Dialog();
        second.id = 1;
        second.title = "Start";
        second.text = "I am dialog 1 in category B.";

        out.dialog("Category A", first,
            "dialog id 1 in the first category. See the note on its twin");
        out.dialog("Category B", second,
            "dialog id 1 AGAIN, in a second category, with the same title. CustomNPC+ resolves "
                + "this on load by renaming the file and reassigning the id, so a converter that "
                + "keys on the id alone silently merges two distinct dialogs. Which one is "
                + "reassigned depends on File.listFiles order and is not fixed");

        Dialog emptyEverything = new Dialog();
        emptyEverything.id = 2;
        emptyEverything.title = "";
        emptyEverything.text = "";
        emptyEverything.command = "";
        emptyEverything.textSound = "";
        out.dialog("Empty", emptyEverything,
            "every string field empty, including TextSound, which has a non-empty default. "
                + "DialogSound is absent entirely because the writer omits it when empty");

        Dialog extremes = new Dialog();
        extremes.id = 3;
        extremes.title = "Integer Extremes";
        extremes.text = "Every numeric field at a boundary.";
        extremes.quest = Integer.MAX_VALUE;
        extremes.color = Integer.MIN_VALUE;
        extremes.titleColor = -1;
        extremes.textWidth = Integer.MAX_VALUE;
        extremes.textHeight = Integer.MIN_VALUE;
        extremes.textOffsetX = Integer.MIN_VALUE;
        extremes.textOffsetY = Integer.MAX_VALUE;
        extremes.npcScale = Float.MAX_VALUE;
        extremes.textPitch = Float.MIN_VALUE;
        extremes.alignment = Byte.MAX_VALUE;
        out.dialog("Empty", extremes,
            "integer and float fields at their type boundaries, and DialogAlignment at 127 -- "
                + "outside the 0-2 the field documents, because nothing validates it on write");
    }
}
