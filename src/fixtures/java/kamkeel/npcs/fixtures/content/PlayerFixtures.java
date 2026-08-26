package kamkeel.npcs.fixtures.content;

import kamkeel.npcs.fixtures.Case;
import kamkeel.npcs.fixtures.Fixtures;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-player records, and the name/uuid index beside them.
 *
 * <p>A record is {@code playerdata/<uuid>.json}, written by {@code PlayerData.getNBT}, which
 * composes fifteen sub-writers -- dialog, bank, quest, transport, faction, item giver, mail,
 * timers, skin overlays, animation, effect, magic, trade, ability and hotbar. Only some are
 * populated here; the rest write their empty form, which is itself worth having, since "what an
 * untouched sub-record looks like" is a contract too.
 *
 * <p>{@code playerdatamap.dat} is a flat {@code {Name, UUID}} list. It is a lookup index rebuilt
 * by scanning the directory when absent, so it is derived rather than authoritative -- but it is
 * what CustomNPC+ reads first.
 *
 * <p>The record can also be gzipped NBT rather than JSON, chosen by {@code ConfigMain.DatFormat}.
 * Both forms are generated, because a reader that only handles the default will meet the other on
 * somebody's server.
 */
public final class PlayerFixtures {

    private PlayerFixtures() {
    }

    /** uuid -> display name, in a fixed order so the index document is reproducible. */
    public static Map<String, String> roster() {
        Map<String, String> roster = new LinkedHashMap<String, String>();
        roster.put("29cc52dd-2c50-4e8f-a388-be6c497cf0b4", "Fresh");
        roster.put("8667ba71-b85a-4004-af54-457a9734eed7", "Progressed");
        roster.put("069a79f4-44e9-4726-a5be-fca90e38aaf5", "Notch");
        roster.put("00000000-0000-0000-0000-000000000000", "NilUuid");
        return roster;
    }

    public static List<Case<PlayerData>> records() {
        List<Case<PlayerData>> out = new ArrayList<Case<PlayerData>>();

        PlayerData fresh = player("29cc52dd-2c50-4e8f-a388-be6c497cf0b4", "Fresh");
        out.add(new Case<PlayerData>("", fresh,
            "a player who has done nothing. Every one of the fifteen sub-records writes its empty "
                + "form -- the baseline for what an untouched record contains"));

        PlayerData progressed = player("8667ba71-b85a-4004-af54-457a9734eed7", "Progressed");
        progressed.dialogData.dialogsRead.addAll(Arrays.asList(1, 2, 3, 41, 90));
        progressed.factionData.factionData.put(0, 1800);
        progressed.factionData.factionData.put(1, 1000);
        progressed.factionData.factionData.put(2, 0);
        progressed.factionData.factionData.put(14, -250);
        progressed.questData.finishedQuests.put(1, Fixtures.EPOCH);
        progressed.questData.finishedQuests.put(30, Fixtures.EPOCH + 86400000L);
        progressed.questData.activeQuests.put(2, questData(2, false));
        progressed.questData.activeQuests.put(71, questData(71, true));
        progressed.companionID = 4;
        progressed.profileSlot = 2;
        out.add(new Case<PlayerData>("", progressed,
            "read dialogs, standing in four factions including a negative one, two finished "
                + "quests with completion timestamps, and two active quests -- one of them marked "
                + "complete but not turned in. Also a companion id and a non-zero profile slot"));

        PlayerData extras = player("069a79f4-44e9-4726-a5be-fca90e38aaf5", "Notch");
        QuestData withExtras = questData(75, false);
        withExtras.extraData.setTag("Killed", killed("Zombie", 14, "Skeleton", 3));
        withExtras.extraData.setBoolean("LocationFound", true);
        withExtras.extraData.setBoolean("Location2Found", true);
        extras.questData.activeQuests.put(75, withExtras);
        extras.dialogData.dialogsRead.add(58);
        out.add(new Case<PlayerData>("", extras,
            "an active quest whose ExtraData holds per-objective progress, in the shapes the "
                + "quest classes actually read. Kill counts are a Killed tag list of Slot/Value "
                + "compounds -- NBTTags.getStringIntegerMap -- not loose integers keyed by mob "
                + "name, and a reached location is a boolean under its own key. The quest stores "
                + "only the targets, so a reader that ignores ExtraData can never show progress"));

        PlayerData party = player("11111111-2222-3333-4444-555555555555", "PartyMember");
        QuestData shared = questData(76, false);
        // The per-player half of a party kill quest: QuestKill keys it by the player's own name
        // rather than by slot, so two members of one party each carry their own tally.
        shared.extraData.setTag("PartyMemberKilled", killed("Creeper", 2));
        shared.extraData.setTag("Killed", killed("Creeper", 5));
        party.questData.activeQuests.put(76, shared);
        out.add(new Case<PlayerData>("", party,
            "a party kill quest, which stores the group total under Killed and each member's own "
                + "share under <playerName>Killed. The second key is composed from a name, so it "
                + "cannot be enumerated from the quest -- only from the party that took it"));

        PlayerData foreign = player("22222222-3333-4444-5555-666666666666", "AddonUser");
        QuestData unclaimed = questData(77, false);
        // ExtraData is an opaque compound: nothing validates its keys, so an addon that stores
        // its own progress there produces a file no objective kind here can read. Deliberate,
        // and the case a reader has to report rather than silently drop.
        unclaimed.extraData.setInteger("dbcaddon:KiCharged", 900);
        unclaimed.extraData.setString("dbcaddon:Form", "SuperSaiyan");
        foreign.questData.activeQuests.put(77, unclaimed);
        out.add(new Case<PlayerData>("", foreign,
            "an active quest whose ExtraData holds only keys no objective kind claims, which is "
                + "what an addon writing its own progress leaves behind. Nothing validates this "
                + "compound, so a reader meets it on a real server and has to say so rather than "
                + "drop it"));

        PlayerData nil = player("00000000-0000-0000-0000-000000000000", "NilUuid");
        nil.dialogData.dialogsRead.add(1);
        out.add(new Case<PlayerData>("", nil,
            "the nil UUID, which is legal in the file and names no player. A reader that treats "
                + "it as absent rather than as a value diverges here"));

        return out;
    }

