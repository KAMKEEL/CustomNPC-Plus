package noppes.npcs.blocks.tiles;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

public class TileSign extends TileVariant implements ITileIcon {

    public ItemStack icon;
    public long time = 0;

    public void readFromNBT(NBTTagCompound compound){
        super.readFromNBT(compound);
        icon = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("BannerIcon"));
    }

    public void writeToNBT(NBTTagCompound compound){
        super.writeToNBT(compound);
        if(icon != null)
            compound.setTag("BannerIcon", icon.writeToNBT(new NBTTagCompound()));
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(){
        return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 2, zCoord + 1);
    }

    public boolean canEdit(){
        return System.currentTimeMillis() - time  < 20000;
    }

    @Override
    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public void setIcon(ItemStack stack) {
        this.icon = stack;
    }

}
