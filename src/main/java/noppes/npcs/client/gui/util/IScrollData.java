package noppes.npcs.client.gui.util;

import noppes.npcs.constants.EnumScrollData;

import java.util.HashMap;
import java.util.Vector;

public interface IScrollData {
    void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type);

    void setSelected(String selected);
}
