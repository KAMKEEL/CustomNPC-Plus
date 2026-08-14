package kamkeel.npcs.fixtures;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Replaces the values CustomNPC+ writes from the wall clock with a constant.
 *
 * <p>This is the one place a generated file departs from what the mod would write, and it exists
 * because of a specific property of one writer. {@code PlayerMail.writeNBT} does:
 *
 * <pre>compound.setLong("TimePast", System.currentTimeMillis() - time);</pre>
 *
 * <p>and a {@code PlayerMail} hangs off every dialog and every quest whether or not any mail was
 * configured -- so <i>every</i> dialog and quest file differs between two runs, on that one key.
 *
 * <p><b>Why normalize rather than document and accept.</b> There is no "the" value here to be
 * faithful to; the mod writes a different one every second. Left alone, regenerating rewrites all
 * 70-odd files, so a corpus held in version control churns completely on every run and a real
 * change is invisible in the noise. Pinning it to zero makes the corpus byte-reproducible, which
 * is what lets a diff mean something.
 *
 * <p>Zero is also the honest value: {@code TimePast} means "how long ago, relative to now", and a
 * file on disk has no now. Nothing reads it back -- {@code PlayerMail.readNBT} loads
 * {@code timePast} and the field is only used for display against a live clock.
 *
 * <p>Every key touched is listed here rather than matched by pattern, and the count is reported,
 * so this can never quietly grow to cover a field that actually matters.
 */
public final class Normalize {

    /** Keys written from the wall clock. Exhaustive, and each one deliberate. */
    private static final List<String> WALL_CLOCK_KEYS = Arrays.asList("TimePast");

    private Normalize() {
    }

    /** Rewrites wall-clock keys in place, at any depth. Returns how many were changed. */
    public static int wallClock(NBTTagCompound compound) {
        int changed = 0;
        for (String key : keysOf(compound)) {
            NBTBase tag = compound.getTag(key);
            if (tag == null) {
                continue;
            }
            if (WALL_CLOCK_KEYS.contains(key) && tag.getId() == 4) {
                compound.setLong(key, 0L);
                changed++;
            } else if (tag.getId() == 10) {
                changed += wallClock((NBTTagCompound) tag);
            } else if (tag.getId() == 9) {
                NBTTagList list = (NBTTagList) tag;
                if (list.func_150303_d() == 10) {
                    for (int i = 0; i < list.tagCount(); i++) {
                        // getCompoundTagAt returns the live entry, so this edits in place.
                        changed += wallClock(list.getCompoundTagAt(i));
                    }
                }
            }
        }
        return changed;
    }

    @SuppressWarnings("unchecked")
    private static List<String> keysOf(NBTTagCompound compound) {
        return new ArrayList<String>(compound.func_150296_c());
    }
}
