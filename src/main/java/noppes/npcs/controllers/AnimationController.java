package noppes.npcs.controllers;

import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.handler.IAnimationHandler;
import noppes.npcs.api.handler.data.IAnimation;
import noppes.npcs.api.handler.data.IFaction;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.util.NBTJsonUtil;

import java.io.File;
import java.util.*;

public class AnimationController implements IAnimationHandler {
    public HashMap<Integer, Animation> animations;
    public static AnimationController Instance;
    private int lastUsedID = 0;

    public AnimationController() {
        Instance = this;
        animations = new HashMap<>();
        load();
    }

    public static AnimationController getInstance(){
        return Instance;
    }

    public void load(){
        LogWriter.info("Loading animations...");
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
                    animation.name = file.getName().substring(0, file.getName().length() - 5);
                    animation.readFromNBT(NBTJsonUtil.LoadFile(file));
                    animations.put(animation.id, animation);
                } catch(Exception e) {
                    LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
                }
            }
        }
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

}
