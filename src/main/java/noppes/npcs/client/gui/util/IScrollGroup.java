package noppes.npcs.client.gui.util;

import java.util.HashMap;
import java.util.Vector;

public interface IScrollGroup {
	public void setScrollGroup(Vector<String> list,HashMap<String,Integer> data);
	public void setSelectedGroup(String selected);
}
