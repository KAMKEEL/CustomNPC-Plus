package kamkeel.npcs.network.packets.player;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumPlayerPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.constants.EnumRoleType;

import java.io.IOException;

public class GetNPCRole extends AbstractPacket {
    public static final String packetName = "Player|GetNPCRole";

    @Override
    public Enum getType() {
        return EnumPlayerPacket.GetRole;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.PLAYER_PACKET;
    }

    @Override
    public boolean needsNPC() {
        return true;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {

    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (npc.advanced.role == EnumRoleType.None)
            return;
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, npc.roleInterface.writeToNBT(new NBTTagCompound()));
    }
}
