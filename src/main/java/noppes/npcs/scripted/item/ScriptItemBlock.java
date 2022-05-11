package noppes.npcs.scripted.item;

import net.minecraft.item.ItemStack;
import noppes.npcs.scripted.interfaces.item.IItemBlock;

public class ScriptItemBlock extends ScriptItemStack implements IItemBlock {

    public ScriptItemBlock(ItemStack item) {
        super(item);
    }

    public String getBlockName() {
        return null;
    }
}
