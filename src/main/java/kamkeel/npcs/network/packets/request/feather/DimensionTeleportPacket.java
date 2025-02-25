package kamkeel.npcs.network.packets.request.feather;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.WorldServer;
import noppes.npcs.NoppesUtilPlayer;

import java.io.IOException;

public final class DimensionTeleportPacket extends AbstractPacket {
    public static String packetName = "Request|DimensionTeleport";

    private int dimensionID;

    public DimensionTeleportPacket() {}

    public DimensionTeleportPacket(int dimensionID) {
        this.dimensionID = dimensionID;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.DimensionTeleport;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(dimensionID);
    }

    @SideOnly(Side.SERVER)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(player, EnumItemPacketType.TELEPORTER))
            return;

        int dimension = in.readInt();
        WorldServer world = MinecraftServer.getServer().worldServerForDimension(dimension);
        ChunkCoordinates coords = world.getEntrancePortalLocation();
        if(coords == null){
            coords = world.getSpawnPoint();
            if(!world.isAirBlock(coords.posX, coords.posY, coords.posZ))
                coords.posY = world.getTopSolidOrLiquidBlock(coords.posX, coords.posZ);
            else{
                while(world.isAirBlock(coords.posX, coords.posY - 1, coords.posZ) && coords.posY > 0){
                    coords.posY--;
                }
                if(coords.posY == 0)
                    coords.posY = world.getTopSolidOrLiquidBlock(coords.posX, coords.posZ);
            }
        }
        NoppesUtilPlayer.teleportPlayer((EntityPlayerMP) player, coords.posX, coords.posY, coords.posZ, dimension);
    }
}
