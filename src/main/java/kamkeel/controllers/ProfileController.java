package kamkeel.controllers;

import kamkeel.controllers.data.IProfileData;
import kamkeel.controllers.data.Profile;
import kamkeel.controllers.data.Slot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.util.CustomNPCsThreader;
import noppes.npcs.util.NBTJsonUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.UUID;

public class ProfileController {

    public static HashMap<String, IProfileData> profileTypes;
    public static HashMap<UUID, Profile> activeProfiles;

    public static String profile_directory = "profiles";

    public ProfileController(){
        profileTypes = new HashMap<>();
        activeProfiles = new HashMap<>();
    }

    /**
     * Register during ServerStartEvent()
     *
     * @param type - IProfile Type to always Save/Load
     * @return true/false if profile was successfully added
     */
    public static boolean registerProfileType(IProfileData type){
        if(profileTypes.containsKey(type.getTagName()))
            return false;

        profileTypes.put(type.getTagName(), type);
        return true;
    }

    public static File getProfileDir(){
        try{
            File file = new File(CustomNpcs.getWorldSaveDirectory(), profile_directory);
            if(!file.exists())
                file.mkdir();
            return file;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static synchronized void login(EntityPlayer player){
        if(player == null)
            return;

        Profile profile;
        if(activeProfiles.containsKey(player.getUniqueID())){
            profile = activeProfiles.get(player.getUniqueID());
            profile.player = player;
        } else {
            NBTTagCompound profileCompound = load(player);
            profile = new Profile(player, profileCompound);
            activeProfiles.put(player.getUniqueID(), profile);
        }
    }

    public static synchronized NBTTagCompound load(EntityPlayer player) {
        File saveDir = getProfileDir();
        String filename = player.getUniqueID().toString();
        filename += ".dat";
        try {
            File file = new File(saveDir, filename);
            if(file.exists()){
                return NBTJsonUtil.loadNBTData(file);
            }
        } catch (Exception e) {
            LogWriter.error("Error loading profile file: " + filename, e);
        }
        return new NBTTagCompound();
    }

    public static synchronized void save(EntityPlayer player, Profile profile) {
        CustomNPCsThreader.customNPCThread.execute(() -> {
            final NBTTagCompound compound = profile.writeToNBT();
            final String filename = player.getUniqueID() + ".dat";
            try {
                File saveDir = getProfileDir();
                File file = new File(saveDir, filename + "_new");
                File file1 = new File(saveDir, filename);
                CompressedStreamTools.writeCompressed(compound, new FileOutputStream(file));
                if(file1.exists()){
                    file1.delete();
                }
                file.renameTo(file1);
            } catch (Exception e) {
                LogWriter.except(e);
            }
        });
    }

    public void changeSlot(EntityPlayer player, int slotNumber){
        if(!activeProfiles.containsKey(player.getUniqueID()))
            return;

        Profile profile = activeProfiles.get(player.getUniqueID());
        if(profile.currentID == slotNumber)
            return;

        // Saves Current Slot Data
        saveSlotData(player);

        // Sets Player to New Slot
        changePlayerSlot(player, slotNumber);

        // Save Slot File
        save(player, profile);
    }

    public void saveSlotData(EntityPlayer player){
        if(!activeProfiles.containsKey(player.getUniqueID()))
            return;

        NBTTagCompound dataCompound;
        Slot slot;

        Profile profile = activeProfiles.get(player.getUniqueID());
        if(!profile.slots.isEmpty() && profile.slots.containsKey(profile.currentID)){
            slot = profile.slots.get(profile.currentID);
            dataCompound = (NBTTagCompound) slot.getCompound().copy();
        } else {
            slot = new Slot(profile.currentID, "Slot 1");
            dataCompound = new NBTTagCompound();
        }

        for(IProfileData profileData : profileTypes.values()){
            NBTTagCompound cloned = (NBTTagCompound) profileData.getCurrentNBT(player).copy();
            dataCompound.setTag(profileData.getTagName(), cloned);
        }

        slot.setCompound(dataCompound);
    }

    public void changePlayerSlot(EntityPlayer player, int id){
        if(!activeProfiles.containsKey(player.getUniqueID()))
            return;

        NBTTagCompound slotCompound;
        Slot slot;
        Profile profile = activeProfiles.get(player.getUniqueID());
        if(!profile.slots.isEmpty() && profile.slots.containsKey(id)){
            slot = profile.slots.get(id);
            slotCompound = (NBTTagCompound) slot.getCompound().copy();
        } else {
            slot = new Slot(id, "Slot " + id);
            slot.setCompound(new NBTTagCompound());
            slotCompound = new NBTTagCompound();
        }

        slot.setLastLoaded(System.currentTimeMillis());
        profile.slots.put(id, slot);

        for(IProfileData profileData : profileTypes.values()){
            if(slotCompound.hasKey(profileData.getTagName())){
                profileData.setNBT(player, slotCompound.getCompoundTag(profileData.getTagName()));
            } else {
                profileData.setNBT(player, new NBTTagCompound());
            }
            profileData.save(player);
        }

        profile.currentID = id;
    }
}
