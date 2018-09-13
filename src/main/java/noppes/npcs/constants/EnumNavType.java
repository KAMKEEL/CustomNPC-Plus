package noppes.npcs.constants;

import java.util.ArrayList;

public enum EnumNavType {
	Default("aitactics.rush"),
	Dodge("aitactics.stagger"),
	Surround("aitactics.orbit"),
	HitNRun("aitactics.hitandrun"),
	Ambush("aitactics.ambush"),
	Stalk("aitactics.stalk"),
	None("gui.none");
	
	String name;
	EnumNavType(String name){
		this.name = name;
	}
	public static String[] names(){
		ArrayList<String> list = new ArrayList<String>();
		for(EnumNavType e : values())
			list.add(e.name);
		
		return list.toArray(new String[list.size()]);
	}
}
