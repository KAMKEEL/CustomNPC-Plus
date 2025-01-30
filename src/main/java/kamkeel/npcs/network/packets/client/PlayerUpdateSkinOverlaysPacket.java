package kamkeel.npcs.network.packets.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
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

    @SideOnly(Side.CLIENT)
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
