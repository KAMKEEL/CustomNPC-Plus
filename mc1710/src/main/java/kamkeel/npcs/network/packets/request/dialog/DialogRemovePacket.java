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
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.Dialog;

import java.io.IOException;

public final class DialogRemovePacket extends AbstractPacket {
    public static String packetName = "Request|DialogRemove";

    private int diagId;
    private boolean sendGroup;

    public DialogRemovePacket(int diagId, boolean sendGroup) {
        this.diagId = diagId;
        this.sendGroup = sendGroup;
    }

    public DialogRemovePacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.DialogRemove;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_DIALOG;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(diagId);
        out.writeBoolean(sendGroup);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;
        int diagId = in.readInt();
        boolean sendGroup = in.readBoolean();
        Dialog dialog = DialogController.Instance.dialogs.get(diagId);
        if (dialog != null && dialog.category != null) {
            DialogController.Instance.removeDialog(dialog);
            if (sendGroup) {
                NoppesUtilServer.sendDialogGroup((EntityPlayerMP) player, dialog.category);
            } else {
                NoppesUtilServer.sendDialogData((EntityPlayerMP) player, dialog.category);
            }
        }
    }
}
