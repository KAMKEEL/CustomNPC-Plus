package noppes.npcs.client.gui.util.animation.keys;

import org.lwjgl.input.Keyboard;

public class AnimationKeyPresets extends KeyPresetManager {

    ////////////////////
    //Grid Point controls
    public final KeyPreset FREE_TRANSFORM = add("Free Transform", Keyboard.KEY_G, false);
    public final KeyPreset ADD_POINT = add("Add Point", Keyboard.KEY_Z, false);
    public final KeyPreset DELETE_POINT = add("Delete Point", Keyboard.KEY_X, false);

    public AnimationKeyPresets() {
        super("animation");
    }
}
