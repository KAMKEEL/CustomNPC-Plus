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
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.Dialog;

import java.io.IOException;

public final class DialogSavePacket extends AbstractPacket {
    public static String packetName = "Request|DialogSave";

    private int categoryId;
    private NBTTagCompound dialogNBT;
    private boolean sendGroup;

    public DialogSavePacket(int categoryId, NBTTagCompound dialogNBT, boolean sendGroup) {
        this.categoryId = categoryId;
        this.dialogNBT = dialogNBT;
        this.sendGroup = sendGroup;
    }

    public DialogSavePacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.DialogSave;
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
        out.writeInt(categoryId);
        ByteBufUtils.writeNBT(out, dialogNBT);
        out.writeBoolean(sendGroup);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        int cat = in.readInt();
        NBTTagCompound compound = ByteBufUtils.readNBT(in);
        boolean sendGroup = in.readBoolean();
        Dialog dialog = new Dialog();
        dialog.readNBT(compound);
        DialogController.Instance.saveDialog(cat, dialog);

        if (dialog.category != null) {
            if (sendGroup) {
                NoppesUtilServer.sendDialogGroup((EntityPlayerMP) player, dialog.category);
            } else {
                NoppesUtilServer.sendDialogData((EntityPlayerMP) player, dialog.category);
            }
        }
    }
}
