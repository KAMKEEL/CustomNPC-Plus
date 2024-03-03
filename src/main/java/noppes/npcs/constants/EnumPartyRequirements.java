package noppes.npcs.constants;

import java.util.ArrayList;

public enum EnumPartyRequirements {
	Leader("party.leader"),
	All("party.all"),
	Valid("party.valid");

	String name;

	EnumPartyRequirements(String name){
		this.name = name;
	}
	public static String[] names(){
		ArrayList<String> list = new ArrayList<String>();
		for(EnumPartyRequirements e : values())
			list.add(e.name);

		return list.toArray(new String[list.size()]);
	}
}
