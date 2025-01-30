package kamkeel.npcs.network.packets.client.npc;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public final class EditNpcPacket extends AbstractPacket {
    public static final String packetName = "Client|EditNpc";

    int entityId;
    public EditNpcPacket() {}

    public EditNpcPacket(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public Enum getType() {
        return EnumClientPacket.EDIT_NPC;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.CLIENT_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.entityId);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(in.readInt());
        if (!(entity instanceof EntityNPCInterface)) {
            return;
        }
        NoppesUtil.setLastNpc((EntityNPCInterface) entity);
    }
}
