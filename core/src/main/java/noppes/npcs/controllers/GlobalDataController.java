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
import noppes.npcs.core.NBT;
import static noppes.npcs.util.CustomNPCsThreader.customNPCThread;

public class GlobalDataController {
    public static GlobalDataController Instance;
    private int itemGiverId = 0;

    public GlobalDataController() {
        Instance = this;
        load();
    }

    private void load() {
        File saveDir = PlatformServiceHolder.get().getIWorldSaveDirectory();
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
        // OLD: INbt INbt1;
        // OLD: try (FileInputStream fis = new FileInputStream(file)) {
        // OLD:     INbt1 = NBTIO.readCompressed(fis);
        // OLD: }
        INbt INbt1 = PlatformServiceHolder.get().readCompressedNBT(file);
        itemGiverId = INbt1.getInteger("itemGiverId");
    }

    public void saveData() {
        customNPCThread.execute(() -> {
            try {
                File saveDir = PlatformServiceHolder.get().getIWorldSaveDirectory();

                // OLD: INbt INbt = new INbt();
                INbt INbt = NBT.compound();
                INbt.setInteger("itemGiverId", itemGiverId);

                File file = new File(saveDir, "global.dat_new");
                File file1 = new File(saveDir, "global.dat_old");
                File file2 = new File(saveDir, "global.dat");
                // OLD: NBTIO.writeCompressed(INbt, new FileOutputStream(file));
                PlatformServiceHolder.get().writeCompressedNBT(INbt, file);
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
