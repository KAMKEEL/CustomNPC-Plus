package kamkeel.npcs.fixtures.content;

import kamkeel.npcs.fixtures.Case;
import kamkeel.npcs.fixtures.Fixtures;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.constants.EnumPartyExchange;
import noppes.npcs.constants.EnumPartyObjectives;
import noppes.npcs.constants.EnumPartyRequirements;
import noppes.npcs.constants.EnumProfileSync;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.constants.EnumQuestRepeat;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.quests.QuestDialog;
import noppes.npcs.quests.QuestItem;
import noppes.npcs.quests.QuestKill;
import noppes.npcs.quests.QuestLocation;
import noppes.npcs.quests.QuestManual;

import java.util.ArrayList;
import java.util.List;

/**
 * Quests, one axis at a time.
 *
 * <p><b>A quest has no availability.</b> Worth stating because it is easy to conclude otherwise:
 * {@code Quest.readNBTPartial} calls {@code VersionCompatibility.CheckAvailabilityCompatibility},
 * whose name says availability, and which is in fact a generic "fill in keys this file predates"
 * pass applied to every {@code ICompatibilty}. {@code Quest} declares no {@code Availability}
 * field, {@code writeToNBTPartial} writes no {@code Availability*} key, and neither does the 1.20
 * line. Gating is a property of the dialog that offers the quest.
 *
 * <p>Ranges: 1-9 the six types, 10-29 repeat and completion, 30-49 rewards, 50-69 party and
 * profile, 70-89 objectives in depth, 90-99 combinations.
 */
public final class QuestFixtures {

    private QuestFixtures() {
    }

    public static List<Case<Quest>> maximal() {
        List<Case<Quest>> out = new ArrayList<Case<Quest>>();
        types(out);
        repeatAndCompletion(out);
        rewards(out);
        partyAndProfile(out);
        objectives(out);
        combinations(out);
        return out;
    }

    // ------------------------------------------------------------------ types

    private static void types(List<Case<Quest>> out) {
        int id = 1;
        for (EnumQuestType type : EnumQuestType.values()) {
            Quest quest = quest(id, "Type " + type.name(), type);
            populateObjective(quest, type);
            out.add(new Case<Quest>("Types", quest,
                "quest type " + type.name() + " (ordinal " + type.ordinal() + ") with its "
                    + "objective populated. NOTE: Kill and AreaKill both build a QuestKill and "
                    + "write the same keys -- the only thing separating them on disk is Type"));
            id++;
        }
    }

    /** Kill and AreaKill differ by Type alone, so they get compared directly. */
    private static void populateObjective(Quest quest, EnumQuestType type) {
        if (type == EnumQuestType.Item) {
            QuestItem items = (QuestItem) quest.questInterface;
            items.items.items.put(0, Fixtures.item("minecraft:iron_ingot", 12));
            items.items.items.put(1, Fixtures.namedItem("minecraft:written_book", 1, 0, "Proof"));
            items.leaveItems = true;
            items.ignoreDamage = true;
            items.ignoreNBT = false;
        } else if (type == EnumQuestType.Dialog) {
            QuestDialog dialogs = (QuestDialog) quest.questInterface;
            dialogs.dialogs.put(0, 1);
            dialogs.dialogs.put(1, 2);
            dialogs.dialogs.put(2, 3);
        } else if (type == EnumQuestType.Kill || type == EnumQuestType.AreaKill) {
            QuestKill kill = (QuestKill) quest.questInterface;
            kill.targets.put("Zombie", 20);
            kill.targets.put("Skeleton", 15);
            kill.targetType = type == EnumQuestType.AreaKill ? 1 : 0;
            kill.customTargetType = "noppes.npcs.entity.EntityCustomNpc";
        } else if (type == EnumQuestType.Location) {
            QuestLocation location = (QuestLocation) quest.questInterface;
            location.location = "Northern Watchtower";
            location.location2 = "Sunken Crypt";
            location.location3 = "The Long Bridge";
        } else if (type == EnumQuestType.Manual) {
            QuestManual manual = (QuestManual) quest.questInterface;
            manual.manuals.put("Speak to the smith", 1);
            manual.manuals.put("Gather reeds", 30);
        }
    }

