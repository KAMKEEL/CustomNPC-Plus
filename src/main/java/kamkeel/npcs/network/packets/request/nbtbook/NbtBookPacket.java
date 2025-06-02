package kamkeel.npcs.network.packets.request.nbtbook;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.*;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import noppes.npcs.CustomNpcsPermissions;

import java.io.IOException;

public class NbtBookPacket extends AbstractPacket {

    public static String packetName = "Request|NbtBook";

    private Action type;

    private int entityId;

    private NBTTagCompound compound;
    private int x;
    private int y;
    private int z;

    public NbtBookPacket() {
    }

    public NbtBookPacket(int entityId, NBTTagCompound compound) {
        this.type = Action.ENTITY;
        this.entityId = entityId;
        this.compound = compound;
    }

    public NbtBookPacket(int x, int y, int z, NBTTagCompound compound) {
        this.type = Action.BLOCK;
        this.x = x;
        this.y = y;
        this.z = z;
        this.compound = compound;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.NbtBookSave;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.TOOL_NBT_BOOK;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(type.ordinal());
        if(type == Action.BLOCK) {
            out.writeInt(x);
            out.writeInt(y);
            out.writeInt(z);
        } else if(type == Action.ENTITY) {
            out.writeInt(entityId);
        }

        ByteBufUtils.writeNBT(out, compound);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        Action requestedAction = Action.values()[in.readInt()];
        if (requestedAction == Action.BLOCK) {
            int x = in.readInt();
            int y = in.readInt();
            int z = in.readInt();
            NBTTagCompound compound = ByteBufUtils.readNBT(in);
            TileEntity tile = player.worldObj.getTileEntity(x, y, z);
            if(tile != null) {
                tile.readFromNBT(compound);
                tile.markDirty();
            }
        } else if (requestedAction == Action.ENTITY) {
            int entityId = in.readInt();
            NBTTagCompound compound = ByteBufUtils.readNBT(in);
            Entity entity = player.worldObj.getEntityByID(entityId);
            if(entity != null) {
                entity.readFromNBT(compound);
            }
        }
    }

    public static void SaveBlock(int x, int y, int z, NBTTagCompound compound) {
        PacketClient.sendClient(new NbtBookPacket(x, y, z, compound));
    }

    public static void SaveEntity(int entityId, NBTTagCompound compound) {
        PacketClient.sendClient(new NbtBookPacket(entityId, compound));
    }

    private enum Action {
        BLOCK,
        ENTITY
    }
}
