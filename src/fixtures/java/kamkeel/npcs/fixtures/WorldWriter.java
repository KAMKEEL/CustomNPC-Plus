package kamkeel.npcs.fixtures;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.controllers.data.CustomEffect;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.util.NBTJsonUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Writes one CustomNPC+ world.
 *
 * <p><b>What is authoritative and what is copied.</b> Every byte of content here comes from
 * CustomNPC+'s own {@code writeToNBT} / {@code writeNBT} and from {@link NBTJsonUtil} and
 * {@link CompressedStreamTools}. What this class supplies is only <i>where the file goes and what
 * it is called</i>, because the controllers reach that through
 * {@code CustomNpcs.getWorldSaveDirectory()}, which returns null without a running server, and
 * because every {@code save*} fires {@code SyncController.syncUpdate} into a network stack that
 * does not exist offline.
 *
 * <p>So each method below names the controller and method it was copied from. If a layout changes
 * upstream, that reference is where to look. The layout logic is a handful of lines per system;
 * the serialization, which is the part a fixture actually asserts, is never reimplemented.
 *
 * <p>The generated {@code <world>/customnpcs/} directory is exactly a CustomNPC+ world and holds
 * nothing else. Readable renderings of the binary files go in a sibling {@code _readable/} tree so
 * that dropping {@code customnpcs/} into a real save is safe.
 */
public final class WorldWriter {

    private final String world;
    private final File root;
    private final File customnpcs;
    private final File readable;
    private final Manifest manifest;
    private int normalized;

    public WorldWriter(File outputRoot, String world, Manifest manifest) {
        this.world = world;
        this.root = new File(outputRoot, world);
        this.customnpcs = new File(root, "customnpcs");
        this.readable = new File(root, "_readable");
        this.manifest = manifest;
        mkdirs(customnpcs);
        mkdirs(readable);
    }

    public String world() {
        return world;
    }

    // ------------------------------------------------------------------
    // Dialogs -- DialogController.saveDialog / loadCategoryDir
    //
    // The directory is the category and the filename is the id. Neither appears inside the file:
    // saveDialog writes writeToNBTPartial, which omits DialogId, and loadCategoryDir recovers the
    // id by parsing the filename. A dialog's identity is where it sits.
    // ------------------------------------------------------------------
    public void dialog(String category, Dialog dialog, String note) {
        String dir = NoppesStringUtils.cleanFileName(category);
        File file = new File(new File(customnpcs, "dialogs"), dir + "/" + dialog.id + ".json");
        writeJson(file, dialog.writeToNBTPartial(new NBTTagCompound()));
        manifest.record(world, "dialog", String.valueOf(dialog.id), rel(file), note);
    }

    // ------------------------------------------------------------------
    // Quests -- QuestController.saveQuest / loadCategoryDir
    //
    // Same shape as dialogs, and the same identity rule.
    // ------------------------------------------------------------------
    public void quest(String category, Quest quest, String note) {
        String dir = NoppesStringUtils.cleanFileName(category);
        File file = new File(new File(customnpcs, "quests"), dir + "/" + quest.id + ".json");
        writeJson(file, quest.writeToNBTPartial(new NBTTagCompound()));
        manifest.record(world, "quest", String.valueOf(quest.id), rel(file), note);
    }

    // ------------------------------------------------------------------
    // Custom effects -- CustomEffectController.saveCustomEffect / loadEffectsFromDir
    //
    // The filename is the name: loadEffectsFromDir overwrites whatever `name` the file contained
    // with the filename minus .json. An empty category means the effect sits at the root of
    // customeffects/, which is what CategoryManager calls Uncategorized -- there is no category
    // file anywhere, only the directory.
    // ------------------------------------------------------------------
    public void effect(String category, CustomEffect effect, String note) {
        effect(category, effect.getName(), effect, note);
    }

    /**
     * As {@link #effect}, with the filename stated separately.
     *
     * <p>Needed because the filename and the {@code name} key are genuinely independent on disk,
     * and a fixture proving the loader prefers the filename has to be able to disagree with it.
     */
    public void effect(String category, String fileName, CustomEffect effect, String note) {
        File dir = new File(customnpcs, "customeffects");
        if (category != null && !category.isEmpty()) {
            dir = new File(dir, NoppesStringUtils.cleanFileName(category));
        }
        File file = new File(dir, NoppesStringUtils.cleanFileName(fileName) + ".json");
        writeJson(file, effect.writeToNBT(true));
        manifest.record(world, "effect", String.valueOf(effect.id), rel(file), note);
    }

