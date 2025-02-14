package noppes.npcs.blocks.tiles;

import net.minecraft.item.ItemStack;

public interface ITileIcon {
    boolean canEdit();

    void setTime(long time);

    void setIcon(ItemStack stack);
}
