package kamkeel.npcs.fixtures.worlds;

import kamkeel.npcs.fixtures.FixtureWorld;
import kamkeel.npcs.fixtures.WorldWriter;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.constants.EnumAvailabilityQuest;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.quests.QuestKill;

/**
 * Documents as an older CustomNPC+ would have left them.
 *
 * <p><b>Synthesized, and the only world here that is.</b> Every other file in these fixtures is
 * whatever CustomNPC+ writes today. These are produced by writing a current document and then
 * removing keys and lowering {@code ModRev} -- because producing genuinely old output means
 * checking out an old CustomNPC+ and running it, which is worth doing and is not this.
 *
 * <p>They are still worth having, because the upgrade path is real code with no other test. When a
 * loaded document's {@code ModRev} differs from {@code VersionCompatibility.ModRev},
 * {@code CheckAvailabilityCompatibility} walks a freshly constructed object's NBT and copies in
 * every key the file is missing. A reader that skips that step reads zero for absent integers,
 * which is a valid-looking value for most of these fields and a wrong one for all of them.
 *
 * <p>Each case names exactly what was removed, so the synthesis is auditable rather than implied.
 */
public final class LegacyWorld implements FixtureWorld {

    /** Anything below VersionCompatibility.ModRev, which is 23 at the time of writing. */
    private static final int OLD_REV = 17;

    public String name() {
        return "legacy";
    }

    public String purpose() {
        return "documents aged by hand -- keys removed and ModRev lowered -- to exercise the "
            + "default-filling upgrade path. The only synthesized world here; every case names "
            + "which keys it removed.";
    }

    public void build(WorldWriter out) {
        agedDialog(out);
        preUpgradeKillQuest(out);
        questWithoutAvailability(out);
    }

    private void agedDialog(WorldWriter out) {
        Dialog dialog = new Dialog();
        dialog.id = 1;
        dialog.title = "Aged Dialog";
        dialog.text = "Written by a version that had fewer fields.";
        dialog.color = 0x336699;

        NBTTagCompound compound = dialog.writeToNBTPartial(new NBTTagCompound());
        String[] removed = {
            "TextSound", "TextPitch", "TextWidth", "TextHeight", "ShowOptionLine",
            "PreviousBlocks", "NPCScale", "Color", "TitleColor", "DialogShowWheel",
            "DialogDarkScreen", "Images", "ColorSettings"
        };
        for (String key : removed) {
            compound.removeTag(key);
        }
        compound.setInteger("ModRev", OLD_REV);

        out.dialogRaw("Aged", 1, compound,
            "ModRev lowered to " + OLD_REV + " and 13 keys removed: " + join(removed) + ". Ten of "
                + "them have a non-zero default that readNBTPartial restores explicitly, and the "
                + "Color that was set is among the removed ones -- so a reader that does not "
                + "default-fill produces a black dialog with no text sound and a zero scale");
    }

    /**
     * The trap Scribe's own notes record: before the kill/dialog split, a Kill quest stored its
     * targets under {@code QuestDialogs} -- the dialog quest's key. QuestKill.readEntityFromNBT
     * still falls back to it when {@code QuestKills} is absent. Miss the fallback and every
     * pre-upgrade kill quest converts with no targets, which reads as already complete.
     */
    private void preUpgradeKillQuest(WorldWriter out) {
        Quest quest = new Quest();
        quest.id = 1;
        quest.title = "Old Kill Quest";
        quest.logText = "Cull the pigs.";
        quest.setType(EnumQuestType.Kill);
        QuestKill kill = (QuestKill) quest.questInterface;
        kill.targets.put("Pig", 10);
        kill.targets.put("Cow", 4);
        kill.targetType = 0;

        NBTTagCompound compound = quest.writeToNBTPartial(new NBTTagCompound());
        NBTBase targets = compound.getTag("QuestKills");
        compound.removeTag("QuestKills");
        compound.setTag("QuestDialogs", targets);
        compound.setInteger("ModRev", OLD_REV);

        out.questRaw("Aged", 1, compound,
            "a Kill quest whose targets sit under QuestDialogs instead of QuestKills, which is "
                + "where a pre-split CustomNPC+ put them. QuestKill.readEntityFromNBT falls back "
                + "to that key. A reader without the fallback loads two targets as zero targets, "
                + "and a quest with no targets counts as complete");
    }

    private void questWithoutAvailability(WorldWriter out) {
        Quest quest = new Quest();
        quest.id = 2;
        quest.title = "Aged Item Quest";
        quest.logText = "Bring me three ingots.";
        quest.setType(EnumQuestType.Item);
        quest.rewardExp = 500;

        NBTTagCompound compound = quest.writeToNBTPartial(new NBTTagCompound());
        String[] removed = {
            "PartyOptions", "ProfileOptions", "CustomCooldown", "RandomReward", "QuestCompletion"
        };
        for (String key : removed) {
            compound.removeTag(key);
        }
        compound.setInteger("ModRev", OLD_REV);

        out.questRaw("Aged", 2, compound,
            "ModRev lowered and the newer sub-objects removed: " + join(removed) + ". Party and "
                + "profile options are whole compounds, so this exercises the recursive half of "
                + "CompatabilityFix rather than the flat half");

        // A dialog whose quest availability slots were never written at all, which is what every
        // document below the availability rework looks like.
        Dialog gated = new Dialog();
        gated.id = 2;
        gated.title = "Gated, Then Aged";
        gated.text = "Only for those who finished quest 1.";
        gated.availability.questAvailable = EnumAvailabilityQuest.After;
        gated.availability.questId = 1;

        NBTTagCompound gatedCompound = gated.writeToNBTPartial(new NBTTagCompound());
        String[] gatedRemoved = {
            "AvailabilityQuest2", "AvailabilityQuest3", "AvailabilityQuest4",
            "AvailabilityQuest2Id", "AvailabilityQuest3Id", "AvailabilityQuest4Id"
        };
        for (String key : gatedRemoved) {
            gatedCompound.removeTag(key);
        }
        gatedCompound.setInteger("ModRev", OLD_REV);

        out.dialogRaw("Aged", 2, gatedCompound,
            "the first AvailabilityQuest slot set and the other three absent entirely. Their "
                + "defaults are Always/-1, so default-filling and reading-zero happen to agree "
                + "here -- which is why this file is a weak test on its own and a useful one "
                + "beside the aged dialog above, where they disagree");
    }

    private static String join(String[] keys) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.length; i++) {
            sb.append(keys[i]);
            if (i + 1 < keys.length) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