    /**
     * A dialog written from a compound rather than from a {@link Dialog}.
     *
     * <p>Only for the legacy world, which ages a document by removing keys a newer CustomNPC+
     * added. The compound still comes from CustomNPC+'s writer; what is synthesized is the
     * subtraction. Every caller must say in its note which keys it removed and why.
     */
    public void dialogRaw(String category, int id, NBTTagCompound compound, String note) {
        String dir = NoppesStringUtils.cleanFileName(category);
        File file = new File(new File(customnpcs, "dialogs"), dir + "/" + id + ".json");
        writeJson(file, compound);
        manifest.record(world, "dialog", String.valueOf(id), rel(file), note);
    }

    /** As {@link #dialogRaw}, for quests. */
    public void questRaw(String category, int id, NBTTagCompound compound, String note) {
        String dir = NoppesStringUtils.cleanFileName(category);
        File file = new File(new File(customnpcs, "quests"), dir + "/" + id + ".json");
        writeJson(file, compound);
        manifest.record(world, "quest", String.valueOf(id), rel(file), note);
    }

    /**
     * A gzipped NBT document at the root of the world.
     *
     * <p>The shape every {@code .dat} controller uses -- FactionController.saveFactions,
     * CustomEffectController.saveCustomEffects, GlobalDataController.save and the rest: build one
     * compound, {@code CompressedStreamTools.writeCompressed} it, rotate the previous file to
     * {@code _old}. The rotation is not reproduced, because a generated world has no previous file
     * and inventing a {@code .dat_old} would be inventing history.
     */
    public void dat(String system, String fileName, NBTTagCompound compound, String note) {
        File file = new File(customnpcs, fileName);
        mkdirs(file.getParentFile());
        normalized += Normalize.wallClock(compound);
        try {
            OutputStream out = new FileOutputStream(file);
            try {
                CompressedStreamTools.writeCompressed(compound, out);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed writing " + file, e);
        }
        renderReadable(fileName, compound);
        manifest.record(world, system, fileName, rel(file), note);
    }

    /** A JSON document at an explicit path under {@code customnpcs/}, for the per-player records. */
    public void json(String system, String id, String relativePath, NBTTagCompound compound, String note) {
        File file = new File(customnpcs, relativePath);
        writeJson(file, compound);
        manifest.record(world, system, id, rel(file), note);
    }

    private void writeJson(File file, NBTTagCompound compound) {
        mkdirs(file.getParentFile());
        normalized += Normalize.wallClock(compound);
        try {
            NBTJsonUtil.SaveFile(file, compound);
        } catch (Exception e) {
            throw new IllegalStateException("Failed writing " + file, e);
        }
    }

    /** How many wall-clock values were pinned in this world. See {@link Normalize}. */
    public int normalizedCount() {
        return normalized;
    }

    /**
     * Writes CustomNPC+'s own JSON rendering of a binary document beside the world, for reading.
     *
     * <p>Not a fixture and not parseable as strict JSON -- NBTJsonUtil suffixes scalars with their
     * type ({@code 5b}, {@code 5L}, {@code 1.0f}), which is exactly what makes it worth having:
     * the widths are visible, and a fixture whose value silently became a byte shows up here.
     */
    private void renderReadable(String fileName, NBTTagCompound compound) {
        File file = new File(readable, fileName + ".txt");
        mkdirs(file.getParentFile());
        try {
            Writer out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            try {
                out.write(NBTJsonUtil.Convert(compound));
            } finally {
                out.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed writing " + file, e);
        }
    }

    private String rel(File file) {
        String base = root.getAbsolutePath();
        String path = file.getAbsolutePath();
        return path.startsWith(base) ? path.substring(base.length() + 1).replace('\\', '/') : path;
    }

    private static void mkdirs(File dir) {
        if (dir != null && !dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Could not create " + dir);
        }
    }
}
