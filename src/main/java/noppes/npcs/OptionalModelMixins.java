package noppes.npcs;

import net.minecraft.launchwrapper.Launch;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

/**
 * Which mixins into other mods' player models to load, each only when its
 * class is there. With UniMixins the late loader asks after every mod jar is
 * on the class path. Without it the early plugin asks before that, finds the
 * jar in the mods folder and lends it to the class loader so Mixin can read
 * the target.
 */
public final class OptionalModelMixins {

    /** Jars lent to the class loader early, taken off the source list again once read. */
    private static final List<URL> lentJars = new ArrayList<URL>();

    private OptionalModelMixins() {
    }

    /** UniMixins is a coremod, so its late loader interface is readable this early. */
    public static boolean lateLoaderAvailable() {
        try {
            return Launch.classLoader.getClassBytes("com.gtnewhorizon.gtnhmixins.ILateMixinLoader") != null;
        } catch (IOException exception) {
            return false;
        }
    }

    /** The mixin names, relative to the late package. */
    public static List<String> playerModelMixins(boolean late) {
        List<String> mixins = new ArrayList<String>();
        if (hasClass("noppes.mpm.client.model.ModelMPM", late)) {
            mixins.add("MixinMPMModel");
        }
        if (hasClass("noppes.mpm.client.model.ModelScaleRenderer", late)) {
            mixins.add("MixinMPMModelScaleRenderer");
        }
        if (hasClass("micdoodle8.mods.galacticraft.core.client.model.ModelPlayerGC", late)) {
            mixins.add("MixinGalacticraftPlayerModel");
        }
        if (hasClass("api.player.model.ModelPlayerAPI", late)) {
            mixins.add("MixinPlayerAPIModel");
        }
        return mixins;
    }

    /**
     * FML lists every class-path source as a mod source, so a lent jar left
     * on the list is found twice and refused as a duplicate. Off the list the
     * jar stays readable, and FML adds it once itself.
     */
    public static void releaseLentJars() {
        if (lentJars.isEmpty()) return;
        Launch.classLoader.getSources().removeAll(lentJars);
        System.out.println("[CustomNPC+] Took " + lentJars.size() + " optional mod jar(s) back off the source list");
        lentJars.clear();
    }

    private static boolean hasClass(String className, boolean late) {
        try {
            if (Launch.classLoader.getClassBytes(className) != null) {
                System.out.println("[CustomNPC+] Optional class " + className + ": on the class path");
                return true;
            }
        } catch (IOException exception) {
            System.out.println("[CustomNPC+] Could not inspect optional class " + className + ": " + exception);
        }
        File jar = late ? null : jarHolding(className);
        if (jar == null) {
            System.out.println("[CustomNPC+] Optional class " + className + ": not found");
            return false;
        }
        // Mixin drops a @Pseudo target it cannot read when the config is
        // prepared. Lending the jar now lets it read the class.
        try {
            URL url = jar.toURI().toURL();
            Launch.classLoader.addURL(url);
            lentJars.add(url);
            System.out.println("[CustomNPC+] Optional class " + className + ": in mods/"
                + jar.getName() + ", lent to the class path");
            return true;
        } catch (MalformedURLException exception) {
            System.out.println("[CustomNPC+] Optional class " + className + ": in mods/"
                + jar.getName() + " but it could not be lent to the class path: " + exception);
            return false;
        }
    }

    /** The jar under mods/ that holds the class, or null. */
    private static File jarHolding(String className) {
        String entry = className.replace('.', '/') + ".class";
        File[] folders = {new File("mods"), new File("mods" + File.separator + "1.7.10")};
        for (File folder : folders) {
            File[] jars = folder.listFiles();
            if (jars == null) continue;
            for (File jar : jars) {
                if (!jar.getName().toLowerCase().endsWith(".jar")) continue;
                try (ZipFile zip = new ZipFile(jar)) {
                    if (zip.getEntry(entry) != null) return jar;
                } catch (IOException ignored) {
                    // Not a readable jar; nothing to find in it.
                }
            }
        }
        return null;
    }
}
