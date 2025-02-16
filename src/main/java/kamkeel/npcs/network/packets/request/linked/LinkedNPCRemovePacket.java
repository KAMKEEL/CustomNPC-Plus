package kamkeel.npcs.network.packets.request.linked;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.ScrollListPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.controllers.LinkedNpcController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class LinkedNPCRemovePacket extends AbstractPacket {
    public static String packetName = "Request|LinkedNPCRemove";

    private String data;

    public LinkedNPCRemovePacket(String data) {
        this.data = data;
    }

    public LinkedNPCRemovePacket() {}

    @Override
    public Enum getType() {
        return EnumRequestPacket.LinkedNPCRemove;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_LINKED;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeString(out, data);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;

        String received = ByteBufUtils.readString(in);
        LinkedNpcController.Instance.removeData(received);
        List<String> list = new ArrayList<>();
        for (LinkedNpcController.LinkedData d : LinkedNpcController.Instance.list) {
            list.add(d.name);
        }
        ScrollListPacket.sendList((EntityPlayerMP) player, list);
    }
}
