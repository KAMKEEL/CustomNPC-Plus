package noppes.npcs.controllers;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.handler.IAnimationHandler;
import noppes.npcs.api.handler.data.IAnimation;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.util.NBTJsonUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class AnimationController implements IAnimationHandler {
    public HashMap<Integer, Animation> animations;
    private HashMap<Integer, String> bootOrder;

    public static AnimationController Instance;
    private int lastUsedID = 0;

    public AnimationController() {
        Instance = this;
        bootOrder = new HashMap<>();
        animations = new HashMap<>();
        load();
    }

    public static AnimationController getInstance(){
        return Instance;
    }

    public void load(){
        bootOrder = new HashMap<>();
        animations = new HashMap<>();
        LogWriter.info("Loading animations...");
        readAnimationMap();
        loadAnimations();
        LogWriter.info("Done loading animations.");
    }

    private void loadAnimations(){
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

                    if(animation.id == -1){
                        animation.id = getUnusedId();
                    }

                    int originalID = animation.id;
                    int setID = animation.id;
                    while (bootOrder.containsKey(setID) || animations.containsKey(setID)){
                        if(bootOrder.containsKey(setID))
                            if(bootOrder.get(setID).equals(animation.name))
                                break;

                        setID++;
                    }

                    animation.id = setID;
                    if(originalID != setID){
                        LogWriter.info("Found Animation ID Mismatch: " + animation.name + ", New ID: " + setID);
                        animation.save();
                    }

                    animations.put(animation.id, animation);
                } catch(Exception e) {
                    LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
                }
            }
        }

        saveAnimationMap();
    }

    private File getDir() {
        return new File(CustomNpcs.getWorldSaveDirectory(), "animations");
    }

    public int getUnusedId(){
        if(lastUsedID == 0){
            for(int catid : animations.keySet()){
                if(catid > lastUsedID)
                    lastUsedID = catid;
            }

        }
        lastUsedID++;
        return lastUsedID;
    }

    public IAnimation saveAnimation(IAnimation animation){
        if(animation.getID() < 0){
            animation.setID(getUnusedId());
            while(hasName(animation.getName()))
                animation.setName(animation.getName() + "_");
        }
        else{
            Animation existing = animations.get(animation.getID());
            if(existing != null && !existing.name.equals(animation.getName()))
                while(hasName(animation.getName()))
                    animation.setName(animation.getName() + "_");
        }

        animations.remove(animation.getID());
        animations.put(animation.getID(), (Animation) animation);

        saveAnimationMap();

        // Save Animation File
        File dir = this.getDir();
        if(!dir.exists())
            dir.mkdirs();

        File file = new File(dir, animation.getName() + ".json_new");
        File file2 = new File(dir, animation.getName() +  ".json");

        try {
            NBTJsonUtil.SaveFile(file, ((Animation)animation).writeToNBT());
            if(file2.exists())
                file2.delete();
            file.renameTo(file2);
        } catch (Exception e) {
            LogWriter.except(e);
        }
        return animations.get(animation.getID());
    }

    public boolean hasName(String newName) {
        if(newName.trim().isEmpty())
            return true;
        for(Animation animation : animations.values())
            if(animation.name.equals(newName))
                return true;
        return false;
    }

    public void delete(String name) {
        Animation delete =  getAnimationFromName(name);
        if(delete != null){
            Animation foundAnimation = this.animations.remove(delete.getID());
            if(foundAnimation != null && foundAnimation.name != null){
                File dir = this.getDir();
                for (File file : dir.listFiles()) {
                    if (!file.isFile() || !file.getName().endsWith(".json"))
                        continue;
                    if (file.getName().equals(foundAnimation.name+".json")) {
                        file.delete();
                        break;
                    }
                }

                saveAnimationMap();
            }
        }
    }

    public void delete(int id) {
        if(!this.animations.containsKey(id))
            return;

        Animation foundAnimation = this.animations.remove(id);
        if(foundAnimation != null && foundAnimation.name != null){
            File dir = this.getDir();
            for (File file : dir.listFiles()) {
                if (!file.isFile() || !file.getName().endsWith(".json"))
                    continue;
                if (file.getName().equals(foundAnimation.name+".json")) {
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
        return getAnimationFromName(name);
    }

    public IAnimation get(int id) {
        return this.animations.get(id);
    }

    public IAnimation[] getAnimations() {
        ArrayList<IAnimation> animations = new ArrayList<>(this.animations.values());
        return animations.toArray(new IAnimation[0]);
    }

    public Animation getAnimationFromName(String animation){
        for (Map.Entry<Integer,Animation> entryAnimation : AnimationController.getInstance().animations.entrySet()){
            if (entryAnimation.getValue().name.equalsIgnoreCase(animation)){
                return entryAnimation.getValue();
            }
        }
        return null;
    }

    public String[] getNames() {
        String[] names = new String[animations.size()];
        int i = 0;
        for(Animation animation : animations.values()){
            names[i] = animation.name.toLowerCase();
            i++;
        }
        return names;
    }


    ////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////
    // ANIMATION MAP

    public File getMapDir(){
        File dir = CustomNpcs.getWorldSaveDirectory();
        if(!dir.exists())
            dir.mkdir();
        return dir;
    }

    public void readAnimationMap(){
        bootOrder.clear();

        try {
            File file = new File(getMapDir(), "animations.dat");
            if(file.exists()){
                loadAnimationMapFile(file);
            }
        } catch (Exception e) {
            try {
                File file = new File(getMapDir(), "animations.dat_old");
                if(file.exists()){
                    loadAnimationMapFile(file);
                }
            } catch (Exception ignored) {}
        }
    }

    public NBTTagCompound writeMapNBT(){
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList animationList = new NBTTagList();
        for(Integer key: animations.keySet()){
            Animation animation = animations.get(key);
            if(!animation.getName().isEmpty()){
                NBTTagCompound animationCompound = new NBTTagCompound();
                animationCompound.setString("Name", animation.getName());
                animationCompound.setInteger("ID", key);

                animationList.appendTag(animationCompound);
            }
        }
        nbt.setTag("Animations", animationList);
        return nbt;
    }

    public void readMapNBT(NBTTagCompound compound){
        NBTTagList list = compound.getTagList("Animations", 10);
        if(list != null){
            for(int i = 0; i < list.tagCount(); i++)
            {
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

    public void readAnimationMap(DataInputStream stream) throws IOException{
        NBTTagCompound nbtCompound = CompressedStreamTools.read(stream);
        this.readMapNBT(nbtCompound);
    }

    public void saveAnimationMap(){
        try {
            File saveDir = getMapDir();
            File file = new File(saveDir, "animations.dat_new");
            File file1 = new File(saveDir, "animations.dat_old");
            File file2 = new File(saveDir, "animations.dat");
            CompressedStreamTools.writeCompressed(this.writeMapNBT(), new FileOutputStream(file));
            if(file1.exists())
            {
                file1.delete();
            }
            file2.renameTo(file1);
            if(file2.exists())
            {
                file2.delete();
            }
            file.renameTo(file2);
            if(file.exists())
            {
                file.delete();
            }
        } catch (Exception e) {
            LogWriter.except(e);
        }
    }

    ////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////
}