    // ------------------------------------------------------------------ repeat and completion

    private static void repeatAndCompletion(List<Case<Quest>> out) {
        int id = 10;
        for (EnumQuestRepeat repeat : EnumQuestRepeat.values()) {
            Quest quest = quest(id, "Repeat " + repeat.name(), EnumQuestType.Manual);
            quest.repeat = repeat;
            // The two CUSTOM modes are the only ones that read customCooldown, but it is written
            // unconditionally, so setting it everywhere shows that.
            quest.customCooldown = 3600000L;
            ((QuestManual) quest.questInterface).manuals.put("Do the thing", 1);
            out.add(new Case<Quest>("Repeat", quest,
                "QuestRepeat = " + repeat.name() + " (ordinal " + repeat.ordinal() + ") with "
                    + "CustomCooldown 3600000. MCDAILY/MCWEEKLY count world ticks, RLDAILY/RLWEEKLY "
                    + "count wall time, and only MCCUSTOM/RLCUSTOM read the cooldown"));
            id++;
        }

        int completionId = 18;
        for (EnumQuestCompletion completion : EnumQuestCompletion.values()) {
            Quest quest = quest(completionId, "Completion " + completion.name(), EnumQuestType.Manual);
            quest.completion = completion;
            quest.completerNpc = completion == EnumQuestCompletion.Npc ? "Quest Giver" : "";
            ((QuestManual) quest.questInterface).manuals.put("Do the thing", 1);
            out.add(new Case<Quest>("Repeat", quest,
                "QuestCompletion = " + completion.name() + ". Npc requires turning in to the named "
                    + "NPC; Instant completes the moment the objective is met"));
            completionId++;
        }

        Quest cooldownExtremes = quest(20, "Cooldown Extremes", EnumQuestType.Manual);
        cooldownExtremes.repeat = EnumQuestRepeat.RLCUSTOM;
        cooldownExtremes.customCooldown = Long.MAX_VALUE;
        ((QuestManual) cooldownExtremes.questInterface).manuals.put("Wait", 1);
        out.add(new Case<Quest>("Repeat", cooldownExtremes,
            "CustomCooldown at Long.MAX_VALUE -- the one long on a quest, and the field most "
                + "likely to be narrowed to an int by a reader"));
    }

    // ------------------------------------------------------------------ rewards

