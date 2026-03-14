package noppes.npcs.controllers;

import noppes.npcs.core.NBT;
import kamkeel.npcs.platform.PlatformServiceHolder;
import noppes.npcs.api.INbt;

import java.io.File;

import static noppes.npcs.util.CustomNPCsThreader.customNPCThread;

public class GlobalDataController {
    public static GlobalDataController Instance;
    private int itemGiverId = 0;

    public GlobalDataController() {
        Instance = this;
        load();
    }

    private void load() {
        File saveDir = PlatformServiceHolder.get().getWorldSaveDirectory();
        try {
            File file = new File(saveDir, "global.dat");
            if (file.exists()) {
                loadData(file);
            }
        } catch (Exception e) {
            try {
                File file = new File(saveDir, "global.dat_old");
                if (file.exists()) {
                    loadData(file);
                }

            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }

    private void loadData(File file) throws Exception {
        // OLD: NBTTagCompound nbttagcompound1;
        // OLD: try (FileInputStream fis = new FileInputStream(file)) {
        // OLD:     nbttagcompound1 = CompressedStreamTools.readCompressed(fis);
        // OLD: }
        INbt nbttagcompound1 = PlatformServiceHolder.get().readCompressedNBT(file);
        itemGiverId = nbttagcompound1.getInteger("itemGiverId");
    }

    public void saveData() {
        customNPCThread.execute(() -> {
            try {
                File saveDir = PlatformServiceHolder.get().getWorldSaveDirectory();

                // OLD: NBTTagCompound nbttagcompound = new NBTTagCompound();
                INbt nbttagcompound = NBT.compound();
                nbttagcompound.setInteger("itemGiverId", itemGiverId);

                File file = new File(saveDir, "global.dat_new");
                File file1 = new File(saveDir, "global.dat_old");
                File file2 = new File(saveDir, "global.dat");
                // OLD: CompressedStreamTools.writeCompressed(nbttagcompound, new FileOutputStream(file));
                PlatformServiceHolder.get().writeCompressedNBT(nbttagcompound, file);
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
                e.printStackTrace();
            }
        });
    }

    public int incrementItemGiverId() {
        itemGiverId++;
        saveData();
        return itemGiverId;
    }
}
