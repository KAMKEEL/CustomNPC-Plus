package kamkeel.npcs.network.packets.data;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.Random;

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
        return PacketHandler.DATA_PACKET;
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

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        spawnParticle(in);
    }

    @SideOnly(Side.CLIENT)
    public static void spawnParticle(ByteBuf buffer) throws IOException{
        double posX = buffer.readDouble();
        double posY = buffer.readDouble();
        double posZ = buffer.readDouble();
        float height = buffer.readFloat();
        float width = buffer.readFloat();
        float yOffset = buffer.readFloat();

        String particle = ByteBufUtils.readString(buffer);
        World worldObj = Minecraft.getMinecraft().theWorld;

        Random rand = worldObj.rand;
        if(particle.equals("heal")){
            for (int k = 0; k < 6; k++)
            {
                worldObj.spawnParticle("instantSpell", posX + (rand.nextDouble() - 0.5D) * (double)width, (posY + rand.nextDouble() * (double)height) - (double)yOffset, posZ + (rand.nextDouble() - 0.5D) * (double)width, 0, 0, 0);
                worldObj.spawnParticle("spell", posX + (rand.nextDouble() - 0.5D) * (double)width, (posY + rand.nextDouble() * (double)height) - (double)yOffset, posZ + (rand.nextDouble() - 0.5D) * (double)width, 0, 0, 0);
            }
        }
    }
}
