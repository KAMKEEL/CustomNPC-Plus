package kamkeel.npcs.fixtures.content;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.controllers.data.Faction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Factions, and the {@code factions.dat} document that holds them.
 *
 * <p>Layout copied from FactionController.getNBT: {@code lastID} plus an {@code NPCFactions} list
 * of {@link Faction#writeNBT} compounds. The controller iterates a HashMap; this iterates a list,
 * so the order is stable between runs. Order is not part of the format -- the loader keys each
 * entry on its own {@code Slot} -- but an unstable order would make every regeneration a diff.
 */
public final class FactionFixtures {

    private FactionFixtures() {
    }

    /** The three CustomNPC+ creates for a new world, when factions.dat is absent or empty. */
    public static List<Faction> vanillaDefaults() {
        return new ArrayList<Faction>(Arrays.asList(
            new Faction(0, "Friendly", 0x00DD00, 2000),
            new Faction(1, "Neutral", 0xF2DD00, 1000),
            new Faction(2, "Aggressive", 0xDD0000, 0)));
    }

    /**
     * Every flag and threshold moved off its default, and the mutual-hostility graph populated.
     *
     * <p>The three booleans are the interesting part for a port: {@code hideFaction} suppresses the
     * standing message, {@code getsAttacked} lets vanilla mobs target members, {@code isPassive}
     * stops members retaliating. They are independent, so they get an entry each rather than one
     * faction with all three set.
     */
    public static List<Faction> maximal() {
        List<Faction> factions = vanillaDefaults();

        Faction hidden = faction(10, "Hidden Watchers", 0x2B2B2B, 900);
        hidden.hideFaction = true;
        hidden.neutralPoints = 250;
        hidden.friendlyPoints = 2750;
        factions.add(hidden);

        Faction attacked = faction(11, "Hunted", 0xAA5500, 1200);
        attacked.getsAttacked = true;
        factions.add(attacked);

        Faction passive = faction(12, "Pacifists", 0x55FFFF, 1800);
        passive.isPassive = true;
        factions.add(passive);

        Faction all = faction(13, "Every Flag", 0xFF00FF, 0);
        all.hideFaction = true;
        all.getsAttacked = true;
        all.isPassive = true;
        all.neutralPoints = 0;
        all.friendlyPoints = 0;
        factions.add(all);

        // A hostility graph rather than a single edge: the reader has to keep a set per faction,
        // and a reader that keeps one integer looks correct until the second entry.
        Faction warring = faction(14, "Iron Legion", 0xB0B0B0, 400);
        warring.attackFactions.addAll(Arrays.asList(2, 10, 11, 12));
        factions.add(warring);

        Faction rival = faction(15, "Ash Covenant", 0x7A0000, 600);
        rival.attackFactions.addAll(Arrays.asList(14, 0));
        factions.add(rival);

        // Thresholds inverted -- friendly below neutral. CustomNPC+ does not validate this, and
        // Availability and Faction read the boundary differently, so it is worth having on disk.
        Faction inverted = faction(16, "Inverted Bands", 0x123456, 1000);
        inverted.neutralPoints = 1800;
        inverted.friendlyPoints = 200;
        factions.add(inverted);

        Faction negative = faction(17, "Debtors", 0x000080, -500);
        negative.neutralPoints = -1000;
        negative.friendlyPoints = -100;
        factions.add(negative);

        return factions;
    }

    /** Names and values that are legal, unusual, and have broken a reader somewhere. */
    public static List<Faction> edge() {
        List<Faction> factions = new ArrayList<Faction>();

        factions.add(faction(0, "", 0, 0));

        // Built from code points rather than written as literals. This source stays pure ASCII on
        // purpose: a generator whose own file gets mojibaked by a CP-1252 toolchain would emit
        // fixtures that assert the mojibake, and every reader downstream would agree with it.
        Faction unicode = faction(1, Text.KATAKANA_FACTION + " " + Text.EM_DASH + " " + Text.ELEVES,
            0xFFFFFF, 1000);
        factions.add(unicode);

        Faction formatting = faction(2,
            Text.SECTION + "cRed " + Text.SECTION + "lBold" + Text.SECTION + "r", 0xFF5555, 1000);
        factions.add(formatting);

        Faction longName = faction(3, repeat("Faction With A Very Long Name ", 12), 0x00FF00, 1000);
        factions.add(longName);

        Faction extremes = faction(4, "Integer Extremes", 0xFFFFFF, Integer.MAX_VALUE);
        extremes.neutralPoints = Integer.MIN_VALUE;
        extremes.friendlyPoints = Integer.MAX_VALUE;
        extremes.color = -1;
        factions.add(extremes);

        // Points to factions that are not in this file. A converter that resolves eagerly breaks
        // here; one that keeps the id does not.
        Faction dangling = faction(5, "Dangling References", 0x808080, 1000);
        dangling.attackFactions.addAll(Arrays.asList(9001, -3, 0));
        factions.add(dangling);

        // A non-contiguous, high id. Nothing requires ids to be dense, and lastID is separate.
        factions.add(faction(4096, "Sparse Id", 0x00FFFF, 1000));

        return factions;
    }

    /** The controller-shaped document. Copied from FactionController.getNBT. */
    public static NBTTagCompound document(List<Faction> factions, int lastUsedId) {
        NBTTagList list = new NBTTagList();
        for (Faction faction : factions) {
            NBTTagCompound tag = new NBTTagCompound();
            faction.writeNBT(tag);
            list.appendTag(tag);
        }
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("lastID", lastUsedId);
        compound.setTag("NPCFactions", list);
        return compound;
    }

    private static Faction faction(int id, String name, int color, int defaultPoints) {
        return new Faction(id, name, color, defaultPoints);
    }

    private static String repeat(String s, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(s);
        }
        return sb.toString();
    }
}
