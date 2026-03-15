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
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.LinkedItemController;
import noppes.npcs.controllers.data.LinkedItem;

import java.io.IOException;

public final class LinkedItemClonePacket extends AbstractPacket {
    public static String packetName = "Request|LinkedItemClone";

    private int id;

    public LinkedItemClonePacket() {
    }

    public LinkedItemClonePacket(int id) {
        this.id = id;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.LinkedItemClone;
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
        out.writeInt(this.id);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        int linkedId = in.readInt();
        LinkedItem clone = LinkedItemController.getInstance().cloneLinkedItem(linkedId);
        if (clone != null) {
            NoppesUtilServer.sendLinkedItemDataAll((EntityPlayerMP) player);
            GuiDataPacket.sendGuiData((EntityPlayerMP) player, clone.writeToNBT(false));
        }
    }
}
