package kamkeel.npcs.network.packets.request.dialog;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.*;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.Quest;

import java.io.IOException;

public final class DialogGetPacket extends AbstractPacket {
    public static String packetName = "Request|DialogGet";

    private int dialogID;

    public DialogGetPacket() {
    }

    public DialogGetPacket(int dialogID) {
        this.dialogID = dialogID;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.DialogGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.dialogID);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(packetName, player, EnumItemPacketType.WAND, EnumItemPacketType.BLOCK))
            return;

        Dialog dialog = DialogController.Instance.dialogs.get(in.readInt());
        if (dialog != null) {
            NBTTagCompound compound = dialog.writeToNBT(new NBTTagCompound());
            Quest quest = QuestController.Instance.quests.get(dialog.quest);
            if (quest != null)
                compound.setString("DialogQuestName", quest.title);
            GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
        }
    }

    public static void getDialog(int id) {
        PacketClient.sendClient(new DialogGetPacket(id));
    }
}
