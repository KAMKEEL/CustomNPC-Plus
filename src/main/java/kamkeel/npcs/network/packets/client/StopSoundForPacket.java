package kamkeel.npcs.network.packets.client;

import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.controllers.ScriptSoundController;

import java.io.IOException;

public final class StopSoundForPacket extends AbstractPacket {
    public static final String packetName = "Client|StopSoundFor";

    public StopSoundForPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.STOP_SOUND_FOR;
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
        if(CustomNpcs.side() != Side.CLIENT)
            return;

        int soundId = in.readInt();
        ScriptSoundController.Instance.stopSound(soundId);
    }
}
