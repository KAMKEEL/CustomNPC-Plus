package kamkeel.npcs.controllers.data.ability;

import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import static kamkeel.npcs.controllers.data.ability.AbilityPhase.*;

public enum AbilityAnimation {;

    private final HashMap<AbilityPhase, String> animations = new HashMap<>();

    AbilityAnimation(String idle, String windUp, String active, String recovery) {
        animations.put(IDLE, idle);
        animations.put(WINDUP, windUp);
        animations.put(ACTIVE, active);
        animations.put(RECOVERY, recovery);
    }

    AbilityAnimation(String idle, String windUp, String active) {
        animations.put(IDLE, idle);
        animations.put(WINDUP, windUp);
        animations.put(ACTIVE, active);
    }

    AbilityAnimation(String windUp, String active) {
        animations.put(WINDUP, windUp);
        animations.put(ACTIVE, active);
    }

    AbilityAnimation(String anim, AbilityPhase phase) {
        animations.put(phase, anim);
    }

    public String fileName(AbilityPhase phase) {
        return animations.get(phase);
    }

    public File get(AbilityPhase phase) {
        if (animations.get(phase).isEmpty() || animations.get(phase).equals("N/A"))
            return null;

        return getFile(animations.get(phase));
    }

    public File idle() {
        return get(IDLE);
    }

    public File windUp() {
        return get(WINDUP);
    }

    public File active() {
        return get(ACTIVE);
    }

    public File recovery() {
        return get(RECOVERY);
    }

    private File getFile(String fileName) {
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
