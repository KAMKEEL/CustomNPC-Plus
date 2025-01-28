package kamkeel.network.packets.client;

import io.netty.buffer.ByteBuf;
import kamkeel.network.AbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.Server;
import noppes.npcs.client.NoppesUtil;

import java.io.IOException;

public final class PlayerUpdateSkinOverlaysPacket extends AbstractPacket {
    public static final String packetName = "Client|PlayerUpdateSkinOverlays";

    public PlayerUpdateSkinOverlaysPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.PLAYER_UPDATE_SKIN_OVERLAYS;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.CLIENT_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        // TODO: Send Packet
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        String playerName = Server.readString(in);
        NBTTagCompound nbt = Server.readNBT(in);
        EntityPlayer sendingPlayer = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(playerName);
        if (sendingPlayer != null) {
            NoppesUtil.updateSkinOverlayData(sendingPlayer, nbt);
        }
    }
}
