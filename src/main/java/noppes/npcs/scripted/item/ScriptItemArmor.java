package noppes.npcs.scripted.item;

import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import noppes.npcs.api.item.IItemArmor;

public class ScriptItemArmor extends ScriptItemStack implements IItemArmor {
    protected ItemArmor armor;

    public ScriptItemArmor(ItemStack item) {
        super(item);
        this.armor = (ItemArmor) item.getItem();
    }

    public int getType() {
        return 3;
    }

    public int getArmorSlot() {
        return armor.armorType;
    }

    public String getArmorMaterial() {
        return armor.getArmorMaterial().toString();
    }
}
