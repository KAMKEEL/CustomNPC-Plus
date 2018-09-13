package noppes.npcs.client.gui.util;

import java.util.HashMap;
import java.util.Vector;

public interface IScrollData {
	public void setData(Vector<String> list,HashMap<String,Integer> data);
	public void setSelected(String selected);
}
