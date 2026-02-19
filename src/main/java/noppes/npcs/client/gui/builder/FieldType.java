package noppes.npcs.client.gui.builder;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public enum FieldType {
    FLOAT,
    INT,
    BOOLEAN,
    ENUM,
    STRING_ENUM,
    SUB_GUI,
    STRING,
    SECTION_HEADER,
    LABEL,
    EFFECTS_LIST,
    CUSTOM_EFFECTS_LIST,
    EFFECT_ACTIONS_LIST,
    ROW
}
