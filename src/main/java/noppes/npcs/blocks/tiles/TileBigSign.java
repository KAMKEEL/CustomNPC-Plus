package noppes.npcs.blocks.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import noppes.npcs.TextBlock;

public class TileBigSign extends TileEntity {

    public int rotation;
    public boolean canEdit = true;
    public boolean hasChanged = true;
    public TextBlock block;
    private String signText = "";

    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        rotation = compound.getInteger("SignRotation");
        setText(compound.getString("SignText"));
    }

    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("SignRotation", rotation);
        compound.setString("SignText", signText);
    }

    public boolean canUpdate() {
        return false;
    }

    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        NBTTagCompound compound = pkt.func_148857_g();
        readFromNBT(compound);
    }

    public String getText() {
        return signText;
    }

    public void setText(String text) {
        this.signText = text;
        hasChanged = true;
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound compound = new NBTTagCompound();
        writeToNBT(compound);
        S35PacketUpdateTileEntity packet = new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, compound);
        return packet;
    }
}

