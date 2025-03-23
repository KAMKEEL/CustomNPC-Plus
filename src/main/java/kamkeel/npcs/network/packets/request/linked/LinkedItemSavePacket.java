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
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.LinkedItemController;
import noppes.npcs.controllers.data.LinkedItem;

import java.io.IOException;

public final class LinkedItemSavePacket extends AbstractPacket {
    public static String packetName = "Request|LinkedItemSave";

    private String prevName;
    private NBTTagCompound compound;

    public LinkedItemSavePacket() {
    }

    public LinkedItemSavePacket(NBTTagCompound compound, String prev) {
        this.compound = compound;
        this.prevName = prev;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.LinkedItemSave;
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
        ByteBufUtils.writeString(out, this.prevName);
        ByteBufUtils.writeNBT(out, this.compound);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        String prevName = ByteBufUtils.readString(in);
        LinkedItem linkedItem = new LinkedItem();
        linkedItem.readFromNBT(ByteBufUtils.readNBT(in));
        LinkedItemController.getInstance().saveLinkedItem(linkedItem);

        if (!prevName.isEmpty() && !prevName.equals(linkedItem.name)) {
            LinkedItemController.getInstance().deleteLinkedItemFile(prevName);
        }

        NoppesUtilServer.sendLinkedItemDataAll((EntityPlayerMP) player);
    }
}
