package kamkeel.npcs.fixtures.content;

import kamkeel.npcs.fixtures.Case;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.data.CustomEffect;
import noppes.npcs.controllers.data.EffectScript;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Custom effects, which persist in two places at once.
 *
 * <p>Each effect is a file at {@code customeffects/<Name>.json} -- or
 * {@code customeffects/<Category>/<Name>.json}, where the subdirectory is the category -- written
 * with {@code writeToNBT(true)}. <b>The filename is the name</b>: {@code loadEffectsFromDir}
 * overwrites whatever {@code name} the file contained with the filename minus {@code .json}, so
 * the {@code name} key inside is dead weight and the two can legitimately disagree. There is a
 * fixture for that below.
 *
 * <p>Alongside them, {@code customeffects.dat} is an index of {@code {Name, ID}} pairs plus
 * {@code lastID} -- not the effects themselves. An effect whose name is empty is left out of the
 * index entirely, which is its own case.
 */
public final class EffectFixtures {

    private EffectFixtures() {
    }

    public static List<Case<CustomEffect>> maximal() {
        List<Case<CustomEffect>> out = new ArrayList<Case<CustomEffect>>();

        CustomEffect bare = effect(1, "Bare");
        out.add(new Case<CustomEffect>("", bare,
            "every field at its constructor default. Note menuName defaults to a coloured "
                + "'NEW EFFECT' containing a section sign, so even the bare case is not ASCII"));

        CustomEffect full = effect(2, "Fully Configured");
        full.menuName = Text.SECTION + "bFully " + Text.SECTION + "6Configured";
        full.length = 1200;
        full.everyXTick = 10;
        full.lossOnDeath = false;
        full.icon = "customnpcs:textures/gui/effects.png";
        full.iconX = 32;
        full.iconY = 48;
        full.width = 24;
        full.height = 24;
        out.add(new Case<CustomEffect>("", full,
            "every scalar off its default: a 60-second duration, a 10-tick period, an icon with "
                + "its atlas offsets and size, and lossOnDeath cleared"));

        CustomEffect animated = effect(3, "Animated Icon");
        animated.animated = true;
        animated.frameCount = 8;
        animated.frametime = 3;
        animated.icon = "customnpcs:textures/gui/anim.png";
        out.add(new Case<CustomEffect>("", animated,
            "an animated icon -- iconAnimated, iconFrameCount and iconFrameTime. Both counts are "
                + "clamped to a minimum of 1 on read, so zero cannot round-trip"));

        CustomEffect tagged = effect(4, "Tagged");
        tagged.tagUUIDs.addAll(Arrays.asList(
            UUID.fromString("0e5f2b6a-1c3d-4e5f-8a9b-0c1d2e3f4a5b"),
            UUID.fromString("1a2b3c4d-5e6f-4071-8293-a4b5c6d7e8f9"),
            UUID.fromString("ffffffff-ffff-4fff-bfff-ffffffffffff")));
        out.add(new Case<CustomEffect>("", tagged,
            "three tag UUIDs. TagController.writeTagUUIDs omits the key entirely when the set is "
                + "empty, so every other effect here has no TagUUIDs key at all"));

        CustomEffect scripted = effect(5, "Scripted");
        scripted.setScriptHandler(script(
            "function onEffectAdd(event) { event.player.message('effect added'); }\n"
                + "function onEffectTick(event) {}\n"
                + "function onEffectRemove(event) {}\n", true, "ECMAScript"));
        out.add(new Case<CustomEffect>("", scripted,
            "a ScriptData compound carrying ScriptLanguage, ScriptEnabled and a ScriptContent "
                + "unit with three hook functions"));

        CustomEffect scriptDisabled = effect(6, "Script Disabled");
        scriptDisabled.setScriptHandler(script("function onEffectTick(event) {}", false, "javascript"));
        out.add(new Case<CustomEffect>("", scriptDisabled,
            "ScriptEnabled false, and a language string that is not the canonical 'ECMAScript' -- "
                + "normalizeLanguage runs on read, not on write"));

        CustomEffect hugeScript = effect(7, "Huge Script");
        StringBuilder sb = new StringBuilder();
        while (sb.length() < 70000) {
            sb.append("// a line of script that exists only to push past the 65535 char limit\n");
        }
        hugeScript.setScriptHandler(script(sb.toString(), true, "ECMAScript"));
        out.add(new Case<CustomEffect>("", hugeScript,
            "a script over 65535 characters. NBT strings cannot hold that, so ScriptContainer "
                + "splits it across Script plus ExpandedScript0..N, bounded by "
                + "ConfigScript.ExpandedScriptLimit which defaults to 2. Check the written file "
                + "to see how many parts survived -- this fixture exists to record that"));

        CustomEffect noName = effect(8, "");
        out.add(new Case<CustomEffect>("", noName,
            "an EMPTY name. writeMapNBT skips these, so this effect is absent from "
                + "customeffects.dat while its file exists on disk -- and since the filename is "
                + "the name, its file is '.json'. Recorded rather than avoided"));

        CustomEffect categorised = effect(10, "In A Category");
        out.add(new Case<CustomEffect>("Buffs", categorised,
            "an effect inside a category subdirectory. Categories are directories and nothing "
                + "else -- there is no category file and no id stored anywhere"));

        CustomEffect unicode = effect(11, "Unicode");
        unicode.menuName = Text.KATAKANA_FACTION + " " + Text.EM_DASH + " " + Text.ELEVES;
        out.add(new Case<CustomEffect>("Buffs", unicode,
            "a non-Latin menu name inside a category"));

        CustomEffect extremes = effect(12, "Extremes");
        extremes.length = Integer.MAX_VALUE;
        extremes.everyXTick = Integer.MAX_VALUE;
        extremes.iconX = Integer.MIN_VALUE;
        extremes.iconY = Integer.MIN_VALUE;
        extremes.width = 0;
        extremes.height = 0;
        extremes.frameCount = 0;
        extremes.frametime = 0;
        out.add(new Case<CustomEffect>("", extremes,
            "integer boundaries, plus zero width, height, frame count and frame time. Width and "
                + "height fall back to 16 on read only when their key is ABSENT, so an explicit "
                + "zero survives where a missing key does not"));

        return out;
    }

