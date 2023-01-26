package noppes.npcs.controllers;

import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.handler.IAnimationHandler;
import noppes.npcs.api.handler.data.IAnimation;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.util.NBTJsonUtil;

import java.io.File;
import java.util.*;

public class AnimationController implements IAnimationHandler {
    public HashMap<String, Animation> animations;
    public static AnimationController instance;

    public AnimationController() {
        instance = this;
        animations = new HashMap<>();
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
                    animation.name = file.getName().substring(0, file.getName().length() - 5);
                    animation.readFromNBT(NBTJsonUtil.LoadFile(file));
                    animations.put(animation.name,animation);
                } catch(Exception e) {
                    LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
                }
            }
        }
    }

    private File getDir() {
        return new File(CustomNpcs.getWorldSaveDirectory(), "animations");
    }

    public IAnimation saveAnimation(IAnimation animation){
        animations.put(animation.getName(), (Animation) animation);

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
        return animations.get(animation.getName());
    }

    public void delete(String name) {
        if(!this.animations.containsKey(name))
            return;
        File dir = this.getDir();
        for (File file : dir.listFiles()) {
            if (!file.isFile() || !file.getName().endsWith(".json"))
                continue;
            if (file.getName().equals(name+".json")) {
                file.delete();
                break;
            }
        }
        this.animations.remove(name);
    }

    public IAnimation get(String name) {
        return this.animations.get(name);
    }

    public IAnimation[] getAnimations() {
        ArrayList<IAnimation> animations = new ArrayList<>(this.animations.values());
        return animations.toArray(new IAnimation[0]);
    }
}
