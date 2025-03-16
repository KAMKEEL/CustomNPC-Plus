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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.controllers.LinkedItemController;
import noppes.npcs.controllers.data.LinkedItem;

import java.io.IOException;

public final class LinkedItemBuildPacket extends AbstractPacket {
    public static String packetName = "Request|LinkedItemBuild";

    private int id;

    public LinkedItemBuildPacket() {
    }

    public LinkedItemBuildPacket(int id) {
        this.id = id;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.LinkedItemBuild;
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
        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;

        int id = in.readInt();
        LinkedItem linkedItem = LinkedItemController.getInstance().get(id);
        if (linkedItem == null)
            return;

        EntityPlayerMP playerMP = (EntityPlayerMP) player;
        playerMP.inventory.addItemStackToInventory(linkedItem.createStack().getMCItemStack());
    }
}
