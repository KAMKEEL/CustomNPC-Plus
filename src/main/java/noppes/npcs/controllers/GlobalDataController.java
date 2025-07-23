package noppes.npcs.controllers;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import static noppes.npcs.util.CustomNPCsThreader.customNPCThread;

public class GlobalDataController {
    public static GlobalDataController Instance;
    private int itemGiverId = 0;

    public GlobalDataController() {
        Instance = this;
        load();
    }

    private void load() {
        File saveDir = CustomNpcs.getWorldSaveDirectory();
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
        NBTTagCompound nbttagcompound1;
        try (FileInputStream fis = new FileInputStream(file)) {
            nbttagcompound1 = CompressedStreamTools.readCompressed(fis);
        }
        itemGiverId = nbttagcompound1.getInteger("itemGiverId");
    }

    public void saveData() {
        customNPCThread.execute(() -> {
            try {
                File saveDir = CustomNpcs.getWorldSaveDirectory();

                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setInteger("itemGiverId", itemGiverId);

                File file = new File(saveDir, "global.dat_new");
                File file1 = new File(saveDir, "global.dat_old");
                File file2 = new File(saveDir, "global.dat");
                CompressedStreamTools.writeCompressed(nbttagcompound, new FileOutputStream(file));
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
