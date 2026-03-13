package noppes.npcs.constants;

import java.util.ArrayList;

public enum EnumPartyObjectives {
    Shared("party.shared"),
    All("party.all"),
    Leader("party.leader");

    public final String name;

    EnumPartyObjectives(String name) {
        this.name = name;
    }

    public static String[] names() {
        ArrayList<String> list = new ArrayList<String>();
        for (EnumPartyObjectives e : values())
            list.add(e.name);

        return list.toArray(new String[list.size()]);
    }
}
