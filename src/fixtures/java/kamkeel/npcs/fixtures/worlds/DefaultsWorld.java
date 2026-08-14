package kamkeel.npcs.fixtures.worlds;

import kamkeel.npcs.fixtures.FixtureWorld;
import kamkeel.npcs.fixtures.WorldWriter;
import kamkeel.npcs.fixtures.content.FactionFixtures;
import noppes.npcs.constants.EnumOptionType;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.Faction;

import java.util.List;

/**
 * What CustomNPC+ writes for a world nobody has edited.
 *
 * <p>Worth its own world because "the defaults" is a real contract and an easy thing to get subtly
 * wrong: a reader that supplies its own idea of a default agrees with this file on every field it
 * happens to share and disagrees on the rest, silently. Reproduced from
 * FactionController.loadFactions and DialogController.loadDefaultDialogs.
 */
public final class DefaultsWorld implements FixtureWorld {

    public String name() {
        return "defaults";
    }

    public String purpose() {
        return "exactly what CustomNPC+ creates for a fresh world -- the three default factions and "
            + "the three Villager dialogs -- and nothing else.";
    }

    public void build(WorldWriter out) {
        List<Faction> factions = FactionFixtures.vanillaDefaults();
        out.dat("faction", "factions.dat", FactionFixtures.document(factions, 0),
            "the three factions FactionController creates when factions.dat is missing or empty: "
                + "Friendly 2000, Neutral 1000, Aggressive 0");

        // Copied field for field from DialogController.loadDefaultDialogs.
        Dialog start = new Dialog();
        start.id = 1;
        start.title = "Start";
        start.text = "Hello {player}, \n\nWelcome to our village. I hope you enjoy your stay";

        Dialog village = new Dialog();
        village.id = 2;
        village.title = "Ask about village";
        village.text = "This village has been around for ages. Enjoy your stay here.";

        Dialog who = new Dialog();
        who.id = 3;
        who.title = "Who are you";
        who.text = "I'm a villager here. I have lived in this village my whole life.";

        DialogOption toVillage = new DialogOption();
        toVillage.title = "Tell me something about this village";
        toVillage.dialogId = 2;
        toVillage.optionType = EnumOptionType.DialogOption;

        DialogOption toWho = new DialogOption();
        toWho.title = "Who are you?";
        toWho.dialogId = 3;
        toWho.optionType = EnumOptionType.DialogOption;

        DialogOption goodbye = new DialogOption();
        goodbye.title = "Goodbye";
        goodbye.optionType = EnumOptionType.QuitOption;

        start.options.put(0, toWho);
        start.options.put(1, toVillage);
        start.options.put(2, goodbye);

        DialogOption back = new DialogOption();
        back.title = "Back";
        back.dialogId = 1;

        village.options.put(1, back);
        who.options.put(1, back);

        out.dialog("Villager", start,
            "CustomNPC+'s own default dialog 1, with its three options. Note showWheel is false "
                + "here because the object was never read back -- readNBTPartial defaults an "
                + "absent DialogShowWheel to true, which the writer does not do");
        out.dialog("Villager", village, "CustomNPC+'s own default dialog 2");
        out.dialog("Villager", who, "CustomNPC+'s own default dialog 3");
    }
}
