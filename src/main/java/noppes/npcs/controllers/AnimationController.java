package noppes.npcs.controllers;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.handler.IAnimationHandler;
import noppes.npcs.api.handler.data.IAnimation;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.BuiltInAnimation;
import noppes.npcs.util.NBTJsonUtil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

public class AnimationController implements IAnimationHandler {
    public HashMap<Integer, Animation> animations;
    private HashMap<Integer, String> bootOrder;

    // Built-in animations loaded from assets folder (read-only, keyed by name)
    public HashMap<String, BuiltInAnimation> builtInAnimations = new HashMap<>();
    private static final String BUILTIN_ANIMATIONS_PATH = "/assets/customnpcs/animations";
    private static final String BUILTIN_ANIMATIONS_RESOURCE = "assets/customnpcs/animations";

    public static AnimationController Instance;
    private int lastUsedID = 0;

    public AnimationController() {
        Instance = this;
        bootOrder = new HashMap<>();
        animations = new HashMap<>();
        load();
    }

    public static AnimationController getInstance() {
        return Instance;
    }

    public void load() {
        bootOrder = new HashMap<>();
        animations = new HashMap<>();
        builtInAnimations = new HashMap<>();
        LogWriter.info("Loading animations...");
        loadBuiltInAnimations();
        readAnimationMap();
        loadAnimations();
        LogWriter.info("Done loading animations.");
    }

    /**
     * Load built-in animations from assets/customnpcs/animations/ folder.
     * These animations are read-only and accessed by name only (no IDs).
     * Scans the folder directly instead of using a manifest.
     */
    private void loadBuiltInAnimations() {
        try {
            URL resourceUrl = CustomNpcs.class.getResource(BUILTIN_ANIMATIONS_PATH);
            if (resourceUrl == null) {
                LogWriter.info("Built-in animations folder not found: " + BUILTIN_ANIMATIONS_PATH);
                return;
            }

            URI uri = resourceUrl.toURI();
            Path animationsPath;

            if (uri.getScheme().equals("jar")) {
                // Running from JAR - need to create a FileSystem to access it
                FileSystem fileSystem = null;
                try {
                    fileSystem = FileSystems.getFileSystem(uri);
                } catch (Exception e) {
                    fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                }
                animationsPath = fileSystem.getPath(BUILTIN_ANIMATIONS_PATH);
            } else {
                // Running from file system (dev environment)
                animationsPath = Paths.get(uri);
            }

            // Scan the directory for .json files
            try (Stream<Path> paths = Files.walk(animationsPath, 1)) {
                paths.filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String animName = fileName.substring(0, fileName.length() - 5); // Remove .json
                        try {
                            loadBuiltInAnimation(animName);
                        } catch (Exception e) {
                            LogWriter.error("Error loading built-in animation: " + animName, e);
                        }
                    });
            }

