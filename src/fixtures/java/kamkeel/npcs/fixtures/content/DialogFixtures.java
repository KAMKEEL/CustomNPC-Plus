package kamkeel.npcs.fixtures.content;

import kamkeel.npcs.fixtures.Case;
import kamkeel.npcs.fixtures.Fixtures;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.constants.EnumAvailabilityDialog;
import noppes.npcs.constants.EnumAvailabilityFaction;
import noppes.npcs.constants.EnumAvailabilityFactionType;
import noppes.npcs.constants.EnumAvailabilityQuest;
import noppes.npcs.constants.EnumDayTime;
import noppes.npcs.constants.EnumOptionType;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogImage;
import noppes.npcs.controllers.data.DialogOption;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialogs, built by axis rather than by example.
 *
 * <p>A dialog has about forty fields across six sub-objects, and picking a few realistic-looking
 * ones covers the fields the author happened to think of. So each block below walks one axis to
 * its ends -- every enum constant, every slot, both sides of every boolean -- and the id ranges
 * are reserved per axis so a new case does not renumber the others.
 *
 * <p>Ranges: 1-9 basics, 10-29 options, 30-59 availability, 60-79 appearance, 80-89 images,
 * 90-99 mail / quest link / faction rewards.
 */
public final class DialogFixtures {

    private DialogFixtures() {
    }

    public static List<Case<Dialog>> maximal() {
        List<Case<Dialog>> out = new ArrayList<Case<Dialog>>();
        basics(out);
        options(out);
        availability(out);
        appearance(out);
        images(out);
        attachments(out);
        return out;
    }

    // ------------------------------------------------------------------ basics

    private static void basics(List<Case<Dialog>> out) {
        Dialog bare = dialog(1, "Bare", "");
        out.add(new Case<Dialog>("Basics", bare,
            "every field left at its constructor default, with an empty text. The baseline a "
                + "reader's defaults are compared against"));

        Dialog plain = dialog(2, "Plain Greeting",
            "Hello {player},\n\nWelcome to our village. I hope you enjoy your stay");
        out.add(new Case<Dialog>("Basics", plain,
            "title and text only, with the {player} placeholder and embedded newlines that "
                + "CustomNPC+'s own default dialog uses"));

        Dialog longText = dialog(3, "Long Text", repeat(
            "This sentence exists so the stored text crosses the length CustomNPC+ warns about. ", 200));
        out.add(new Case<Dialog>("Basics", longText,
            "text over 15000 chars, which DialogController logs as an 'Extremely big dialog'. "
                + "Loads normally -- the check is a warning, not a limit"));

        Dialog unicode = dialog(4, Text.KATAKANA_FACTION + " " + Text.EM_DASH + " " + Text.ELEVES,
            Text.CYRILLIC_QUEST + "\n" + Text.RTL + "\n" + Text.ASTRAL);
        out.add(new Case<Dialog>("Basics", unicode,
            "title and text outside Latin-1, including one astral-plane code point. NBT strings "
                + "are modified UTF-8, so the astral char is stored as a surrogate pair"));

        Dialog formatted = dialog(5, Text.SECTION + "6Formatted " + Text.SECTION + "lTitle",
            Text.SECTION + "cRed " + Text.SECTION + "aGreen " + Text.SECTION + "r" + "plain");
        out.add(new Case<Dialog>("Basics", formatted,
            "section-sign colour codes in title and text, which is how all CustomNPC+ content is "
                + "coloured"));

        Dialog command = dialog(6, "Runs A Command", "The gate opens.");
        command.command = "/say {player} opened the gate";
        out.add(new Case<Dialog>("Basics", command,
            "DialogCommand set -- a command run on the server when the dialog opens"));

        Dialog sounds = dialog(7, "Sounds", "Listen.");
        sounds.sound = "customnpcs:dialog.open";
        sounds.textSound = "minecraft:random.orb";
        sounds.textPitch = 0.75F;
        out.add(new Case<Dialog>("Basics", sounds,
            "DialogSound plus a non-default TextSound and TextPitch. Note DialogSound is written "
                + "only when non-empty, so the key is absent on every other dialog here"));
    }

    // ------------------------------------------------------------------ options

