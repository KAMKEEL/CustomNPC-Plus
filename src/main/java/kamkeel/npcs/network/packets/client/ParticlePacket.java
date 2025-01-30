package kamkeel.npcs.network.packets.client;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.client.NoppesUtil;

import java.io.IOException;

public final class ParticlePacket extends AbstractPacket {
    public static final String packetName = "Client|Particle";

    private double x;
    private double y;
    private double z;

    private float height;
    private float width;
    private float yOffset;

    private String particle;

    public ParticlePacket() {}

    public ParticlePacket(double x, double y, double z, float height, float width, float yOffset, String particle) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.height = height;
        this.width = width;
        this.yOffset = yOffset;
        this.particle = particle;
    }

    @Override
    public Enum getType() {
        return EnumClientPacket.PARTICLE;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.CLIENT_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeDouble(x);
        out.writeDouble(y);
        out.writeDouble(z);

        out.writeFloat(height);
        out.writeFloat(width);
        out.writeFloat(yOffset);

        ByteBufUtils.writeString(out, particle);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        NoppesUtil.spawnParticle(in);
    }
}
