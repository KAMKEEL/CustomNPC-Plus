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
import kamkeel.npcs.network.packets.data.large.ScrollDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.DialogCategory;

import java.io.IOException;

public final class DialogCategorySavePacket extends AbstractPacket {
    public static String packetName = "Request|DialogCategorySave";

    private NBTTagCompound categoryNBT;

    public DialogCategorySavePacket(NBTTagCompound categoryNBT) {
        this.categoryNBT = categoryNBT;
    }

    public DialogCategorySavePacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.DialogCategorySave;
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
        ByteBufUtils.writeNBT(out, categoryNBT);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP)) return;
        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player)) return;
        NBTTagCompound compound = ByteBufUtils.readNBT(in);
        DialogCategory category = new DialogCategory();
        category.readNBT(compound);
        DialogController.Instance.saveCategory(category);
        ScrollDataPacket.sendScrollData((EntityPlayerMP) player, DialogController.Instance.getScroll(), EnumScrollData.OPTIONAL);
    }
}