    private static void options(List<Case<Dialog>> out) {
        // One dialog per option type, because the type drives what the other fields mean.
        int id = 10;
        for (EnumOptionType type : EnumOptionType.values()) {
            Dialog d = dialog(id, "Option " + type.name(), "Choose.");
            DialogOption option = new DialogOption();
            option.title = type.name() + " option";
            option.optionType = type;
            option.dialogId = type == EnumOptionType.DialogOption ? 2 : -1;
            option.optionColor = 0x00FF00;
            option.command = type == EnumOptionType.CommandBlock ? "/time set day" : "";
            d.options.put(0, option);
            out.add(new Case<Dialog>("Options", d,
                "a single option of type " + type.name() + " (ordinal " + type.ordinal() + ")"));
            id++;
        }

        Dialog full = dialog(20, "Six Options", "Pick one.");
        for (int slot = 0; slot < 6; slot++) {
            DialogOption option = new DialogOption();
            option.title = "Option in slot " + slot;
            option.optionType = EnumOptionType.DialogOption;
            option.dialogId = 2;
            option.optionColor = 0x100000 * (slot + 1);
            full.options.put(slot, option);
        }
        out.add(new Case<Dialog>("Options", full,
            "six options in contiguous slots 0-5, each a different colour"));

        // Slots are a map key, not a list index. A reader that treats Options as an ordered list
        // silently renumbers this one.
        Dialog sparse = dialog(21, "Sparse Slots", "The gaps matter.");
        int[] slots = {0, 3, 7, 12};
        for (int slot : slots) {
            DialogOption option = new DialogOption();
            option.title = "Slot " + slot;
            option.optionType = EnumOptionType.DialogOption;
            option.dialogId = 2;
            sparse.options.put(slot, option);
        }
        out.add(new Case<Dialog>("Options", sparse,
            "options at non-contiguous slots 0, 3, 7 and 12. OptionSlot is stored per entry, so "
                + "the gaps survive; a reader treating the list as positional loses them"));

        Dialog dangling = dialog(22, "Dangling Option", "Where does this go?");
        DialogOption toNowhere = new DialogOption();
        toNowhere.title = "To a dialog that is not here";
        toNowhere.optionType = EnumOptionType.DialogOption;
        toNowhere.dialogId = 999999;
        dangling.options.put(0, toNowhere);
        out.add(new Case<Dialog>("Options", dangling,
            "an option pointing at a dialog id that does not exist in this world. Conversion must "
                + "keep the id rather than resolve it"));

        Dialog emptyTitle = dialog(23, "Empty Option Title", "...");
        DialogOption blank = new DialogOption();
        blank.title = "";
        blank.optionType = EnumOptionType.DialogOption;
        blank.dialogId = 2;
        blank.optionColor = 0;
        emptyTitle.options.put(0, blank);
        out.add(new Case<Dialog>("Options", emptyTitle,
            "an option with an empty title and DialogColor 0. On read, colour 0 is replaced by the "
                + "default 0xe0e0e0 -- so this file does not round-trip, by design upstream"));
    }

    // ------------------------------------------------------------------ availability

