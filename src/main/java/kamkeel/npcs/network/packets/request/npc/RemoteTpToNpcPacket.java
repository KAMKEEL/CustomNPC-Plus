package kamkeel.npcs.network.packets.request.npc;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public final class RemoteTpToNpcPacket extends AbstractPacket {
    public static String packetName = "Request|RemoteTpToNpc";

    private int entityID;

    public RemoteTpToNpcPacket() {
    }

    public RemoteTpToNpcPacket(int entityID) {
        this.entityID = entityID;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.RemoteTpToNpc;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.NPC_TELEPORT;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.entityID);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;
        Entity entity = player.worldObj.getEntityByID(in.readInt());
        if (!(entity instanceof EntityNPCInterface))
            return;
        npc = (EntityNPCInterface) entity;
        ((EntityPlayerMP) player).playerNetServerHandler.setPlayerLocation(npc.posX, npc.posY, npc.posZ, 0, 0);
    }
}
