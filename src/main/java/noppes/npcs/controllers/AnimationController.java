package noppes.npcs.controllers;

import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.handler.data.IAnimation;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.util.NBTJsonUtil;

import java.io.File;
import java.util.*;

public class AnimationController {
    public HashMap<Integer, Animation> animations = new HashMap<>();
    public static AnimationController instance;

    public AnimationController() {
        instance = this;
        load();
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
                    animation.id = Integer.parseInt(file.getName().split("_")[0]);
                    animation.readFromNBT(NBTJsonUtil.LoadFile(file));
                    animations.put(animation.id,animation);
                } catch(Exception e) {
                    LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
                }
            }
        }
    }

    public void saveAnimations() {
        Collection<Animation> animationLists = animations.values();
        for (Animation animation : animationLists) {
            this.saveAnimation(animation);
        }
    }

    public IAnimation saveAnimation(Animation animation){
        animations.put(animation.id, animation);

        File dir = this.getDir();
        if(!dir.exists())
            dir.mkdirs();

        File file = new File(dir, animation.id + "_" + animation.name + ".json_new");
        File file2 = new File(dir, animation.id + "_" + animation.name +  ".json");

        try {
            NBTJsonUtil.SaveFile(file, animation.writeToNBT());
            if(file2.exists())
                file2.delete();
            file.renameTo(file2);
        } catch (Exception e) {
            LogWriter.except(e);
        }
        return animation;
    }

    private File getDir(){
        return new File(CustomNpcs.getWorldSaveDirectory(), "animations");
    }

    public void delete(int id) {
        Animation animation = this.animations.get(id);
        if(animation == null)
            return;
        File dir = this.getDir();
        for (File file : dir.listFiles()) {
            if (!file.isFile() || !file.getName().endsWith(".json"))
                continue;
            if (file.getName().startsWith(id + "_")) {
                if (!file.delete())
                    return;
                else break;
            }
        }
        this.animations.remove(id);
    }

    public IAnimation get(int id) {
        return this.animations.get(id);
    }
}
