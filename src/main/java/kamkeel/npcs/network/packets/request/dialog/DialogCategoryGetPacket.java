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
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.DialogCategory;

import java.io.IOException;

public final class DialogCategoryGetPacket extends AbstractPacket {
    public static String packetName = "Request|DialogCategoryGet";

    private int categoryID;

    public DialogCategoryGetPacket() {
    }

    public DialogCategoryGetPacket(int categoryID) {
        this.categoryID = categoryID;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.DialogCategoryGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.categoryID);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP)) return;
        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player)) return;
        int id = in.readInt();
        DialogCategory category = DialogController.Instance.categories.get(id);
        if (category != null) {
            NBTTagCompound comp = category.writeNBT(new NBTTagCompound());
            comp.removeTag("Dialogs");
            GuiDataPacket.sendGuiData((EntityPlayerMP) player, comp);
        }
    }
}
