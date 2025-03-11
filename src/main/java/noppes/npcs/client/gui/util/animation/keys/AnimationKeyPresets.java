package noppes.npcs.client.gui.util.animation.keys;

import org.lwjgl.input.Keyboard;

public class AnimationKeyPresets extends KeyPresetManager {

    ////////////////////
    //Grid controls
    public final KeyPreset RESET_GRID = add("Reset Grid").setDefaultState(Keyboard.KEY_R, false, false, false).setDescription("Resets the grid's XY scale and translation to the origins");


    ////////////////////
    //Grid Point controls
    public final KeyPreset FREE_TRANSFORM = add("Free Transform").setDefaultState(Keyboard.KEY_G, false, false, false).setDescription("Freely transform the selected point with the mouse");
    public final KeyPreset ADD_POINT = add("Add Point").setDefaultState(Keyboard.KEY_Z, false, false, false).setDescription("Add a new point");
    public final KeyPreset DELETE_POINT = add("Delete Point").setDefaultState(Keyboard.KEY_X, false, false, false).setDescription("Delete selected point").markDone(this);

    public AnimationKeyPresets() {
        super("animation");
    }
}