            LogWriter.info("Loaded " + builtInAnimations.size() + " built-in animations.");
        } catch (Exception e) {
            LogWriter.error("Error scanning built-in animations folder", e);
        }
    }

    /**
     * Load a single built-in animation by name from assets folder.
     */
    private void loadBuiltInAnimation(String animName) throws Exception {
        String resourcePath = BUILTIN_ANIMATIONS_PATH + "/" + animName + ".json";
        try (InputStream stream = CustomNpcs.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                return;
            }

            // Read JSON content
            StringBuilder content = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            reader.close();

            // Parse NBT from JSON
            NBTTagCompound nbt = NBTJsonUtil.Convert(content.toString());

            BuiltInAnimation animation = new BuiltInAnimation(animName);
            animation.readFromNBT(nbt);

            // Store with lowercase key for case-insensitive lookup
            builtInAnimations.put(animName.toLowerCase(), animation);
        }
    }

    private void loadAnimations() {
        animations.clear();

        File dir = getDir();
        if (!dir.exists()) {
            dir.mkdir();
        } else {
            for (File file : dir.listFiles()) {
                if (!file.isFile() || !file.getName().endsWith(".json"))
                    continue;
                try {
                    Animation animation = new Animation();
                    animation.readFromNBT(NBTJsonUtil.LoadFile(file));
                    animation.name = file.getName().substring(0, file.getName().length() - 5);

                    if (animation.id == -1) {
                        animation.id = getUnusedId();
                    }

                    int originalID = animation.id;
                    int setID = animation.id;
                    while (bootOrder.containsKey(setID) || animations.containsKey(setID)) {
                        if (bootOrder.containsKey(setID))
                            if (bootOrder.get(setID).equals(animation.name))
                                break;

                        setID++;
                    }

                    animation.id = setID;
                    if (originalID != setID) {
                        LogWriter.info("Found Animation ID Mismatch: " + animation.name + ", New ID: " + setID);
                        animation.save();
                    }

                    animations.put(animation.id, animation);
                } catch (Exception e) {
                    LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
                }
            }
        }

        saveAnimationMap();
    }

    private File getDir() {
        return new File(CustomNpcs.getWorldSaveDirectory(), "animations");
    }

    public int getUnusedId() {
        if (lastUsedID == 0) {
            for (int catid : animations.keySet()) {
                if (catid > lastUsedID)
                    lastUsedID = catid;
            }

        }
        lastUsedID++;
        return lastUsedID;
    }

    public IAnimation saveAnimation(IAnimation animation) {
        // Reject saving built-in animations
        if (animation instanceof BuiltInAnimation) {
            LogWriter.info("Cannot save built-in animation: " + animation.getName());
            return animation;
        }

        if (animation.getID() < 0) {
            animation.setID(getUnusedId());
            while (hasName(animation.getName()))
                animation.setName(animation.getName() + "_");
        } else {
            Animation existing = animations.get(animation.getID());
            if (existing != null && !existing.name.equals(animation.getName()))
                while (hasName(animation.getName()))
                    animation.setName(animation.getName() + "_");
        }

        animations.remove(animation.getID());
        animations.put(animation.getID(), (Animation) animation);

        saveAnimationMap();

        // Save Animation File
        File dir = this.getDir();
        if (!dir.exists())
            dir.mkdirs();

        File file = new File(dir, animation.getName() + ".json_new");
        File file2 = new File(dir, animation.getName() + ".json");

        try {
            NBTJsonUtil.SaveFile(file, ((Animation) animation).writeToNBT());
            if (file2.exists())
                file2.delete();
            file.renameTo(file2);
        } catch (Exception e) {
            LogWriter.except(e);
        }
        return animations.get(animation.getID());
    }

    public boolean hasName(String newName) {
        if (newName.trim().isEmpty())
            return true;
        for (Animation animation : animations.values())
            if (animation.name.equals(newName))
                return true;
        return false;
    }

    public void delete(String name) {
        Animation delete = getAnimationFromName(name);
        if (delete != null) {
            // Reject deleting built-in animations
            if (delete instanceof BuiltInAnimation) {
                LogWriter.info("Cannot delete built-in animation: " + name);
                return;
            }

            Animation foundAnimation = this.animations.remove(delete.getID());
            if (foundAnimation != null && foundAnimation.name != null) {
                File dir = this.getDir();
                for (File file : dir.listFiles()) {
                    if (!file.isFile() || !file.getName().endsWith(".json"))
                        continue;
                    if (file.getName().equals(foundAnimation.name + ".json")) {
                        file.delete();
                        break;
                    }
                }

                saveAnimationMap();
            }
        }
    }

    public void delete(int id) {
        // Only user animations have valid IDs (>= 0)
        if (id < 0 || !this.animations.containsKey(id))
            return;

        Animation foundAnimation = this.animations.remove(id);
        if (foundAnimation != null && foundAnimation.name != null) {
            File dir = this.getDir();
            for (File file : dir.listFiles()) {
                if (!file.isFile() || !file.getName().endsWith(".json"))
                    continue;
                if (file.getName().equals(foundAnimation.name + ".json")) {
                    file.delete();
                    break;
                }
            }

            saveAnimationMap();
        }
    }

    public boolean has(String name) {
        return getAnimationFromName(name) != null;
    }

    public IAnimation get(String name) {
        // Check user/custom animations first (allows overriding built-in)
        for (Map.Entry<Integer, Animation> entry : animations.entrySet()) {
            if (entry.getValue().name.equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        // Then check built-in animations
        BuiltInAnimation builtIn = builtInAnimations.get(name.toLowerCase());
        if (builtIn != null) {
            return builtIn;
        }
        return null;
    }

    /**
     * Get a user animation by ID.
     * Built-in animations have no IDs and cannot be retrieved this way.
     */
    public IAnimation get(int id) {
        // Only user animations have valid IDs
        if (id < 0) {
            return null;
        }
        return this.animations.get(id);
    }

    /**
     * Get a built-in animation by name.
     * @param name The animation name (case-insensitive)
     * @return The built-in animation, or null if not found
     */
    public BuiltInAnimation getBuiltInAnimation(String name) {
        return builtInAnimations.get(name.toLowerCase());
    }

    public IAnimation[] getAnimations() {
        ArrayList<IAnimation> animations = new ArrayList<>(this.animations.values());
        return animations.toArray(new IAnimation[0]);
    }

    /**
     * Get all built-in animations.
     */
    public IAnimation[] getBuiltInAnimations() {
        return builtInAnimations.values().toArray(new IAnimation[0]);
    }

    /**
     * Get all animations (both built-in and user-created).
     */
    public IAnimation[] getAllAnimations() {
        ArrayList<IAnimation> all = new ArrayList<>();
        all.addAll(builtInAnimations.values());
        all.addAll(animations.values());
        return all.toArray(new IAnimation[0]);
    }

    /**
     * Check if an animation name is a built-in animation.
     * @param name The animation name to check
     * @return true if this is a built-in animation
     */
    public boolean isBuiltIn(String name) {
        return builtInAnimations.containsKey(name.toLowerCase());
    }

    /**
     * @deprecated Built-in animations no longer use IDs. Use isBuiltIn(String name) instead.
     */
    @Deprecated
    public boolean isBuiltIn(int id) {
        return false; // Built-in animations have no IDs
    }

    public Animation getAnimationFromName(String animation) {
        // Check user/custom animations first (allows overriding built-in)
        for (Map.Entry<Integer, Animation> entryAnimation : animations.entrySet()) {
            if (entryAnimation.getValue().name.equalsIgnoreCase(animation)) {
                return entryAnimation.getValue();
            }
        }
        // Then check built-in animations
        BuiltInAnimation builtIn = builtInAnimations.get(animation.toLowerCase());
        if (builtIn != null) {
            return builtIn;
        }
        return null;
    }

    /**
     * Get names of all built-in animations.
     */
    public String[] getBuiltInAnimationNames() {
        return builtInAnimations.keySet().toArray(new String[0]);
    }

    public String[] getNames() {
        String[] names = new String[animations.size()];
        int i = 0;
        for (Animation animation : animations.values()) {
            names[i] = animation.name.toLowerCase();
            i++;
        }
        return names;
    }


    /// /////////////////////////////////////////////////////
    /// /////////////////////////////////////////////////////
    // ANIMATION MAP
    public File getMapDir() {
        File dir = CustomNpcs.getWorldSaveDirectory();
        if (!dir.exists())
            dir.mkdir();
        return dir;
    }

    public void readAnimationMap() {
        bootOrder.clear();

        try {
            File file = new File(getMapDir(), "animations.dat");
            if (file.exists()) {
                loadAnimationMapFile(file);
            }
        } catch (Exception e) {
            try {
                File file = new File(getMapDir(), "animations.dat_old");
                if (file.exists()) {
                    loadAnimationMapFile(file);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public NBTTagCompound writeMapNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList animationList = new NBTTagList();
        for (Integer key : animations.keySet()) {
            Animation animation = animations.get(key);
            if (!animation.getName().isEmpty()) {
                NBTTagCompound animationCompound = new NBTTagCompound();
                animationCompound.setString("Name", animation.getName());
                animationCompound.setInteger("ID", key);

                animationList.appendTag(animationCompound);
            }
        }
        nbt.setTag("Animations", animationList);
        return nbt;
    }

    public void readMapNBT(NBTTagCompound compound) {
        NBTTagList list = compound.getTagList("Animations", 10);
        if (list != null) {
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
                String animationName = nbttagcompound.getString("Name");
                Integer key = nbttagcompound.getInteger("ID");
                bootOrder.put(key, animationName);
            }
        }
    }

    private void loadAnimationMapFile(File file) throws IOException {
        DataInputStream var1 = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));
        readAnimationMap(var1);
        var1.close();
    }

    public void readAnimationMap(DataInputStream stream) throws IOException {
        NBTTagCompound nbtCompound = CompressedStreamTools.read(stream);
        this.readMapNBT(nbtCompound);
    }

    public void saveAnimationMap() {
        try {
            File saveDir = getMapDir();
            File file = new File(saveDir, "animations.dat_new");
            File file1 = new File(saveDir, "animations.dat_old");
            File file2 = new File(saveDir, "animations.dat");
            CompressedStreamTools.writeCompressed(this.writeMapNBT(), new FileOutputStream(file));
            if (file1.exists()) {
                file1.delete();
            }
            file2.renameTo(file1);
            if (file2.exists()) {
                file2.delete();
            }
            file.renameTo(file2);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            LogWriter.except(e);
        }
    }

    ////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////
}
