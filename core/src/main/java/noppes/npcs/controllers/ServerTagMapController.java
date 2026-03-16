package noppes.npcs.controllers;


import java.io.File;
import kamkeel.npcs.platform.PlatformServiceHolder;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;
import noppes.npcs.controllers.data.TagMap;

public class ServerTagMapController {
    public static ServerTagMapController Instance;
    public TagMap tagMap;

    public ServerTagMapController() {
    }

    public File getDir() {
        File dir = new File(PlatformServiceHolder.get().getIWorldSaveDirectory(), "clones");
        if (!dir.exists())
            dir.mkdir();
        return dir;
    }

    public File getCloneTabDir(int tab) {
        File dir = new File(getDir(), tab + "");
        if (!dir.exists())
            dir.mkdir();
        return dir;
    }

    public File getCloneFolderDir(String folderName) {
        File dir = new File(getDir(), folderName);
        if (!dir.exists())
            dir.mkdir();
        return dir;
    }

    public TagMap getTagMap(int tab) {
        this.tagMap = new TagMap(tab);
        try {
            File file = new File(getCloneTabDir(tab), "___tagmap.dat");
            if (file.exists()) {
                loadTagMapFile(file);
            }
        } catch (Exception e) {
            try {
                File file = new File(getCloneTabDir(tab), "___tagmap.dat_old");
                if (file.exists()) {
                    loadTagMapFile(file);
                }
            } catch (Exception ignored) {
            }
        }
        return this.tagMap;
    }

    private void loadTagMapFile(File file) throws Exception {
        // OLD: DataInputStream var1 = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));
        // OLD: INbt nbtCompound = CompressedStreamTools.read(var1);
        // OLD: this.tagMap.readNBT(new NBTWrapper(nbtCompound));
        // OLD: var1.close();
        INbt nbtCompound = PlatformServiceHolder.get().readCompressedNBT(file);
        this.tagMap.readNBT(nbtCompound);
    }

    public TagMap getTagMap(String folderName) {
        this.tagMap = new TagMap(folderName);
        try {
            File file = new File(getCloneFolderDir(folderName), "___tagmap.dat");
            if (file.exists()) {
                loadTagMapFile(file);
            }
        } catch (Exception e) {
            try {
                File file = new File(getCloneFolderDir(folderName), "___tagmap.dat_old");
                if (file.exists()) {
                    loadTagMapFile(file);
                }
            } catch (Exception ignored) {
            }
        }
        return this.tagMap;
    }

    public void saveTagMap(TagMap tagMap) {
        try {
            File saveDir;
            if (tagMap.cloneFolder != null) {
                saveDir = getCloneFolderDir(tagMap.cloneFolder);
            } else {
                saveDir = getCloneTabDir(tagMap.cloneTab);
            }
            File file = new File(saveDir, "___tagmap.dat_new");
            File file1 = new File(saveDir, "___tagmap.dat_old");
            File file2 = new File(saveDir, "___tagmap.dat");
            // OLD: NBTIO.writeCompressed(((NBTWrapper) tagMap.writeNBT()).getMCTag(), new FileOutputStream(file));
            PlatformServiceHolder.get().writeCompressedNBT(tagMap.writeNBT(), file);
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
            // OLD: LogWriter.except(e);
            PlatformServiceHolder.get().logError("Error saving tag map", e);
        }
    }

}
