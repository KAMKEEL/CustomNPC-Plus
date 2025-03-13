package kamkeel.npcs.network.packets.player;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumPlayerPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.controllers.PlayerDataController;

import java.io.IOException;

public class ScreenSizePacket extends AbstractPacket {
    public static final String packetName = "Player|ScreenSize";
    private int width, height;

    public ScreenSizePacket() {
    }

    public ScreenSizePacket(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public Enum getType() {
        return EnumPlayerPacket.ScreenSize;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.PLAYER_PACKET;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(width);
        out.writeInt(height);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        int width = in.readInt(), height = in.readInt();
        PlayerDataController.Instance.getPlayerData(player).screenSize.setSize(width, height);
    }
}