    /**
     * The filename for {@link #nameMismatch()}, deliberately unequal to the effect's name field.
     */
    public static final String MISMATCH_FILENAME = "Name On Disk";

    /**
     * An effect whose {@code name} key disagrees with its filename.
     *
     * <p>Kept out of {@link #maximal()} because it cannot be expressed there: everything in that
     * list is written to a file named after the effect, which is what makes a mismatch impossible
     * to state. loadEffectsFromDir assigns {@code effect.name} from the filename after reading, so
     * the key inside is discarded and a reader that trusts it gets a different answer.
     */
    public static CustomEffect nameMismatch() {
        CustomEffect mismatched = effect(9, "Name Inside The File");
        return mismatched;
    }

    /** The index document. Copied from CustomEffectController.writeMapNBT. */
    public static NBTTagCompound index(List<Case<CustomEffect>> effects, int lastUsedId) {
        NBTTagList list = new NBTTagList();
        for (Case<CustomEffect> c : effects) {
            CustomEffect effect = c.value;
            // writeMapNBT skips unnamed effects. Reproduced, because the omission is the contract.
            if (!effect.getName().isEmpty()) {
                NBTTagCompound entry = new NBTTagCompound();
                entry.setString("Name", effect.getName());
                entry.setInteger("ID", effect.id);
                list.appendTag(entry);
            }
        }
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("CustomEffects", list);
        compound.setInteger("lastID", lastUsedId);
        return compound;
    }

    private static CustomEffect effect(int id, String name) {
        CustomEffect effect = new CustomEffect();
        effect.id = id;
        effect.name = name;
        return effect;
    }

    private static EffectScript script(String source, boolean enabled, String language) {
        EffectScript handler = new EffectScript();
        handler.setLanguage(language);
        handler.setEnabled(enabled);
        ScriptContainer container = new ScriptContainer(handler);
        container.script = source;
        handler.addScriptUnit(container);
        return handler;
    }
}
