package kamkeel.network.packets.client;

import io.netty.buffer.ByteBuf;
import kamkeel.network.AbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.client.NoppesUtil;

import java.io.IOException;

public final class ScriptedParticlePacket extends AbstractPacket {
    public static final String packetName = "Client|ScriptedParticle";

    public ScriptedParticlePacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.SCRIPTED_PARTICLE;
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
        NoppesUtil.spawnScriptedParticle(player, in);
    }
}
