package noppes.npcs.client.gui.util.animation.keys;

import org.lwjgl.input.Keyboard;

public class AnimationKeyPresets extends KeyPresetManager {

    ////////////////////
    //Grid controls
    public final KeyPreset PAN_GRID = add("Pan Grid").setDefaultState(KeyPreset.RIGHT_MOUSE, false, false, false).setDescription("Holding pans the grid with mouse cursor");
    public final KeyPreset LOCK_X = add("Lock X Axis").setDefaultState(Keyboard.KEY_X, false, false, false).setDescription("Holding locks the X axis while zooming or panning. \nDoesn't conflict").shouldConflict(false);
    public final KeyPreset LOCK_Y = add("Lock Y Axis").setDefaultState(Keyboard.KEY_Y, false, false, false).setDescription("Holding locks the Y axis while zooming or panning. \nDoesn't conflict").shouldConflict(false);
    public final KeyPreset RESET_GRID = add("Reset Grid").setDefaultState(Keyboard.KEY_R, false, false, false).setDescription("Resets the grid's XY scale and translation to the origins");


    ////////////////////
    //Grid Point controls
    public final KeyPreset SELECT_POINT = add("Select Point").setDefaultState(KeyPreset.LEFT_MOUSE, false, false, false).setDescription("Select point under cursor");
    public final KeyPreset ADD_POINT = add("Add Point").setDefaultState(Keyboard.KEY_Z, false, false, false).setDescription("Add a new point at the play-head's X");
    public final KeyPreset DELETE_POINT = add("Delete Point").setDefaultState(Keyboard.KEY_X, false, false, false).setDescription("Delete selected point");
    public final KeyPreset FREE_TRANSFORM = add("Free Transform").setDefaultState(Keyboard.KEY_G, false, false, false).setDescription("Freely transform the selected point with the mouse").markDone(this);

    public AnimationKeyPresets() {
        super("animation");
    }
}
