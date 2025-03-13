package noppes.npcs.blocks.tiles;

import net.minecraft.util.AxisAlignedBB;

public class TileChair extends TileVariant {

    public boolean isPushed() {
        return this.variant == 1;
    }

    public void push() {
        // Variant 14 is not pushed, 1 is pushed
        this.variant = this.variant == 14 ? 1 : 14;
        this.markDirty();
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (isPushed()) {
            // When pushed, return a thicker vertical slab based on rotation.
            switch (this.rotation) {
                case 0: // North.
                    return AxisAlignedBB.getBoundingBox(xCoord + 0.1, yCoord, zCoord, xCoord + 0.9, yCoord + 1, zCoord + 0.3);
                case 1: // East.
                    return AxisAlignedBB.getBoundingBox(xCoord + 0.7, yCoord, zCoord + 0.1, xCoord + 1, yCoord + 1, zCoord + 0.9);
                case 2: // South.
                    return AxisAlignedBB.getBoundingBox(xCoord + 0.1, yCoord, zCoord + 0.7, xCoord + 0.9, yCoord + 1, zCoord + 1);
                case 3: // West.
                    return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord + 0.1, xCoord + 0.3, yCoord + 1, zCoord + 0.9);
                default:
                    return AxisAlignedBB.getBoundingBox(xCoord, yCoord - 1, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
            }
        } else {
            // Default render bounding box.
            return AxisAlignedBB.getBoundingBox(xCoord, yCoord - 1, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
        }
    }
}
