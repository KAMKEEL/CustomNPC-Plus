package kamkeel.npcs.network.packets.data;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.config.ConfigMain;

import java.io.IOException;

public final class LoginPacket extends AbstractPacket {
    public static final String packetName = "CNPC+|Login";

    public LoginPacket() {
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.LOGIN;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeBoolean(ConfigMain.PartiesEnabled);
        out.writeBoolean(ConfigMain.ProfilesEnabled);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        ClientCacheHandler.allowParties = in.readBoolean();
        ClientCacheHandler.allowProfiles = in.readBoolean();
    }
}
