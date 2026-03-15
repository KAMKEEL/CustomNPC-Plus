package kamkeel.npcs.network.packets.request.dialog;

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
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.DialogController;

import java.io.IOException;

public final class DialogsGetPacket extends AbstractPacket {
    public static String packetName = "Request|DialogsGet";

    private int categoryID;
    private boolean sendGroup;

    public DialogsGetPacket() {
    }

    public DialogsGetPacket(int categoryID, boolean send) {
        this.categoryID = categoryID;
        this.sendGroup = send;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.DialogsGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.categoryID);
        out.writeBoolean(this.sendGroup);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(packetName, player, EnumItemPacketType.WAND, EnumItemPacketType.BLOCK))
            return;

        int catID = in.readInt();
        boolean sendGroup = in.readBoolean();
        if (sendGroup) {
            NoppesUtilServer.sendDialogGroup((EntityPlayerMP) player, DialogController.Instance.categories.get(catID));
        } else {
            NoppesUtilServer.sendDialogData((EntityPlayerMP) player, DialogController.Instance.categories.get(catID));
        }
    }
}