    private static void rewards(List<Case<Quest>> out) {
        Quest exp = quest(30, "Experience Reward", EnumQuestType.Manual);
        exp.rewardExp = 2500;
        ((QuestManual) exp.questInterface).manuals.put("Earn it", 1);
        out.add(new Case<Quest>("Rewards", exp, "RewardExp only"));

        Quest items = quest(31, "Item Rewards", EnumQuestType.Manual);
        items.rewardItems.items.put(0, Fixtures.item("minecraft:diamond", 5));
        items.rewardItems.items.put(1, Fixtures.item("minecraft:gold_ingot", 32));
        items.rewardItems.items.put(8, Fixtures.namedItem("minecraft:diamond_pickaxe", 1, 3, "Miner's Due"));
        ((QuestManual) items.questInterface).manuals.put("Earn it", 1);
        out.add(new Case<Quest>("Rewards", items,
            "three of the nine reward slots filled, including slot 8 and one stack with custom "
                + "NBT. Stored under the Rewards tag as an NpcMiscInventory"));

        Quest full = quest(32, "Nine Item Rewards", EnumQuestType.Manual);
        String[] nine = {
            "minecraft:stone", "minecraft:dirt", "minecraft:oak_stairs", "minecraft:torch",
            "minecraft:bread", "minecraft:coal", "minecraft:string", "minecraft:bone",
            "minecraft:paper"
        };
        for (int i = 0; i < nine.length; i++) {
            full.rewardItems.items.put(i, Fixtures.item(nine[i], i + 1));
        }
        ((QuestManual) full.questInterface).manuals.put("Earn it", 1);
        out.add(new Case<Quest>("Rewards", full,
            "all nine reward slots filled, each a different count -- the inventory at capacity"));

        Quest random = quest(33, "Random Reward", EnumQuestType.Manual);
        random.randomReward = true;
        random.rewardItems.items.put(0, Fixtures.item("minecraft:emerald", 1));
        random.rewardItems.items.put(1, Fixtures.item("minecraft:redstone", 1));
        ((QuestManual) random.questInterface).manuals.put("Earn it", 1);
        out.add(new Case<Quest>("Rewards", random,
            "RandomReward true -- one of the reward slots is granted rather than all of them"));

        Quest faction = quest(34, "Faction Reward", EnumQuestType.Manual);
        faction.factionOptions.factionId = 14;
        faction.factionOptions.factionPoints = 500;
        faction.factionOptions.decreaseFactionPoints = false;
        faction.factionOptions.faction2Id = 15;
        faction.factionOptions.faction2Points = 250;
        faction.factionOptions.decreaseFaction2Points = true;
        ((QuestManual) faction.questInterface).manuals.put("Earn it", 1);
        out.add(new Case<Quest>("Rewards", faction,
            "both faction reward slots, nested under QuestFactionPoints -- note a dialog writes "
                + "the same FactionOptions keys flat, not nested"));

        Quest mail = quest(35, "Mail Reward", EnumQuestType.Manual);
        mail.mail.subject = "Your reward";
        mail.mail.sender = "The Guild";
        mail.mail.time = Fixtures.EPOCH;
        NBTTagCompound message = new NBTTagCompound();
        message.setString("Text", "Enclosed, with thanks.");
        mail.mail.message = message;
        mail.mail.items[0] = Fixtures.item("minecraft:gold_nugget", 64);
        mail.mail.items[3] = Fixtures.item("minecraft:cake", 1);
        ((QuestManual) mail.questInterface).manuals.put("Earn it", 1);
        out.add(new Case<Quest>("Rewards", mail,
            "QuestMail with two of four item slots, including the last slot"));

        Quest command = quest(36, "Command On Completion", EnumQuestType.Manual);
        command.command = "/give {player} minecraft:diamond 1";
        ((QuestManual) command.questInterface).manuals.put("Earn it", 1);
        out.add(new Case<Quest>("Rewards", command, "QuestCommand with a {player} placeholder"));

        Quest chain = quest(37, "Chains To Another", EnumQuestType.Manual);
        chain.nextQuestid = 30;
        chain.nextQuestTitle = "Experience Reward";
        ((QuestManual) chain.questInterface).manuals.put("Earn it", 1);
        out.add(new Case<Quest>("Rewards", chain,
            "NextQuestId 30 with a matching NextQuestTitle. On read CustomNPC+ overwrites the "
                + "title from the live controller and blanks it when the target is missing, so "
                + "the id is the real link and the title is a cache"));

        Quest danglingChain = quest(38, "Chains Nowhere", EnumQuestType.Manual);
        danglingChain.nextQuestid = 999999;
        danglingChain.nextQuestTitle = "A quest that does not exist";
        ((QuestManual) danglingChain.questInterface).manuals.put("Earn it", 1);
        out.add(new Case<Quest>("Rewards", danglingChain,
            "NextQuestId pointing at nothing, with a stale title still on disk -- what a world "
                + "looks like after the target quest was deleted"));
    }

    // ------------------------------------------------------------------ party and profile