    private static void availability(List<Case<Dialog>> out) {
        // Every EnumAvailabilityQuest constant, one dialog each. These are the rules whose loss
        // makes a gated dialog visible to everybody, so each constant gets its own file.
        int id = 30;
        for (EnumAvailabilityQuest rule : EnumAvailabilityQuest.values()) {
            Dialog d = dialog(id, "Quest rule " + rule.name(), "Gated on a quest.");
            d.availability.questAvailable = rule;
            d.availability.questId = 1;
            out.add(new Case<Dialog>("Availability", d,
                "AvailabilityQuest = " + rule.name() + " (ordinal " + rule.ordinal()
                    + ") against quest 1"));
            id++;
        }

        // All four quest slots at once, each a different rule and a different target.
        Dialog fourQuests = dialog(40, "Four Quest Rules", "Four gates.");
        fourQuests.availability.questAvailable = EnumAvailabilityQuest.After;
        fourQuests.availability.questId = 1;
        fourQuests.availability.quest2Available = EnumAvailabilityQuest.Before;
        fourQuests.availability.quest2Id = 2;
        fourQuests.availability.quest3Available = EnumAvailabilityQuest.Active;
        fourQuests.availability.quest3Id = 3;
        fourQuests.availability.quest4Available = EnumAvailabilityQuest.NotAcceptable;
        fourQuests.availability.quest4Id = 4;
        out.add(new Case<Dialog>("Availability", fourQuests,
            "all four AvailabilityQuest slots set to different rules and different quests. The "
                + "case a reader that reads only the first slot passes anyway"));

        int dialogRuleId = 41;
        for (EnumAvailabilityDialog rule : EnumAvailabilityDialog.values()) {
            Dialog d = dialog(dialogRuleId, "Dialog rule " + rule.name(), "Gated on a dialog.");
            d.availability.dialogAvailable = rule;
            d.availability.dialogId = 2;
            out.add(new Case<Dialog>("Availability", d,
                "AvailabilityDialog = " + rule.name() + " against dialog 2 -- read tracking"));
            dialogRuleId++;
        }

        Dialog fourDialogs = dialog(44, "Four Dialog Rules", "Four read-gates.");
        fourDialogs.availability.dialogAvailable = EnumAvailabilityDialog.After;
        fourDialogs.availability.dialogId = 1;
        fourDialogs.availability.dialog2Available = EnumAvailabilityDialog.Before;
        fourDialogs.availability.dialog2Id = 2;
        fourDialogs.availability.dialog3Available = EnumAvailabilityDialog.After;
        fourDialogs.availability.dialog3Id = 3;
        fourDialogs.availability.dialog4Available = EnumAvailabilityDialog.Before;
        fourDialogs.availability.dialog4Id = 4;
        out.add(new Case<Dialog>("Availability", fourDialogs,
            "all four AvailabilityDialog slots set"));

        // Faction rules: type x stance, both slots.
        int factionRuleId = 45;
        for (EnumAvailabilityFactionType type : EnumAvailabilityFactionType.values()) {
            for (EnumAvailabilityFaction stance : EnumAvailabilityFaction.values()) {
                if (type == EnumAvailabilityFactionType.Always && stance != EnumAvailabilityFaction.Friendly) {
                    continue; // Always ignores the stance; one file is enough for it.
                }
                Dialog d = dialog(factionRuleId, "Faction " + type.name() + " " + stance.name(),
                    "Gated on standing.");
                d.availability.factionAvailable = type;
                d.availability.factionStance = stance;
                d.availability.factionId = 10;
                out.add(new Case<Dialog>("Availability", d,
                    "AvailabilityFaction = " + type.name() + ", stance " + stance.name()
                        + ", against faction 10 (Hidden Watchers)"));
                factionRuleId++;
            }
        }

        Dialog bothFactions = dialog(53, "Both Faction Slots", "Two standings.");
        bothFactions.availability.factionAvailable = EnumAvailabilityFactionType.Is;
        bothFactions.availability.factionStance = EnumAvailabilityFaction.Friendly;
        bothFactions.availability.factionId = 0;
        bothFactions.availability.faction2Available = EnumAvailabilityFactionType.IsNot;
        bothFactions.availability.faction2Stance = EnumAvailabilityFaction.Hostile;
        bothFactions.availability.faction2Id = 2;
        out.add(new Case<Dialog>("Availability", bothFactions,
            "both faction slots, one Is/Friendly and one IsNot/Hostile"));

        int daytimeId = 54;
        for (EnumDayTime time : EnumDayTime.values()) {
            Dialog d = dialog(daytimeId, "Daytime " + time.name(), "Time-gated.");
            d.availability.daytime = time;
            out.add(new Case<Dialog>("Availability", d,
                "AvailabilityDayTime = " + time.name() + " (ordinal " + time.ordinal() + ")"));
            daytimeId++;
        }

        Dialog level = dialog(57, "Min Level", "You look experienced.");
        level.availability.minPlayerLevel = 30;
        out.add(new Case<Dialog>("Availability", level,
            "AvailabilityMinPlayerLevel = 30"));

        Dialog everything = dialog(58, "Every Rule At Once", "Nothing gets through this.");
        everything.availability.daytime = EnumDayTime.Night;
        everything.availability.minPlayerLevel = 99;
        everything.availability.dialogAvailable = EnumAvailabilityDialog.After;
        everything.availability.dialogId = 1;
        everything.availability.dialog2Available = EnumAvailabilityDialog.Before;
        everything.availability.dialog2Id = 2;
        everything.availability.dialog3Available = EnumAvailabilityDialog.After;
        everything.availability.dialog3Id = 3;
        everything.availability.dialog4Available = EnumAvailabilityDialog.Before;
        everything.availability.dialog4Id = 4;
        everything.availability.questAvailable = EnumAvailabilityQuest.After;
        everything.availability.questId = 1;
        everything.availability.quest2Available = EnumAvailabilityQuest.Active;
        everything.availability.quest2Id = 2;
        everything.availability.quest3Available = EnumAvailabilityQuest.NotActive;
        everything.availability.quest3Id = 3;
        everything.availability.quest4Available = EnumAvailabilityQuest.Acceptable;
        everything.availability.quest4Id = 4;
        everything.availability.factionAvailable = EnumAvailabilityFactionType.Is;
        everything.availability.factionStance = EnumAvailabilityFaction.Friendly;
        everything.availability.factionId = 0;
        everything.availability.faction2Available = EnumAvailabilityFactionType.IsNot;
        everything.availability.faction2Stance = EnumAvailabilityFaction.Hostile;
        everything.availability.faction2Id = 2;
        out.add(new Case<Dialog>("Availability", everything,
            "every availability slot set simultaneously -- ten rules plus daytime and level. The "
                + "single densest availability block in these fixtures"));
    }

