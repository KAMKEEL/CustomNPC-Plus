package noppes.npcs.client.key.impl;

import noppes.npcs.client.key.KeyPreset;
import noppes.npcs.client.key.KeyPresetManager;
import org.lwjgl.input.Keyboard;

public class ScriptEditorKeys extends KeyPresetManager {

    /// /////////////////
    // Editor shortcuts (mapped from GuiScriptTextArea.handleShortcutKeys)
    public final KeyPreset COPY = add("Copy").setDefaultState(Keyboard.KEY_C, true, false, false);
    public final KeyPreset PASTE = add("Paste").setDefaultState(Keyboard.KEY_V, true, false, false);
    public final KeyPreset CUT = add("Cut").setDefaultState(Keyboard.KEY_X, true, false, false);
    public final KeyPreset DUPLICATE = add("Duplicate").setDefaultState(Keyboard.KEY_D, true, false, false);
    public final KeyPreset UNDO = add("Undo").setDefaultState(Keyboard.KEY_Z, true, false, false);
    public final KeyPreset REDO = add("Redo").setDefaultState(Keyboard.KEY_Y, true, false, false);

    public final KeyPreset FORMAT = add("Format Code").setDefaultState(Keyboard.KEY_F, false, true, false);
    public final KeyPreset TOGGLE_COMMENT = add("Toggle Comment").setDefaultState(Keyboard.KEY_SLASH, true, false, false);

    // Search/Replace
    public final KeyPreset SEARCH = add("Search").setDefaultState(Keyboard.KEY_F, true, false, false);
    public final KeyPreset SEARCH_REPLACE = add("Replace").setDefaultState(Keyboard.KEY_F, true, false, true);

    // Navigation
    public final KeyPreset GO_TO_LINE = add("Go to Line").setDefaultState(Keyboard.KEY_G, true, false, false);

    // Refactoring
    public final KeyPreset RENAME = add("Rename").setDefaultState(Keyboard.KEY_R, true, false, false);

    // View
    public final KeyPreset FULLSCREEN = add("Toggle Fullscreen").setDefaultState(Keyboard.KEY_F11, false, false, false);

    public ScriptEditorKeys() {
        super("script_editor");
        load();
    }
}