    private static void partyAndProfile(List<Case<Quest>> out) {
        // The party block is written only when AllowParty is true. Both halves matter.
        Quest disabled = quest(50, "Party Disabled", EnumQuestType.Manual);
        disabled.partyOptions.allowParty = false;
        disabled.partyOptions.minPartySize = 4;
        disabled.partyOptions.maxPartySize = 8;
        ((QuestManual) disabled.questInterface).manuals.put("Solo", 1);
        out.add(new Case<Quest>("Party", disabled,
            "AllowParty false with sizes assigned. Proves the eight party keys are ABSENT from "
                + "the file when the flag is off -- the values are lost upstream, so there is "
                + "nothing for a converter to preserve"));

        Quest enabled = quest(51, "Party Enabled", EnumQuestType.Manual);
        enabled.partyOptions.allowParty = true;
        enabled.partyOptions.onlyParty = true;
        enabled.partyOptions.partyRequirements = EnumPartyRequirements.All;
        enabled.partyOptions.rewardControl = EnumPartyExchange.Enrolled;
        enabled.partyOptions.completeFor = EnumPartyExchange.Valid;
        enabled.partyOptions.executeCommand = EnumPartyExchange.All;
        enabled.partyOptions.minPartySize = 2;
        enabled.partyOptions.maxPartySize = 6;
        enabled.partyOptions.objectiveRequirement = EnumPartyObjectives.Leader;
        ((QuestManual) enabled.questInterface).manuals.put("Together", 1);
        out.add(new Case<Quest>("Party", enabled,
            "AllowParty true and all eight party keys set to non-default enum constants"));

        int id = 52;
        for (EnumPartyRequirements requirement : EnumPartyRequirements.values()) {
            Quest quest = quest(id, "Party Requirement " + requirement.name(), EnumQuestType.Manual);
            quest.partyOptions.allowParty = true;
            quest.partyOptions.partyRequirements = requirement;
            ((QuestManual) quest.questInterface).manuals.put("Together", 1);
            out.add(new Case<Quest>("Party", quest,
                "PartyRequirements = " + requirement.name() + " (ordinal " + requirement.ordinal() + ")"));
            id++;
        }
        for (EnumPartyExchange exchange : EnumPartyExchange.values()) {
            Quest quest = quest(id, "Party Reward " + exchange.name(), EnumQuestType.Manual);
            quest.partyOptions.allowParty = true;
            quest.partyOptions.rewardControl = exchange;
            ((QuestManual) quest.questInterface).manuals.put("Together", 1);
            out.add(new Case<Quest>("Party", quest,
                "RewardControl = " + exchange.name() + " (ordinal " + exchange.ordinal() + ")"));
            id++;
        }
        for (EnumPartyObjectives objective : EnumPartyObjectives.values()) {
            Quest quest = quest(id, "Party Objective " + objective.name(), EnumQuestType.Manual);
            quest.partyOptions.allowParty = true;
            quest.partyOptions.objectiveRequirement = objective;
            ((QuestManual) quest.questInterface).manuals.put("Together", 1);
            out.add(new Case<Quest>("Party", quest,
                "ObjectiveRequirement = " + objective.name() + " (ordinal " + objective.ordinal() + ")"));
            id++;
        }

        Quest profileOff = quest(62, "Profiles Disabled", EnumQuestType.Manual);
        profileOff.profileOptions.enableOptions = false;
        profileOff.profileOptions.cooldownControl = EnumProfileSync.Shared;
        ((QuestManual) profileOff.questInterface).manuals.put("Alone", 1);
        out.add(new Case<Quest>("Party", profileOff,
            "EnableProfiles false with a Shared cooldown assigned -- the two profile keys are "
                + "absent from the file, same shape as the party block"));

        int profileId = 63;
        for (EnumProfileSync sync : EnumProfileSync.values()) {
            Quest quest = quest(profileId, "Profile " + sync.name(), EnumQuestType.Manual);
            quest.profileOptions.enableOptions = true;
            quest.profileOptions.cooldownControl = sync;
            quest.profileOptions.completeControl = sync;
            ((QuestManual) quest.questInterface).manuals.put("Alone", 1);
            out.add(new Case<Quest>("Party", quest,
                "EnableProfiles true, CooldownControl and CompleteControl = " + sync.name()));
            profileId++;
        }
    }

