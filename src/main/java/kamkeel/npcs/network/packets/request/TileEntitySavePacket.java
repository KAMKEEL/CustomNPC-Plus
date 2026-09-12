package kamkeel.npcs.network.packets.request;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.blocks.tiles.ITilePermission;

import java.io.IOException;

/**
 * Convenience packet for saving extra data about Tile Entities. <br>
 * <br>
 * <b>Any tile entity that uses this packet should implement {@link ITilePermission}</b> <br>
 * Not implementing results in the packet treated as malformed and rejected.
 */
public final class TileEntitySavePacket extends AbstractPacket {
    public static final String packetName = "Request|TileEntitySave";

    private NBTTagCompound compound;

    public TileEntitySavePacket() {
    }

    public TileEntitySavePacket(NBTTagCompound compound) {
        this.compound = compound;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.TileEntitySave;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, this.compound);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        NBTTagCompound incomingCompound = ByteBufUtils.readNBT(in);

        if (isCorrectPermission(incomingCompound, player))
            return;

        if (!PacketUtil.verifyItemPacket(packetName, player, EnumItemPacketType.WAND, EnumItemPacketType.BLOCK))
            return;

        NoppesUtilServer.saveTileEntity((EntityPlayerMP) player, incomingCompound);
    }

    private static boolean isCorrectPermission(NBTTagCompound incomingCompound, EntityPlayer player) {
        int x = incomingCompound.getInteger("x");
        int y = incomingCompound.getInteger("y");
        int z = incomingCompound.getInteger("z");
        TileEntity tile = player.worldObj.getTileEntity(x, y, z);

        if (tile == null)
            return true;

        boolean validatesPacket = tile instanceof ITilePermission;
        if (!validatesPacket) {
            LogWriter.error(String.format("%s tried to use TileEntitySavePacket on %s -- should this class implement ITileValidityChecker??", player.getCommandSenderName(), tile));
            return true; // Assume packet is not meant for this entity from the get-go as there is no checking process.
        }

        ITilePermission validityChecker = (ITilePermission) tile;
        CustomNpcsPermissions.Permission perm = validityChecker.getPermission();

        return !CustomNpcsPermissions.hasPermission(player, perm);
    }
}
