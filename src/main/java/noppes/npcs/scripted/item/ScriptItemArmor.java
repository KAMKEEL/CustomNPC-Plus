package noppes.npcs.scripted.item;

import net.minecraft.item.ItemStack;
import noppes.npcs.scripted.interfaces.item.IItemArmor;

public class ScriptItemArmor extends ScriptItemStack implements IItemArmor {

    public ScriptItemArmor(ItemStack item) {
        super(item);
    }

    public int getArmorSlot() {
        return 0;
    }

    public String getArmorMaterial() {
        return null;
    }
}
