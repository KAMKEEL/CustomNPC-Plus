package kamkeel.npcs.fixtures.worlds;

import kamkeel.npcs.fixtures.Case;
import kamkeel.npcs.fixtures.FixtureWorld;
import kamkeel.npcs.fixtures.WorldWriter;
import kamkeel.npcs.fixtures.content.DialogFixtures;
import kamkeel.npcs.fixtures.content.EffectFixtures;
import kamkeel.npcs.fixtures.content.FactionFixtures;
import kamkeel.npcs.fixtures.content.PlayerFixtures;
import kamkeel.npcs.fixtures.content.QuestFixtures;
import kamkeel.npcs.fixtures.content.WorldDataFixtures;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.CustomEffect;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.Quest;

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

        for (Case<Quest> c : QuestFixtures.maximal()) {
            out.quest(c.category, c.value, c.note);
        }

        List<Case<CustomEffect>> effects = EffectFixtures.maximal();
        for (Case<CustomEffect> c : effects) {
            out.effect(c.category, c.value, c.note);
        }
        out.effect("", EffectFixtures.MISMATCH_FILENAME, EffectFixtures.nameMismatch(),
            "the name key inside the file says 'Name Inside The File' while the file is called '"
                + EffectFixtures.MISMATCH_FILENAME + ".json'. loadEffectsFromDir overwrites the "
                + "field from the filename after reading, so the filename wins and a reader "
                + "trusting the key gets a different answer");

        out.dat("effect", "customeffects.dat", EffectFixtures.index(effects, 12),
            "the custom effect INDEX -- {Name, ID} pairs and lastID, not the effects themselves, "
                + "which are the .json files under customeffects/. The unnamed effect is absent "
                + "from this index by design");

        out.dat("global", "global.dat", WorldDataFixtures.global(42),
            "one integer. The smallest document CustomNPC+ writes, and a useful floor for a "
                + "reader's envelope handling");

        out.dat("tag", "tags.dat", WorldDataFixtures.tags(),
            "five tags with explicit UUIDs, covering hidden, empty-named, non-Latin and a colour "
                + "of -1. Tag.uuid defaults to randomUUID(), so these are pinned deliberately");

        out.dat("transport", "transport.dat", WorldDataFixtures.transport(),
            "three categories and six locations, including negative coordinates, the Nether and "
                + "End by numeric id, and a modded dimension 7265 -- the int dimension that "
                + "stopped being expressible after 1.16");

        out.dat("bank", "bank.dat", WorldDataFixtures.banks(),
            "three banks with currency and upgrade inventories and per-slot types. Keyed under "
                + "'Data' with no lastID, unlike every other document here");

        out.dat("spawn", "spawns.dat", WorldDataFixtures.spawns(),
            "three natural spawn entries. One carries three numbered SpawnCompound keys rather "
                + "than a list, which a reader has to probe for rather than iterate");

        List<Case<PlayerData>> players = PlayerFixtures.records();
        for (Case<PlayerData> c : players) {
            out.json("player", c.value.uuid, "playerdata/" + c.value.uuid + ".json",
                c.value.getNBT(), c.note);
        }
        // The same record as gzipped NBT, which is what ConfigMain.DatFormat selects instead. Same
        // compound, different container -- a reader has to handle both because a server operator
        // chooses between them.
        PlayerData asDat = players.get(1).value;
        out.dat("player", "playerdata/" + asDat.uuid + ".dat", asDat.getNBT(),
            "the Progressed player again, written as gzipped NBT rather than JSON. "
                + "ConfigMain.DatFormat picks the container; the compound is identical. Both "
                + "extensions are loaded by PlayerDataController, so both are real");

        out.dat("player", "playerdatamap.dat", PlayerFixtures.index(PlayerFixtures.roster()),
            "the name/uuid index. Derived rather than authoritative -- CustomNPC+ rebuilds it by "
                + "scanning playerdata/ when the file is missing -- but it is what gets read first");
    }
}
