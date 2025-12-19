package noppes.npcs.client.key.impl;

import noppes.npcs.client.key.KeyPreset;
import noppes.npcs.client.key.KeyPresetManager;
import org.lwjgl.input.Keyboard;

public class ScriptEditorKeys extends KeyPresetManager {
    
    ////////////////////
    // Editor shortcuts (mapped from GuiScriptTextArea.handleShortcutKeys)
    public final KeyPreset CUT = add("Cut").setDefaultState(Keyboard.KEY_X, true, false, false).setDescription("Cut selection or current line");
    public final KeyPreset COPY = add("Copy").setDefaultState(Keyboard.KEY_C, true, false, false).setDescription("Copy selection");
    public final KeyPreset PASTE = add("Paste").setDefaultState(Keyboard.KEY_V, true, false, false).setDescription("Paste clipboard at caret");
    public final KeyPreset DUPLICATE = add("Duplicate").setDefaultState(Keyboard.KEY_D, true, false, false).setDescription("Duplicate selection or current line");


    public final KeyPreset UNDO = add("Undo").setDefaultState(Keyboard.KEY_Z, true, false, false).setDescription("Undo last edit");
    public final KeyPreset REDO = add("Redo").setDefaultState(Keyboard.KEY_Y, true, false, false).setDescription("Redo last undone edit");
    public final KeyPreset FORMAT = add("Format").setDefaultState(Keyboard.KEY_F, true, false, false).setDescription("Format code / indent");
    public final KeyPreset TOGGLE_COMMENT = add("Toggle Comment").setDefaultState(Keyboard.KEY_SLASH, true, false, false).setDescription("Toggle comment for selection or line");

    public ScriptEditorKeys() {
        super("script_editor");
        load();
    }
}
