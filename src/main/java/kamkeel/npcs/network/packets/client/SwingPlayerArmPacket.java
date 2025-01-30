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
import noppes.npcs.CustomNpcs;

import java.io.IOException;

public final class SwingPlayerArmPacket extends AbstractPacket {
    public static final String packetName = "Client|SwingPlayerArm";

    public SwingPlayerArmPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.SWING_PLAYER_ARM;
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
        Minecraft.getMinecraft().thePlayer.swingItem();
    }
}
