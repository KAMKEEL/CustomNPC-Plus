package noppes.npcs.constants;

import java.util.ArrayList;

public enum EnumCombatPolicy {
    Flip("aicombat.flip"),
    Brute("aicombat.brute"),
    Stubborn("aicombat.stubborn"),
    Tactical("aicombat.tactical");

    public final String name;

    EnumCombatPolicy(String name) {
        this.name = name;
    }

    public static String[] names() {
        ArrayList<String> list = new ArrayList<String>();
        for (EnumCombatPolicy e : values())
            list.add(e.name);

        return list.toArray(new String[list.size()]);
    }
}