    /** PlayerDataController.writeNBT -- a flat Name/UUID list, with no lastID. */
    public static NBTTagCompound index(Map<String, String> roster) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<String, String> entry : roster.entrySet()) {
            NBTTagCompound player = new NBTTagCompound();
            // The controller keys the map by NAME and stores the uuid as the value, which is the
            // opposite of what the file's key order suggests at a glance.
            player.setString("Name", entry.getValue());
            player.setString("UUID", entry.getKey());
            list.appendTag(player);
        }
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("PlayerDataMap", list);
        return compound;
    }

    private static PlayerData player(String uuid, String name) {
        PlayerData data = new PlayerData();
        data.uuid = uuid;
        data.playername = name;
        return data;
    }

    /**
     * An entry in a player's active quest list.
     *
     * <p>{@code QuestData} needs a {@link Quest} to construct, but only writes three keys and
     * never touches it -- the quest id is the map key in the enclosing list, not part of the
     * entry. So a bare Quest carrying the right id is enough and nothing is invented.
     */
    /**
     * A counter map in the shape {@code NBTTags.nbtStringIntegerMap} writes and
     * {@code getStringIntegerMap} reads: a list of {@code {Slot, Value}} compounds.
     *
     * <p>Written out by hand rather than through {@code NBTTags} so the fixture states the shape
     * it is asserting. A generator that borrows the reader's own helper agrees with it by
     * construction and proves nothing about it.
     */
    private static NBTTagList killed(Object... slotsAndCounts) {
        NBTTagList list = new NBTTagList();
        for (int at = 0; at < slotsAndCounts.length; at += 2) {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("Slot", (String) slotsAndCounts[at]);
            entry.setInteger("Value", ((Integer) slotsAndCounts[at + 1]).intValue());
            list.appendTag(entry);
        }
        return list;
    }

    private static QuestData questData(int questId, boolean completed) {
        Quest quest = new Quest();
        quest.id = questId;
        QuestData data = new QuestData(quest);
        data.isCompleted = completed;
        data.sendAlerts = true;
        return data;
    }
}