    // ------------------------------------------------------------------ objectives in depth

    private static void objectives(List<Case<Quest>> out) {
        Quest emptyKill = quest(70, "Kill Nothing", EnumQuestType.Kill);
        out.add(new Case<Quest>("Objectives", emptyKill,
            "a Kill quest with an EMPTY target list. isCompleted counts completed targets against "
                + "the target count, so zero of zero passes -- this quest is complete on accept"));

        Quest manyKill = quest(71, "Kill Many Kinds", EnumQuestType.Kill);
        QuestKill kill = (QuestKill) manyKill.questInterface;
        String[] mobs = {"Zombie", "Skeleton", "Creeper", "Spider", "Enderman", "Blaze"};
        for (int i = 0; i < mobs.length; i++) {
            kill.targets.put(mobs[i], (i + 1) * 10);
        }
        out.add(new Case<Quest>("Objectives", manyKill,
            "six kill targets. Stored as a TreeMap, so the file is alphabetical rather than "
                + "insertion-ordered -- Blaze first, Zombie last"));

        Quest customTarget = quest(72, "Kill A Custom NPC", EnumQuestType.Kill);
        QuestKill custom = (QuestKill) customTarget.questInterface;
        custom.targets.put("Village Elder", 1);
        custom.targetType = 1;
        custom.customTargetType = "noppes.npcs.entity.EntityCustomNpc";
        out.add(new Case<Quest>("Objectives", customTarget,
            "TargetType 1 with a CustomTargetType class name -- targets an NPC by display name "
                + "rather than an entity by type"));

        Quest emptyItems = quest(73, "Bring Nothing", EnumQuestType.Item);
        QuestItem noItems = (QuestItem) emptyItems.questInterface;
        noItems.ignoreDamage = true;
        noItems.ignoreNBT = true;
        noItems.leaveItems = true;
        out.add(new Case<Quest>("Objectives", emptyItems,
            "an Item quest with no required items and all three matching flags on"));

        Quest variantItems = quest(74, "Bring Variants", EnumQuestType.Item);
        QuestItem variants = (QuestItem) variantItems.questInterface;
        variants.items.items.put(0, Fixtures.variant("minecraft:wool", 8, 14));
        variants.items.items.put(1, Fixtures.variant("minecraft:planks", 4, 3));
        variants.items.items.put(2, Fixtures.variant("minecraft:dye", 16, 4));
        out.add(new Case<Quest>("Objectives", variantItems,
            "three pre-Flattening variants -- red wool (14), jungle planks (3) and lapis dye (4). "
                + "Damage is a variant selector here, not durability, and each becomes a distinct "
                + "item after 1.13, so this is the case a variant table has to cover"));

        Quest manyDialogs = quest(75, "Read Many Dialogs", EnumQuestType.Dialog);
        QuestDialog dialogs = (QuestDialog) manyDialogs.questInterface;
        for (int i = 0; i < 5; i++) {
            dialogs.dialogs.put(i, 30 + i);
        }
        out.add(new Case<Quest>("Objectives", manyDialogs,
            "five dialog objectives. NOTE the key collision worth knowing about: a Dialog quest "
                + "stores these under QuestDialogs, which is exactly where a pre-split Kill quest "
                + "stored its targets -- see the legacy world"));

        Quest oneLocation = quest(76, "One Location", EnumQuestType.Location);
        ((QuestLocation) oneLocation.questInterface).location = "The Old Well";
        out.add(new Case<Quest>("Objectives", oneLocation,
            "only the first of three location slots set; the other two are written as empty"));

        Quest manuals = quest(77, "Manual Counters", EnumQuestType.Manual);
        QuestManual manual = (QuestManual) manuals.questInterface;
        manual.manuals.put("Deliver the letter", 1);
        manual.manuals.put("Light the beacons", 3);
        manual.manuals.put("Survive the night", 1);
        out.add(new Case<Quest>("Objectives", manuals,
            "three manual counters -- progress a script or an operator advances by hand"));
    }

