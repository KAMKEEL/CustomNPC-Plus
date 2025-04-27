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
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.controllers.LinkedNpcController;
import noppes.npcs.controllers.LinkedNpcController.LinkedData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class LinkedNPCAddPacket extends AbstractPacket {
    public static String packetName = "Request|LinkedNPCAdd";

    private String data;

    public LinkedNPCAddPacket(String data) {
        this.data = data;
    }

    public LinkedNPCAddPacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.LinkedNPCAdd;
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
        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        String received = ByteBufUtils.readString(in);
        LinkedNpcController.Instance.addData(received);
        List<String> list = new ArrayList<>();
        for (LinkedData d : LinkedNpcController.Instance.list) {
            list.add(d.name);
        }
        ScrollListPacket.sendList((EntityPlayerMP) player, list, EnumScrollData.OPTIONAL);
    }
}
