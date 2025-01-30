package kamkeel.npcs.network.packets.client.sound;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.controllers.ScriptSoundController;

import java.io.IOException;

public final class PauseSoundsPacket extends AbstractPacket {
    public static final String packetName = "Client|PauseSounds";

    public PauseSoundsPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.PAUSE_SOUNDS;
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
        ScriptSoundController.Instance.pauseAllSounds();
    }
}