    // ------------------------------------------------------------------ combinations

    private static void combinations(List<Case<Quest>> out) {
        Quest everything = quest(90, "Everything At Once", EnumQuestType.Item);
        everything.logText = "Bring the ingots, and be quick about it.";
        everything.completeText = "You have my thanks.";
        everything.completerNpc = "Quartermaster";
        everything.command = "/say {player} finished";
        everything.repeat = EnumQuestRepeat.MCCUSTOM;
        everything.customCooldown = 172800000L;
        everything.completion = EnumQuestCompletion.Instant;
        everything.nextQuestid = 1;
        everything.nextQuestTitle = "Type Item";
        everything.rewardExp = 1000;
        everything.randomReward = true;
        everything.rewardItems.items.put(0, Fixtures.item("minecraft:diamond", 3));
        everything.rewardItems.items.put(4, Fixtures.namedItem("minecraft:iron_sword", 1, 5, "Reward"));
        everything.factionOptions.factionId = 0;
        everything.factionOptions.factionPoints = 75;
        everything.partyOptions.allowParty = true;
        everything.partyOptions.onlyParty = false;
        everything.partyOptions.partyRequirements = EnumPartyRequirements.Valid;
        everything.partyOptions.rewardControl = EnumPartyExchange.All;
        everything.partyOptions.minPartySize = 2;
        everything.partyOptions.maxPartySize = 4;
        everything.profileOptions.enableOptions = true;
        everything.profileOptions.cooldownControl = EnumProfileSync.Shared;
        everything.profileOptions.completeControl = EnumProfileSync.Individual;
        everything.mail.subject = "Everything";
        everything.mail.sender = "Quartermaster";
        everything.mail.time = Fixtures.EPOCH;
        everything.mail.items[0] = Fixtures.item("minecraft:emerald", 12);
        QuestItem items = (QuestItem) everything.questInterface;
        items.items.items.put(0, Fixtures.item("minecraft:iron_ingot", 20));
        items.items.items.put(2, Fixtures.variant("minecraft:wool", 1, 11));
        items.leaveItems = false;
        items.ignoreDamage = false;
        items.ignoreNBT = true;
        out.add(new Case<Quest>("Combinations", everything,
            "every quest sub-object populated at once: objective, rewards, faction, party, "
                + "profile, mail, chain, cooldown and command"));

        Quest bare = quest(91, "Bare", EnumQuestType.Item);
        out.add(new Case<Quest>("Combinations", bare,
            "every field at its constructor default, for comparison against id 90"));

        Quest unicode = quest(92, Text.CYRILLIC_QUEST + " " + Text.EM_DASH + " " + Text.ELEVES,
            EnumQuestType.Manual);
        unicode.logText = Text.RTL;
        unicode.completeText = Text.ASTRAL;
        unicode.completerNpc = Text.KATAKANA_FACTION;
        ((QuestManual) unicode.questInterface).manuals.put(Text.CYRILLIC_QUEST, 1);
        out.add(new Case<Quest>("Combinations", unicode,
            "non-Latin title, log text, complete text, completer name and objective key. The "
                + "objective key is the interesting one -- it is an NBT map key, not a value"));
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Builds a quest with its type applied.
     *
     * <p>Order matters: {@code setType} replaces {@code questInterface} and stamps it with the
     * quest id, so both must be set before the objective is populated.
     */
    private static Quest quest(int id, String title, EnumQuestType type) {
        Quest quest = new Quest();
        quest.id = id;
        quest.title = title;
        quest.setType(type);
        quest.logText = "Objective for " + title + ".";
        quest.completeText = "Done: " + title + ".";
        return quest;
    }
}
