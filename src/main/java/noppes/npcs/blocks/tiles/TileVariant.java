package noppes.npcs.blocks.tiles;

import kamkeel.npcs.util.ColorUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import static noppes.npcs.items.ItemNpcTool.BRUSH_COLOR_TAG;

public class TileVariant extends TileEntity {

    public static int variantVersion = 1;

    int version = variantVersion;
	public int variant = 14;
	public int rotation;

    public void readFromNBT(NBTTagCompound compound){
        super.readFromNBT(compound);
        version = compound.getInteger("CNPCVersion");
        FixTileData(version, compound, this);
        variant = compound.getInteger("CNPCVariant");
        rotation = compound.getInteger("CNPCRotation");
    }

    public void writeToNBT(NBTTagCompound compound){
    	super.writeToNBT(compound);
        compound.setInteger("CNPCVersion", version);
    	compound.setInteger("CNPCVariant", variant);
    	compound.setInteger("CNPCRotation", rotation);
    }

    public boolean canUpdate(){
        return false;
    }

    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt){
    	NBTTagCompound compound = pkt.func_148857_g();
    	readFromNBT(compound);
    }

    @Override
    public Packet getDescriptionPacket(){
    	NBTTagCompound compound = new NBTTagCompound();
    	writeToNBT(compound);
    	compound.removeTag("Items");
    	S35PacketUpdateTileEntity packet = new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, compound);
    	return packet;
    }

	@Override
    public AxisAlignedBB getRenderBoundingBox(){
		return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
    }

	public int powerProvided(){
		return 0;
	}


    public static void FixTileData(int version, NBTTagCompound compound, TileEntity tileEntity){
        if(version == variantVersion)
            return;
        boolean fixMade = false;
        if(version < 1){
            if(compound.hasKey("BannerColor")){
                int bannerColor = compound.getInteger("BannerColor");
                if(isColorTile(tileEntity)){
                    compound.setInteger(BRUSH_COLOR_TAG, ColorUtil.colorTableInts[bannerColor]);
                }
                compound.setInteger("CNPCVariant", bannerColor);
                fixMade = true;
            }
            if(compound.hasKey("BannerRotation")){
                compound.setInteger("CNPCRotation", compound.getInteger("BannerRotation"));
                fixMade = true;
            }
        }

        if(fixMade)
            tileEntity.markDirty();
    }

    public static boolean isColorTile(TileEntity tileEntity){
        return tileEntity instanceof TileColorable;
    }
}
