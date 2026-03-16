package noppes.npcs.controllers;

import noppes.npcs.constants.ClientOnly;
import kamkeel.npcs.util.IVector3;
import kamkeel.npcs.controllers.data.ability.preview.PreviewEntityHandler;
import kamkeel.npcs.controllers.data.ability.gui.SubGuiAbilityConfig;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityConfigCallback;
import kamkeel.npcs.controllers.data.ability.gui.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.IChainedAbilityFieldProvider;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityFieldProvider;
import noppes.npcs.entity.EntityNPCInterface;
import kamkeel.npcs.entity.EntityEnergyDome;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import kamkeel.npcs.entity.EntityEnergyPanel;
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.entity.EntityAbilityLaser;
import kamkeel.npcs.entity.EntityAbilityDisc;
import kamkeel.npcs.entity.EntityAbilityBeam;
import kamkeel.npcs.entity.EntityEnergyProjectile;
import kamkeel.npcs.util.ByteBufUtils;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NBTTags;
import noppes.npcs.util.JsonException;
import noppes.npcs.util.NBTJsonUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LinkedNpcController {
    public static LinkedNpcController Instance;
    public List<LinkedData> list = new ArrayList<LinkedData>();

    public LinkedNpcController() {
        Instance = this;
        load();
    }

    private void load() {
        try {
            loadNpcs();
        } catch (Exception e) {
            LogWriter.except(e);
        }
    }

    public File getDir() {
        File dir = new File(CustomNpcs.getWorldSaveDirectory(), "linkednpcs");
        if (!dir.exists())
            dir.mkdir();
        return dir;
    }

    private void loadNpcs() {
        LogWriter.info("Loading Linked Npcs");
        File dir = getDir();
        if (dir.exists()) {
            List<LinkedData> list = new ArrayList<LinkedData>();
            for (File file : dir.listFiles()) {
                if (file.getName().endsWith(".json")) {
                    try {
                        INbt compound = NBTJsonUtil.LoadFile(file);
                        ;
                        LinkedData linked = new LinkedData();
                        linked.setNBT(compound);
                        list.add(linked);
                    } catch (Exception e) {
                        LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
                    }
                }
            }

            this.list = list;
        }
        LogWriter.info("Done loading Linked Npcs");
    }

    public void save() {
        for (LinkedData npc : list) {
            try {
                saveNpc(npc);
            } catch (IOException e) {
                LogWriter.except(e);
            }
        }
    }

    private void saveNpc(LinkedData npc) throws IOException {
        File file = new File(getDir(), npc.name + ".json_new");
        File file1 = new File(getDir(), npc.name + ".json");
        try {
            NBTJsonUtil.SaveFile(file, npc.getNBT());
            if (file1.exists()) {
                file1.delete();
            }
            file.renameTo(file1);

        } catch (JsonException e) {
            LogWriter.except(e);
        }
    }

    public static class LinkedData {
        public String name = "LinkedNpc";
        public long time = 0;
        public INbt data = new INbt();

        public LinkedData() {
            time = System.currentTimeMillis();
        }

        public void setNBT(INbt compound) {
            name = compound.getString("LinkedName");
            data = compound.getCompoundTag("NPCData");
        }

        public INbt getNBT() {
            INbt compound = new INbt();
            compound.setString("LinkedName", name);
            compound.setTag("NPCData", data);
            return compound;
        }
    }

    public void loadNpcData(EntityNPCInterface npc) {
        if (npc.linkedName.isEmpty())
            return;
        LinkedData data = getData(npc.linkedName);
        if (data == null) {
            npc.linkedLast = 0;
            npc.linkedName = "";
            npc.linkedData = null;
        } else {
            npc.linkedData = data;
            if (npc.posX == 0 && npc.posY == 0 && npc.posZ == 0)
                return;
            npc.linkedLast = data.time;
            List<int[]> points = npc.ais.getMovingPath();

            INbt compound = NBTTags.NBTMerge(readNpcData(npc), data.data);

            npc.display.readToNBT(compound);
            npc.stats.readToNBT(compound);
            npc.advanced.readToNBT(compound);
            npc.inventory.readEntityFromNBT(compound);
            if (compound.hasKey("ModelData"))
                ((EntityCustomNpc) npc).modelData.readFromNBT(compound.getCompoundTag("ModelData"));

            npc.ais.readToNBT(compound);
            npc.transform.readToNBT(compound);
            npc.abilities.readFromNBT(compound);
            npc.ais.setMovingPath(points);
            npc.updateClient = true;
        }
    }

    private void cleanTags(INbt compound) {
        compound.removeTag("MovingPathNew");
    }

    public LinkedData getData(String name) {
        for (LinkedData data : list) {
            if (data.name.equalsIgnoreCase(name))
                return data;
        }
        return null;
    }

    private INbt readNpcData(EntityNPCInterface npc) {
        INbt compound = new INbt();
        npc.display.writeToNBT(compound);
        npc.inventory.writeEntityToNBT(compound);
        npc.stats.writeToNBT(compound);
        npc.ais.writeToNBT(compound);
        npc.advanced.writeToNBT(compound);
        npc.transform.writeToNBT(compound);
        npc.abilities.writeToNBT(compound);
        compound.setTag("ModelData", ((EntityCustomNpc) npc).modelData.writeToNBT());
        return compound;
    }

    public void saveNpcData(EntityNPCInterface npc) {
        INbt compound = readNpcData(npc);
        cleanTags(compound);

        if (npc.linkedData.data.equals(compound))
            return;

        npc.linkedData.data = compound;
        npc.linkedData.time = System.currentTimeMillis();
        save();
    }

    public void removeData(String name) {
        Iterator<LinkedData> ita = list.iterator();
        while (ita.hasNext()) {
            if (ita.next().name.equalsIgnoreCase(name))
                ita.remove();
        }
        save();
    }

    public void addData(String name) {
        if (getData(name) != null || name.isEmpty())
            return;

        LinkedData data = new LinkedData();
        data.name = name;
        list.add(data);
        save();
    }
}
