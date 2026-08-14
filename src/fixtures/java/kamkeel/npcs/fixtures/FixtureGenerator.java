package kamkeel.npcs.fixtures;

import kamkeel.npcs.fixtures.worlds.DefaultsWorld;
import kamkeel.npcs.fixtures.worlds.EdgeWorld;
import kamkeel.npcs.fixtures.worlds.LegacyWorld;
import kamkeel.npcs.fixtures.worlds.MaximalWorld;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates CustomNPC+ save data offline, for use as test fixtures.
 *
 * <p>The point is that the bytes are written by CustomNPC+ itself. Hand-authoring a
 * {@code factions.dat} that looks right is easy and proves nothing: it asserts what its author
 * believed the format to be. Everything here goes through the shipping data classes' own
 * serialization, so a fixture is evidence about the format rather than about the author.
 *
 * <p>Usage: {@code gradlew scribeFixtures -Pout=<dir> [-Pworlds=maximal,edge]}
 */
public final class FixtureGenerator {

    private static final List<FixtureWorld> ALL = Arrays.<FixtureWorld>asList(
        new MaximalWorld(),
        new DefaultsWorld(),
        new EdgeWorld(),
        new LegacyWorld());

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("usage: FixtureGenerator <output-dir> [--worlds a,b,c]");
            System.exit(2);
        }
        File out = new File(args[0]).getAbsoluteFile();

        List<FixtureWorld> selected = new ArrayList<FixtureWorld>(ALL);
        for (int i = 1; i < args.length - 1; i++) {
            if ("--worlds".equals(args[i])) {
                selected = select(args[i + 1]);
            }
        }

        System.out.println("[fixtures] bootstrapping Minecraft");
        Fixtures.bootMinecraft();

        Manifest manifest = new Manifest();
        recordKnownWarnings(manifest);

        int normalized = 0;
        for (FixtureWorld world : selected) {
            WorldWriter writer = new WorldWriter(out, world.name(), manifest);
            manifest.world(world.name(), world.purpose());
            int before = manifest.count();
            world.build(writer);
            normalized += writer.normalizedCount();
            System.out.println("[fixtures] " + pad(world.name())
                + (manifest.count() - before) + " files");
        }

        manifest.write(new File(out, "manifest.json"));
        Readme.write(new File(out, "README.md"), selected, manifest);

        System.out.println("[fixtures] " + manifest.count() + " files total -> " + out);
        System.out.println("[fixtures]   dialogs " + manifest.countOf("dialog")
            + ", quests " + manifest.countOf("quest"));
        // Reported every run so the one normalization cannot quietly spread to other keys.
        System.out.println("[fixtures]   " + normalized + " wall-clock values pinned (TimePast)");
    }

    /**
     * Facts a consumer cannot get from the bytes, and would otherwise learn from a flaky test.
     *
     * <p>Each was found by reading the writer, not by guessing.
     */
    private static void recordKnownWarnings(Manifest manifest) {
        manifest.warn("NORMALIZED: TimePast is pinned to 0 in every document. PlayerMail.writeNBT "
            + "stamps it with System.currentTimeMillis() - time, and a PlayerMail hangs off every "
            + "dialog and every quest whether or not mail was configured -- so left alone, every "
            + "file here would differ between two runs and a version-controlled corpus would churn "
            + "completely on each regeneration. This is the ONLY key these fixtures rewrite after "
            + "CustomNPC+ produced it. Nothing reads the value back except a live-clock display.");
        manifest.warn("Quest.writeToNBTPartial writes NextQuestTitle from the live QuestController, "
            + "which is empty offline, so NextQuestTitle is written as the value set on the object "
            + "rather than resolved from the target quest. NextQuestId is the real link.");
        manifest.warn("PlayerMail.writeNBT writes MailQuestTitle only when the quest resolves in the "
            + "live QuestController. Offline it never resolves, so the key is absent.");
        manifest.warn("These worlds hold no level.dat and no region files. They are the customnpcs/ "
            + "directory of a world, which is the part CustomNPC+ owns.");
    }

    private static List<FixtureWorld> select(String csv) {
        Map<String, FixtureWorld> byName = new LinkedHashMap<String, FixtureWorld>();
        for (FixtureWorld w : ALL) {
            byName.put(w.name(), w);
        }
        List<FixtureWorld> selected = new ArrayList<FixtureWorld>();
        for (String name : csv.split(",")) {
            FixtureWorld w = byName.get(name.trim());
            if (w == null) {
                throw new IllegalArgumentException("No such world: " + name + " (have " + byName.keySet() + ")");
            }
            selected.add(w);
        }
        return selected;
    }

    private static String pad(String name) {
        StringBuilder sb = new StringBuilder(name);
        while (sb.length() < 12) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
