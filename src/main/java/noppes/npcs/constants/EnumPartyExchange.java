package noppes.npcs.constants;

import java.util.ArrayList;

public enum EnumPartyExchange {
	Leader("party.leader"),
	All("party.all"),
	Enrolled("party.enrolled"),
    Valid("party.valid");

	final String name;

	EnumPartyExchange(String name){
		this.name = name;
	}
	public static String[] names(){
		ArrayList<String> list = new ArrayList<String>();
		for(EnumPartyExchange e : values())
			list.add(e.name);

		return list.toArray(new String[list.size()]);
	}
}