    // ------------------------------------------------------------------ appearance

    private static void appearance(List<Case<Dialog>> out) {
        Dialog flags = dialog(60, "Flags Inverted", "Every boolean off its default.");
        flags.hideNPC = true;
        flags.showWheel = true;
        flags.disableEsc = true;
        flags.darkenScreen = false;
        flags.showOptionLine = false;
        flags.renderGradual = true;
        flags.showPreviousBlocks = false;
        out.add(new Case<Dialog>("Appearance", flags,
            "all seven display booleans flipped away from their defaults. Four of them read back "
                + "as true when their key is absent, so a writer that omits defaults changes "
                + "behaviour"));

        Dialog colors = dialog(61, "Colors", "Coloured.");
        colors.color = 0x336699;
        colors.titleColor = 0xCC3300;
        out.add(new Case<Dialog>("Appearance", colors,
            "Color and TitleColor off the 0xe0e0e0 default"));

        Dialog colorData = dialog(62, "Color Data Enabled", "Full chrome colours.");
        colorData.colorData.setEnableColorSettings(true);
        colorData.colorData.setLineColor1(0xff112233);
        colorData.colorData.setLineColor2(0xff445566);
        colorData.colorData.setLineColor3(0xff778899);
        colorData.colorData.setSlotColor(0xffaabbcc);
        colorData.colorData.setButtonAcceptColor(0xff00ff00);
        colorData.colorData.setButtonRejectColor(0xffff0000);
        out.add(new Case<Dialog>("Appearance", colorData,
            "DialogColorData with ColorSettings enabled and all six colours set. The six keys are "
                + "written ONLY when the flag is on -- see id 63 for the other half"));

        Dialog colorDataOff = dialog(63, "Color Data Disabled", "Colours set but not enabled.");
        colorDataOff.colorData.setEnableColorSettings(false);
        colorDataOff.colorData.setLineColor1(0xff112233);
        colorDataOff.colorData.setSlotColor(0xffaabbcc);
        out.add(new Case<Dialog>("Appearance", colorDataOff,
            "ColorSettings off with colours assigned. Proves the six colour keys are absent from "
                + "the file, so the values are lost upstream and there is nothing to convert"));

        Dialog layout = dialog(64, "Layout", "Positioned.");
        layout.textWidth = 420;
        layout.textHeight = 260;
        layout.textOffsetX = -12;
        layout.textOffsetY = 34;
        layout.titlePos = 2;
        layout.titleOffsetX = 8;
        layout.titleOffsetY = -6;
        layout.optionOffsetX = 5;
        layout.optionOffsetY = -5;
        layout.optionSpaceX = 3;
        layout.optionSpaceY = 14;
        layout.npcScale = 2.25F;
        layout.npcOffsetX = -40;
        layout.npcOffsetY = 18;
        out.add(new Case<Dialog>("Appearance", layout,
            "every geometry field set, several negative. Fourteen integers and one float that a "
                + "dialog screen has to place things by"));

        for (byte alignment = 0; alignment <= 2; alignment++) {
            Dialog d = dialog(65 + alignment, "Alignment " + alignment, "Aligned.");
            d.alignment = alignment;
            out.add(new Case<Dialog>("Appearance", d,
                "DialogAlignment = " + alignment + " (0 bottom, 1 top, 2 centre). Stored as a byte, "
                    + "not an int -- the one narrow scalar on a dialog"));
        }
    }

    // ------------------------------------------------------------------ images

    private static void images(List<Case<Dialog>> out) {
        Dialog one = dialog(80, "One Image", "Look.");
        one.dialogImages.put(0, image(0, "customnpcs:textures/gui/dialog.png", 1));
        out.add(new Case<Dialog>("Images", one,
            "a single DialogImage with every field at a non-default"));

        Dialog many = dialog(81, "Three Images", "Look again.");
        for (int i = 0; i < 3; i++) {
            DialogImage img = image(i, "customnpcs:textures/gui/image" + i + ".png", i);
            img.x = 10 * i;
            img.y = 20 * i;
            img.alignment = i;
            many.dialogImages.put(i, img);
        }
        out.add(new Case<Dialog>("Images", many,
            "three images with imageType 0, 1 and 2 -- Default, Text and Option"));

        Dialog defaults = dialog(82, "Default Image", "An untouched image.");
        defaults.dialogImages.put(0, new DialogImage(0));
        out.add(new Case<Dialog>("Images", defaults,
            "a DialogImage left entirely at its constructor defaults, including an empty texture"));
    }

