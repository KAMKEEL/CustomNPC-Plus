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

public final class RemoteResetPacket extends AbstractPacket {
    public static String packetName = "Request|RemoteReset";

    private int entityID;

    public RemoteResetPacket() {}

    public RemoteResetPacket(int entityID) {
        this.entityID = entityID;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.RemoteReset;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.NPC_RESET;
    }

    @Override


    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.entityID);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;

        Entity entity = player.worldObj.getEntityByID(in.readInt());
        if(!(entity instanceof EntityNPCInterface))
            return;
        npc = (EntityNPCInterface) entity;
        npc.reset();
    }
}
