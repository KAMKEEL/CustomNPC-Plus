package kamkeel.npcs.controllers.data.ability;

import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.handler.data.IAnimation;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.util.NBTJsonUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import static kamkeel.npcs.controllers.data.ability.AbilityPhase.*;

public enum AbilityAnimation {;
    private final HashMap<AbilityPhase, String> animationNames = new HashMap<>();

    AbilityAnimation(String idle, String windUp, String active, String recovery) {
        animationNames.put(IDLE, idle);
        animationNames.put(WINDUP, windUp);
        animationNames.put(ACTIVE, active);
        animationNames.put(DAZED, recovery);
    }

    AbilityAnimation(String idle, String windUp, String active) {
        animationNames.put(IDLE, idle);
        animationNames.put(WINDUP, windUp);
        animationNames.put(ACTIVE, active);
    }

    AbilityAnimation(String windUp, String active) {
        animationNames.put(WINDUP, windUp);
        animationNames.put(ACTIVE, active);
    }

    AbilityAnimation(String anim, AbilityPhase phase) {
        animationNames.put(phase, anim);
    }

    public String fileName(AbilityPhase phase) {
        return animationNames.get(phase);
    }

    public File getFile(AbilityPhase phase) {
        if (animationNames.get(phase).isEmpty() || animationNames.get(phase).equals("N/A"))
            return null;

        return getFileFromName(animationNames.get(phase));
    }

    public IAnimation getAnimation(AbilityPhase phase) {
        try {
            File file = getFile(phase);

            if (file == null) {
                return null;
            }

            Animation animation = new Animation();
            animation.readFromNBT(NBTJsonUtil.LoadFile(file));

            return animation;
        } catch (Exception e) {
            LogWriter.except(e);
        }

        return null;
    }

    public IAnimation idle() {
        return getAnimation(IDLE);
    }

    public IAnimation windUp() {
        return getAnimation(WINDUP);
    }

    public IAnimation active() {
        return getAnimation(ACTIVE);
    }

    public IAnimation dazed() {
        return getAnimation(DAZED);
    }

    private File getFileFromName(String fileName) {
        try (InputStream stream = CustomNpcs.class.getResourceAsStream("/internal/data/animations/" + name() + "/" + fileName + ".json")) {

            File file = new File("temp");

            FileUtils.copyInputStreamToFile(stream, file);

            return file;
        } catch (Exception i) {
            LogWriter.except(i);
        }
        return null;
    }
}
