package noppes.npcs.blocks.tiles;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import noppes.npcs.constants.EnumBannerVariant;

public class TileBanner extends TileColorable implements ITileIcon {

    public ItemStack icon;
    public EnumBannerVariant bannerTrim = EnumBannerVariant.Normal;
    public long time = 0;

    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        icon = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("BannerIcon"));

        int variantIndex = compound.getInteger("BannerVariant");
        if (variantIndex >= 0 && variantIndex < EnumBannerVariant.values().length) {
            bannerTrim = EnumBannerVariant.values()[variantIndex];
        } else {
            bannerTrim = EnumBannerVariant.Normal;
        }
    }

    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (icon != null)
            compound.setTag("BannerIcon", icon.writeToNBT(new NBTTagCompound()));
        compound.setInteger("BannerVariant", bannerTrim.ordinal());
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 2, zCoord + 1);
    }

    @Override
    public boolean canEdit() {
        return System.currentTimeMillis() - time < 20000;
    }

    @Override
    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public void setIcon(ItemStack stack) {
        this.icon = stack;
    }


    public void changeVariant() {
        EnumBannerVariant[] variants = EnumBannerVariant.values();
        int nextIndex = (bannerTrim.ordinal() + 1) % variants.length;
        bannerTrim = variants[nextIndex];

        this.markDirty();
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }
}