    // ------------------------------------------------------------------ attachments

    private static void attachments(List<Case<Dialog>> out) {
        Dialog quest = dialog(90, "Starts A Quest", "Take this.");
        quest.quest = 1;
        out.add(new Case<Dialog>("Attachments", quest,
            "DialogQuest = 1 -- opening this dialog starts quest 1. The link a conversion that "
                + "drops the field turns into a quest nobody can be given"));

        Dialog mail = dialog(91, "Sends Mail", "Check your inbox.");
        mail.mail.subject = "A letter from the village";
        mail.mail.sender = "Elder Marrow";
        mail.mail.time = Fixtures.EPOCH;
        mail.mail.beenRead = false;
        NBTTagCompound message = new NBTTagCompound();
        message.setString("Text", "Thank you for your help.");
        mail.mail.message = message;
        mail.mail.items[0] = Fixtures.item("minecraft:golden_apple", 3, 1);
        mail.mail.items[2] = Fixtures.namedItem("minecraft:diamond_sword", 1, 12, "Villager's Thanks");
        out.add(new Case<Dialog>("Attachments", mail,
            "DialogMail with subject, sender, a message compound and two of four item slots -- "
                + "one of them a variant damage, one carrying custom NBT. NOTE: TimePast inside "
                + "this compound is not reproducible between runs"));

        Dialog reward = dialog(92, "Faction Reward", "The Legion remembers.");
        reward.factionOptions.factionId = 14;
        reward.factionOptions.factionPoints = 250;
        reward.factionOptions.decreaseFactionPoints = false;
        reward.factionOptions.faction2Id = 15;
        reward.factionOptions.faction2Points = 100;
        reward.factionOptions.decreaseFaction2Points = true;
        out.add(new Case<Dialog>("Attachments", reward,
            "both faction reward slots -- one granting points, one decreasing them"));

        Dialog everything = dialog(93, "Everything At Once",
            "This dialog exercises every sub-object CustomNPC+ hangs off a dialog.");
        everything.quest = 2;
        everything.command = "/say done";
        everything.sound = "customnpcs:dialog.open";
        everything.color = 0x445566;
        everything.titleColor = 0x998877;
        everything.hideNPC = true;
        everything.renderGradual = true;
        everything.npcScale = 1.5F;
        everything.availability.daytime = EnumDayTime.Day;
        everything.availability.minPlayerLevel = 5;
        everything.availability.questAvailable = EnumAvailabilityQuest.Before;
        everything.availability.questId = 3;
        everything.factionOptions.factionId = 0;
        everything.factionOptions.factionPoints = 10;
        everything.colorData.setEnableColorSettings(true);
        everything.colorData.setSlotColor(0xff00ffff);
        everything.mail.subject = "Everything";
        everything.mail.sender = "The Generator";
        everything.mail.time = Fixtures.EPOCH;
        everything.mail.items[0] = Fixtures.item("minecraft:emerald", 64);
        everything.dialogImages.put(0, image(0, "customnpcs:textures/gui/all.png", 2));
        DialogOption opt = new DialogOption();
        opt.title = "Continue";
        opt.optionType = EnumOptionType.DialogOption;
        opt.dialogId = 2;
        opt.optionColor = 0xFFAA00;
        everything.options.put(0, opt);
        out.add(new Case<Dialog>("Attachments", everything,
            "one dialog touching every sub-object at once: options, availability, faction rewards, "
                + "colour data, mail, an image and a quest link"));
    }

    // ------------------------------------------------------------------ helpers

    private static Dialog dialog(int id, String title, String text) {
        Dialog dialog = new Dialog();
        dialog.id = id;
        dialog.title = title;
        dialog.text = text;
        return dialog;
    }

    private static DialogImage image(int id, String texture, int imageType) {
        DialogImage image = new DialogImage(id);
        image.texture = texture;
        image.x = 12;
        image.y = 34;
        image.width = 64;
        image.height = 48;
        image.textureX = 8;
        image.textureY = 16;
        image.scale = 1.75F;
        image.color = 0x88CCFF;
        image.selectedColor = 0xFFCC88;
        image.alpha = 0.5F;
        image.rotation = 45.0F;
        image.imageType = imageType;
        image.alignment = 1;
        return image;
    }

    static String repeat(String s, int times) {
        StringBuilder sb = new StringBuilder(s.length() * times);
        for (int i = 0; i < times; i++) {
            sb.append(s);
        }
        return sb.toString();
    }
}
