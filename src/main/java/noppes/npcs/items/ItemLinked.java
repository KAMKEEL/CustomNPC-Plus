package noppes.npcs.items;

import noppes.npcs.CustomNpcs;

public class ItemLinked extends ItemCustomizable {

    public ItemLinked() {
        maxStackSize = 1;
        CustomNpcs.proxy.registerItem(this);
        setHasSubtypes(true);
    }
}
