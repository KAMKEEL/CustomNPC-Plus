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
import kamkeel.npcs.network.packets.data.ScrollSelectedPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomNpcsPermissions;

import java.io.IOException;

public final class RemoteFreezePacket extends AbstractPacket {
    public static String packetName = "Request|RemoteFreeze";

    public RemoteFreezePacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.RemoteFreeze;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.NPC_FREEZE;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;
        CustomNpcs.FreezeNPCs = !CustomNpcs.FreezeNPCs;
        ScrollSelectedPacket.setSelectedList((EntityPlayerMP) player, CustomNpcs.FreezeNPCs ? "Unfreeze NPCs" : "Freeze NPCs");
    }
}
