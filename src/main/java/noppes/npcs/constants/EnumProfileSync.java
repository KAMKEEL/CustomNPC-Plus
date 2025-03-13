package noppes.npcs.constants;

import java.util.ArrayList;

public enum EnumProfileSync {
    Individual("profile.individual"),
    Shared("party.shared");

    public final String name;

    EnumProfileSync(String name) {
        this.name = name;
    }

    public static String[] names() {
        ArrayList<String> list = new ArrayList<String>();
        for (EnumProfileSync e : values())
            list.add(e.name);

        return list.toArray(new String[list.size()]);
    }
}
