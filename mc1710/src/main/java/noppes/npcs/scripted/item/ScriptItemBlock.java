package noppes.npcs.scripted.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import noppes.npcs.api.item.IItemBlock;

public class ScriptItemBlock extends ScriptItemStack implements IItemBlock {
    protected String blockName;

    public ScriptItemBlock(ItemStack item) {
        super(item);
        Block b = Block.getBlockFromItem(item.getItem());
        this.blockName = Block.blockRegistry.getNameForObject(b);
    }

    public int getType() {
        return 2;
    }

    public String getBlockName() {
        return blockName;
    }
}
